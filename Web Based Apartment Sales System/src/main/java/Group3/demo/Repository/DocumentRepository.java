package Group3.demo.Repository;

import Group3.demo.Entity.Document;
import Group3.demo.Entity.enums.DocumentOwner;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentRepository extends JpaRepository<Document, Integer> {
    List<Document> findByOwnerTypeAndOwnerId(DocumentOwner ownerType, Integer ownerId);
    List<Document> findByOwnerType(DocumentOwner ownerType);
}

