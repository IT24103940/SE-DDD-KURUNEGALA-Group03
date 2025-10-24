package Group3.demo.Entity;

import Group3.demo.Entity.enums.PromotionStatus;
import Group3.demo.Entity.enums.DiscountType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity @Table(name = "promotions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Promotion extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 128)
    private String title;

    @Column(length = 2048)
    private String description;

    private LocalDate startDate;
    private LocalDate endDate;

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 16)
    private PromotionStatus status;

    @Column(length = 512)
    private String bannerImageUrl;

    @Column(precision = 18, scale = 2)
    private BigDecimal budget;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "apartment_id")
    private Apartment apartment;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "created_by_id", nullable = false)
    private User createdBy;

    // Phase 3: Discount configuration
    @Enumerated(EnumType.STRING)
    @Column(length = 16)
    private DiscountType discountType; // PERCENT or FIXED

    @Column(precision = 18, scale = 2)
    private BigDecimal discountValue; // percent (0-100) or fixed amount
}
