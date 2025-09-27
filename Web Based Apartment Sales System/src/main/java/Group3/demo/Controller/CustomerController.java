package Group3.demo.Controller;

import Group3.demo.Entity.Customer;
import Group3.demo.Service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {
    private final CustomerService service;
    public CustomerController(CustomerService service) { this.service = service; }

    @PostMapping
    public Customer create(@Valid @RequestBody Customer c) { return service.createOrGet(c); }

    @GetMapping
    public List<Customer> all() { return service.all(); }
}
