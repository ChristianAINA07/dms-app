package com.example.demo; // Package unique pour tout le projet

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    // Permet d'accéder aux données de la table des documents
}
