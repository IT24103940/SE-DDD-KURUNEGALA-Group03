package Group3.demo.Repository;

import Group3.demo.Entity.TicketMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketMessageRepository extends JpaRepository<TicketMessage, Integer> {
    List<TicketMessage> findByTicketId(Integer ticketId);
}
