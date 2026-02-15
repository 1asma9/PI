package edu.connexion3a8.entities;

import java.time.LocalDateTime;
import java.util.Objects;

public class Blog {
    private int id;
    private String titre;
    private String contenu;
    private String image_couverture;
    private String author_id;
    private boolean status;
    private LocalDateTime date_creation;
    private LocalDateTime date_publication;
    private String extrait;
    private String slug;

    public Blog() {
    }

    public Blog(int id , String titre ,String contenu, String image_couverture ,String author_id , boolean status , LocalDateTime date_creation ,
                LocalDateTime date_publication, String extrait, String slug) {
        this.id = id;
        this.titre = titre;
        this.contenu = contenu;
        this.image_couverture = image_couverture;
        this.author_id = author_id;
        this.status = status;
        this.date_creation = date_creation;
        this.date_publication = date_publication;
        this.extrait = extrait;
        this.slug = slug;
    }

    // -------Getters et setters ------


    public int getId() {
        return id;
    }

    public String getTitre() {
        return titre;
    }

    public String getContenu() {
        return contenu;
    }

    public String getImage_couverture() {
        return image_couverture;
    }

    public String getAuthor_id() {
        return author_id;
    }

    public boolean isStatus() {
        return status;
    }

    public LocalDateTime getDate_creation() {
        return date_creation;
    }

    public LocalDateTime getDate_publication() {
        return date_publication;
    }

    public String getExtrait() {
        return extrait;
    }

    public String getSlug() {
        return slug;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public void setContenu(String contenu) {
        this.contenu = contenu;
    }

    public void setImage_couverture(String image_couverture) {
        this.image_couverture = image_couverture;
    }

    public void setAuthor_id(String author_id) {
        this.author_id = author_id;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public void setDate_creation(LocalDateTime date_creation) {
        this.date_creation = date_creation;
    }

    public void setDate_publication(LocalDateTime date_publication) {
        this.date_publication = date_publication;
    }

    public void setExtrait(String extrait) {
        this.extrait = extrait;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    //--------Equals + HashCode + toString -------

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Blog blog = (Blog) o;
        return getId() == blog.getId() && isStatus() == blog.isStatus() && Objects.equals(getTitre(), blog.getTitre()) && Objects.equals(getContenu(), blog.getContenu()) && Objects.equals(getImage_couverture(), blog.getImage_couverture()) && Objects.equals(getAuthor_id(), blog.getAuthor_id()) && Objects.equals(getDate_creation(), blog.getDate_creation()) && Objects.equals(getDate_publication(), blog.getDate_publication()) && Objects.equals(getExtrait(), blog.getExtrait()) && Objects.equals(getSlug(), blog.getSlug());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getTitre(), getContenu(), getImage_couverture(), getAuthor_id(), isStatus(), getDate_creation(), getDate_publication(), getExtrait(), getSlug());
    }

    @Override
    public String toString() {
        return "Blog{" +
                "id=" + id +
                ", titre='" + titre + '\'' +
                ", contenu='" + contenu + '\'' +
                ", image_couverture='" + image_couverture + '\'' +
                ", author_id='" + author_id + '\'' +
                ", status=" + status +
                ", date_creation=" + date_creation +
                ", date_publication=" + date_publication +
                ", extrait='" + extrait + '\'' +
                ", slug='" + slug + '\'' +
                '}';
    }
}

