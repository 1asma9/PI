package edu.pidev.entities;

import java.time.LocalDate;

public class ReservationActivite {

    private int idReservation;
    private LocalDate dateReservation;
    private int nombrePersonnes;
    private String statut;
    private int idActivite;

    // Constructor for INSERT (no idReservation)
    public ReservationActivite(LocalDate dateReservation, int nombrePersonnes, String statut, int idActivite) {
        this.dateReservation = dateReservation;
        this.nombrePersonnes = nombrePersonnes;
        this.statut = statut;
        this.idActivite = idActivite;
    }

    // Constructor for SELECT / UPDATE (with idReservation)
    public ReservationActivite(int idReservation, LocalDate dateReservation, int nombrePersonnes, String statut, int idActivite) {
        this.idReservation = idReservation;
        this.dateReservation = dateReservation;
        this.nombrePersonnes = nombrePersonnes;
        this.statut = statut;
        this.idActivite = idActivite;
    }

    public ReservationActivite() {}

    public int getIdReservation() { return idReservation; }
    public void setIdReservation(int idReservation) { this.idReservation = idReservation; }

    public LocalDate getDateReservation() { return dateReservation; }
    public void setDateReservation(LocalDate dateReservation) { this.dateReservation = dateReservation; }

    public int getNombrePersonnes() { return nombrePersonnes; }
    public void setNombrePersonnes(int nombrePersonnes) { this.nombrePersonnes = nombrePersonnes; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public int getIdActivite() { return idActivite; }
    public void setIdActivite(int idActivite) { this.idActivite = idActivite; }

    @Override
    public String toString() {
        return "ReservationActivite{" +
                "idReservation=" + idReservation +
                ", dateReservation=" + dateReservation +
                ", nombrePersonnes=" + nombrePersonnes +
                ", statut='" + statut + '\'' +
                ", idActivite=" + idActivite +
                '}';
    }
}
