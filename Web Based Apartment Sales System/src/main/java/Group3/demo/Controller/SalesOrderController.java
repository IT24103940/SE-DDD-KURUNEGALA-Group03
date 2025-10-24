package Group3.demo.Controller;

import Group3.demo.Entity.SalesOrder;
import Group3.demo.Entity.Apartment;
import Group3.demo.Entity.Promotion;
import Group3.demo.Entity.Document;
import Group3.demo.Entity.Lead;
import Group3.demo.Entity.enums.*;
import Group3.demo.Repository.*;
import Group3.demo.Service.FinanceService;
import Group3.demo.Service.DiscountService;
import Group3.demo.Util.OrderNumberGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Controller
@RequestMapping("/sales/orders")
@RequiredArgsConstructor
public class SalesOrderController {

    private final SalesOrderRepository salesOrderRepo;
    private final LeadRepository leadRepo;
    private final ApartmentRepository apartmentRepo;
    private final UserRepository userRepo;
    private final FinanceService financeService;
    private final InvoiceRepository invoiceRepo;
    private final PromotionRepository promotionRepo;
    private final DocumentRepository documentRepo;
    private final PaymentRepository paymentRepo;
    private final DiscountService discountService;

    @GetMapping
    public String listOrders(Model model, @RequestParam(value = "error", required = false) String error) {
        var orders = salesOrderRepo.findAll();
        model.addAttribute("orders", orders);
        if (error != null) model.addAttribute("errorMessage", error);
        Map<Integer, List<Document>> orderDocs = new HashMap<>();
        for (var o : orders) {
            orderDocs.put(o.getId(), documentRepo.findByOwnerTypeAndOwnerId(Group3.demo.Entity.enums.DocumentOwner.SALES_ORDER, o.getId()));
        }
        model.addAttribute("orderDocs", orderDocs);
        return "sales/orders";
    }

    @GetMapping("/new")
    public String newOrder(Model model, @RequestParam(value = "leadId", required = false) Integer leadId) {
        var leads = leadRepo.findAll();
        var apartments = apartmentRepo.findAll();
        var users = userRepo.findAll().stream()
                .filter(u -> u.getType().name().equals("CUSTOMER")).toList();
        model.addAttribute("leads", leads);
        model.addAttribute("apartments", apartments);
        model.addAttribute("customers", users);
        model.addAttribute("statuses", SalesOrderStatus.values());
        model.addAttribute("paymentPlans", PaymentPlan.values());
        List<Promotion> activePromos = promotionRepo.findAll().stream()
                .filter(p -> p.getStatus() == PromotionStatus.ACTIVE)
                .toList();
        model.addAttribute("promotions", activePromos);
        // compact lead meta for JS: id, apartmentId, customerId, promotionId, paymentPlan
        List<Map<String, Object>> leadData = new ArrayList<>();
        for (var l : leads) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", l.getId());
            m.put("apartmentId", l.getApartment() != null ? l.getApartment().getId() : null);
            m.put("customerId", l.getCustomer() != null ? l.getCustomer().getId() : null);
            m.put("promotionId", l.getPromotion() != null ? l.getPromotion().getId() : null);
            m.put("paymentPlan", l.getPaymentPlan() != null ? l.getPaymentPlan().name() : null);
            leadData.add(m);
        }
        model.addAttribute("leadData", leadData);
        if (leadId != null) model.addAttribute("selectedLeadId", leadId);

