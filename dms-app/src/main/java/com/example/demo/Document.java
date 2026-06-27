package com.example.demo;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "documents")
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Titre global du document (Ex: Contrat Orange)
    @Column(nullable = false)
    private String title;

    // Liaison avec le dossier de classification
    @ManyToOne
    @JoinColumn(name = "folder_id", nullable = false)
    private Folder folder;

    // Date de création globale du document dans le système
    private LocalDateTime createdAt;

    // Constructeur : initialisation automatique de la date
    public Document() {
        this.createdAt = LocalDateTime.now();
    }

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public Folder getFolder() { return folder; }
    public void setFolder(Folder folder) { this.folder = folder; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
