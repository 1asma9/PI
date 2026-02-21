package hebergement.controllers;

import hebergement.entities.Disponibilite;
import hebergement.entities.Hebergement;
import hebergement.entities.TypeHebergement;
import hebergement.services.DisponibiliteService;
import hebergement.services.HebergementService;
import hebergement.services.TypeHebergementService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class AddHebergementController {

    private final HebergementService hs = new HebergementService();
    private final DisponibiliteService ds = new DisponibiliteService();
    private final TypeHebergementService ts = new TypeHebergementService();

    @FXML private TextField tfDesc;
    @FXML private TextField tfAdresse;
    @FXML private TextField tfPrix;
    @FXML private ComboBox<TypeHebergement> cbType;
    @FXML private Label lblHebMsg;

    @FXML private Label lblImage;
    private String selectedImagePath = null;

    @FXML private ComboBox<Hebergement> cbHebergement;
    @FXML private DatePicker dpDebut;
    @FXML private DatePicker dpFin;
    @FXML private CheckBox chkDisponible;
    @FXML private Label lblDispoMsg;

    @FXML
    public void initialize() {
        loadTypes();
        loadHebergements();
        lblHebMsg.setText("");
        lblDispoMsg.setText("");
        if (lblImage != null) lblImage.setText("Aucune image choisie");
    }

    // ====================== LOAD TYPES ======================
    private void loadTypes() {
        try {
            List<TypeHebergement> types = ts.getData();
            cbType.setItems(FXCollections.observableArrayList(types));

            cbType.setCellFactory(lv -> new ListCell<>() {
                @Override protected void updateItem(TypeHebergement item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? "" : item.getLibelle());
                }
            });
            cbType.setButtonCell(new ListCell<>() {
                @Override protected void updateItem(TypeHebergement item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? "" : item.getLibelle());
                }
            });

        } catch (Exception e) {
            showError("Erreur", "Impossible de charger les types : " + e.getMessage());
        }
    }

    // ====================== LOAD HEBERGEMENTS ======================
    private void loadHebergements() {
        try {
            List<Hebergement> list = hs.getData();
            cbHebergement.setItems(FXCollections.observableArrayList(list));

            cbHebergement.setCellFactory(lv -> new ListCell<>() {
                @Override protected void updateItem(Hebergement item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? "" : ("#" + item.getId() + " - " + item.getDescription()));
                }
            });
            cbHebergement.setButtonCell(new ListCell<>() {
                @Override protected void updateItem(Hebergement item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? "" : ("#" + item.getId() + " - " + item.getDescription()));
                }
            });

        } catch (Exception e) {
            showError("Erreur", "Impossible de charger les hébergements : " + e.getMessage());
        }
    }

    // ====================== CHOOSE IMAGE ======================
    @FXML
    void chooseImage() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choisir une image");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg")
        );

        File file = chooser.showOpenDialog(null);
        if (file == null) return;

        try {
            String newName = UUID.randomUUID() + "_" + file.getName();

            File dest = new File("uploads/" + newName);

            Files.copy(file.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);

            selectedImagePath = "uploads/" + newName;
            lblImage.setText("✅ " + file.getName());

        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur image", "Impossible de copier l'image : " + e.getMessage());
        }
    }

    // ====================== ADD HEBERGEMENT ======================
    @FXML
    void addHebergement() {
        lblHebMsg.setText("");

        String desc = tfDesc.getText() == null ? "" : tfDesc.getText().trim();
        String adr  = tfAdresse.getText() == null ? "" : tfAdresse.getText().trim();
        String prixS = tfPrix.getText() == null ? "" : tfPrix.getText().trim();
        TypeHebergement type = cbType.getValue();

        if (desc.isEmpty()) { showInfo("Validation", "Description obligatoire."); return; }
        if (adr.isEmpty())  { showInfo("Validation", "Adresse obligatoire."); return; }
        if (type == null)   { showInfo("Validation", "Choisir un type."); return; }

        double prix;
        try {
            prix = Double.parseDouble(prixS);
        } catch (Exception e) {
            showInfo("Validation", "Prix invalide (ex: 250.0).");
            return;
        }
        if (prix <= 0) { showInfo("Validation", "Prix doit être > 0."); return; }

        try {
            Hebergement h = new Hebergement(desc, adr, prix, type.getId());
            h.setImagePath(selectedImagePath);

            hs.addEntity(h);

            lblHebMsg.setText("✅ Hébergement ajouté.");
            tfDesc.clear(); tfAdresse.clear(); tfPrix.clear();
            cbType.getSelectionModel().clearSelection();

            selectedImagePath = null;
            lblImage.setText("Aucune image choisie");

            loadHebergements();

        } catch (Exception e) {
            showError("Erreur ajout", e.getMessage());
        }
    }

    // ====================== ADD DISPONIBILITE ======================
    @FXML
    void addDisponibilite() {
        lblDispoMsg.setText("");

        Hebergement selected = cbHebergement.getValue();
        LocalDate debut = dpDebut.getValue();
        LocalDate fin = dpFin.getValue();

        if (selected == null) { showInfo("Validation", "Choisir un hébergement."); return; }
        if (debut == null || fin == null) { showInfo("Validation", "Choisir début et fin."); return; }
        if (fin.isBefore(debut)) { showInfo("Validation", "Date fin doit être >= date début."); return; }

        try {
            Disponibilite dispo = new Disponibilite(
                    selected.getId(),
                    debut,
                    fin,
                    chkDisponible.isSelected()
            );
            ds.addEntity(dispo);

            lblDispoMsg.setText("✅ Disponibilité ajoutée.");
            dpDebut.setValue(null);
            dpFin.setValue(null);
            chkDisponible.setSelected(true);

        } catch (Exception e) {
            showError("Erreur dispo", e.getMessage());
        }
    }

    // ====================== UTIL ======================
    private void showInfo(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    private void showError(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}
