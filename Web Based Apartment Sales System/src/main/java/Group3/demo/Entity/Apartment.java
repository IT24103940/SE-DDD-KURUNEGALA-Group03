package Group3.demo.Entity;

import Group3.demo.Entity.enums.ApartmentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity @Table(name = "apartments")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Apartment extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique = true, nullable = false, length = 32)
    private String code;

    @Column(nullable = false, length = 128)
    private String title;

    @Column(length = 2048)
    private String description;

    @Column(length = 128)
    private String city;

    @Column(precision = 18, scale = 2, nullable = false)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private ApartmentStatus status;

    private Integer bedrooms;
    private Integer bathrooms;
    private Integer areaSqFt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "listed_by_id")
    private User listedBy;

    // Apartment.java
    @Column(length = 512)
    private String imageUrl;
}
