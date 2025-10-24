package Group3.demo.Entity;

import Group3.demo.Entity.enums.PaymentMethod;
import Group3.demo.Entity.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity @Table(name = "payments")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Payment extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    @Column(precision = 18, scale = 2, nullable = false)
    private BigDecimal paidAmount;

    private LocalDate paidDate;

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 16)
    private PaymentMethod method;

    @Column(length = 128)
    private String reference;

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 16)
    private PaymentStatus status;
}
