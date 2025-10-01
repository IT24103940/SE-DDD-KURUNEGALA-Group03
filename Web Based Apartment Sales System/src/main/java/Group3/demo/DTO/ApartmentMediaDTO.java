package Group3.demo.DTO;

import lombok.*;
import Group3.demo.Entity.ApartmentStatus;

import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ApartmentDto {
    private String title;
    private String location;
    private Integer sizeSqft;
    private Double price;
    private String description;
    private ApartmentStatus status;
    private List<ApartmentMediaDTO> media;  // Replace imagePaths with this for detailed media
}