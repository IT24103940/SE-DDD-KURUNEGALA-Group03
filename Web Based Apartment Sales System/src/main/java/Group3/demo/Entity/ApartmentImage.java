package group3.demo;

import Group3.demo.Entity.Apartment;
import jakarta.persistence.*;

@Entity
@Table(name = "apartment_images")
public class ApartmentImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "apartment_id", nullable = false)
    private Integer apartmentId;

    @Column(nullable = false)
    private String imagePath;

    @ManyToOne
    @JoinColumn(name = "apartment_id", insertable = false, updatable = false)
    private Apartment apartment;

    public ApartmentImage() {

    }

    // Constructors
    public void ApartmentMediaDTO() {}

    public ApartmentImage(Integer apartmentId, String imagePath) {
        this.apartmentId = apartmentId;
        this.imagePath = imagePath;
    }

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getApartmentId() { return apartmentId; }
    public void setApartmentId(Integer apartmentId) { this.apartmentId = apartmentId; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    public Apartment getApartment() { return apartment; }
    public void setApartment(Apartment apartment) { this.apartment = apartment; }
}
