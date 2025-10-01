package Group3.demo.DTO;

import Group3.demo.Entity.ApartmentStatus;

import java.util.List;

public class ApartmentDto {
    private String title;
    private String location;
    private Integer sizeSqft;
    private Double price;
    private String description;
    private ApartmentStatus status;
    private List<String> imagePaths;  // For frontend transfer

    // Constructors, Getters, Setters (similar to Entity, omitted for brevity)
    public ApartmentDto() {}

    // ... (add all getters/setters like in Apartment)
}