package Group3.demo.Entity;

import Group3.demo.Entity.enums.SalesOrderStatus;
import Group3.demo.Entity.enums.PaymentPlan;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity @Table(name = "sales_orders")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SalesOrder extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "lead_id", nullable = false)
    private Lead lead;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "apartment_id", nullable = false)
    private Apartment apartment;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "sales_manager_id", nullable = false)
    private User salesManager;

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 16)
    private SalesOrderStatus status;

    @Column(precision = 18, scale = 2, nullable = false)
    private BigDecimal totalAmount;

    private LocalDateTime signedAt;
    private LocalDateTime closedAt;

    @Enumerated(EnumType.STRING)
    @Column(length = 16)
    private PaymentPlan paymentPlan; // FULL or HALF (Phase 1 defaults to FULL)

    // Phase 3: promotion linkage and computed amounts
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promotion_id")
    private Promotion promotion;

    @Column(precision = 18, scale = 2)
    private BigDecimal discountAmount; // applied discount at order time

    @Column(precision = 18, scale = 2)
    private BigDecimal netAmount; // totalAmount - discountAmount
}
