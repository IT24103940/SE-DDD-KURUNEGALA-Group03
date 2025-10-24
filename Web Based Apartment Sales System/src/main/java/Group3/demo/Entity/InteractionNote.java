package Group3.demo.Entity;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "interaction_notes")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class InteractionNote extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "lead_id", nullable = false)
    private Lead lead;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Column(nullable = false, length = 2048)
    private String note;
}