        // minimal apt and promo data for price suggestion
        List<Map<String, Object>> aptData = new ArrayList<>();
        for (var a : apartments) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", a.getId());
            m.put("price", a.getPrice());
            aptData.add(m);
        }
        model.addAttribute("aptData", aptData);
        List<Map<String, Object>> promoData = new ArrayList<>();
        for (var p : activePromos) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", p.getId());
            m.put("type", p.getDiscountType() != null ? p.getDiscountType().name() : null);
            m.put("value", p.getDiscountValue());
            promoData.add(m);
        }
        model.addAttribute("promoData", promoData);
        return "sales/order-form";
    }

    @GetMapping("/{id}/edit")
    public String editOrderForm(@PathVariable("id") Integer id, Model model) {
        SalesOrder order = salesOrderRepo.findById(id).orElseThrow();
        var leads = leadRepo.findAll();
        model.addAttribute("order", order);
        model.addAttribute("leads", leads);
        model.addAttribute("apartments", apartmentRepo.findAll());
        model.addAttribute("customers", userRepo.findAll().stream()
                .filter(u -> u.getType().name().equals("CUSTOMER")).toList());
        model.addAttribute("statuses", SalesOrderStatus.values());
        model.addAttribute("paymentPlans", PaymentPlan.values());
        List<Map<String, Object>> leadData = new ArrayList<>();
        for (var l : leads) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", l.getId());
            m.put("apartmentId", l.getApartment() != null ? l.getApartment().getId() : null);
            m.put("customerId", l.getCustomer() != null ? l.getCustomer().getId() : null);
            leadData.add(m);
        }
        model.addAttribute("leadData", leadData);
        return "sales/order-edit";
    }

    @PostMapping("/{id}/update")
    public String updateOrder(@PathVariable("id") Integer id,
                              @RequestParam("leadId") Integer leadId,
                              @RequestParam(value = "apartmentId", required = false) Integer apartmentId,
                              @RequestParam(value = "customerId", required = false) Integer customerId,
                              @RequestParam("totalAmount") BigDecimal totalAmount,
                              @RequestParam("status") SalesOrderStatus status,
                              @RequestParam(value = "paymentPlan", required = false) PaymentPlan paymentPlan,
                              Authentication authentication) {
        SalesOrder order = salesOrderRepo.findById(id).orElseThrow();

        Lead lead = leadRepo.findById(leadId).orElseThrow();
        order.setLead(lead);
        Apartment apt = lead.getApartment();
        order.setApartment(apt);
        order.setCustomer(lead.getCustomer());

        var current = userRepo.findByUsername(authentication.getName()).orElseThrow();
        order.setSalesManager(current);
        order.setTotalAmount(totalAmount);
        order.setStatus(status);
        order.setSignedAt(order.getSignedAt() != null ? order.getSignedAt() : LocalDateTime.now());
        order.setPaymentPlan(paymentPlan != null ? paymentPlan : PaymentPlan.FULL);
        salesOrderRepo.save(order);
        // trigger lead status recompute
        financeService.recomputeLeadStatusForOrder(order.getId());
        return "redirect:/sales/orders";
    }

    @PostMapping
    public String createOrder(@RequestParam("leadId") Integer leadId,
                              @RequestParam(value = "apartmentId", required = false) Integer apartmentId,
                              @RequestParam(value = "customerId", required = false) Integer customerId,
                              @RequestParam("totalAmount") BigDecimal totalAmount,
                              @RequestParam(value = "status", required = false) SalesOrderStatus status,
                              @RequestParam(value = "paymentPlan", required = false) PaymentPlan paymentPlan,
                              @RequestParam(value = "promotionId", required = false) Integer promotionId,
                              Authentication authentication,
                              RedirectAttributes redirectAttributes) {
        Lead lead = leadRepo.findById(leadId).orElseThrow();
        Apartment apt = lead.getApartment();
        if (apt == null) {
            redirectAttributes.addAttribute("error", "Lead has no associated apartment");
            return "redirect:/sales/orders";
        }
        if (apt.getStatus() == ApartmentStatus.SOLD) {
            redirectAttributes.addAttribute("error", "Apartment is sold");
            return "redirect:/sales/orders";
        }

        PaymentPlan plan = paymentPlan != null ? paymentPlan : PaymentPlan.FULL;

        Promotion promo = null;
        BigDecimal discount = BigDecimal.ZERO;
        BigDecimal effective = totalAmount;
        if (promotionId != null) {
            promo = promotionRepo.findById(promotionId).orElseThrow();
            if (!isPromotionApplicable(promo, apt)) {
                throw new IllegalArgumentException("Selected promotion is not applicable");
            }
            discount = discountService.calculateDiscount(promo, totalAmount);
            if (discount.compareTo(totalAmount) > 0) discount = totalAmount;
            effective = totalAmount.subtract(discount);
        }

        var current = userRepo.findByUsername(authentication.getName()).orElseThrow();

        SalesOrder order = SalesOrder.builder()
                .lead(lead)
                .apartment(apt)
                .customer(lead.getCustomer())
                .salesManager(current)
                .totalAmount(totalAmount)
                .discountAmount(discount)
                .netAmount(effective)
                .promotion(promo)
                .status(status != null ? status : SalesOrderStatus.PENDING)
                .paymentPlan(plan)
                .signedAt(LocalDateTime.now())
                .build();
        order = salesOrderRepo.save(order);

        // Reserve apartment on order creation (idempotent)
        if (apt.getStatus() == ApartmentStatus.AVAILABLE) {
            apt.setStatus(ApartmentStatus.RESERVED);
            apartmentRepo.save(apt);
        }

        // Auto-generate invoice(s) using GROSS order total (ignore discounts for invoice amounts)
        if (plan == PaymentPlan.FULL) {
            String invNumber = OrderNumberGenerator.INSTANCE.generate(order.getId(), "FULL");
            financeService.createInvoiceWithDetails(order.getId(), invNumber, totalAmount, LocalDate.now(), LocalDate.now().plusDays(7), InvoiceType.FULL);
        } else {
            BigDecimal deposit = totalAmount.multiply(new BigDecimal("0.5")).setScale(2, RoundingMode.HALF_UP);
            BigDecimal balance = totalAmount.subtract(deposit);
            String depNumber = OrderNumberGenerator.INSTANCE.generate(order.getId(), "DEP");
            String balNumber = OrderNumberGenerator.INSTANCE.generate(order.getId(), "BAL");
            financeService.createInvoiceWithDetails(order.getId(), depNumber, deposit, LocalDate.now(), LocalDate.now().plusDays(7), InvoiceType.DEPOSIT);
            financeService.createInvoiceWithDetails(order.getId(), balNumber, balance, LocalDate.now(), LocalDate.now().plusDays(30), InvoiceType.BALANCE);
        }

        // trigger lead status recompute after create
        financeService.recomputeLeadStatusForOrder(order.getId());
        return "redirect:/sales/orders";
    }

    private boolean isPromotionApplicable(Promotion p, Apartment apt) {
        if (p.getStatus() != PromotionStatus.ACTIVE) return false;
        LocalDate today = LocalDate.now();
        if (p.getStartDate() != null && today.isBefore(p.getStartDate())) return false;
        if (p.getEndDate() != null && today.isAfter(p.getEndDate())) return false;
        if (p.getApartment() != null && !p.getApartment().getId().equals(apt.getId())) return false;
        return true;
    }

    @PostMapping("/{id}/delete")
    public String deleteOrder(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
        var invoices = invoiceRepo.findBySalesOrderId(id);
        boolean hasPayments = invoices.stream()
                .anyMatch(inv -> !paymentRepo.findByInvoiceId(inv.getId()).isEmpty());
        if (!invoices.isEmpty() || hasPayments) {
            redirectAttributes.addAttribute("error", "Cannot delete order with invoices or payments. Use Cancel if no payments.");
            return "redirect:/sales/orders";
        }
        salesOrderRepo.deleteById(id);
        return "redirect:/sales/orders";
    }

    @PostMapping("/{id}/cancel")
    public String cancelOrder(@PathVariable("id") Integer id, Model model) {
        var order = salesOrderRepo.findById(id).orElseThrow();
        if (order.getStatus() == SalesOrderStatus.COMPLETED || order.getStatus() == SalesOrderStatus.CANCELED) {
            model.addAttribute("errorMessage", "Order cannot be canceled in its current state");
            return listOrders(model, null);
        }
        var invoices = invoiceRepo.findBySalesOrderId(id);
        boolean hasPayments = invoices.stream()
                .anyMatch(inv -> !paymentRepo.findByInvoiceId(inv.getId()).isEmpty());
        if (hasPayments) {
            model.addAttribute("errorMessage", "Cannot cancel order that has invoice payments. Refund first.");
            return listOrders(model, null);
        }
        for (var inv : invoices) {
            invoiceRepo.deleteById(inv.getId());
        }
        var apt = order.getApartment();
        if (apt != null) {
            apt.setStatus(ApartmentStatus.AVAILABLE);
            apartmentRepo.save(apt);
        }
        order.setStatus(SalesOrderStatus.CANCELED);
        salesOrderRepo.save(order);
        // update lead status as LOST after cancel
        financeService.recomputeLeadStatusForOrder(order.getId());
        return "redirect:/sales/orders";
    }
}
