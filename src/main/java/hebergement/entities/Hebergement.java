package hebergement.entities;

public class Hebergement {
    private int id;
    private String description;
    private String adresse;
    private double prix;
    private String imagePath;


    // ✅ NEW
    private Integer typeId;      // nullable
    private String typeLibelle;  // optionnel (pour affichage après JOIN)

    public Hebergement() {}

    public Hebergement(String description, String adresse, double prix) {
        this.description = description;
        this.adresse = adresse;
        this.prix = prix;

    }

    // ✅ NEW constructor avec typeId
    public Hebergement(String description, String adresse, double prix, Integer typeId) {
        this.description = description;
        this.adresse = adresse;
        this.prix = prix;
        this.typeId = typeId;
    }

    // getters/setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }

    public double getPrix() { return prix; }
    public void setPrix(double prix) { this.prix = prix; }

    public Integer getTypeId() { return typeId; }
    public void setTypeId(Integer typeId) { this.typeId = typeId; }

    public String getTypeLibelle() { return typeLibelle; }
    public void setTypeLibelle(String typeLibelle) { this.typeLibelle = typeLibelle; }
    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }


    @Override
    public String toString() {
        return "Hebergement{id=" + id +
                ", description='" + description + '\'' +
                ", adresse='" + adresse + '\'' +
                ", prix=" + prix +
                ", typeId=" + typeId +
                (typeLibelle != null ? ", type='" + typeLibelle + '\'' : "") +
                '}';
    }
}
