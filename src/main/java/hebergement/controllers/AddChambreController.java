package hebergement.controllers;

import hebergement.entities.Chambre;
import hebergement.entities.Hebergement;
import hebergement.services.ChambreService;
import hebergement.services.HebergementService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.List;

public class AddChambreController {

    @FXML private ComboBox<Hebergement> cbHebergement;
    @FXML private TextField tfNumero, tfPrix, tfCapacite;
    @FXML private ComboBox<String> cbTypeChambre;
    @FXML private TextArea taEquipements;
    @FXML private VBox vboxPeriodes;
    @FXML private Label lblMsg;

    private final HebergementService hs = new HebergementService();
    private final ChambreService cs = new ChambreService();

    @FXML
    public void initialize() {
        // Charger hébergements dans ComboBox
        try {
            List<Hebergement> hebs = hs.getData();
            cbHebergement.setItems(FXCollections.observableArrayList(hebs));

            // Afficher description dans le ComboBox
            cbHebergement.setCellFactory(lv -> new ListCell<>() {
                @Override protected void updateItem(Hebergement item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? "" :
                            "#" + item.getId() + " - " + item.getDescription());
                }
            });
            cbHebergement.setButtonCell(new ListCell<>() {
                @Override protected void updateItem(Hebergement item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? "" :
                            "#" + item.getId() + " - " + item.getDescription());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Types de chambre (identiques au PHP)
        cbTypeChambre.setItems(FXCollections.observableArrayList(
                "simple", "double", "suite", "familiale"
        ));

        // Une période par défaut
        ajouterPeriode();
    }

    @FXML
    private void ajouterPeriode() {
        HBox row = new HBox(12);
        row.setStyle("-fx-background-color: #1a2238;" +
                "-fx-background-radius: 10;" +
                "-fx-padding: 12;");

        // Date début
        VBox vbDebut = new VBox(4);
        Label lblDebut = new Label("Date début *");
        lblDebut.setStyle("-fx-text-fill: #8a9bbf; -fx-font-size: 11px;");
        DatePicker dpDebut = new DatePicker();
        dpDebut.setPromptText("mm/dd/yyyy");
        dpDebut.setPrefWidth(180);
        dpDebut.setStyle("-fx-background-color: #131a2a;" +
                "-fx-border-color: #2a3558;" +
                "-fx-border-radius: 8;" +
                "-fx-background-radius: 8;" +
                "-fx-font-size: 13px;");
        vbDebut.getChildren().addAll(lblDebut, dpDebut);

        // Date fin
        VBox vbFin = new VBox(4);
        Label lblFin = new Label("Date fin *");
        lblFin.setStyle("-fx-text-fill: #8a9bbf; -fx-font-size: 11px;");
        DatePicker dpFin = new DatePicker();
        dpFin.setPromptText("mm/dd/yyyy");
        dpFin.setPrefWidth(180);
        dpFin.setStyle("-fx-background-color: #131a2a;" +
                "-fx-border-color: #2a3558;" +
                "-fx-border-radius: 8;" +
                "-fx-background-radius: 8;" +
                "-fx-font-size: 13px;");
        vbFin.getChildren().addAll(lblFin, dpFin);

        // Checkbox Disponible
        CheckBox chk = new CheckBox("Disponible");
        chk.setSelected(true);
        chk.setStyle("-fx-text-fill: #cdd5e0; -fx-font-size: 13px;");
        chk.setTranslateY(18);

        // Bouton supprimer la ligne
        Button btnDel = new Button("✕");
        btnDel.setStyle("-fx-background-color: #FF3B5C;" +
                "-fx-text-fill: white;" +
                "-fx-background-radius: 8;" +
                "-fx-font-size: 12px;" +
                "-fx-cursor: hand;" +
                "-fx-padding: 6 10;");
        btnDel.setTranslateY(18);
        btnDel.setOnAction(e -> vboxPeriodes.getChildren().remove(row));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        row.getChildren().addAll(vbDebut, vbFin, chk, spacer, btnDel);
        vboxPeriodes.getChildren().add(row);
    }

    @FXML
    private void createChambre() {
        // ===== Validation champs =====
        Hebergement heb = cbHebergement.getValue();
        if (heb == null) {
            showMsg("⚠ Choisir un hébergement.", false); return;
        }

        String numero = tfNumero.getText().trim();
        if (numero.isBlank()) {
            showMsg("⚠ Numéro obligatoire.", false); return;
        }
        if (numero.length() > 10) {
            showMsg("⚠ Numéro max 10 caractères.", false); return;
        }

        String type = cbTypeChambre.getValue();
        if (type == null) {
            showMsg("⚠ Choisir un type de chambre.", false); return;
        }

        String prixStr = tfPrix.getText().trim();
        if (prixStr.isBlank()) {
            showMsg("⚠ Prix obligatoire.", false); return;
        }

        String capStr = tfCapacite.getText().trim();
        if (capStr.isBlank()) {
            showMsg("⚠ Capacité obligatoire.", false); return;
        }

        String equipements = taEquipements.getText().trim();
        if (equipements.isBlank()) {
            showMsg("⚠ Équipements obligatoires.", false); return;
        }

        // ===== Parse nombres =====
        double prix;
        int capacite;
        try {
            prix = Double.parseDouble(prixStr);
            if (prix <= 0) {
                showMsg("⚠ Prix doit être positif.", false); return;
            }
        } catch (NumberFormatException e) {
            showMsg("⚠ Prix invalide.", false); return;
        }

        try {
            capacite = Integer.parseInt(capStr);
            if (capacite <= 0) {
                showMsg("⚠ Capacité doit être positive.", false); return;
            }
            if (capacite > 20) {
                showMsg("⚠ Capacité max 20 personnes.", false); return;
            }
        } catch (NumberFormatException e) {
            showMsg("⚠ Capacité invalide.", false); return;
        }

        // ===== Créer et sauvegarder =====
        try {
            Chambre chambre = new Chambre(heb, numero, type, prix, capacite, equipements);
            cs.add(chambre);
            showMsg("✅ Chambre créée avec succès !", true);
            clearForm();
        } catch (IllegalArgumentException e) {
            showMsg("⚠ " + e.getMessage(), false);
        } catch (Exception e) {
            showMsg("❌ Erreur : " + e.getMessage(), false);
            e.printStackTrace();
        }
    }

    @FXML
    private void annuler() {
        clearForm();
    }

    private void clearForm() {
        cbHebergement.setValue(null);
        tfNumero.clear();
        cbTypeChambre.setValue(null);
        tfPrix.clear();
        tfCapacite.clear();
        taEquipements.clear();
        vboxPeriodes.getChildren().clear();
        ajouterPeriode();
        lblMsg.setText("");
    }

    private void showMsg(String msg, boolean success) {
        lblMsg.setText(msg);
        lblMsg.setStyle("-fx-font-size: 12px; -fx-text-fill: "
                + (success ? "#5ccf8a" : "#FF3B5C") + ";");
    }
}