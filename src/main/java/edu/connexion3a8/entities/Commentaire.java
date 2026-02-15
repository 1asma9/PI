package edu.connexion3a8.entities;

public class Commentaire {

    private int id;
    private String contenu;
    private String date;
    private String nomuser;
    private String img;

    private int likesCount;   // nombre total de likes
    private boolean liked;    // true si liké

    // 🔹 Constructeur vide
    public Commentaire() {
    }

    // 🔹 Constructeur complet
    public Commentaire(int id, String contenu, String date, String nomuser,
                       String img, int likesCount, boolean liked) {
        this.id = id;
        this.contenu = contenu;
        this.date = date;
        this.nomuser = nomuser;
        this.img = img;
        this.likesCount = likesCount;
        this.liked = liked;
    }

    // 🔹 Getters
    public int getId() {
        return id;
    }

    public String getContenu() {
        return contenu;
    }

    public String getDate() {
        return date;
    }

    public String getNomuser() {
        return nomuser;
    }

    public String getImg() {
        return img;
    }

    public int getLikesCount() {
        return likesCount;
    }

    public boolean isLiked() {
        return liked;
    }

    // 🔹 Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setContenu(String contenu) {
        this.contenu = contenu;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setNomuser(String nomuser) {
        this.nomuser = nomuser;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public void setLikesCount(int likesCount) {
        this.likesCount = likesCount;
    }

    public void setLiked(boolean liked) {
        this.liked = liked;
    }

    // 🔹 Méthode toString (optionnelle mais utile)
    @Override
    public String toString() {
        return "Commentaire{" +
                "id=" + id +
                ", contenu='" + contenu + '\'' +
                ", date='" + date + '\'' +
                ", nomuser='" + nomuser + '\'' +
                ", img='" + img + '\'' +
                ", likesCount=" + likesCount +
                ", liked=" + liked +
                '}';
    }
}