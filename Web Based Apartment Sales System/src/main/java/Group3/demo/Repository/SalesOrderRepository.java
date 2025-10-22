package Group3.demo.Repository;

import Group3.demo.Entity.SalesOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SalesOrderRepository extends JpaRepository<SalesOrder, Integer> {
    List<SalesOrder> findByCustomerUsername(String username);

    // Guard: check if any orders reference a promotion
    long countByPromotionId(Integer promotionId);
}
