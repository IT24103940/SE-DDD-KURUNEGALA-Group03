package Group3.demo.Service;

import Group3.demo.DTO.LeadDTO;
import Group3.demo.Entity.InteractionNote;
import Group3.demo.Entity.Lead;

import java.util.List;

public interface SalesService {
    Lead createLead(LeadDTO dto);
    List<Lead> listLeads();

    LeadDTO getLeadById(Integer id);
    void updateLead(Integer id, LeadDTO leadDTO);
    void deleteLead(Integer id);
}
