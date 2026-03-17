package hebergement.entities;

public class Hebergement {
    private int id;
    private String description;
    private String adresse;
    private double prix;
    private String imagePath;

    private Integer typeId;
    private String typeLibelle;

    // ✅ NEW
    private Double latitude;
    private Double longitude;
    private Integer idUser;

    public Integer getIdUser() { return idUser; }
    public void setIdUser(Integer idUser) { this.idUser = idUser; }

    public Hebergement() {}

    public Hebergement(String description, String adresse, double prix) {
        this.description = description;
        this.adresse = adresse;
        this.prix = prix;
    }

    public Hebergement(String description, String adresse, double prix, Integer typeId) {
        this.description = description;
        this.adresse = adresse;
        this.prix = prix;
        this.typeId = typeId;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }

    public double getPrix() { return prix; }
    public void setPrix(double prix) { this.prix = prix; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    public Integer getTypeId() { return typeId; }
    public void setTypeId(Integer typeId) { this.typeId = typeId; }

    public String getTypeLibelle() { return typeLibelle; }
    public void setTypeLibelle(String typeLibelle) { this.typeLibelle = typeLibelle; }

    // ✅ lat/lng
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    @Override
    public String toString() {
        return "#" + id + " - " + description;
    }
}