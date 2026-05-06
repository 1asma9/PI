package edu.destination.entities;

import java.time.LocalDate;

public class Voyage {
    private int id;
    private LocalDate dateDepart;
    private LocalDate dateArrivee;
    private String pointDepart;
    private String pointArrivee;
    private double prix;
    private int destinationId;
    private int paid;

    public Voyage() {}

    public Voyage(LocalDate dateDepart, LocalDate dateArrivee, String pointDepart, String pointArrivee, double prix, int destinationId, int paid) {
        this.dateDepart = dateDepart;
        this.dateArrivee = dateArrivee;
        this.pointDepart = pointDepart;
        this.pointArrivee = pointArrivee;
        this.prix = prix;
        this.destinationId = destinationId;
        this.paid = paid;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public LocalDate getDateDepart() { return dateDepart; }
    public void setDateDepart(LocalDate dateDepart) { this.dateDepart = dateDepart; }
    public LocalDate getDateArrivee() { return dateArrivee; }
    public void setDateArrivee(LocalDate dateArrivee) { this.dateArrivee = dateArrivee; }
    public String getPointDepart() { return pointDepart; }
    public void setPointDepart(String pointDepart) { this.pointDepart = pointDepart; }
    public String getPointArrivee() { return pointArrivee; }
    public void setPointArrivee(String pointArrivee) { this.pointArrivee = pointArrivee; }
    public double getPrix() { return prix; }
    public void setPrix(double prix) { this.prix = prix; }
    public int getDestinationId() { return destinationId; }
    public void setDestinationId(int destinationId) { this.destinationId = destinationId; }
    public int getPaid() { return paid; }
    public void setPaid(int paid) { this.paid = paid; }
}
