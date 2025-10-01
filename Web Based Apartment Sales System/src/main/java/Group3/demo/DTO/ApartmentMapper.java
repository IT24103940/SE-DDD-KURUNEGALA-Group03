package Group3.demo.DTO;

import Group3.demo.Entity.Apartment;
import org.apache.catalina.mapper.Mapper;
import org.mapstruct.*;
import java.util.List;

@Mapper(componentModel = "spring")
public interface ApartmentMapper {
    Apartment toEntity(ApartmentDTO dto);
    ApartmentDTO toDto(Apartment entity);
    List<ApartmentDTO> toDtoList(List<Apartment> entities);

    // Likewise for media:
    ApartmentMediaDTO toMediaDto(com.university.apartmentsystem.entity.ApartmentMedia entity);
    com.university.apartmentsystem.entity.ApartmentMedia toMediaEntity(ApartmentMediaDTO dto);
}
