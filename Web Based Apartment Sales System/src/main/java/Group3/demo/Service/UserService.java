package Group3.demo.Service;

import Group3.demo.DTO.CreateUserDTO;
import Group3.demo.Entity.User;

import java.util.List;

public interface UserService {
    List<User> getAllUsers();
    User getUserById(Integer id);
    void createUser(CreateUserDTO dto);
    void updateUser(Integer id, CreateUserDTO dto);
    void deleteUser(Integer id);
    void resetPassword(Integer id);
}
