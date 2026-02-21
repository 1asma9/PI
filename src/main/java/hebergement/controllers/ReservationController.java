package hebergement.controllers;

import hebergement.entities.Hebergement;
import hebergement.entities.Reservation;
import hebergement.services.HebergementService;
import hebergement.services.ReservationService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class ReservationController {

    private final HebergementService hs = new HebergementService();
    private final ReservationService rs = new ReservationService();

    @FXML private ComboBox<Hebergement> cbHebergement;

    @FXML private TextField tfNom;
    @FXML private TextField tfTel;
    @FXML private TextField tfEmail;

    @FXML private DatePicker dpDebut;
    @FXML private DatePicker dpFin;

    @FXML private Label lblTotal;
    @FXML private Label lblMsg;

    @FXML
    public void initialize() {
        lblMsg.setText("");
        loadHebergements();

        // recalculer total dès que l’utilisateur change date ou hébergement
        cbHebergement.valueProperty().addListener((obs, o, n) -> computeTotal());
        dpDebut.valueProperty().addListener((obs, o, n) -> computeTotal());
        dpFin.valueProperty().addListener((obs, o, n) -> computeTotal());
    }

    private void loadHebergements() {
        try {
            List<Hebergement> list = hs.getData();
            cbHebergement.setItems(FXCollections.observableArrayList(list));

            cbHebergement.setCellFactory(lv -> new ListCell<>() {
                @Override protected void updateItem(Hebergement item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? "" :
                            "#" + item.getId() + " - " + item.getDescription() + " (" + item.getPrix() + " DT)");
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
            showError("Erreur", "Impossible de charger les hébergements : " + e.getMessage());
        }
    }

    private void computeTotal() {
        Hebergement h = cbHebergement.getValue();
        LocalDate debut = dpDebut.getValue();
        LocalDate fin = dpFin.getValue();

        if (h == null || debut == null || fin == null) {
            lblTotal.setText("Total: 0.0 DT");
            return;
        }
        if (!fin.isAfter(debut)) {
            lblTotal.setText("Total: 0.0 DT");
            return;
        }

        long nuits = ChronoUnit.DAYS.between(debut, fin);
        double total = nuits * h.getPrix();
        lblTotal.setText("Total: " + total + " DT (" + nuits + " nuits)");
    }

    @FXML
    void addReservation() {
        lblMsg.setText("");

        Hebergement h = cbHebergement.getValue();
        String nom = tfNom.getText() == null ? "" : tfNom.getText().trim();
        String tel = tfTel.getText() == null ? "" : tfTel.getText().trim();
        String email = tfEmail.getText() == null ? "" : tfEmail.getText().trim();
        LocalDate debut = dpDebut.getValue();
        LocalDate fin = dpFin.getValue();


        if (h == null) {
            showInfo("Validation", "Choisir un hébergement.");
            return;
        }

        if (nom.isEmpty()) {
            showInfo("Validation", "Nom du client obligatoire.");
            return;
        }


        if (tel == null || !tel.matches("\\d{8}")) {
            showInfo("Validation", "Le téléphone doit contenir exactement 8 chiffres.");
            return;
        }


        if (email == null || !email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            showInfo("Validation", "Email invalide.");
            return;
        }

        if (debut == null || fin == null) {
            showInfo("Validation", "Choisir date début et date fin.");
            return;
        }

        if (!fin.isAfter(debut)) {
            showInfo("Validation", "Date fin doit être > date début.");
            return;
        }


        try {
            if (!rs.isInsideDisponibilite(h.getId(), debut, fin)) {
                showInfo("Indisponible", "Ces dates ne sont pas dans une période disponible.");
                return;
            }

            if (!rs.isHebergementAvailable(h.getId(), debut, fin)) {
                showInfo("Conflit", "Cet hébergement est déjà réservé sur cette période.");
                return;
            }

            int nuits = (int) ChronoUnit.DAYS.between(debut, fin);
            double total = nuits * h.getPrix();

            Reservation r = new Reservation(
                    h.getId(),
                    nom,
                    tel.isEmpty() ? null : tel,
                    email.isEmpty() ? null : email,
                    debut,
                    fin,
                    nuits,
                    total,
                    "EN_ATTENTE"
            );

            rs.addEntity(r);

            lblMsg.setText("✅ Réservation ajoutée (EN_ATTENTE).");
            clearForm();

        } catch (Exception e) {
            showError("Erreur réservation", e.getMessage());
        }
    }

    private void clearForm() {
        cbHebergement.getSelectionModel().clearSelection();
        tfNom.clear();
        tfTel.clear();
        tfEmail.clear();
        dpDebut.setValue(null);
        dpFin.setValue(null);
        lblTotal.setText("Total: 0.0 DT");
    }

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
