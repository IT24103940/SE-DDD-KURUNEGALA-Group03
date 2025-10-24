package Group3.demo.DTO;

import Group3.demo.Entity.enums.LeadStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LeadDTO {
    @NotNull private Integer apartmentId;
    @NotNull private Integer customerId;
    private Integer assignedToId;
    private String source;
    @NotNull private LeadStatus status;
    private String notes;
    private Integer id;
}
