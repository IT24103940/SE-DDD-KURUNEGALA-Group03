package Group3.demo.Repository;

import Group3.demo.Entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Integer> {
    List<Ticket> findByCustomerUsername(String username);
    List<Ticket> findByCustomerId(Integer customerId);

    @Query("SELECT t FROM Ticket t LEFT JOIN FETCH t.replies WHERE t.customer.id = :customerId")
    List<Ticket> findByCustomerIdWithReplies(@Param("customerId") Integer customerId);
}
