package hebergement.entities;

import java.time.LocalDate;

public class Disponibilite {

    private int id;
    private int hebergementId;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private boolean disponible;

    public Disponibilite() {
    }

    public Disponibilite(int hebergementId, LocalDate dateDebut, LocalDate dateFin, boolean disponible) {
        this.hebergementId = hebergementId;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.disponible = disponible;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getHebergementId() {
        return hebergementId;
    }

    public void setHebergementId(int hebergementId) {
        this.hebergementId = hebergementId;
    }

    public LocalDate getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(LocalDate dateDebut) {
        this.dateDebut = dateDebut;
    }

    public LocalDate getDateFin() {
        return dateFin;
    }

    public void setDateFin(LocalDate dateFin) {
        this.dateFin = dateFin;
    }

    public boolean isDisponible() {
        return disponible;
    }

    public void setDisponible(boolean disponible) {
        this.disponible = disponible;
    }

    @Override
    public String toString() {
        return "Disponibilite{" +
                "id=" + id +
                ", hebergementId=" + hebergementId +
                ", dateDebut=" + dateDebut +
                ", dateFin=" + dateFin +
                ", disponible=" + disponible +
                '}';
    }
}
