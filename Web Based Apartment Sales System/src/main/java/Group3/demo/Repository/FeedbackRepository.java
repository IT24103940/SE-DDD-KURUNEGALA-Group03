package Group3.demo.Repository;

import Group3.demo.Entity.Feedback;
import Group3.demo.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    List<Feedback> findByStatus(Feedback.Status status);
    List<Feedback> findByCustomer(User customer);
    List<Feedback> findByCustomerId(Long customerId);
}
