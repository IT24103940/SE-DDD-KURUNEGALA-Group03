package Group3.demo.Service;

import com.group3.demo.Entity.User;
import com.group3.demo.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private UserRepository repository;

    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public List<User> findAll() {
        return repository.findAll();
    }

    public Optional<User> findById(Long id) {
        return repository.findById(id);
    }

    public User save(User user) {
        user.setPassword(encoder.encode(user.getPassword()));
        user.setUpdatedAt(LocalDateTime.now());
        return repository.save(user);
    }

    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    public User findByUsername(String username) {
        return repository.findByUsername(username);
    }
}
