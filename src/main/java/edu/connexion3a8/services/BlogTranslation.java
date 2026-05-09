package edu.connexion3a8.services;

public class BlogTranslation {
    private String titre;
    private String extrait;
    private String contenu;
    private String language;

    public BlogTranslation() {
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getExtrait() {
        return extrait;
    }

    public void setExtrait(String extrait) {
        this.extrait = extrait;
    }

    public String getContenu() {
        return contenu;
    }

    public void setContenu(String contenu) {
        this.contenu = contenu;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }
}