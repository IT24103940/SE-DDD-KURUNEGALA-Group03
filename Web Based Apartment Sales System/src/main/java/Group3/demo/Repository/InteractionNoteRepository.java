package Group3.demo.Repository;

import Group3.demo.Entity.InteractionNote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InteractionNoteRepository extends JpaRepository<InteractionNote, Integer> {
    List<InteractionNote> findByLead_Id(Integer leadId);
}
