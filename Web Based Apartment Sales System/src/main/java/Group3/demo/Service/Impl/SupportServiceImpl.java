package Group3.demo.Service.Impl;

import Group3.demo.Entity.Ticket;
import Group3.demo.Entity.TicketReply;
import Group3.demo.Entity.User;
import Group3.demo.Entity.enums.TicketPriority;
import Group3.demo.Entity.enums.TicketStatus;
import Group3.demo.Repository.TicketRepository;
import Group3.demo.Repository.TicketReplyRepository;
import Group3.demo.Service.SupportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SupportServiceImpl implements SupportService {

    private final TicketRepository ticketRepo;
    private final TicketReplyRepository ticketReplyRepo;

    /*@Override
    public Ticket createTicket(String subject, String description, TicketPriority priority, User customer) {
        Ticket t = new Ticket();
        t.setSubject(subject);
        t.setDescription(description);
        t.setPriority(priority);
        t.setStatus(TicketStatus.OPEN);
        t.setCustomer(customer);
        return ticketRepo.save(t);
    } */

    @Override
    public Ticket createTicket(String subject, String description, TicketPriority priority, User customer) {
        Ticket t = new Ticket();
        t.setSubject(subject);
        t.setDescription(description);
        t.setPriority(priority);
        t.setStatus(TicketStatus.OPEN);
        t.setCustomer(customer);  // ✅ customer assigned
        return ticketRepo.save(t);
    }


    @Override
    public void updateStatus(Integer id, TicketStatus status) {
        Ticket ticket = ticketRepo.findById(id).orElseThrow();
        ticket.setStatus(status);
        ticketRepo.save(ticket);
    }

    @Override
    public void addMessage(Integer ticketId, User author, String message) {
        Ticket ticket = ticketRepo.findById(ticketId).orElseThrow();
        TicketReply reply = new TicketReply();
        reply.setTicket(ticket);
        reply.setAuthor(author);
        reply.setMessage(message);
        reply.setCreatedAt(java.time.LocalDateTime.now());
        ticketReplyRepo.save(reply);
    }
}
