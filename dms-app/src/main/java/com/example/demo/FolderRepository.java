package com.example.demo; // Package unique pour tout le projet

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FolderRepository extends JpaRepository<Folder, Long> {
    // Permet d'accéder aux données de la table des dossiers
}
