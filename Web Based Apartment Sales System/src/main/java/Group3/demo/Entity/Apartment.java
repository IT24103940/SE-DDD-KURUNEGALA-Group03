package Group3.demo.Entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "apartments")
public class Apartment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String location;

    @Column(name = "size_sqft", nullable = false)
    private Integer sizeSqft;

    @Column(nullable = false, precision = 10, scale = 2)
    private Double price;

    @Column(columnDefinition = "NTEXT")
    private String description;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ApartmentStatus status = ApartmentStatus.AVAILABLE;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @OneToMany(mappedBy = "apartment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ApartmentImage> images;

    // Constructors
    public Apartment() {}

    public Apartment(String title, String location, Integer sizeSqft, Double price, String description) {
        this.title = title;
        this.location = location;
        this.sizeSqft = sizeSqft;
        this.price = price;
        this.description = description;
    }

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public Integer getSizeSqft() { return sizeSqft; }
    public void setSizeSqft(Integer sizeSqft) { this.sizeSqft = sizeSqft; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public ApartmentStatus getStatus() { return status; }
    public void setStatus(ApartmentStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public List<ApartmentImage> getImages() { return images; }
    public void setImages(List<ApartmentImage> images) { this.images = images; }
}

public enum ApartmentStatus {
    AVAILABLE, SOLD, ARCHIVED
}