package com.example.demo;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "activity_logs")
public class ActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Utilisateur qui a fait l'action (Ex: Rabe, Aina)
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Type d'action (Ex: UPLOAD, MODIFICATION, TELECHARGEMENT)
    @Column(nullable = false)
    private String action;

    // Détails de l'action pour l'historique
    private String details;

    // Date et heure de l'action
    private LocalDateTime timestamp;

    // Constructeur automatique
    public ActivityLog() {
        this.timestamp = LocalDateTime.now();
    }

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
