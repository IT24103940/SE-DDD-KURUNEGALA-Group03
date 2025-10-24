package Group3.demo.Controller;

import Group3.demo.Entity.Invoice;
import Group3.demo.Entity.Payment;
import Group3.demo.Entity.SalesOrder;
import Group3.demo.Entity.SystemLog;
import Group3.demo.Entity.enums.LogLevel;
import Group3.demo.Repository.ExpenseRepository;
import Group3.demo.Repository.InvoiceRepository;
import Group3.demo.Repository.PaymentRepository;
import Group3.demo.Repository.SalesOrderRepository;
import Group3.demo.Repository.SystemLogRepository;
import Group3.demo.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/finance/reports")
@RequiredArgsConstructor
public class FinanceReportsController {

    private final SalesOrderRepository salesOrderRepo;
    private final InvoiceRepository invoiceRepo;
    private final PaymentRepository paymentRepo;
    private final ExpenseRepository expenseRepo;
    private final SystemLogRepository systemLogRepo;
    private final UserRepository userRepo;

    @GetMapping
    public String reports(Model model, Authentication auth) {
        // Sales vs Promotions effectiveness
        var orders = salesOrderRepo.findAll();
        Map<String, Object> salesSummary = buildSalesSummary(orders);

        // Payment aging
        var invoices = invoiceRepo.findAll();
        var payments = paymentRepo.findAll();
        Map<String, Object> aging = buildAgingBuckets(invoices, payments);

        // Revenue by month (last 12 months)
        List<Map<String, Object>> monthly = buildMonthlySummary(payments);

        model.addAttribute("salesSummary", salesSummary);
        model.addAttribute("aging", aging);
        model.addAttribute("monthly", monthly);

        // Audit log: who viewed the report
        if (auth != null) {
            var log = SystemLog.builder()
                    .level(LogLevel.INFO)
                    .message("Viewed Finance Reports")
                    .build();
            userRepo.findByUsername(auth.getName()).ifPresent(log::setUser);
            systemLogRepo.save(log);
        }
        return "finance/reports";
    }

    private Map<String, Object> buildSalesSummary(List<SalesOrder> orders) {
        BigDecimal withPromoNet = BigDecimal.ZERO;
        BigDecimal withPromoDiscount = BigDecimal.ZERO;
        int withPromoCount = 0;
        BigDecimal withoutPromoNet = BigDecimal.ZERO;
        int withoutPromoCount = 0;

        Map<Integer, Map<String, Object>> perPromotion = new LinkedHashMap<>();

        for (var o : orders) {
            boolean hasPromo = o.getPromotion() != null;
            BigDecimal net = Optional.ofNullable(o.getNetAmount()).orElse(Optional.ofNullable(o.getTotalAmount()).orElse(BigDecimal.ZERO));
            BigDecimal disc = Optional.ofNullable(o.getDiscountAmount()).orElse(BigDecimal.ZERO);
            if (hasPromo) {
                withPromoCount++;
                withPromoNet = withPromoNet.add(net);
                withPromoDiscount = withPromoDiscount.add(disc);
                Integer pid = o.getPromotion().getId();
                Map<String, Object> row = perPromotion.computeIfAbsent(pid, k -> new HashMap<>());
                row.put("promotionId", pid);
                row.put("promotionTitle", o.getPromotion().getTitle());
                row.put("orders", ((Number)row.getOrDefault("orders", 0)).intValue() + 1);
                row.put("discountTotal", ((BigDecimal)row.getOrDefault("discountTotal", BigDecimal.ZERO)).add(disc));
                row.put("netTotal", ((BigDecimal)row.getOrDefault("netTotal", BigDecimal.ZERO)).add(net));
            } else {
                withoutPromoCount++;
                withoutPromoNet = withoutPromoNet.add(net);
            }
        }

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("withPromo", Map.of(
                "count", withPromoCount,
                "netTotal", withPromoNet,
                "discountTotal", withPromoDiscount
        ));
        summary.put("withoutPromo", Map.of(
                "count", withoutPromoCount,
                "netTotal", withoutPromoNet
        ));
        summary.put("perPromotion", new ArrayList<>(perPromotion.values()));
        return summary;
    }

