package edu.destination.entities;

import java.time.LocalDate;
import java.util.Objects;

public class Destination {

    private int idDestination;
    private String nom;
    private String pays;
    private String description;
    private boolean statut;
    private String meilleureSaison;
    private double latitude;
    private double longitude;
    private int nbVisites;

    // Nouveaux attributs
    private double prix;
    private LocalDate dateDepart;
    private LocalDate dateArrivee;

    // Constructeur vide
    public Destination() {}

    // Constructeur sans id (pour ajout)
    public Destination(String nom, String pays, String description,
                       boolean statut, String meilleureSaison,
                       double latitude, double longitude, int nbVisites,
                       double prix, LocalDate dateDepart, LocalDate dateArrivee) {
        this.nom = nom;
        this.pays = pays;
        this.description = description;
        this.statut = statut;
        this.meilleureSaison = meilleureSaison;
        this.latitude = latitude;
        this.longitude = longitude;
        this.nbVisites = nbVisites;
        this.prix = prix;
        this.dateDepart = dateDepart;
        this.dateArrivee = dateArrivee;
    }

    // Constructeur complet
    public Destination(int idDestination, String nom, String pays, String description,
                       boolean statut, String meilleureSaison,
                       double latitude, double longitude, int nbVisites,
                       double prix, LocalDate dateDepart, LocalDate dateArrivee) {
        this.idDestination = idDestination;
        this.nom = nom;
        this.pays = pays;
        this.description = description;
        this.statut = statut;
        this.meilleureSaison = meilleureSaison;
        this.latitude = latitude;
        this.longitude = longitude;
        this.nbVisites = nbVisites;
        this.prix = prix;
        this.dateDepart = dateDepart;
        this.dateArrivee = dateArrivee;
    }

    // Getters & Setters
    public int getIdDestination() {
        return idDestination;
    }

    public void setIdDestination(int idDestination) {
        this.idDestination = idDestination;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPays() {
        return pays;
    }

    public void setPays(String pays) {
        this.pays = pays;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean getStatut() {
        return statut;
    }

    public void setStatut(boolean statut) {
        this.statut = statut;
    }

    public String getMeilleureSaison() {
        return meilleureSaison;
    }

    public void setMeilleureSaison(String meilleureSaison) {
        this.meilleureSaison = meilleureSaison;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public int getNbVisites() {
        return nbVisites;
    }

    public void setNbVisites(int nbVisites) {
        this.nbVisites = nbVisites;
    }

    public double getPrix() {
        return prix;
    }

    public void setPrix(double prix) {
        this.prix = prix;
    }

    public LocalDate getDateDepart() {
        return dateDepart;
    }

    public void setDateDepart(LocalDate dateDepart) {
        this.dateDepart = dateDepart;
    }

    public LocalDate getDateArrivee() {
        return dateArrivee;
    }

    public void setDateArrivee(LocalDate dateArrivee) {
        this.dateArrivee = dateArrivee;
    }

    @Override
    public String toString() {
        return "Destination{" +
                "idDestination=" + idDestination +
                ", nom='" + nom + '\'' +
                ", pays='" + pays + '\'' +
                ", description='" + description + '\'' +
                ", statut=" + statut +
                ", meilleureSaison='" + meilleureSaison + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", nbVisites=" + nbVisites +
                ", prix=" + prix +
                ", dateDepart=" + dateDepart +
                ", dateArrivee=" + dateArrivee +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Destination)) return false;
        Destination that = (Destination) o;
        return idDestination == that.idDestination;
    }

    @Override
    public int hashCode() {
        return Objects.hash(idDestination);
    }
}