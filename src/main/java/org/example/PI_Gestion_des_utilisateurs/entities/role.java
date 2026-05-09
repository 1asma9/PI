package org.example.PI_Gestion_des_utilisateurs.entities;

public class role {
    private int id;
    private String nom;
    private String description;

    public role() {
    }

    public role(int id, String nom, String description) {
        this(nom, description);
        this.id = id;
    }

    public role(String nom, String description) {
        this.nom = nom;
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "role{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
