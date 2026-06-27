package com.example.demo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {
    // Récupère l'intégralité des lignes du registre d'audit triées par date décroissante
    List<ActivityLog> findByOrderByTimestampDesc();
}
