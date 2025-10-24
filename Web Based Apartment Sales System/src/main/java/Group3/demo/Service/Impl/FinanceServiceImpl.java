package Group3.demo.Service.Impl;

import Group3.demo.Entity.*;
import Group3.demo.Entity.enums.*;
import Group3.demo.Repository.*;
import Group3.demo.Service.FinanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class FinanceServiceImpl implements FinanceService {

    @Autowired private InvoiceRepository invoiceRepo;
    @Autowired private PaymentRepository paymentRepo;
    @Autowired private RefundRepository refundRepo;
    @Autowired private ExpenseRepository expenseRepo;
    @Autowired private SalesOrderRepository salesOrderRepo;
    @Autowired private ApartmentRepository apartmentRepo;
    @Autowired private LeadRepository leadRepo;
    @Autowired private InteractionNoteRepository noteRepo;

    // -------- Invoices --------
    @Override
    public List<Invoice> getAllInvoices() {
        return invoiceRepo.findAll();
    }

    @Override
    public Invoice getInvoiceById(Integer id) {
        return invoiceRepo.findById(id).orElseThrow();
    }

    @Override
    public void createInvoice(Integer orderId, String number, BigDecimal amount) {
        // delegate to detailed method with defaults
        createInvoiceWithDetails(orderId, number, amount, LocalDate.now(), LocalDate.now().plusDays(7), InvoiceType.FULL);
    }

    @Override
    public void createInvoiceWithDetails(Integer orderId, String number, BigDecimal amount, LocalDate issueDate, LocalDate dueDate, InvoiceType type) {
        Invoice inv = new Invoice();
        inv.setInvoiceNumber(number);
        inv.setAmount(amount);
        inv.setStatus(InvoiceStatus.PENDING);
        inv.setIssueDate(issueDate);
        inv.setDueDate(dueDate);
        inv.setType(type);

        if (salesOrderRepo != null && orderId != null) {
            inv.setSalesOrder(salesOrderRepo.findById(orderId).orElseThrow());
        }
        invoiceRepo.save(inv);
    }

    @Override
    public void updateInvoice(Integer id, String number, BigDecimal amount, InvoiceStatus status, Integer orderId) {
        Invoice inv = invoiceRepo.findById(id).orElseThrow();
        inv.setInvoiceNumber(number);
        inv.setAmount(amount);
        inv.setStatus(status);

        if (salesOrderRepo != null && orderId != null) {
            inv.setSalesOrder(salesOrderRepo.findById(orderId).orElseThrow());
        }
        invoiceRepo.save(inv);
    }

    @Override
    public void deleteInvoice(Integer id) {
        invoiceRepo.deleteById(id);
    }

    // -------- Payments --------
    @Override
    public List<Payment> getAllPayments() {
        return paymentRepo.findAll();
    }

    @Override
    public void createPayment(Integer invoiceId, BigDecimal paidAmount, PaymentMethod method, String reference) {
        Invoice inv = invoiceRepo.findById(invoiceId).orElseThrow();

        // Guard: prevent overpayment (allow tiny rounding tolerance)
        BigDecimal alreadyPaid = paymentRepo.findByInvoiceId(invoiceId).stream()
                .map(Payment::getPaidAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal newTotal = alreadyPaid.add(paidAmount);
        if (newTotal.compareTo(inv.getAmount().add(new BigDecimal("0.0001"))) > 0) {
            throw new IllegalArgumentException("Payment exceeds invoice amount");
        }

        Payment p = new Payment();
        p.setInvoice(inv);
        p.setPaidAmount(paidAmount);
        p.setPaidDate(LocalDate.now());
        p.setMethod(method);
        p.setReference(reference);
        p.setStatus(PaymentStatus.CONFIRMED);
        paymentRepo.save(p);

        // recompute statuses
        recomputeInvoiceAndOrder(inv.getId());
    }

    @Override
    public void deletePayment(Integer id) {
        paymentRepo.deleteById(id);
    }

    // -------- Expenses --------
    @Override
    public List<Expense> getAllExpenses() {
        return expenseRepo.findAll();
    }

    @Override
    public void createExpense(BigDecimal amount, String type, String description) {
        Expense e = new Expense();
        e.setAmount(amount);
        e.setType(resolveExpenseType(type));
        e.setDescription(description);
        e.setExpenseDate(LocalDate.now());
        expenseRepo.save(e);
    }

    // Resolve a possibly noisy string (contains emoji/spaces) to ExpenseType
    private ExpenseType resolveExpenseType(String source) {
        if (source == null) return null;
        String cleaned = source.replaceAll("[^\\p{L}\\p{Nd}_ ]+", "").trim().replaceAll("\\s+", "_").toUpperCase();
        try {
            return ExpenseType.valueOf(cleaned);
        } catch (IllegalArgumentException ex) {
            for (ExpenseType t : ExpenseType.values()) {
                if (t.name().equalsIgnoreCase(cleaned)) return t;
                if (t.name().replaceAll("_", "").equalsIgnoreCase(cleaned.replaceAll("_", ""))) return t;
            }
            throw new IllegalArgumentException("Unknown ExpenseType: " + source);
        }
    }

    @Override
    public void deleteExpense(Integer id) {
        expenseRepo.deleteById(id);
    }

    // -------- Summary + Orders --------
    @Override
    public Map<String, Object> getFinanceSummary() {
        BigDecimal totalPayments = paymentRepo.findAll().stream()
                .map(Payment::getPaidAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalExpenses = expenseRepo.findAll().stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal profitLoss = totalPayments.subtract(totalExpenses);

        return Map.of(
                "totalPayments", totalPayments,
                "totalExpenses", totalExpenses,
                "profitLoss", profitLoss
        );
    }


    @Override
    public List<?> getAllOrders() {
        return (salesOrderRepo != null) ? salesOrderRepo.findAll() : Collections.emptyList();
    }

    @Override
    public void recomputeInvoiceAndOrder(Integer invoiceId) {
        Invoice inv = invoiceRepo.findById(invoiceId).orElseThrow();
        // Recompute invoice status from payments
        BigDecimal totalPaid = paymentRepo.findByInvoiceId(inv.getId()).stream()
                .map(Payment::getPaidAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalPaid.compareTo(inv.getAmount()) >= 0) {
            inv.setStatus(InvoiceStatus.PAID);
        } else if (inv.getDueDate() != null && inv.getDueDate().isBefore(LocalDate.now())) {
            inv.setStatus(InvoiceStatus.OVERDUE);
        } else {
            inv.setStatus(InvoiceStatus.PENDING);
        }
        invoiceRepo.save(inv);

        // Propagate to order and apartment
        if (inv.getSalesOrder() != null && salesOrderRepo != null) {
            Integer orderId = inv.getSalesOrder().getId();
            var order = salesOrderRepo.findById(orderId).orElseThrow();
            List<Invoice> invoices = invoiceRepo.findBySalesOrderId(orderId);
            boolean allPaid = !invoices.isEmpty() && invoices.stream().allMatch(i -> i.getStatus() == InvoiceStatus.PAID);
            if (allPaid) {
                order.setStatus(SalesOrderStatus.COMPLETED);
                order.setClosedAt(LocalDateTime.now());
                salesOrderRepo.save(order);
                if (apartmentRepo != null) {
                    var apt = order.getApartment();
                    apt.setStatus(ApartmentStatus.SOLD);
                    apartmentRepo.save(apt);
                }
            }
            // Recompute lead status after any payment change
            recomputeLeadStatusForOrder(orderId);
        }
    }

    @Override
    public void recomputeLeadStatusForOrder(Integer orderId) {
        if (salesOrderRepo == null || leadRepo == null) return;
        var order = salesOrderRepo.findById(orderId).orElseThrow();
        var lead = order.getLead();
        if (lead == null) return;

        // Use gross order total only; ignore any discount/net fields for lead status
        BigDecimal requiredTotal = order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO;

        // Sum confirmed payments across all invoices of this order
        List<Invoice> invoices = invoiceRepo.findBySalesOrderId(orderId);
        BigDecimal paid = BigDecimal.ZERO;
        for (var inv : invoices) {
            var ps = paymentRepo.findByInvoiceId(inv.getId());
            for (var p : ps) {
                if (p.getStatus() == PaymentStatus.CONFIRMED) {
                    paid = paid.add(p.getPaidAmount());
                }
            }
        }
        // Sum refunds related to this order
        BigDecimal refunded = BigDecimal.ZERO;
        for (var r : refundRepo.findAll()) {
            var pay = r.getPayment();
            if (pay != null && pay.getInvoice() != null && pay.getInvoice().getSalesOrder() != null
                    && Objects.equals(pay.getInvoice().getSalesOrder().getId(), orderId)) {
                refunded = refunded.add(r.getAmount());
            }
        }
        BigDecimal netPaid = paid.subtract(refunded);

        // Determine status
        LeadStatus newStatus;
        if (order.getStatus() == SalesOrderStatus.CANCELED || refunded.compareTo(BigDecimal.ZERO) > 0) {
            newStatus = LeadStatus.LOST;
        } else if (requiredTotal.compareTo(BigDecimal.ZERO) > 0 && netPaid.compareTo(requiredTotal) >= 0) {
            newStatus = LeadStatus.WON;
        } else if (requiredTotal.compareTo(BigDecimal.ZERO) > 0 && netPaid.compareTo(requiredTotal.multiply(new BigDecimal("0.5"))) >= 0) {
            newStatus = LeadStatus.QUALIFIED;
        } else {
            // CONTACTED if there is at least one interaction note; otherwise NEW
            boolean hasNotes = noteRepo != null && !noteRepo.findByLead_Id(lead.getId()).isEmpty();
            newStatus = hasNotes ? LeadStatus.CONTACTED : LeadStatus.NEW;
        }

        if (lead.getStatus() != newStatus) {
            lead.setStatus(newStatus);
            leadRepo.save(lead);
        }
    }
}
