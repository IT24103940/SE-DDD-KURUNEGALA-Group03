package Group3.demo.Service;

import Group3.demo.Entity.Customer;
import Group3.demo.Repository.CustomerRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerService {
    private final CustomerRepository repo;
    public CustomerService(CustomerRepository repo) { this.repo = repo; }

    public Customer createOrGet(Customer customer) {
        return repo.findByEmail(customer.getEmail()).orElseGet(() -> repo.save(customer));
    }
    public List<Customer> all() { return repo.findAll(); }
}
