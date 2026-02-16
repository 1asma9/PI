package edu.pidev.controllers;

import edu.pidev.entities.ReservationActivite;
import edu.pidev.services.ReservationActiviteService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class ReservationFormController {

    @FXML private Label lblActivite;
    @FXML private Label lblMode;
    @FXML private DatePicker dpDate;
    @FXML private TextField tfNombre;
    @FXML private ComboBox<String> cbStatut;
    @FXML private Label lblError;
    @FXML private Button btnSave;
    @FXML private FlowPane reservationsContainer;

    private final ReservationActiviteService service = new ReservationActiviteService();

    // contexte
    private int idActivite = -1;
    private String nomActivite = "-";

    // mode update
    private int selectedIdReservation = -1;

    @FXML
    public void initialize() {
        // ✅ safety: in case FXML mismatch
        if (cbStatut != null) {
            cbStatut.getItems().setAll("EN_ATTENTE", "CONFIRMEE", "ANNULEE");
            cbStatut.setValue("EN_ATTENTE");
        }
        if (dpDate != null) dpDate.setValue(LocalDate.now());

        setModeAdd();
    }

    // appelé depuis AffichageActivitesController quand on clique 📌
    public void setData(int idActivite, String nomActivite) {
        this.idActivite = idActivite;
        this.nomActivite = (nomActivite == null || nomActivite.isBlank()) ? "-" : nomActivite.trim();
        lblActivite.setText("Activité : " + this.nomActivite);
        refreshList();
    }

    // ====== ACTIONS UI ======
    @FXML
    private void onSave() {
        hideError();

        if (idActivite <= 0) {
            showError("Ouvre cette page depuis l’icône 📌 d’une activité.");
            return;
        }

        LocalDate date = dpDate.getValue();
        if (date == null) {
            showError("Veuillez choisir une date.");
            return;
        }

        int nb;
        try {
            nb = Integer.parseInt(tfNombre.getText().trim());
        } catch (Exception e) {
            showError("Nombre de personnes invalide.");
            return;
        }

        String statut = cbStatut.getValue();
        if (statut == null || statut.isBlank()) {
            showError("Statut invalide.");
            return;
        }

        if (selectedIdReservation <= 0) {
            // ADD
            ReservationActivite r = new ReservationActivite(date, nb, statut, idActivite);
            service.addReservation(r);
        } else {
            // UPDATE
            ReservationActivite r = new ReservationActivite(selectedIdReservation, date, nb, statut, idActivite);
            service.updateReservation(r);
        }

        setModeAdd();
        refreshList();
    }

    @FXML
    private void onClear() {
        setModeAdd();
    }

    @FXML
    private void onRefresh() {
        refreshList();
    }

    /**
     * ✅ FIXED BACK:
     * - uses FXMLLoader instance
     * - reuses SAME Stage
     * - clears stylesheets to avoid stacking
     * - maximizes + show
     */
    @FXML
    private void onBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/affichage_activites.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);

            // ✅ clean stylesheets then add affichage.css
            scene.getStylesheets().clear();
            var css = getClass().getResource("/affichage.css");
            if (css != null) scene.getStylesheets().add(css.toExternalForm());

            Stage stage = (Stage) lblActivite.getScene().getWindow();

            stage.setScene(scene);

            // ✅ force full screen like you want
            stage.setMaximized(true);

            // ✅ VERY IMPORTANT: show again
            stage.show();

            // ✅ force focus so scroll works immediately
            javafx.application.Platform.runLater(() -> {
                root.requestFocus();
            });

        } catch (Exception e) {
            e.printStackTrace();
            showError("Retour impossible: " + e.getMessage());
        }
    }


    // ====== LIST ======
    private void refreshList() {
        reservationsContainer.getChildren().clear();

        if (idActivite <= 0) return;

        List<ReservationActivite> list = service.getReservationsByActivite(idActivite);

        for (ReservationActivite r : list) {
            reservationsContainer.getChildren().add(createReservationCard(r));
        }
    }

    private VBox createReservationCard(ReservationActivite r) {
        VBox card = new VBox(12);
        card.getStyleClass().add("resCard");

        // Header with ID and status badge
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label idLabel = new Label("📋 #" + r.getIdReservation());
        idLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 700; -fx-text-fill: #8b7a66;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label statusBadge = new Label(r.getStatut());
        statusBadge.getStyleClass().addAll("statusBadge", "status-" + r.getStatut());

        header.getChildren().addAll(idLabel, spacer, statusBadge);

        // Details with icons
        VBox details = new VBox(8);
        details.setStyle("-fx-padding: 5 0;");

        HBox dateBox = new HBox(10);
        Label dateIcon = new Label("📅");
        dateIcon.setStyle("-fx-font-size: 14px;");
        Label dateLabel = new Label(r.getDateReservation().toString());
        dateLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #0f2a2a;");
        dateBox.getChildren().addAll(dateIcon, dateLabel);

        HBox peopleBox = new HBox(10);
        Label peopleIcon = new Label("👥");
        peopleIcon.setStyle("-fx-font-size: 14px;");
        String peopleText = r.getNombrePersonnes() + " personne" + (r.getNombrePersonnes() > 1 ? "s" : "");
        Label peopleLabel = new Label(peopleText);
        peopleLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #0f2a2a;");
        peopleBox.getChildren().addAll(peopleIcon, peopleLabel);

        details.getChildren().addAll(dateBox, peopleBox);

        Separator separator = new Separator();
        separator.setStyle("-fx-background-color: #e0d6cc; -fx-opacity: 0.5;");

        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_RIGHT);

        Button edit = new Button("✏️");
        edit.getStyleClass().addAll("iconBtn", "btnEdit");
        edit.setTooltip(new Tooltip("Modifier cette réservation"));
        edit.setOnAction(e -> loadToForm(r));

        Button del = new Button("🗑️");
        del.getStyleClass().addAll("iconBtn", "btnDelete");
        del.setTooltip(new Tooltip("Supprimer cette réservation"));
        del.setOnAction(e -> deleteReservation(r));

        actions.getChildren().addAll(edit, del);

        card.getChildren().addAll(header, details, separator, actions);
        return card;
    }

    private void loadToForm(ReservationActivite r) {
        selectedIdReservation = r.getIdReservation();
        dpDate.setValue(r.getDateReservation());
        tfNombre.setText(String.valueOf(r.getNombrePersonnes()));
        cbStatut.setValue(r.getStatut());

        lblMode.setText("Modifier la réservation #" + r.getIdReservation());
        btnSave.setText("Enregistrer");
    }

    private void deleteReservation(ReservationActivite r) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer cette réservation ?");
        confirm.setContentText("Réservation #" + r.getIdReservation());

        Optional<ButtonType> res = confirm.showAndWait();
        if (res.isPresent() && res.get() == ButtonType.OK) {
            service.deleteReservation(r.getIdReservation());
            refreshList();
            setModeAdd();
        }
    }

    // ====== MODE ======
    private void setModeAdd() {
        selectedIdReservation = -1;
        if (dpDate != null) dpDate.setValue(LocalDate.now());
        if (tfNombre != null) tfNombre.clear();
        if (cbStatut != null) cbStatut.setValue("EN_ATTENTE");
        if (lblMode != null) lblMode.setText("Ajouter une réservation");
        if (btnSave != null) btnSave.setText("Ajouter");
        hideError();
    }

    private void showError(String msg) {
        lblError.setText(msg);
        lblError.setVisible(true);
        lblError.setStyle("-fx-text-fill: #c0392b; -fx-font-weight: 700;");
    }

    private void hideError() {
        lblError.setVisible(false);
        lblError.setText("");
    }
}
