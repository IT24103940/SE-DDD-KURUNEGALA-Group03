package Group3.demo.Controller;

import Group3.demo.DTO.CreateUserDTO;
import Group3.demo.Entity.User;
import Group3.demo.Entity.enums.RoleName;
import Group3.demo.Entity.enums.UserType;
import Group3.demo.Service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/it/users")
public class ItTechnicianController {

    private final UserService userService;

    public ItTechnicianController(UserService userService) {
        this.userService = userService;
    }

    // LIST all users
    @GetMapping
    public String listUsers(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        return "it/users";
    }

    // SHOW create form

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("dto", new CreateUserDTO());
        model.addAttribute("types", UserType.values());

        // By default show all, filtering will happen on frontend JS
        model.addAttribute("staffRoles", List.of("ADMIN_OFFICER", "IT_TECHNICIAN", "SALES_MANAGER", "FINANCE_ASSISTANT", "MARKETING_EXECUTIVE", "CUSTOMER_SUPPORT"));
        model.addAttribute("customerRoles", List.of("CUSTOMER"));

        return "it/user-form";
    }


    // CREATE user
    @PostMapping
    public String createUser(@ModelAttribute("dto") CreateUserDTO dto) {
        userService.createUser(dto);
        return "redirect:/it/users";
    }

    // SHOW edit form
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable("id") Integer id, Model model) {
        User user = userService.getUserById(id);
        CreateUserDTO dto = new CreateUserDTO(user);
        model.addAttribute("dto", dto);
        model.addAttribute("userId", id);
        model.addAttribute("types", UserType.values());
        model.addAttribute("staffRoles", List.of("ADMIN_OFFICER", "IT_TECHNICIAN", "SALES_MANAGER", "FINANCE_ASSISTANT", "MARKETING_EXECUTIVE", "CUSTOMER_SUPPORT"));
        model.addAttribute("customerRoles", List.of("CUSTOMER"));


        return "it/user-form";
    }

    // UPDATE user
    @PostMapping("/{id}/edit")
    public String updateUser(@PathVariable("id") Integer id, @ModelAttribute("dto") CreateUserDTO dto) {
        userService.updateUser(id, dto);
        return "redirect:/it/users";
    }

    // DELETE user
    @PostMapping("/{id}/delete")
    public String deleteUser(@PathVariable("id") Integer id) {
        userService.deleteUser(id);
        return "redirect:/it/users";
    }

    // RESET password
    @PostMapping("/{id}/reset")
    public String resetPassword(@PathVariable("id") Integer id) {
        userService.resetPassword(id);
        return "redirect:/it/users";
    }
}
