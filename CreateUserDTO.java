package Group3.demo.DTO;

import Group3.demo.Entity.User;
import Group3.demo.Entity.enums.RoleName;
import Group3.demo.Entity.enums.UserType;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CreateUserDTO {
    private Integer id;
    private String username;
    private String fullName;
    private String email;
    private String password;

    private String phone;   // ✅ add this
    private boolean enabled; // ✅ also match entity

    private UserType type;
    private List<RoleName> roles = new ArrayList<>();

    public CreateUserDTO() {}

    // Map User -> DTO
    public CreateUserDTO(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.fullName = user.getFullName();
        this.email = user.getEmail();
        this.password = user.getPassword();
        this.phone = user.getPhone();   // ✅ map phone
        this.enabled = user.isEnabled();// ✅ map enabled
        this.type = user.getType();

        if (user.getRoles() != null) {
            this.roles = user.getRoles().stream()
                    .map(role -> role.getName())
                    .collect(Collectors.toList());
        }
    }

    // Getters & Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getPhone() { return phone; }       // ✅ getter
    public void setPhone(String phone) { this.phone = phone; } // ✅ setter

    public boolean isEnabled() { return enabled; }   // ✅ getter
    public void setEnabled(boolean enabled) { this.enabled = enabled; } // ✅ setter

    public UserType getType() { return type; }
    public void setType(UserType type) { this.type = type; }

    public List<RoleName> getRoles() { return roles; }
    public void setRoles(List<RoleName> roles) { this.roles = roles; }
}
