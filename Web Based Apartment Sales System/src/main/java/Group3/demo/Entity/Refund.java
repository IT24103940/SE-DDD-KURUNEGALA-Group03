package Group3.demo.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity @Table(name = "refunds")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Refund extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    @Column(precision = 18, scale = 2, nullable = false)
    private BigDecimal amount;

    private LocalDate refundedDate;

    @Column(length = 512)
    private String reason;
}
