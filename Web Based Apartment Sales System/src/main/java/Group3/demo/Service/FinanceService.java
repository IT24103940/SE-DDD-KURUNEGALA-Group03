package Group3.demo.Service;

import Group3.demo.Entity.*;
import Group3.demo.Entity.enums.InvoiceStatus;
import Group3.demo.Entity.enums.InvoiceType;
import Group3.demo.Entity.enums.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface FinanceService {

    // Invoices
    List<Invoice> getAllInvoices();
    Invoice getInvoiceById(Integer id);
    void createInvoice(Integer orderId, String number, BigDecimal amount);
    void updateInvoice(Integer id, String number, BigDecimal amount, InvoiceStatus status, Integer orderId);
    void deleteInvoice(Integer id);

    // Payments
    List<Payment> getAllPayments();
    void createPayment(Integer invoiceId, BigDecimal paidAmount, PaymentMethod method, String reference);
    void deletePayment(Integer id);

    // Expenses
    List<Expense> getAllExpenses();
    void createExpense(BigDecimal amount, String type, String description);
    void deleteExpense(Integer id);

    // Summary + Orders
    Map<String, Object> getFinanceSummary();
    List<?> getAllOrders();
    void recomputeInvoiceAndOrder(Integer invoiceId);

    // Phase 2: detailed invoice creation
    void createInvoiceWithDetails(Integer orderId, String number, BigDecimal amount, LocalDate issueDate, LocalDate dueDate, InvoiceType type);

    // Lead status recompute based on order payments/refunds/notes
    void recomputeLeadStatusForOrder(Integer orderId);
}
