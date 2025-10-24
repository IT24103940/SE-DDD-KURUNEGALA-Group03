package Group3.demo.Controller;

import Group3.demo.Entity.Ticket;
import Group3.demo.Entity.TicketReply;
import Group3.demo.Entity.User;
import Group3.demo.Entity.enums.TicketPriority;
import Group3.demo.Entity.enums.TicketStatus;
import Group3.demo.Repository.CustomerRepository;
import Group3.demo.Repository.TicketRepository;
import Group3.demo.Repository.TicketReplyRepository;
import Group3.demo.Repository.UserRepository;
import Group3.demo.Service.SupportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/support")
@RequiredArgsConstructor
public class CustomerSupportController {

    private final SupportService supportService;
    private final TicketRepository ticketRepo;
    private final TicketReplyRepository ticketReplyRepo;
    private final CustomerRepository customerRepo;
    private final UserRepository userRepo;

    // Dashboard landing
    @GetMapping({"", "/", "/dashboard"})
    public String dashboard() {
        return "support/dashboard";
    }

    /* ----------- All Tickets ----------- */
    @GetMapping("/tickets")
    public String tickets(Model model) {
        model.addAttribute("tickets", ticketRepo.findAll());
        model.addAttribute("customers", customerRepo.findAll()); // dropdown to pick customer
        return "support/tickets";
    }

    /* ----------- Create Ticket ----------- */
    @PostMapping("/tickets")
    public String createTicket(@RequestParam String subject,
                               @RequestParam String description,
                               @RequestParam TicketPriority priority,
                               @RequestParam Integer customerId) {
        User customer = customerRepo.findById(customerId).orElseThrow();
        supportService.createTicket(subject, description, priority, customer);
        return "redirect:/support/tickets";
    }



    /* ----------- View Ticket Detail ----------- */
    @GetMapping("/tickets/{id}")
    public String ticketDetail(@PathVariable("id") Integer id, Model model) {
        Ticket ticket = ticketRepo.findById(id).orElseThrow();
        java.util.List<TicketReply> replies = ticketReplyRepo.findByTicketId(id);
        model.addAttribute("ticket", ticket);
        model.addAttribute("replies", replies);
        model.addAttribute("statuses", TicketStatus.values());
        return "support/ticket-detail";
    }

    /* ----------- Update Ticket Status ----------- */
    @PostMapping("/tickets/{id}/status")
    public String updateStatus(@PathVariable("id") Integer id, @RequestParam TicketStatus status) {
        supportService.updateStatus(id, status);
        return "redirect:/support/tickets/" + id;
    }

    /* ----------- Reply to Ticket ----------- */
    @PostMapping("/tickets/{id}/reply")
    public String reply(@PathVariable("id") Integer id,
                        @RequestParam String message) {

        // Hardcoded support officer (make sure "sup1" exists in users table)
        User supportOfficer = userRepo.findByUsername("sup1")
                .orElseThrow(() -> new RuntimeException("Support officer user not found"));

        supportService.addMessage(id, supportOfficer, message);
        return "redirect:/support/tickets/" + id;
    }

    /* ----------- Delete Ticket ----------- */
    @PostMapping("/tickets/{id}/delete")
    public String deleteTicket(@PathVariable("id") Integer id) {
        // delete messages first → then ticket
        ticketReplyRepo.deleteAll(ticketReplyRepo.findByTicketId(id));
        ticketRepo.deleteById(id);
        return "redirect:/support/tickets";
    }

    @PostMapping("/support/submit")
    public String submitTicket(@RequestParam String subject,
                               @RequestParam String description,
                               @RequestParam TicketPriority priority,
                               @RequestParam Integer customerId,
                               RedirectAttributes redirectAttrs) {

        User customer = userRepo.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        Ticket ticket = supportService.createTicket(subject, description, priority, customer);

        // Save first message from customer as well
        supportService.addMessage(ticket.getId(), customer, description);

        // 🔥 Add flash message
        redirectAttrs.addFlashAttribute("successMessage", "✅ Ticket created successfully!");

        // 🔥 Redirect back to home
        return "redirect:/";
    }

}
