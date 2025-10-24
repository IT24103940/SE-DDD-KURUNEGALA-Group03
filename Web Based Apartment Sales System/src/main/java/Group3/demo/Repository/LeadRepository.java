package Group3.demo.Repository;

import Group3.demo.Entity.Lead;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LeadRepository extends JpaRepository<Lead, Integer> {
    List<Lead> findByAssignedTo_Id(Integer userId);

    boolean existsByApartment_Id(Integer apartmentId);
}
