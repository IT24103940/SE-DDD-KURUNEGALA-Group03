package Group3.demo.Config;

import Group3.demo.Entity.*;
import Group3.demo.Entity.enums.*;
import Group3.demo.Repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Configuration
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final RoleRepository roleRepo;
    private final UserRepository userRepo;
    private final ApartmentRepository apartmentRepo;
    private final PromotionRepository promotionRepo;

    private final PasswordEncoder encoder;

    @Override
    public void run(String... args) {
        // Roles
        for (var rn : RoleName.values()) {
            roleRepo.findByName(rn).orElseGet(() -> roleRepo.save(Role.builder().name(rn).build()));
        }

        // Users (one per role + a customer)
        createUserIfMissing("admin1", "Admin Officer", "admin@demo.com", RoleName.ADMIN_OFFICER, UserType.STAFF);
        createUserIfMissing("it1", "IT Technician", "it@demo.com", RoleName.IT_TECHNICIAN, UserType.STAFF);
        createUserIfMissing("sales1", "Sales Manager", "sales@demo.com", RoleName.SALES_MANAGER, UserType.STAFF);
        createUserIfMissing("fin1", "Finance Assistant", "finance@demo.com", RoleName.FINANCE_ASSISTANT, UserType.STAFF);
        createUserIfMissing("mkt1", "Marketing Exec", "mkt@demo.com", RoleName.MARKETING_EXECUTIVE, UserType.STAFF);
        createUserIfMissing("sup1", "Support Officer", "support@demo.com", RoleName.CUSTOMER_SUPPORT, UserType.STAFF);
        createUserIfMissing("cust1", "John Customer", "customer@demo.com", RoleName.CUSTOMER, UserType.CUSTOMER);

        // Apartments
        if (apartmentRepo.count() == 0) {
            apartmentRepo.saveAll(List.of(
                    Apartment.builder().code("APT-101").title("Cozy 2BHK").city("Colombo")
                            .price(new BigDecimal("120000.00")).status(ApartmentStatus.AVAILABLE).bedrooms(2).bathrooms(1).areaSqFt(850).build(),
                    Apartment.builder().code("APT-102").title("Luxury Penthouse").city("Kandy")
                            .price(new BigDecimal("350000.00")).status(ApartmentStatus.AVAILABLE).bedrooms(4).bathrooms(3).areaSqFt(2100).build()
            ));
        }

        // Promotions
        if (promotionRepo.count() == 0) {
            var mk = userRepo.findByUsername("mkt1")
                    .orElseThrow(() -> new RuntimeException("Marketing user not found"));
            promotionRepo.save(Promotion.builder()
                    .title("New Year Sale").status(PromotionStatus.ACTIVE)
                    .startDate(LocalDate.now().minusDays(2))
                    .endDate(LocalDate.now().plusDays(20))
                    .budget(new BigDecimal("5000.00"))
                    .createdBy(mk)
                    .description("Discounts on selected apartments")
                    .build());
        }
    }

    private void createUserIfMissing(String username, String name, String email, RoleName roleName, UserType type) {
        if (userRepo.findByUsername(username).isEmpty()) {
            var role = roleRepo.findByName(roleName)
                    .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));
            var u = User.builder()
                    .username(username)
                    .fullName(name)
                    .email(email)
                    .enabled(true)
                    .type(type)
                    // ⚠️ Replace with env variable or config in real app
                    .password(encoder.encode("password"))
                    .roles(Set.of(role))
                    .build();
            userRepo.save(u);
        }
    }
}
