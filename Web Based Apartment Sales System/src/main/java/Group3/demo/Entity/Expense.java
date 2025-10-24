package Group3.demo.Entity;

import Group3.demo.Entity.enums.ExpenseType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity @Table(name = "expenses")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Expense extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 16)
    private ExpenseType type;

    @Column(precision = 18, scale = 2, nullable = false)
    private BigDecimal amount;

    private LocalDate expenseDate;

    @Column(length = 512)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "promotion_id")
    private Promotion promotion;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "apartment_id")
    private Apartment apartment;
}
