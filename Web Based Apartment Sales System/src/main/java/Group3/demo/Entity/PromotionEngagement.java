package Group3.demo.Entity;

import Group3.demo.Entity.enums.PromotionEventType;
import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "promotion_engagements")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PromotionEngagement extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "promotion_id", nullable = false)
    private Promotion promotion;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id")
    private User user; // nullable for anonymous

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 16)
    private PromotionEventType eventType;
}
