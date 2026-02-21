package hebergement.entities;

public class TypeHebergement {

    private int id;
    private String libelle;

    public TypeHebergement() {
    }

    public TypeHebergement(String libelle) {
        this.libelle = libelle;
    }

    public TypeHebergement(int id, String libelle) {
        this.id = id;
        this.libelle = libelle;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    @Override
    public String toString() {
        return "TypeHebergement{" +
                "id=" + id +
                ", libelle='" + libelle + '\'' +
                '}';
    }
}
