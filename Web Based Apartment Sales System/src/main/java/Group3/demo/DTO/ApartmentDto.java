package Group3.demo.DTO;

import Group3.demo.Entity.enums.ApartmentStatus;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ApartmentDTO {
    @NotBlank private String code;
    @NotBlank private String title;
    private String description;
    private String city;
    @NotNull @DecimalMin("0.0") private BigDecimal price;
    @NotNull private ApartmentStatus status;
    private Integer bedrooms;
    private Integer bathrooms;
    private Integer areaSqFt;
}
