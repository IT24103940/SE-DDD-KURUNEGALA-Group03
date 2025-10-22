package Group3.demo.Repository;

import Group3.demo.Entity.Apartment;
import Group3.demo.Entity.enums.ApartmentStatus;
import Group3.demo.Entity.enums.PromotionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ApartmentRepository extends JpaRepository<Apartment, Integer> {
    Optional<Apartment> findByCode(String code);
    List<Apartment> findByStatus(ApartmentStatus status);

    // Dynamic search with optional filters
    @Query("""
            select a from Apartment a
            where a.status = :status
              and (:q is null or lower(a.title) like lower(concat('%', :q, '%'))
                               or lower(a.description) like lower(concat('%', :q, '%')))
              and (:city is null or lower(a.city) like lower(concat('%', :city, '%')))
              and (:minPrice is null or a.price >= :minPrice)
              and (:maxPrice is null or a.price <= :maxPrice)
              and (:minBedrooms is null or a.bedrooms >= :minBedrooms)
              and (:maxBedrooms is null or a.bedrooms <= :maxBedrooms)
              and (:minBathrooms is null or a.bathrooms >= :minBathrooms)
              and (:maxBathrooms is null or a.bathrooms <= :maxBathrooms)
              and (:minArea is null or a.areaSqFt >= :minArea)
              and (:maxArea is null or a.areaSqFt <= :maxArea)
              and (
                    :hasPromotion is null
                 or (:hasPromotion = true and exists (select 1 from Promotion p where p.apartment = a and p.status = :promoStatus))
                 or (:hasPromotion = false and not exists (select 1 from Promotion p where p.apartment = a and p.status = :promoStatus))
              )
            order by a.createdAt desc
            """)
    List<Apartment> searchAvailable(
            @Param("status") ApartmentStatus status,
            @Param("q") String q,
            @Param("city") String city,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("minBedrooms") Integer minBedrooms,
            @Param("maxBedrooms") Integer maxBedrooms,
            @Param("minBathrooms") Integer minBathrooms,
            @Param("maxBathrooms") Integer maxBathrooms,
            @Param("minArea") Integer minArea,
            @Param("maxArea") Integer maxArea,
            @Param("hasPromotion") Boolean hasPromotion,
            @Param("promoStatus") PromotionStatus promoStatus
    );

    @Query("select distinct a.city from Apartment a where a.status = :status and a.city is not null order by a.city asc")
    List<String> findDistinctCities(@Param("status") ApartmentStatus status);
}
