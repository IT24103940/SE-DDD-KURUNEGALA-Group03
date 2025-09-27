package Group3.demo.Entity;

import Group3.demo.Entity.enums.PaymentStatus;
import Group3.demo.Entity.enums.SaleStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity @Table(name = "sales")
public class Sale {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long saleId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "lead_id")
    private Lead lead;                    // sale comes from a qualified/converted lead

    @Column(length = 50)
    private String apartmentCode;

    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SaleStatus status = SaleStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus paymentStatus = PaymentStatus.UNPAID;

    private LocalDate contractDate;

    // getters/setters
    public Long getSaleId() { return saleId; }
    public void setSaleId(Long saleId) { this.saleId = saleId; }
    public Lead getLead() { return lead; }
    public void setLead(Lead lead) { this.lead = lead; }
    public String getApartmentCode() { return apartmentCode; }
    public void setApartmentCode(String apartmentCode) { this.apartmentCode = apartmentCode; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public SaleStatus getStatus() { return status; }
    public void setStatus(SaleStatus status) { this.status = status; }
    public PaymentStatus getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(PaymentStatus paymentStatus) { this.paymentStatus = paymentStatus; }
    public LocalDate getContractDate() { return contractDate; }
    public void setContractDate(LocalDate contractDate) { this.contractDate = contractDate; }
}
