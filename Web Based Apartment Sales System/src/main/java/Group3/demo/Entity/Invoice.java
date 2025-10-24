package Group3.demo.Entity;

import Group3.demo.Entity.enums.InvoiceStatus;
import Group3.demo.Entity.enums.InvoiceType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity @Table(name = "invoices")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Invoice extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "sales_order_id", nullable = false)
    private SalesOrder salesOrder;

    @Column(nullable = false, length = 64, unique = true)
    private String invoiceNumber;

    @Column(precision = 18, scale = 2, nullable = false)
    private BigDecimal amount;

    private LocalDate issueDate;
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 16)
    private InvoiceStatus status;

    @Enumerated(EnumType.STRING)
    @Column(length = 16)
    private InvoiceType type; // FULL, DEPOSIT, BALANCE
}
