package com.example.demo;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "folders")
public class Folder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Nom du dossier (Ex: RH, Finance)
    @Column(nullable = false)
    private String name;

    // Gestion de l'arborescence : dossier parent (sous-dossier)
    @ManyToOne
    @JoinColumn(name = "parent_id")
    private Folder parent;

    // Liste des sous-dossiers associés
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    private List<Folder> subFolders;

    // Constructeur par défaut requis par JPA
    public Folder() {}

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Folder getParent() { return parent; }
    public void setParent(Folder parent) { this.parent = parent; }
    public List<Folder> getSubFolders() { return subFolders; }
    public void setSubFolders(List<Folder> subFolders) { this.subFolders = subFolders; }
}
