package Group3.demo.Entity;

import Group3.demo.Entity.enums.Channel;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity @Table(name = "interactions")
public class Interaction {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long interactionId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "lead_id")
    private Lead lead;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Channel channel;      // CALL, EMAIL, VISIT, MESSAGE

    @Column(nullable = false, length = 1000)
    private String notes;

    private LocalDateTime interactionDate;

    @PrePersist
    public void prePersist() {
        if (interactionDate == null) interactionDate = LocalDateTime.now();
    }

    // getters/setters
    public Long getInteractionId() { return interactionId; }
    public void setInteractionId(Long interactionId) { this.interactionId = interactionId; }
    public Lead getLead() { return lead; }
    public void setLead(Lead lead) { this.lead = lead; }
    public Channel getChannel() { return channel; }
    public void setChannel(Channel channel) { this.channel = channel; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public LocalDateTime getInteractionDate() { return interactionDate; }
    public void setInteractionDate(LocalDateTime interactionDate) { this.interactionDate = interactionDate; }
}
