package Group3.demo.Entity;

import Group3.demo.Entity.enums.LeadStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity @Table(name = "leads")
public class Lead {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long leadId;

    @ManyToOne(optional = false, cascade = CascadeType.ALL)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @Column(length = 50)          // e.g., "Facebook", "Walk-in"
    private String source;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LeadStatus status = LeadStatus.NEW;

    @Column(length = 100)         // username or staff code
    private String assignedTo;

    @Column(length = 50)
    private String apartmentCode;

    private BigDecimal budget;    // optional

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() { createdAt = updatedAt = LocalDateTime.now(); }
    @PreUpdate
    public void preUpdate() { updatedAt = LocalDateTime.now(); }

    // getters/setters
    public Long getLeadId() { return leadId; }
    public void setLeadId(Long leadId) { this.leadId = leadId; }
    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public LeadStatus getStatus() { return status; }
    public void setStatus(LeadStatus status) { this.status = status; }
    public String getAssignedTo() { return assignedTo; }
    public void setAssignedTo(String assignedTo) { this.assignedTo = assignedTo; }
    public String getApartmentCode() { return apartmentCode; }
    public void setApartmentCode(String apartmentCode) { this.apartmentCode = apartmentCode; }
    public BigDecimal getBudget() { return budget; }
    public void setBudget(BigDecimal budget) { this.budget = budget; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
