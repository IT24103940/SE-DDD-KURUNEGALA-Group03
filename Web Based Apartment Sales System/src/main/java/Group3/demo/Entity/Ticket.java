package Group3.demo.Entity;

import Group3.demo.Entity.enums.TicketPriority;
import Group3.demo.Entity.enums.TicketStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "tickets")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Ticket extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "assigned_to_id")
    private User assignedTo;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "apartment_id")
    private Apartment apartment;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "sales_order_id")
    private SalesOrder salesOrder;

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 16)
    private TicketStatus status;

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 16)
    private TicketPriority priority;

    @Column(nullable = false, length = 256)
    private String subject;

    @Column(length = 2048)
    private String description;

    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<TicketReply> replies = new java.util.ArrayList<>();
}
