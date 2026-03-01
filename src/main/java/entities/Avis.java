package entities;

import java.util.Date;

public class Avis {
    private int id;
    private int userId;
    private int note;
    private String commentaire;
    private Date dateCreation;
    private String reponseAdmin;
    private Date dateReponse;

    public Avis() {
    }

    public Avis(int userId, int note, String commentaire) {
        this.userId = userId;
        this.note = note;
        this.commentaire = commentaire;
    }

    public Avis(int id, int userId, int note, String commentaire) {
        this.id = id;
        this.userId = userId;
        this.note = note;
        this.commentaire = commentaire;
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

    public int getNote() {
        return note;
    }

    public void setNote(int note) {
        this.note = note;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public void setCommentaire(String commentaire) {
        this.commentaire = commentaire;
    }

    public Date getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(Date dateCreation) {
        this.dateCreation = dateCreation;
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
        return "Avis{id=" + id + ", note=" + note + ", commentaire='" + commentaire + "', dateReponse=" + dateReponse
                + "}";
    }
}
