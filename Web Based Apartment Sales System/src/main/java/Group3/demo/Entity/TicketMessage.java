package Group3.demo.Entity;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "ticket_messages")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TicketMessage extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Column(nullable = false, length = 2048)
    private String message;
}
