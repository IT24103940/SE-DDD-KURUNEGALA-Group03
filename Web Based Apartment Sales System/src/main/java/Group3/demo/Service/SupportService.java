package Group3.demo.Service;

import Group3.demo.Entity.Ticket;
import Group3.demo.Entity.User;
import Group3.demo.Entity.enums.TicketPriority;
import Group3.demo.Entity.enums.TicketStatus;

public interface SupportService {

    Ticket createTicket(String subject, String description, TicketPriority priority, User customer);

    void updateStatus(Integer id, TicketStatus status);

    void addMessage(Integer ticketId, User author, String message);
}
