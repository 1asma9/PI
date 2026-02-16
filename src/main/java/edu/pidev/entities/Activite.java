package edu.pidev.entities;

public class Activite {

    private int idActivite;
    private String nom;
    private String description;
    private String type;
    private double prix;
    private int duree;
    private String lieu;

    public Activite() {}

    // INSERT (sans id)
    public Activite(String nom, String description, String type, double prix, int duree, String lieu) {
        this.nom = nom;
        this.description = description;
        this.type = type;
        this.prix = prix;
        this.duree = duree;
        this.lieu = lieu;
    }

    // SELECT (avec id)
    public Activite(int idActivite, String nom, String description, String type, double prix, int duree, String lieu) {
        this.idActivite = idActivite;
        this.nom = nom;
        this.description = description;
        this.type = type;
        this.prix = prix;
        this.duree = duree;
        this.lieu = lieu;
    }

    public int getIdActivite() { return idActivite; }
    public void setIdActivite(int idActivite) { this.idActivite = idActivite; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public double getPrix() { return prix; }
    public void setPrix(double prix) { this.prix = prix; }

    public int getDuree() { return duree; }
    public void setDuree(int duree) { this.duree = duree; }

    public String getLieu() { return lieu; }
    public void setLieu(String lieu) { this.lieu = lieu; }

    @Override
    public String toString() {
        return "Activite{" +
                "idActivite=" + idActivite +
                ", nom='" + nom + '\'' +
                ", description='" + description + '\'' +
                ", type='" + type + '\'' +
                ", prix=" + prix +
                ", duree=" + duree +
                ", lieu='" + lieu + '\'' +
                '}';
    }


}
