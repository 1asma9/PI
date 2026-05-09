package hebergement.entities;

import java.time.LocalDate;

public class Reservation {

    private int id;
    private int hebergementId;

    private String clientNom;
    private String clientTel;
    private String clientEmail;

    private LocalDate dateDebut;
    private LocalDate dateFin;

    private int nbNuits;
    private double total;

    private String statut; // EN_ATTENTE / CONFIRME / ANNULE

    public Reservation() {}

    public Reservation(int hebergementId, String clientNom, String clientTel, String clientEmail,
                       LocalDate dateDebut, LocalDate dateFin, int nbNuits, double total, String statut) {
        this.hebergementId = hebergementId;
        this.clientNom = clientNom;
        this.clientTel = clientTel;
        this.clientEmail = clientEmail;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.nbNuits = nbNuits;
        this.total = total;
        this.statut = statut;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getHebergementId() { return hebergementId; }
    public void setHebergementId(int hebergementId) { this.hebergementId = hebergementId; }

    public String getClientNom() { return clientNom; }
    public void setClientNom(String clientNom) { this.clientNom = clientNom; }

    public String getClientTel() { return clientTel; }
    public void setClientTel(String clientTel) { this.clientTel = clientTel; }

    public String getClientEmail() { return clientEmail; }
    public void setClientEmail(String clientEmail) { this.clientEmail = clientEmail; }

    public LocalDate getDateDebut() { return dateDebut; }
    public void setDateDebut(LocalDate dateDebut) { this.dateDebut = dateDebut; }

    public LocalDate getDateFin() { return dateFin; }
    public void setDateFin(LocalDate dateFin) { this.dateFin = dateFin; }

    public int getNbNuits() { return nbNuits; }
    public void setNbNuits(int nbNuits) { this.nbNuits = nbNuits; }

    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    @Override
    public String toString() {
        return "Reservation{" +
                "id=" + id +
                ", hebergementId=" + hebergementId +
                ", clientNom='" + clientNom + '\'' +
                ", dateDebut=" + dateDebut +
                ", dateFin=" + dateFin +
                ", nbNuits=" + nbNuits +
                ", total=" + total +
                ", statut='" + statut + '\'' +
                '}';
    }
}
