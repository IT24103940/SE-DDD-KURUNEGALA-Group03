package Group3.demo.Service.Impl;

import Group3.demo.DTO.LeadDTO;
import Group3.demo.Entity.*;
import Group3.demo.Repository.*;
import Group3.demo.Service.SalesService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SalesServiceImpl implements SalesService {

    private final LeadRepository leadRepo;
    private final UserRepository userRepo;
    private final ApartmentRepository apartmentRepo;

    @Override
    public Lead createLead(LeadDTO dto) {
        var customer = userRepo.findById(dto.getCustomerId()).orElseThrow();
        var apt = apartmentRepo.findById(dto.getApartmentId()).orElseThrow();
        User assigned = null;
        if (dto.getAssignedToId() != null) {
            assigned = userRepo.findById(dto.getAssignedToId()).orElseThrow();
        }
        var lead = Lead.builder()
                .customer(customer)
                .apartment(apt)
                .assignedTo(assigned)
                .status(dto.getStatus())
                .source(dto.getSource())
                .notes(dto.getNotes())
                .build();
        return leadRepo.save(lead);
    }

    @Override
    public LeadDTO getLeadById(Integer id) {
        return leadRepo.findById(id)
                .map(this::convertToDTO)
                .orElseThrow(() -> new RuntimeException("Lead not found"));
    }

    @Override
    public void updateLead(Integer id, LeadDTO dto) {
        Lead lead = leadRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Lead not found"));

        var customer = userRepo.findById(dto.getCustomerId()).orElseThrow();
        var apt = apartmentRepo.findById(dto.getApartmentId()).orElseThrow();
        User assigned = dto.getAssignedToId() != null ? userRepo.findById(dto.getAssignedToId()).orElse(null) : null;

        lead.setStatus(dto.getStatus());
        lead.setSource(dto.getSource());
        lead.setNotes(dto.getNotes());
        lead.setAssignedTo(assigned);
        lead.setCustomer(customer);
        lead.setApartment(apt);

        leadRepo.save(lead);  // ✅ modifies the same entity
    }


    @Override
    public void deleteLead(Integer id) {
        Lead lead = leadRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Lead not found"));

        if (!lead.getSalesOrders().isEmpty()) {
            throw new RuntimeException("Cannot delete lead with existing sales orders");
        }

        leadRepo.deleteById(id);
    }

    @Override
    public List<Lead> listLeads() {
        return leadRepo.findAll();
    }

    // Convert Entity → DTO
    private LeadDTO convertToDTO(Lead lead) {
        var dto = new LeadDTO();
        dto.setId(lead.getId());
        dto.setCustomerId(lead.getCustomer().getId());
        dto.setApartmentId(lead.getApartment().getId());
        dto.setAssignedToId(lead.getAssignedTo() != null ? lead.getAssignedTo().getId() : null);
        dto.setStatus(lead.getStatus());
        dto.setSource(lead.getSource());
        dto.setNotes(lead.getNotes());
        return dto;
    }
}
