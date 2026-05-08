package controllers;

import entities.Reclamation;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import services.ReclamationService;
import tools.AlertHelper;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class ModifierReclamationController implements Initializable {

    @FXML
    private TextField txtTitre;
    @FXML
    private TextArea txtDescription;
    @FXML
    private Button btnSave;
    @FXML
    private Button btnCancel;

    private Reclamation reclamation;
    private ReclamationService reclamationService = new ReclamationService();

    // Variables pour stocker les valeurs initiales
    private String titreInitial;
    private String descriptionInitiale;

    public void setReclamation(Reclamation reclamation) {
        this.reclamation = reclamation;

        // Sauvegarder les valeurs initiales
        titreInitial = reclamation.getTitre() == null ? "" : reclamation.getTitre();
        descriptionInitiale = reclamation.getDescription() == null ? "" : reclamation.getDescription();

        // Remplir les champs
        txtTitre.setText(titreInitial);
        txtDescription.setText(descriptionInitiale);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
    }

    @FXML
    void enregistrer(ActionEvent event) {
        // Récupérer les nouvelles valeurs
        String nouveauTitre = txtTitre.getText().trim();
        String nouvelleDescription = txtDescription.getText().trim();

        // 1. VALIDATION : Champs vides
        if (nouveauTitre.isEmpty()) {
            AlertHelper.showError("Erreur", "Le titre ne peut pas être vide !");
            txtTitre.requestFocus();
            return;
        }

        if (nouvelleDescription.isEmpty()) {
            AlertHelper.showError("Erreur", "La description ne peut pas être vide !");
            txtDescription.requestFocus();
            return;
        }

        // 2. VALIDATION : Longueur minimale
        if (nouveauTitre.length() < 5) {
            AlertHelper.showWarning("Attention", "Le titre doit contenir au moins 5 caractères !");
            txtTitre.requestFocus();
            return;
        }

        if (nouvelleDescription.length() < 10) {
            AlertHelper.showWarning("Attention", "La description doit contenir au moins 10 caractères !");
            txtDescription.requestFocus();
            return;
        }

        // 3. VALIDATION : Détecter si des modifications ont été faites
        boolean titreModifie = !nouveauTitre.equals(titreInitial);
        boolean descriptionModifiee = !nouvelleDescription.equals(descriptionInitiale);

        if (!titreModifie && !descriptionModifiee) {
            AlertHelper.showInfo("Aucune modification",
                    "Vous n'avez effectué aucune modification.\n\nLes données sont identiques aux valeurs d'origine.");
            return;
        }

        // 4. Afficher un résumé des modifications
        StringBuilder modifications = new StringBuilder("Modifications détectées :\n\n");

        if (titreModifie) {
            modifications.append("✓ Titre modifié\n");
            modifications.append("   Ancien : ").append(titreInitial).append("\n");
            modifications.append("   Nouveau : ").append(nouveauTitre).append("\n\n");
        }

        if (descriptionModifiee) {
            modifications.append("✓ Description modifiée\n");
            modifications.append("   Ancien : ")
                    .append(descriptionInitiale.length() > 50 ? descriptionInitiale.substring(0, 50) + "..."
                            : descriptionInitiale)
                    .append("\n");
            modifications.append("   Nouveau : ")
                    .append(nouvelleDescription.length() > 50 ? nouvelleDescription.substring(0, 50) + "..."
                            : nouvelleDescription)
                    .append("\n\n");
        }

        modifications.append("Voulez-vous enregistrer ces modifications ?");

        // 5. Confirmation avant enregistrement
        if (AlertHelper.showConfirmation("Confirmer les modifications", modifications.toString())) {
            try {
                // Mettre à jour l'objet
                reclamation.setTitre(nouveauTitre);
                reclamation.setDescription(nouvelleDescription);

                // Sauvegarder en base de données
                reclamationService.updateEntity(reclamation);

                // Message de succès
                AlertHelper.showSuccess("Succès", "La réclamation a été modifiée avec succès !");

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
        String titreActuel = txtTitre.getText().trim();
        String descriptionActuelle = txtDescription.getText().trim();

        if (!titreActuel.equals(titreInitial) || !descriptionActuelle.equals(descriptionInitiale)) {
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
            Parent root = FXMLLoader.load(getClass().getResource("/user_reclamations.fxml"));
            btnSave.getScene().setRoot(root);
        } catch (IOException e) {
            AlertHelper.showError("Erreur", "Impossible de retourner à la liste : " + e.getMessage());
        }
    }
}
