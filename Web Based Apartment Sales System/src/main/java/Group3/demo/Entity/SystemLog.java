package Group3.demo.Entity;

import Group3.demo.Entity.enums.LogLevel;
import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "system_logs")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SystemLog extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 16)
    private LogLevel level;

    @Column(nullable = false, length = 2048)
    private String message;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id")
    private User user;
}