    private Map<String, Object> buildAgingBuckets(List<Invoice> invoices, List<Payment> allPayments) {
        // Map invoiceId -> total paid
        Map<Integer, BigDecimal> paidByInvoice = allPayments.stream()
                .collect(Collectors.groupingBy(p -> p.getInvoice().getId(),
                        Collectors.reducing(BigDecimal.ZERO, Payment::getPaidAmount, BigDecimal::add)));

        LocalDate today = LocalDate.now();
        Map<String, Map<String, Object>> buckets = new LinkedHashMap<>();
        for (String b : List.of("CURRENT", "DUE_1_30", "DUE_31_60", "DUE_61_90", "DUE_90_PLUS")) {
            Map<String, Object> row = new HashMap<>();
            row.put("count", 0);
            row.put("outstanding", BigDecimal.ZERO);
            buckets.put(b, row);
        }

        for (var inv : invoices) {
            BigDecimal paid = paidByInvoice.getOrDefault(inv.getId(), BigDecimal.ZERO);
            BigDecimal outstanding = inv.getAmount().subtract(paid);
            if (outstanding.compareTo(BigDecimal.ZERO) <= 0) continue; // skip fully paid or overpaid

            String bucket = "CURRENT";
            if (inv.getDueDate() != null && inv.getDueDate().isBefore(today)) {
                long days = ChronoUnit.DAYS.between(inv.getDueDate(), today);
                if (days <= 30) bucket = "DUE_1_30";
                else if (days <= 60) bucket = "DUE_31_60";
                else if (days <= 90) bucket = "DUE_61_90";
                else bucket = "DUE_90_PLUS";
            }

            Map<String, Object> row = buckets.get(bucket);
            row.put("count", ((Number)row.get("count")).intValue() + 1);
            row.put("outstanding", ((BigDecimal)row.get("outstanding")).add(outstanding));
        }

        return Map.of("buckets", buckets);
    }

    private List<Map<String, Object>> buildMonthlySummary(List<Payment> payments) {
        // Consider all payments; refunds are negative and will reduce net
        LocalDate today = LocalDate.now();
        YearMonth current = YearMonth.from(today);
        List<YearMonth> months = new ArrayList<>();
        for (int i = 11; i >= 0; i--) {
            months.add(current.minusMonths(i));
        }

        // Group payments by YearMonth
        Map<YearMonth, BigDecimal> payByMonth = new HashMap<>();
        for (var p : payments) {
            if (p.getPaidDate() == null) continue;
            YearMonth ym = YearMonth.from(p.getPaidDate());
            BigDecimal amt = p.getPaidAmount();
            payByMonth.put(ym, payByMonth.getOrDefault(ym, BigDecimal.ZERO).add(amt));
        }

        // Group expenses by month
        Map<YearMonth, BigDecimal> expByMonth = new HashMap<>();
        expenseRepo.findAll().forEach(e -> {
            if (e.getExpenseDate() == null) return;
            YearMonth ym = YearMonth.from(e.getExpenseDate());
            expByMonth.put(ym, expByMonth.getOrDefault(ym, BigDecimal.ZERO).add(e.getAmount()));
        });

        List<Map<String, Object>> rows = new ArrayList<>();
        for (YearMonth ym : months) {
            BigDecimal pay = payByMonth.getOrDefault(ym, BigDecimal.ZERO);
            BigDecimal exp = expByMonth.getOrDefault(ym, BigDecimal.ZERO);
            rows.add(Map.of(
                    "month", ym.toString(),
                    "payments", pay,
                    "expenses", exp,
                    "net", pay.subtract(exp)
            ));
        }
        return rows;
    }
}
