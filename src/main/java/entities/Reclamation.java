package entities;

import java.util.Date;

public class Reclamation {
    private int id;
    private int userId;
    private String titre;
    private String description;
    private Date dateCreation;
    private String statut;
    private String reponseAdmin;
    private Date dateReponse;

    public Reclamation() {
    }

    public Reclamation(int userId, String titre, String description) {
        this.userId = userId;
        this.titre = titre;
        this.description = description;
    }

    public Reclamation(int id, int userId, String titre, String description) {
        this.id = id;
        this.userId = userId;
        this.titre = titre;
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(Date dateCreation) {
        this.dateCreation = dateCreation;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public String getReponseAdmin() {
        return reponseAdmin;
    }

    public void setReponseAdmin(String reponseAdmin) {
        this.reponseAdmin = reponseAdmin;
    }

    public Date getDateReponse() {
        return dateReponse;
    }

    public void setDateReponse(Date dateReponse) {
        this.dateReponse = dateReponse;
    }

    @Override
    public String toString() {
        return "Reclamation{id=" + id + ", titre='" + titre + "', statut='" + statut + "', dateReponse=" + dateReponse
                + "}";
    }
}
