package hebergement.entities;

public class Chambre {

    private int id;
    private Hebergement hebergement;  // ManyToOne
    private String numero;            // max 10 chars
    private String typeChambre;       // simple, double, suite, familiale
    private double prixNuit;          // DECIMAL(10,2)
    private int capacite;             // max 20
    private String equipements;       // TEXT

    // ===== Constructeurs =====

    public Chambre() {}

    public Chambre(Hebergement hebergement, String numero, String typeChambre,
                   double prixNuit, int capacite, String equipements) {
        this.hebergement = hebergement;
        this.numero = numero;
        this.typeChambre = typeChambre;
        this.prixNuit = prixNuit;
        this.capacite = capacite;
        this.equipements = equipements;
    }

    // ===== Getters & Setters =====

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Hebergement getHebergement() { return hebergement; }
    public void setHebergement(Hebergement hebergement) {
        this.hebergement = hebergement;
    }

    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }

    public String getTypeChambre() { return typeChambre; }
    public void setTypeChambre(String typeChambre) {
        this.typeChambre = typeChambre;
    }

    public double getPrixNuit() { return prixNuit; }
    public void setPrixNuit(double prixNuit) { this.prixNuit = prixNuit; }

    public int getCapacite() { return capacite; }
    public void setCapacite(int capacite) { this.capacite = capacite; }

    public String getEquipements() { return equipements; }
    public void setEquipements(String equipements) {
        this.equipements = equipements;
    }

    @Override
    public String toString() {
        return "Chambre #" + numero + " (" + typeChambre + ")";
    }
}