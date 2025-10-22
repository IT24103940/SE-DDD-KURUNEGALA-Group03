package Group3.demo.Entity;

import Group3.demo.Entity.enums.DocumentOwner;
import Group3.demo.Entity.enums.DocumentType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity @Table(name = "documents")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Document extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private DocumentOwner ownerType; // SALES_ORDER or INVOICE

    @Column(nullable = false)
    private Integer ownerId; // SalesOrder.id or Invoice.id

    @Enumerated(EnumType.STRING)
    @Column(length = 16)
    private DocumentType docType; // CONTRACT, RECEIPT, KYC, OTHER

    @Column(length = 256)
    private String fileName;

    @Column(length = 512)
    private String fileUrl; // web URL to access, e.g., /uploads/orders/...

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "uploaded_by_id")
    private User uploadedBy;

    private LocalDateTime uploadedAt;
}

