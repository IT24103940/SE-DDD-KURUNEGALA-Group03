package Group3.demo.Service;

import Group3.demo.DTO.TicketDTO;
import Group3.demo.DTO.TicketReplyDTO;
import Group3.demo.Entity.Ticket;
import Group3.demo.Entity.TicketReply;
import Group3.demo.Repository.TicketRepository;
import Group3.demo.Repository.TicketReplyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomerSupportService {
    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private TicketReplyRepository replyRepository;

    public List<TicketDTO> getTicketsByCustomer(Integer customerId) {
        return ticketRepository.findByCustomerId(customerId).stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    public void createTicket(TicketDTO dto) {
        Ticket ticket = new Ticket();
        ticket.setCustomerId(dto.getCustomerId());
        ticket.setSubject(dto.getSubject());
        ticket.setDescription(dto.getDescription());
        ticket.setStatus("Open");
        ticketRepository.save(ticket);
    }

    public TicketDTO getTicket(Integer id) {
        return ticketRepository.findById(id)
            .map(this::convertToDTO)
            .orElseThrow(() -> new RuntimeException("Ticket not found"));
    }

    public List<TicketReplyDTO> getReplies(Integer ticketId) {
        return replyRepository.findByTicketId(ticketId).stream()
            .map(this::convertToReplyDTO)
            .collect(Collectors.toList());
    }

    public void addReply(TicketReplyDTO dto) {
        TicketReply reply = new TicketReply();
        reply.setTicketId(dto.getTicketId());
        reply.setUserId(dto.getUserId());
        reply.setMessage(dto.getMessage());
        replyRepository.save(reply);
    }

    public void updateStatus(Integer ticketId, String status) {
        Ticket ticket = ticketRepository.findById(ticketId)
            .orElseThrow(() -> new RuntimeException("Ticket not found"));
        ticket.setStatus(status);
        ticketRepository.save(ticket);
    }

    private TicketDTO convertToDTO(Ticket ticket) {
        TicketDTO dto = new TicketDTO();
        dto.setId(ticket.getId());
        dto.setCustomerId(ticket.getCustomerId());
        dto.setSubject(ticket.getSubject());
        dto.setDescription(ticket.getDescription());
        dto.setStatus(ticket.getStatus());
        return dto;
    }

    private TicketReplyDTO convertToReplyDTO(TicketReply reply) {
        TicketReplyDTO dto = new TicketReplyDTO();
        dto.setId(reply.getId());
        dto.setTicketId(reply.getTicketId());
        dto.setUserId(reply.getUserId());
        dto.setMessage(reply.getMessage());
        dto.setCreatedAt(reply.getCreatedAt().toString());
        return dto;
    }
}
