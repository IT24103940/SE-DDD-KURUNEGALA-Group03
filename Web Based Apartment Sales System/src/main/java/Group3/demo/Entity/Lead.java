package Group3.demo.Entity;

import Group3.demo.Entity.enums.LeadStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity @Table(name = "leads")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Lead extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "apartment_id", nullable = false)
    private Apartment apartment;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "assigned_to_id")
    private User assignedTo;

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 16)
    private LeadStatus status;

    @Column(length = 64) private String source;

    @Column(length = 2048) private String notes;

    // New: selected promotion (optional)
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "promotion_id")
    private Promotion promotion;

    // New: customer's chosen payment plan (defaults to FULL)
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_plan", length = 16)
    private Group3.demo.Entity.enums.PaymentPlan paymentPlan;

    @OneToMany(mappedBy = "lead", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SalesOrder> salesOrders = new ArrayList<>();
}
