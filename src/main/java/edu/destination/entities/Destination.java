package edu.destination.entities;

import java.util.Objects;

public class Destination {

    private int id;
    private String nom;
    private String pays;
    private String description;
    private boolean statut;
    private String meilleureSaison;
    private double latitude;
    private double longitude;
    private int nbVisites;
    private String videoPath;
    private int nbLikes;

    public Destination() {}

    public Destination(String nom, String pays, String description,
                       boolean statut, String meilleureSaison,
                       double latitude, double longitude, int nbVisites,
                       String videoPath, int nbLikes) {
        this.nom = nom;
        this.pays = pays;
        this.description = description;
        this.statut = statut;
        this.meilleureSaison = meilleureSaison;
        this.latitude = latitude;
        this.longitude = longitude;
        this.nbVisites = nbVisites;
        this.videoPath = videoPath;
        this.nbLikes = nbLikes;
    }

    public Destination(int id, String nom, String pays, String description,
                       boolean statut, String meilleureSaison,
                       double latitude, double longitude, int nbVisites,
                       String videoPath, int nbLikes) {
        this.id = id;
        this.nom = nom;
        this.pays = pays;
        this.description = description;
        this.statut = statut;
        this.meilleureSaison = meilleureSaison;
        this.latitude = latitude;
        this.longitude = longitude;
        this.nbVisites = nbVisites;
        this.videoPath = videoPath;
        this.nbLikes = nbLikes;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getPays() { return pays; }
    public void setPays(String pays) { this.pays = pays; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public boolean getStatut() { return statut; }
    public void setStatut(boolean statut) { this.statut = statut; }
    public String getMeilleureSaison() { return meilleureSaison; }
    public void setMeilleureSaison(String meilleureSaison) { this.meilleureSaison = meilleureSaison; }
    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    public int getNbVisites() { return nbVisites; }
    public void setNbVisites(int nbVisites) { this.nbVisites = nbVisites; }
    public String getVideoPath() { return videoPath; }
    public void setVideoPath(String videoPath) { this.videoPath = videoPath; }
    public int getNbLikes() { return nbLikes; }
    public void setNbLikes(int nbLikes) { this.nbLikes = nbLikes; }

    @Override
    public String toString() {
        return "Destination{id=" + id + ", nom='" + nom + "', pays='" + pays + "'}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Destination)) return false;
        Destination that = (Destination) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}