package Group3.demo.DTO;

import java.time.LocalDate;

public class AdvertisementDTO {
    private Long id;
    private String title;
    private String description;
    private String bannerImage;
    private LocalDate startDate;
    private LocalDate endDate;
    private String createdBy;
    private int clickCount;
    private int viewCount;

    // Getters and Setters (similar to entity, omitted for brevity)
    // You can copy from entity and remove timestamps if not needed in DTO
}