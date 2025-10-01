package Group3.demo.Controller;

import Group3.demo.DTO.TicketDTO;
import Group3.demo.DTO.TicketReplyDTO;
import Group3.demo.Service.CustomerSupportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/support")
public class CustomerSupportController {
    @Autowired
    private CustomerSupportService service;

    @GetMapping("/tickets")
    public String viewTickets(@RequestParam Integer userId, Model model) {
        model.addAttribute("tickets", service.getTicketsByCustomer(userId));
        return "support/view-tickets";
    }

    @GetMapping("/create")
    public String createTicketForm(Model model) {
        model.addAttribute("ticket", new TicketDTO());
        return "support/create-ticket";
    }

    @PostMapping("/create")
    public String createTicket(@ModelAttribute TicketDTO dto) {
        service.createTicket(dto);
        return "redirect:/support/tickets?userId=" + dto.getCustomerId();
    }

    @GetMapping("/ticket/{id}")
    public String viewTicketDetails(@PathVariable Integer id, Model model) {
        model.addAttribute("ticket", service.getTicket(id));
        model.addAttribute("replies", service.getReplies(id));
        model.addAttribute("newReply", new TicketReplyDTO());
        return "support/ticket-details";
    }

    @PostMapping("/reply")
    public String addReply(@ModelAttribute TicketReplyDTO dto) {
        service.addReply(dto);
        return "redirect:/support/ticket/" + dto.getTicketId();
    }

    @PostMapping("/update-status")
    public String updateStatus(@RequestParam Integer ticketId, @RequestParam String status) {
        service.updateStatus(ticketId, status);
        return "redirect:/support/ticket/" + ticketId;
    }
}