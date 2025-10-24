package Group3.demo.Service;

import Group3.demo.DTO.ApartmentDTO;
import Group3.demo.Entity.Apartment;

import java.util.List;

public interface ApartmentService {
    Apartment create(ApartmentDTO dto);
    Apartment update(Integer id, ApartmentDTO dto);
    void delete(Integer id);
    List<Apartment> list();
    Apartment get(Integer id);
}
