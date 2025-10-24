package Group3.demo.Controller;

import Group3.demo.Entity.Feedback;
import Group3.demo.Entity.User;
import Group3.demo.Repository.UserRepository;
import Group3.demo.Service.FeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
public class FeedbackController {
    @Autowired
    private FeedbackService feedbackService;
    @Autowired
    private UserRepository userRepository;

    // Customer: Show feedback form
    @GetMapping("/customer/feedback")
    public String feedbackForm(Model model) {
        model.addAttribute("feedback", new Feedback());
        return "customer/feedback-form";
    }

    // Customer: Submit feedback
    @PostMapping("/customer/feedback")
    public String submitFeedback(@ModelAttribute Feedback feedback, Authentication authentication, RedirectAttributes redirectAttributes) {
        if (authentication == null || !authentication.isAuthenticated()) {
            redirectAttributes.addFlashAttribute("error", "You must be logged in to submit feedback.");
            return "redirect:/customer/auth";
        }
        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "Could not find your user account. Please log in again.");
            return "redirect:/home";
        }
        // Basic validation and defaults
        if (feedback.getRating() < 1) feedback.setRating(1);
        if (feedback.getRating() > 5) feedback.setRating(5);
        if (feedback.getMessage() != null) feedback.setMessage(feedback.getMessage().trim());

        feedback.setCustomer(user);
        feedback.setStatus(Feedback.Status.PENDING);
        feedbackService.saveFeedback(feedback);
        redirectAttributes.addFlashAttribute("successMessage", "Feedback submitted and pending approval.");
        return "redirect:/home";
    }

    // Customer: View own feedback
    @GetMapping("/customer/my-feedback")
    public String myFeedback(Authentication authentication, Model model, RedirectAttributes redirectAttributes) {
        if (authentication == null || !authentication.isAuthenticated()) {
            redirectAttributes.addFlashAttribute("error", "You must be logged in to view your feedback.");
            return "redirect:/customer/auth";
        }
        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "Could not find your user account. Please log in again.");
            return "redirect:/customer/auth";
        }
        List<Feedback> feedbackList = feedbackService.getFeedbackByCustomer(user);
        model.addAttribute("feedbackList", feedbackList);
        return "customer/my-feedback";
    }

    // Customer: Edit feedback form (only if PENDING and owned by current user)
    @GetMapping("/customer/feedback/edit/{id}")
    public String editFeedbackForm(@PathVariable Long id, Authentication authentication, Model model, RedirectAttributes redirectAttributes) {
        if (authentication == null || !authentication.isAuthenticated()) {
            redirectAttributes.addFlashAttribute("error", "You must be logged in to edit feedback.");
            return "redirect:/customer/auth";
        }
        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "Could not find your user account. Please log in again.");
            return "redirect:/customer/auth";
        }
        Optional<Feedback> opt = feedbackService.getFeedbackById(id);
        if (opt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Feedback not found.");
            return "redirect:/customer/my-feedback";
        }
        Feedback fb = opt.get();
        if (!fb.getCustomer().getId().equals(user.getId())) {
            redirectAttributes.addFlashAttribute("error", "You can only edit your own feedback.");
            return "redirect:/customer/my-feedback";
        }
        if (fb.getStatus() != Feedback.Status.PENDING) {
            redirectAttributes.addFlashAttribute("error", "Only pending feedback can be edited.");
            return "redirect:/customer/my-feedback";
        }
        model.addAttribute("feedback", fb);
        return "customer/feedback-form";
    }

    // Customer: Save edited feedback (only if PENDING and owned by current user)
    @PostMapping("/customer/feedback/edit/{id}")
    public String updateFeedback(@PathVariable Long id,
                                 @RequestParam("message") String message,
                                 @RequestParam("rating") int rating,
                                 Authentication authentication,
                                 RedirectAttributes redirectAttributes) {
        if (authentication == null || !authentication.isAuthenticated()) {
            redirectAttributes.addFlashAttribute("error", "You must be logged in to edit feedback.");
            return "redirect:/customer/auth";
        }
        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "Could not find your user account. Please log in again.");
            return "redirect:/customer/auth";
        }
        Optional<Feedback> opt = feedbackService.getFeedbackById(id);
        if (opt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Feedback not found.");
            return "redirect:/customer/my-feedback";
        }
        Feedback fb = opt.get();
        if (!fb.getCustomer().getId().equals(user.getId())) {
            redirectAttributes.addFlashAttribute("error", "You can only edit your own feedback.");
            return "redirect:/customer/my-feedback";
        }
        if (fb.getStatus() != Feedback.Status.PENDING) {
            redirectAttributes.addFlashAttribute("error", "Only pending feedback can be edited.");
            return "redirect:/customer/my-feedback";
        }
        // Basic validation and trimming
        if (rating < 1) rating = 1;
        if (rating > 5) rating = 5;
        if (message != null) message = message.trim();

        fb.setMessage(message);
        fb.setRating(rating);
        feedbackService.saveFeedback(fb);
        redirectAttributes.addFlashAttribute("successMessage", "Feedback updated successfully.");
        return "redirect:/customer/my-feedback";
    }

    // Customer: Delete feedback (can delete own feedback regardless of status)
    @PostMapping("/customer/feedback/delete/{id}")
    public String deleteOwnFeedback(@PathVariable Long id, Authentication authentication, RedirectAttributes redirectAttributes) {
        if (authentication == null || !authentication.isAuthenticated()) {
            redirectAttributes.addFlashAttribute("error", "You must be logged in to delete feedback.");
            return "redirect:/customer/auth";
        }
        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "Could not find your user account. Please log in again.");
            return "redirect:/customer/auth";
        }
        Optional<Feedback> opt = feedbackService.getFeedbackById(id);
        if (opt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Feedback not found.");
            return "redirect:/customer/my-feedback";
        }
        Feedback fb = opt.get();
        if (!fb.getCustomer().getId().equals(user.getId())) {
            redirectAttributes.addFlashAttribute("error", "You can only delete your own feedback.");
            return "redirect:/customer/my-feedback";
        }
        feedbackService.deleteFeedback(id);
        redirectAttributes.addFlashAttribute("successMessage", "Feedback deleted successfully.");
        return "redirect:/customer/my-feedback";
    }

    // Officer: List all feedback
    @GetMapping("/support/feedback")
    public String allFeedback(Model model) {
        List<Feedback> feedbackList = feedbackService.getAllFeedback();
        model.addAttribute("feedbackList", feedbackList);
        return "support/feedback-list";
    }

    // Officer: Approve feedback
    @PostMapping("/support/feedback/{id}/approve")
    public String approveFeedback(@PathVariable Long id) {
        Optional<Feedback> feedbackOpt = feedbackService.getFeedbackById(id);
        feedbackOpt.ifPresent(f -> {
            f.setStatus(Feedback.Status.APPROVED);
            feedbackService.saveFeedback(f);
        });
        return "redirect:/support/feedback";
    }

    // Officer: Reject feedback
    @PostMapping("/support/feedback/{id}/reject")
    public String rejectFeedback(@PathVariable Long id) {
        Optional<Feedback> feedbackOpt = feedbackService.getFeedbackById(id);
        feedbackOpt.ifPresent(f -> {
            f.setStatus(Feedback.Status.REJECTED);
            feedbackService.saveFeedback(f);
        });
        return "redirect:/support/feedback";
    }

    // Officer: Delete feedback
    @PostMapping("/support/feedback/{id}/delete")
    public String deleteFeedback(@PathVariable Long id) {
        feedbackService.deleteFeedback(id);
        return "redirect:/support/feedback";
    }
}
