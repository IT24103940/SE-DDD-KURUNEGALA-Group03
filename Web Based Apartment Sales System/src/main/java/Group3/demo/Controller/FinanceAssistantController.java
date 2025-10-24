package Group3.demo.Controller;

import Group3.demo.Entity.Expense;
import Group3.demo.Entity.Invoice;
import Group3.demo.Entity.Payment;
import Group3.demo.Entity.Document;
import Group3.demo.Entity.enums.ExpenseType;
import Group3.demo.Entity.enums.InvoiceStatus;
import Group3.demo.Entity.enums.PaymentMethod;
import Group3.demo.Entity.enums.PaymentStatus;
import Group3.demo.Entity.enums.DocumentOwner;
import Group3.demo.Entity.enums.InvoiceType;
import Group3.demo.Entity.enums.PaymentPlan;
import Group3.demo.Repository.ExpenseRepository;
import Group3.demo.Repository.InvoiceRepository;
import Group3.demo.Repository.PaymentRepository;
import Group3.demo.Repository.SalesOrderRepository;
import Group3.demo.Repository.DocumentRepository;
import Group3.demo.Service.FinanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/finance")
@RequiredArgsConstructor
public class FinanceAssistantController {

    private final FinanceService financeService;
    private final SalesOrderRepository salesOrderRepo;
    private final InvoiceRepository invoiceRepo;
    private final PaymentRepository paymentRepo;
    private final ExpenseRepository expenseRepo;
    private final DocumentRepository documentRepo;

    // Dashboard landing
    @GetMapping({"", "/", "/dashboard"})
    public String dashboard() {
        return "finance/dashboard";
    }

    /* ---------------- Invoices ---------------- */

    @GetMapping("/invoices")
    public String invoices(Model model) {
        var invoices = invoiceRepo.findAll();
        // Phase 5: auto-recompute to mark overdue, etc.
        for (var inv : invoices) {
            financeService.recomputeInvoiceAndOrder(inv.getId());
        }
        invoices = invoiceRepo.findAll(); // reload after recompute
        model.addAttribute("invoices", invoices);
        model.addAttribute("orders", salesOrderRepo.findAll());
        Map<String, Object> summary = financeService.getFinanceSummary();
        model.addAttribute("summary", summary);
        // documents per invoice
        Map<Integer, java.util.List<Document>> invoiceDocs = new HashMap<>();
        for (var inv : invoices) {
            invoiceDocs.put(inv.getId(), documentRepo.findByOwnerTypeAndOwnerId(DocumentOwner.INVOICE, inv.getId()));
        }
        model.addAttribute("invoiceDocs", invoiceDocs);
        return "finance/invoices";
    }

    @PostMapping("/invoices")
    public String createInvoice(@RequestParam(name = "orderId") Integer orderId,
                                @RequestParam(name = "number") String number,
                                @RequestParam(name = "amount") BigDecimal amount) {
        financeService.createInvoice(orderId, number, amount);
        return "redirect:/finance/invoices";
    }

    @PostMapping("/invoices/{id}/status")
    public String updateInvoiceStatus(@PathVariable("id") Integer id,
                                      @RequestParam(name = "status") InvoiceStatus status) {
        Invoice inv = invoiceRepo.findById(id).orElseThrow();
        inv.setStatus(status);
        invoiceRepo.save(inv);
        return "redirect:/finance/invoices";
    }

