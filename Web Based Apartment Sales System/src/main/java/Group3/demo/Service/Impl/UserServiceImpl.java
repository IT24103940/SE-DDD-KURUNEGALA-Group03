package Group3.demo.Service.Impl;

import Group3.demo.DTO.CreateUserDTO;
import Group3.demo.Entity.Role;
import Group3.demo.Entity.User;
import Group3.demo.Entity.enums.RoleName;
import Group3.demo.Entity.enums.UserType;
import Group3.demo.Repository.RoleRepository;
import Group3.demo.Repository.UserRepository;
import Group3.demo.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;


    public UserServiceImpl(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User getUserById(Integer id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    public void createUser(CreateUserDTO dto) {
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setFullName(dto.getFullName());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setEnabled(true);
        user.setPassword(passwordEncoder.encode(dto.getPassword()));

        // ✅ Always set type = CUSTOMER for new registrations
        user.setType(UserType.CUSTOMER);

        // ✅ Every registered user = CUSTOMER role
        Role customerRole = roleRepository.findByName(RoleName.valueOf("CUSTOMER"))
                .orElseThrow(() -> new RuntimeException("Role not found: CUSTOMER"));

        Set<Role> roleEntities = new HashSet<>();
        roleEntities.add(customerRole);
        user.setRoles(roleEntities);

        userRepository.save(user);
    }


    @Override
    public void updateUser(Integer id, CreateUserDTO dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setUsername(dto.getUsername());
        user.setFullName(dto.getFullName());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setEnabled(dto.isEnabled());
        user.setType(dto.getType());

        // ✅ Encode new password if provided, otherwise keep old one
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        // ✅ Update roles if provided
        if (dto.getRoles() != null && !dto.getRoles().isEmpty()) {
            Set<Role> roleEntities = dto.getRoles().stream()
                    .map(roleName -> roleRepository.findByName(roleName)
                            .orElseThrow(() -> new RuntimeException("Role not found: " + roleName)))
                    .collect(Collectors.toSet());
            user.setRoles(roleEntities);
        }

        userRepository.save(user);
    }




    @Override
    public void deleteUser(Integer id) {
        userRepository.deleteById(id);
    }

    @Override
    public void resetPassword(Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // ✅ Reset to a default password (e.g. "123456")
        String defaultPassword = "123456";
        user.setPassword(passwordEncoder.encode(defaultPassword));

        userRepository.save(user);
    }

}
