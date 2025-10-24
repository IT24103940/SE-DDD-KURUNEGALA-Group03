package Group3.demo.Repository;

import Group3.demo.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<User, Integer> { }
