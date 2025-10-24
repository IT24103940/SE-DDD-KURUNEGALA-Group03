package Group3.demo.Repository;

import Group3.demo.Entity.TicketReply;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TicketReplyRepository extends JpaRepository<TicketReply, Integer> {
    List<TicketReply> findByTicketId(Integer ticketId);
}

