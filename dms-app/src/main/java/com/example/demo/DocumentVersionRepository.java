package com.example.demo; // Package unique pour tout le projet

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;

@Repository
public interface DocumentVersionRepository extends JpaRepository<DocumentVersion, Long> {
    // Compte le nombre de versions de documents ajoutées aujourd'hui
    long countByUploadedAtAfter(LocalDateTime date);
}
