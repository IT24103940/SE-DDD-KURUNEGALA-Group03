package Group3.demo.Service;

import Group3.demo.Entity.Feedback;
import Group3.demo.Entity.User;
import Group3.demo.Repository.FeedbackRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class FeedbackService {
    @Autowired
    private FeedbackRepository feedbackRepository;

    public Feedback saveFeedback(Feedback feedback) {
        return feedbackRepository.save(feedback);
    }

    public List<Feedback> getApprovedFeedback() {
        return feedbackRepository.findByStatus(Feedback.Status.APPROVED);
    }

    public List<Feedback> getAllFeedback() {
        return feedbackRepository.findAll();
    }

    public List<Feedback> getFeedbackByCustomer(User customer) {
        return feedbackRepository.findByCustomerId(customer.getId().longValue());
    }

    public Optional<Feedback> getFeedbackById(Long id) {
        return feedbackRepository.findById(id);
    }

    public void deleteFeedback(Long id) {
        feedbackRepository.deleteById(id);
    }
}
