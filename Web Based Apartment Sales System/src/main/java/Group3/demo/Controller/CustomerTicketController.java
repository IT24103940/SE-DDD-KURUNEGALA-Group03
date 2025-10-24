package Group3.demo.Controller;

import Group3.demo.Entity.Ticket;
import Group3.demo.Entity.User;
import Group3.demo.Entity.enums.TicketPriority;
import Group3.demo.Entity.enums.TicketStatus;
import Group3.demo.Repository.TicketRepository;
import Group3.demo.Repository.TicketReplyRepository;
import Group3.demo.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;   // <- correct import
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/customer")
public class CustomerTicketController {

    private final TicketRepository ticketRepo;
    private final UserRepository userRepo;
    private final TicketReplyRepository ticketReplyRepo;

    // Customer submits issue
    @PostMapping("/support/submit")
    public String submitTicket(@RequestParam String subject,
                               @RequestParam String description,
                               @RequestParam TicketPriority priority,
                               Authentication authentication) {

        User customer = userRepo.findByUsername(authentication.getName()).orElseThrow();

        Ticket t = new Ticket();
        t.setSubject(subject);
        t.setDescription(description);
        t.setPriority(priority);
        t.setStatus(TicketStatus.OPEN);
        t.setCustomer(customer);

        ticketRepo.save(t);
        return "redirect:/customer/tickets";
    }

    // Customer sees only their own tickets
    @GetMapping("/tickets")
    public String myTickets(Model model, Authentication authentication) {
        User me = userRepo.findByUsername(authentication.getName()).orElseThrow();
        model.addAttribute("tickets", ticketRepo.findByCustomerIdWithReplies(me.getId()));
        return "customer/tickets";
    }

    // Customer deletes their ticket
    @PostMapping("/tickets/{id}/delete")
    public String deleteTicket(@PathVariable("id") Integer id, Authentication authentication, Model model) {
        User me = userRepo.findByUsername(authentication.getName()).orElseThrow();
        Ticket ticket = ticketRepo.findById(id).orElseThrow();
        if (!ticket.getCustomer().getId().equals(me.getId())) {
            throw new org.springframework.security.access.AccessDeniedException("Not your ticket");
        }
        if (ticket.getStatus() != Group3.demo.Entity.enums.TicketStatus.OPEN) {
            String msg = "You can't delete a ticket that is " + ticket.getStatus().name().replace('_', ' ').toLowerCase() + ".";
            model.addAttribute("error", msg.substring(0, 1).toUpperCase() + msg.substring(1));
            return myTickets(model, authentication);
        }
        try {
            ticketRepo.delete(ticket);
            model.addAttribute("successMessage", "Ticket deleted successfully.");
        } catch (Exception e) {
            model.addAttribute("error", "Could not delete ticket. Please contact support.");
            return myTickets(model, authentication);
        }
        return myTickets(model, authentication);
    }

    // Customer edits their ticket (show form)
    @GetMapping("/tickets/{id}/edit")
    public String editTicketForm(@PathVariable("id") Integer id, Model model, Authentication authentication) {
        User me = userRepo.findByUsername(authentication.getName()).orElseThrow();
        Ticket ticket = ticketRepo.findById(id).orElseThrow();
        if (!ticket.getCustomer().getId().equals(me.getId())) {
            throw new org.springframework.security.access.AccessDeniedException("Not your ticket");
        }
        if (ticket.getStatus() != Group3.demo.Entity.enums.TicketStatus.OPEN) {
            String msg = "You can't edit a ticket that is " + ticket.getStatus().name().replace('_', ' ').toLowerCase() + ".";
            model.addAttribute("error", msg.substring(0, 1).toUpperCase() + msg.substring(1));
            return myTickets(model, authentication);
        }
        model.addAttribute("ticket", ticket);
        model.addAttribute("priorities", Group3.demo.Entity.enums.TicketPriority.values());
        return "customer/ticket-edit";
    }

    // Customer edits their ticket (submit form)
    @PostMapping("/tickets/{id}/edit")
    public String editTicket(@PathVariable("id") Integer id,
                             @RequestParam String subject,
                             @RequestParam String description,
                             @RequestParam Group3.demo.Entity.enums.TicketPriority priority,
                             Authentication authentication, Model model) {
        User me = userRepo.findByUsername(authentication.getName()).orElseThrow();
        Ticket ticket = ticketRepo.findById(id).orElseThrow();
        if (!ticket.getCustomer().getId().equals(me.getId())) {
            throw new org.springframework.security.access.AccessDeniedException("Not your ticket");
        }
        if (ticket.getStatus() != Group3.demo.Entity.enums.TicketStatus.OPEN) {
            String msg = "You can't edit a ticket that is " + ticket.getStatus().name().replace('_', ' ').toLowerCase() + ".";
            model.addAttribute("error", msg.substring(0, 1).toUpperCase() + msg.substring(1));
            return myTickets(model, authentication);
        }
        ticket.setSubject(subject);
        ticket.setDescription(description);
        ticket.setPriority(priority);
        ticketRepo.save(ticket);
        model.addAttribute("successMessage", "Ticket updated successfully.");
        return myTickets(model, authentication);
    }

    // Customer replies to support officer (only for IN_PROGRESS tickets)
    @PostMapping("/tickets/{id}/reply")
    public String customerReply(@PathVariable("id") Integer id,
                                @RequestParam String message,
                                Authentication authentication, Model model) {
        User me = userRepo.findByUsername(authentication.getName()).orElseThrow();
        Ticket ticket = ticketRepo.findById(id).orElseThrow();
        if (!ticket.getCustomer().getId().equals(me.getId())) {
            throw new org.springframework.security.access.AccessDeniedException("Not your ticket");
        }
        if (ticket.getStatus() != Group3.demo.Entity.enums.TicketStatus.IN_PROGRESS) {
            model.addAttribute("error", "You can only reply to tickets that are in progress.");
            // Show the tickets page with error, not the edit form
            return myTickets(model, authentication);
        }
        // Save reply as TicketReply
        Group3.demo.Entity.TicketReply reply = new Group3.demo.Entity.TicketReply();
        reply.setTicket(ticket);
        reply.setAuthor(me);
        reply.setMessage(message);
        reply.setCreatedAt(java.time.LocalDateTime.now());
        ticketReplyRepo.save(reply);
        model.addAttribute("successMessage", "Message sent successfully.");
        return myTickets(model, authentication);
    }
}
