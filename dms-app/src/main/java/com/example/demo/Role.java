package com.example.demo;

import jakarta.persistence.*;

@Entity
@Table(name = "roles")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Nom du rôle (Ex: ADMIN, MANAGER, EMPLOYE)
    @Column(nullable = false, unique = true)
    private String name;

    // Constructeur vide requis par JPA
    public Role() {}

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
