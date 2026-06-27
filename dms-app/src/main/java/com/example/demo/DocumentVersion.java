package com.example.demo;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "document_versions")
public class DocumentVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Liaison avec le document principal
    @ManyToOne
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    // Numéro de version (Ex: 1, 2, 3)
    @Column(nullable = false)
    private Integer versionNumber;

    // Chemin de stockage local du fichier sur le disque
    @Column(nullable = false)
    private String filePath;

    // Type de fichier (Ex: application/pdf, image/png)
    private String mimeType;

    // Taille du fichier en octets
    private Long fileSize;

    // Utilisateur ayant effectué l'upload de cette version
    @ManyToOne
    @JoinColumn(name = "uploaded_by_id", nullable = false)
    private User uploadedBy;

    // Date précise de l'upload de cette version
    private LocalDateTime uploadedAt;

    // Constructeur automatique
    public DocumentVersion() {
        this.uploadedAt = LocalDateTime.now();
    }

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Document getDocument() { return document; }
    public void setDocument(Document document) { this.document = document; }
    public Integer getVersionNumber() { return versionNumber; }
    public void setVersionNumber(Integer versionNumber) { this.versionNumber = versionNumber; }
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }
    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }
    public User getUploadedBy() { return uploadedBy; }
    public void setUploadedBy(User uploadedBy) { this.uploadedBy = uploadedBy; }
    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }
}
