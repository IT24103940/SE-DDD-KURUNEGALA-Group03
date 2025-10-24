// PaymentRepository.java
package Group3.demo.Repository;

import Group3.demo.Entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Integer> {
    List<Payment> findByInvoiceId(Integer invoiceId);
}