    @PostMapping("/invoices/{id}/edit")
    public String editInvoice(@PathVariable("id") Integer id,
                              @RequestParam(name = "number") String number,
                              @RequestParam(name = "amount") BigDecimal amount,
                              Model model) {
        Invoice inv = invoiceRepo.findById(id).orElseThrow();
        var payments = paymentRepo.findByInvoiceId(id);
        // Guard: disallow changing a PAID invoice and disallow lowering amount below total paid
        BigDecimal totalPaid = payments.stream().map(Payment::getPaidAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        if (inv.getStatus() == InvoiceStatus.PAID || amount.compareTo(totalPaid) < 0) {
            model.addAttribute("errorMessage", "Cannot edit a PAID invoice or set amount below total paid");
            return invoices(model);
        }
        inv.setInvoiceNumber(number);
        inv.setAmount(amount);
        invoiceRepo.save(inv);
        return "redirect:/finance/invoices";
    }

    @PostMapping("/invoices/{id}/delete")
    public String deleteInvoice(@PathVariable("id") Integer id, Model model) {
        Invoice inv = invoiceRepo.findById(id).orElseThrow();
        var payments = paymentRepo.findByInvoiceId(id);
        // Guard: prevent deleting PAID invoice or invoice with any payments
        if (inv.getStatus() == InvoiceStatus.PAID || !payments.isEmpty()) {
            model.addAttribute("errorMessage", "Cannot delete an invoice that is PAID or has payments");
            return invoices(model);
        }
        invoiceRepo.deleteById(id);
        return "redirect:/finance/invoices";
    }

    /* ---------------- Payments ---------------- */

    @GetMapping("/payments")
    public String payments(Model model) {
        var invoices = invoiceRepo.findAll();
        model.addAttribute("invoices", invoices);
        model.addAttribute("payments", paymentRepo.findAll());
        // Provide compact invoice data for auto-fill (gross amounts)
        List<Map<String, Object>> invoiceData = new ArrayList<>();
        for (var inv : invoices) {
            BigDecimal paid = paymentRepo.findByInvoiceId(inv.getId()).stream()
                    .map(Payment::getPaidAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal outstanding = inv.getAmount().subtract(paid);
            if (outstanding.compareTo(BigDecimal.ZERO) < 0) {
                outstanding = BigDecimal.ZERO;
            }
            Map<String, Object> m = new HashMap<>();
            m.put("id", inv.getId());
            m.put("number", inv.getInvoiceNumber());
            m.put("amount", inv.getAmount()); // gross invoice amount
            m.put("outstanding", outstanding);
            m.put("type", inv.getType() != null ? inv.getType().name() : null);
            var so = inv.getSalesOrder();
            m.put("orderPlan", so != null && so.getPaymentPlan() != null ? so.getPaymentPlan().name() : null);
            invoiceData.add(m);
        }
        model.addAttribute("invoiceData", invoiceData);
        return "finance/payments";
    }

    @PostMapping("/payments")
    public String recordPayment(@RequestParam(name = "invoiceId") Integer invoiceId,
                                @RequestParam(name = "amount") BigDecimal amount,
                                @RequestParam(name = "method") PaymentMethod method,
                                @RequestParam(name = "ref", required = false) String reference) {
        financeService.createPayment(invoiceId, amount, method, reference);
        return "redirect:/finance/payments";
    }

    @PostMapping("/payments/{id}/edit")
    public String editPayment(@PathVariable("id") Integer id,
                              @RequestParam(name = "amount") BigDecimal amount,
                              @RequestParam(name = "method") PaymentMethod method,
                              @RequestParam(name = "ref", required = false) String reference,
                              Model model) {
        Payment payment = paymentRepo.findById(id).orElseThrow();
        Invoice inv = payment.getInvoice();
        // Sum other payments on this invoice
        BigDecimal others = paymentRepo.findByInvoiceId(inv.getId()).stream()
                .filter(p -> !p.getId().equals(id))
                .map(Payment::getPaidAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal newTotal = others.add(amount);
        if (newTotal.compareTo(inv.getAmount().add(new BigDecimal("0.0001"))) > 0) {
            model.addAttribute("errorMessage", "Payment exceeds invoice amount");
            return payments(model);
        }
        payment.setPaidAmount(amount);
        payment.setMethod(method);
        payment.setReference(reference);
        paymentRepo.save(payment);
        financeService.recomputeInvoiceAndOrder(inv.getId());
        return "redirect:/finance/payments";
    }

    @PostMapping("/payments/{id}/delete")
    public String deletePayment(@PathVariable("id") Integer id) {
        Payment payment = paymentRepo.findById(id).orElseThrow();
        Integer invoiceId = payment.getInvoice().getId();
        paymentRepo.deleteById(id);
        financeService.recomputeInvoiceAndOrder(invoiceId);
        return "redirect:/finance/payments";
    }

    /* ---------------- Expenses ---------------- */

    @GetMapping("/expenses")
    public String expenses(Model model) {
        model.addAttribute("expenses", expenseRepo.findAll());
        return "finance/expences"; // note: file name is expences.html
    }

    @PostMapping("/expenses")
    public String addExpense(@RequestParam(name = "amount") BigDecimal amount,
                             @RequestParam(name = "type") ExpenseType type,
                             @RequestParam(name = "desc", required = false) String description) {
        financeService.createExpense(amount, type.name(), description);
        return "redirect:/finance/expenses";
    }

    @PostMapping("/expenses/{id}/edit")
    public String editExpense(@PathVariable("id") Integer id,
                              @RequestParam(name = "amount") BigDecimal amount,
                              @RequestParam(name = "type") ExpenseType type,
                              @RequestParam(name = "desc", required = false) String description) {
        Expense expense = expenseRepo.findById(id).orElseThrow();
        expense.setAmount(amount);
        expense.setType(type);
        expense.setDescription(description);
        expenseRepo.save(expense);
        return "redirect:/finance/expenses";
    }

    @PostMapping("/expenses/{id}/delete")
    public String deleteExpense(@PathVariable("id") Integer id) {
        expenseRepo.deleteById(id);
        return "redirect:/finance/expenses";
    }

    // ---------------- Refunds ----------------
    @PostMapping("/payments/{id}/refund")
    public String refundPayment(@PathVariable("id") Integer id,
                                @RequestParam("amount") BigDecimal amount,
                                @RequestParam(name = "ref", required = false) String reference,
                                Model model) {
        Payment original = paymentRepo.findById(id).orElseThrow();
        Invoice inv = original.getInvoice();
        BigDecimal netPaid = paymentRepo.findByInvoiceId(inv.getId()).stream()
                .map(Payment::getPaidAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (amount.compareTo(netPaid.max(BigDecimal.ZERO)) > 0) {
            model.addAttribute("errorMessage", "Refund exceeds net paid amount");
            return payments(model);
        }

        Payment refund = new Payment();
        refund.setInvoice(inv);
        refund.setPaidAmount(amount.negate()); // negative amount for refund
        refund.setMethod(original.getMethod());
        refund.setReference("Refund for #" + original.getId() + (reference != null ? " - " + reference : ""));
        refund.setStatus(PaymentStatus.REFUND);
        paymentRepo.save(refund);
        financeService.recomputeInvoiceAndOrder(inv.getId());
        return "redirect:/finance/payments";
    }

}
