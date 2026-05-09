package controllers;

import entities.Avis;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import services.AvisService;
import tools.AlertHelper;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class ModifierAvisController implements Initializable {

    @FXML
    private ComboBox<String> comboCategorie;
    @FXML
    private ComboBox<Integer> comboNote;
    @FXML
    private TextArea txtCommentaire;
    @FXML
    private Button btnSave;
    @FXML
    private Button btnCancel;

    private Avis avis;
    private AvisService avisService = new AvisService();

    // Variables pour stocker les valeurs initiales
    private Integer noteInitiale;
    private String commentaireInitial;
    private String categorieInitiale;
    private java.util.Map<Integer, String> categoriesMap = new java.util.HashMap<>();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Peupler le ComboBox avec les notes 1-5
        comboNote.getItems().addAll(1, 2, 3, 4, 5);
        try {
            categoriesMap = avisService.getAllTypes();
            comboCategorie.setItems(FXCollections.observableArrayList(categoriesMap.values()));
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void setAvis(Avis avis) {
        this.avis = avis;

        // Sauvegarder les valeurs initiales
        noteInitiale = avis.getNote();
        commentaireInitial = avis.getCommentaire() == null ? "" : avis.getCommentaire();
        categorieInitiale = categoriesMap.get(avis.getTypeId());

        // Remplir les champs
        comboNote.setValue(noteInitiale);
        txtCommentaire.setText(commentaireInitial);
        comboCategorie.setValue(categorieInitiale);
    }

    @FXML
    void enregistrer(ActionEvent event) {
        // Récupérer les nouvelles valeurs
        Integer nouvelleNote = comboNote.getValue();
        String nouveauCommentaire = txtCommentaire.getText().trim();
        String nouvelleCategorie = comboCategorie.getValue();

        // 1. VALIDATION : Champs vides
        if (nouvelleNote == null) {
            AlertHelper.showError("Erreur", "Veuillez sélectionner une note !");
            comboNote.requestFocus();
            return;
        }

        if (nouveauCommentaire.isEmpty()) {
            AlertHelper.showError("Erreur", "Le commentaire ne peut pas être vide !");
            txtCommentaire.requestFocus();
            return;
        }

        // 2. VALIDATION : Longueur minimale
        if (nouveauCommentaire.length() < 5) {
            AlertHelper.showWarning("Attention", "Le commentaire doit contenir au moins 5 caractères !");
            txtCommentaire.requestFocus();
            return;
        }

        // 3. VALIDATION : Détecter si des modifications ont été faites
        boolean noteModifiee = !nouvelleNote.equals(noteInitiale);
        boolean commentaireModifie = !nouveauCommentaire.equals(commentaireInitial);
        boolean categorieModifiee = (nouvelleCategorie != null && !nouvelleCategorie.equals(categorieInitiale));

        if (!noteModifiee && !commentaireModifie && !categorieModifiee) {
            AlertHelper.showInfo("Aucune modification",
                    "Vous n'avez effectué aucune modification.\n\nLes données sont identiques aux valeurs d'origine.");
            return;
        }

        // 4. Afficher un résumé des modifications
        StringBuilder modifications = new StringBuilder("Modifications détectées :\n\n");

        if (noteModifiee) {
            modifications.append("✓ Note modifiée\n");
            modifications.append("   Ancien : ").append(noteInitiale).append("/5\n");
            modifications.append("   Nouveau : ").append(nouvelleNote).append("/5\n\n");
        }

        if (commentaireModifie) {
            modifications.append("✓ Commentaire modifié\n");
            modifications.append("   Ancien : ").append(
                    commentaireInitial.length() > 50 ? commentaireInitial.substring(0, 50) + "..." : commentaireInitial)
                    .append("\n");
            modifications.append("   Nouveau : ").append(
                    nouveauCommentaire.length() > 50 ? nouveauCommentaire.substring(0, 50) + "..." : nouveauCommentaire)
                    .append("\n\n");
        }

        modifications.append("Voulez-vous enregistrer ces modifications ?");

        // 5. Confirmation avant enregistrement
        if (AlertHelper.showConfirmation("Confirmer les modifications", modifications.toString())) {
            try {
                // Mettre à jour l'objet
                avis.setNote(nouvelleNote);
                avis.setCommentaire(nouveauCommentaire);
                
                // Trouver l'ID de la nouvelle catégorie
                int typeId = categoriesMap.entrySet().stream()
                        .filter(entry -> entry.getValue().equals(nouvelleCategorie))
                        .map(java.util.Map.Entry::getKey)
                        .findFirst().orElse(avis.getTypeId());
                avis.setTypeId(typeId);

                // Sauvegarder en base de données
                avisService.updateEntity(avis);

                // Message de succès
                AlertHelper.showSuccess("Succès", "Votre avis a été modifié avec succès !");

                // Retour à la liste
                goBackToList();

            } catch (SQLException e) {
                AlertHelper.showError("Erreur", "Erreur lors de la modification :\n" + e.getMessage());
            }
        }
    }

    @FXML
    void annuler(ActionEvent event) {
        // Vérifier si des modifications non sauvegardées existent
        Integer noteActuelle = comboNote.getValue();
        String commentaireActuel = txtCommentaire.getText().trim();

        if (!noteActuelle.equals(noteInitiale) || !commentaireActuel.equals(commentaireInitial)) {
            if (AlertHelper.showConfirmation("Modifications non sauvegardées",
                    "Vous avez des modifications non sauvegardées.\n\nÊtes-vous sûr de vouloir quitter sans enregistrer ?")) {
                goBackToList();
            }
        } else {
            goBackToList();
        }
    }

    @FXML
    void retourMenu() {
        annuler(null);
    }

    private void goBackToList() {
        try {
            hebergement.controllers.ClientLayoutController layout =
                hebergement.controllers.ClientLayoutController.getInstance();
            if (layout != null) {
                layout.goMonEspace();
            } else {
                javafx.scene.Node anyNode = null;
                if (anyNode == null) try { anyNode = btnSave; } catch (Exception e2) {}
                if (anyNode == null) try { anyNode = comboNote; } catch (Exception e2) {}
                if (anyNode != null && anyNode.getScene() != null) {
                    Parent root = FXMLLoader.load(getClass().getResource("/app/MonEspace.fxml"));
                    anyNode.getScene().setRoot(root);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
