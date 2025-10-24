package Group3.demo.Controller;

import Group3.demo.Entity.*;
import Group3.demo.Entity.enums.*;
import Group3.demo.Repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import Group3.demo.Repository.CustomerRepository;
import Group3.demo.Service.SupportService;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.math.RoundingMode;
import Group3.demo.Service.DiscountService;

@Controller
@RequiredArgsConstructor
@RequestMapping("/customer")
public class CustomerController {

    private final UserRepository userRepo;
    private final ApartmentRepository apartmentRepo;
    private final PromotionRepository promotionRepo;
    private final SalesOrderRepository salesOrderRepo;
    private final TicketRepository ticketRepo;
    private final PasswordEncoder passwordEncoder;
    private final InvoiceRepository invoiceRepo;
    private final CustomerRepository customerRepo;
    private final SupportService supportService;
    private final RoleRepository roleRepository;
    private final PaymentRepository paymentRepo;
    private final DocumentRepository documentRepo;
    private final LeadRepository leadRepo;
    private final DiscountService discountService;

    /* ---------- Auth Page ---------- */
    @GetMapping("/auth")
    public String authPage(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("defaultAuthForm", "login");
        return "customer/auth";
    }

    /* ---------- Register ---------- */
    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("defaultAuthForm", "register");
        return "customer/auth";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute User user, Model model) {
        if (userRepo.findByUsername(user.getUsername()).isPresent()) {
            model.addAttribute("error", "Username already taken!");
            model.addAttribute("defaultAuthForm", "register");
            return "customer/auth";
        }
        if (userRepo.findByEmail(user.getEmail()).isPresent()) {
            model.addAttribute("error", "Email already registered!");
            model.addAttribute("defaultAuthForm", "register");
            return "customer/auth";
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setEnabled(true);
        user.setType(Group3.demo.Entity.enums.UserType.CUSTOMER);
        // Assign CUSTOMER role
        Role customerRole = roleRepository.findByName(RoleName.CUSTOMER)
                .orElseThrow(() -> new RuntimeException("Role not found: CUSTOMER"));
        user.getRoles().add(customerRole);
        userRepo.save(user);

        return "redirect:/home";
    }

    /* ---------- My Orders (list) ---------- */
    @GetMapping("/orders")
    public String myOrders(Model model, Authentication auth) {
        var orders = salesOrderRepo.findByCustomerUsername(auth.getName());
        model.addAttribute("orders", orders);
        return "customer/orders";
    }

    /* ---------- Order Details ---------- */
    @GetMapping("/orders/{id}")
    public String orderDetails(@PathVariable("id") Integer id, Model model, Authentication auth) {
        var order = salesOrderRepo.findById(id).orElseThrow();
        if (!order.getCustomer().getUsername().equals(auth.getName())) {
            throw new IllegalArgumentException("You are not allowed to view this order");
        }
        var invoices = invoiceRepo.findBySalesOrderId(id);
        java.util.Map<Integer, java.util.List<Payment>> invoicePayments = new java.util.HashMap<>();
        for (var inv : invoices) {
            invoicePayments.put(inv.getId(), paymentRepo.findByInvoiceId(inv.getId()));
        }
        var orderDocs = documentRepo.findByOwnerTypeAndOwnerId(Group3.demo.Entity.enums.DocumentOwner.SALES_ORDER, id);
        java.util.Map<Integer, java.util.List<Group3.demo.Entity.Document>> invoiceDocs = new java.util.HashMap<>();
        for (var inv : invoices) {
            invoiceDocs.put(inv.getId(), documentRepo.findByOwnerTypeAndOwnerId(Group3.demo.Entity.enums.DocumentOwner.INVOICE, inv.getId()));
        }
        model.addAttribute("order", order);
        model.addAttribute("invoices", invoices);
        model.addAttribute("invoicePayments", invoicePayments);
        model.addAttribute("orderDocs", orderDocs);
        model.addAttribute("invoiceDocs", invoiceDocs);
        return "customer/order-details";
    }

    /* ---------- Buy Apartment (Customer) ---------- */
    @GetMapping("/buy/{apartmentId}")
    public String confirmBuy(@PathVariable("apartmentId") Integer apartmentId,
                             @RequestParam(value = "promotionId", required = false) Integer promotionId,
                             Model model) {
        Apartment apt = apartmentRepo.findById(apartmentId).orElseThrow();
        if (apt.getStatus() == ApartmentStatus.SOLD) {
            model.addAttribute("error", "This apartment is already sold.");
            return "redirect:/home";
        }
        Promotion promo = null;
        BigDecimal discounted = null;
        if (promotionId != null) {
            promo = promotionRepo.findById(promotionId).orElse(null);
            if (promo != null && promo.getStatus() == PromotionStatus.ACTIVE) {
                BigDecimal orig = apt.getPrice() != null ? apt.getPrice() : BigDecimal.ZERO;
                BigDecimal disc = discountService.calculateDiscount(promo, orig);
                if (disc.compareTo(orig) > 0) disc = orig;
                discounted = orig.subtract(disc).setScale(2, RoundingMode.HALF_UP);
            }
        }
        model.addAttribute("apartment", apt);
        model.addAttribute("promotion", promo);
        model.addAttribute("discountedPrice", discounted);
        return "customer/buy";
    }

    @PostMapping("/buy")
    public String createLeadFromCustomer(@RequestParam("apartmentId") Integer apartmentId,
                                         @RequestParam(value = "promotionId", required = false) Integer promotionId,
                                         @RequestParam("paymentPlan") PaymentPlan paymentPlan,
                                         Authentication authentication,
                                         RedirectAttributes redirectAttributes) {
        var user = userRepo.findByUsername(authentication.getName()).orElseThrow();
        var apt = apartmentRepo.findById(apartmentId).orElseThrow();
        if (apt.getStatus() == ApartmentStatus.SOLD) {
            redirectAttributes.addAttribute("successMessage", "Sorry, this apartment was just sold.");
            return "redirect:/home";
        }
        Promotion promo = null;
        if (promotionId != null) {
            promo = promotionRepo.findById(promotionId).orElse(null);
        }
        // Auto-assign to a Sales Manager if available (pick the first found)
        User assigned = null;
        var smRole = roleRepository.findByName(RoleName.SALES_MANAGER).orElse(null);
        if (smRole != null) {
            assigned = userRepo.findAll().stream()
                    .filter(u -> u.getRoles() != null && u.getRoles().contains(smRole))
                    .findFirst()
                    .orElse(null);
        }
        Lead lead = Lead.builder()
                .customer(user)
                .apartment(apt)
                .promotion(promo)
                .paymentPlan(paymentPlan)
                .assignedTo(assigned)
                .status(LeadStatus.NEW)
                .source("CUSTOMER_PORTAL")
                .notes(promotionId != null ? ("Selected promotion ID: " + promotionId) : null)
                .build();
        lead = leadRepo.save(lead);
        return "redirect:/customer/thank-you?leadId=" + lead.getId();
    }

    @GetMapping("/thank-you")
    public String thankYou(@RequestParam("leadId") Integer leadId, Model model) {
        Lead lead = leadRepo.findById(leadId).orElseThrow();
        model.addAttribute("lead", lead);
        return "customer/thank-you";
    }
}
