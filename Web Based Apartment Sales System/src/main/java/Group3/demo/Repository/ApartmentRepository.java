package Group3.demo.Repository;

import Group3.demo.Entity.Apartment;
import Group3.demo.Entity.ApartmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ApartmentRepository extends JpaRepository<Apartment, Integer> {
    List<Apartment> findByLocationContainingIgnoreCase(String location);
    List<Apartment> findByStatus(ApartmentStatus status);
    List<Apartment> findByPriceBetween(Double minPrice, Double maxPrice);

    @Query("SELECT a FROM Apartment a WHERE " +
            "(:location IS NULL OR a.location LIKE %:location%) AND " +
            "(:status IS NULL OR a.status = :status) AND " +
            "(:minPrice IS NULL OR a.price >= :minPrice) AND " +
            "(:maxPrice IS NULL OR a.price <= :maxPrice)")
    List<Apartment> searchApartments(@Param("location") String location,
                                     @Param("status") ApartmentStatus status,
                                     @Param("minPrice") Double minPrice,
                                     @Param("maxPrice") Double maxPrice);


