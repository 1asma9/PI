package edu.pidev.controllers;

import edu.pidev.entities.Activite;
import edu.pidev.entities.ReservationActivite;
import edu.pidev.services.ActiviteService;
import edu.pidev.services.ReservationActiviteService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import javafx.event.ActionEvent;
import javafx.scene.Node;

public class ReservationFormController {

    @FXML private Label lblActivite;
    @FXML private Label lblMode;
    @FXML private DatePicker dpDate;
    @FXML private TextField tfNombre;
    @FXML private ComboBox<String> cbStatut;
    @FXML private Label lblError;
    @FXML private Button btnSave;
    @FXML private FlowPane reservationsContainer;

    // ✅ AI panel
    @FXML private Label aiExpectText;

    private final ReservationActiviteService service = new ReservationActiviteService();
    private final ActiviteService activiteService = new ActiviteService();

    // contexte
    private int idActivite = -1;
    private String nomActivite = "-";

    // mode update
    private int selectedIdReservation = -1;

    // ✅ Selected activity full object (for AI payload)
    private Activite selectedActivite = null;

    // ✅ AI endpoint
    private static final String AI_EXPECT_URL = "http://localhost/pidev_api/ai_expect.php";
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    @FXML
    public void initialize() {
        if (cbStatut != null) {
            cbStatut.getItems().setAll("EN_ATTENTE", "CONFIRMEE", "ANNULEE");
            cbStatut.setValue("EN_ATTENTE");
        }
        if (dpDate != null) dpDate.setValue(LocalDate.now());

        if (aiExpectText != null) {
            aiExpectText.setText("Click Generate to get AI tips for this activity.");
        }

        setModeAdd();
    }

    public void setData(int idActivite, String nomActivite) {
        this.idActivite = idActivite;
        this.nomActivite = (nomActivite == null || nomActivite.isBlank()) ? "-" : nomActivite.trim();
        lblActivite.setText("Activité : " + this.nomActivite);

        // ✅ Load full activity data for AI (type/lieu/duree/prix)
        selectedActivite = null;
        try {
            List<Activite> all = activiteService.getAllActivites();
            selectedActivite = all.stream()
                    .filter(a -> a.getIdActivite() == idActivite)
                    .findFirst()
                    .orElse(null);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (aiExpectText != null) {
            aiExpectText.setText("Click Generate to get AI tips for this activity.");
        }

        refreshList();
    }

    // ===================== AI BUTTON =====================
    @FXML
    private void onGenerateWhatToExpect() {
        if (aiExpectText == null) return;

        if (idActivite <= 0) {
            aiExpectText.setText("Open this page from an activity first.");
            return;
        }

        if (selectedActivite == null) {
            aiExpectText.setText("Activity details not loaded. Try reopening the reservation screen.");
            return;
        }

        aiExpectText.setText("Generating AI tips...");

        String jsonBody = "{"
                + "\"title\":\"" + escapeJson(selectedActivite.getNom()) + "\","
                + "\"type\":\"" + escapeJson(selectedActivite.getType()) + "\","
                + "\"lieu\":\"" + escapeJson(selectedActivite.getLieu()) + "\","
                + "\"duree\":" + selectedActivite.getDuree() + ","
                + "\"prix\":" + selectedActivite.getPrix()
                + "}";

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(AI_EXPECT_URL))
                .timeout(Duration.ofSeconds(30))
                .header("Content-Type", "application/json; charset=utf-8")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
                .build();

        new Thread(() -> {
            try {
                HttpResponse<String> res = httpClient.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

                if (res.statusCode() != 200) {
                    Platform.runLater(() ->
                            aiExpectText.setText("AI error (" + res.statusCode() + "): " + res.body())
                    );
                    return;
                }

                String text = extractJsonField(res.body(), "text");
                if (text == null || text.isBlank()) text = "No AI text returned.";

                String finalText = text.replace("\\n", "\n");

                Platform.runLater(() -> aiExpectText.setText(finalText));

            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() ->
                        aiExpectText.setText("AI request failed: " + e.getMessage())
                );
            }
        }).start();
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    // simple JSON extraction: {"ok":true,"text":"...."}
    private String extractJsonField(String json, String field) {
        String key = "\"" + field + "\":";
        int i = json.indexOf(key);
        if (i == -1) return null;
        int start = json.indexOf("\"", i + key.length());
        if (start == -1) return null;
        int end = json.indexOf("\"", start + 1);
        while (end != -1 && json.charAt(end - 1) == '\\') { // skip escaped quotes
            end = json.indexOf("\"", end + 1);
        }
        if (end == -1) return null;
        return json.substring(start + 1, end);
    }

    // ===================== ACTIONS UI =====================
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

    @FXML
    private void onBack(ActionEvent event) {
        switchScene(event, "/affichage_activites_back.fxml", "/affichage.css");
    }

    private void switchScene(ActionEvent event, String fxml, String cssFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            Scene scene = stage.getScene();
            if (scene == null) {
                scene = new Scene(root);
                stage.setScene(scene);
            } else {
                scene.setRoot(root);
            }

            scene.getStylesheets().clear();
            var css = getClass().getResource(cssFile);
            if (css != null) scene.getStylesheets().add(css.toExternalForm());

            stage.show();

            // ✅ maximize AFTER show (prevents stuck)
            Platform.runLater(() -> stage.setMaximized(true));

        } catch (Exception e) {
            e.printStackTrace();
            showError("Retour impossible: " + e.getMessage());
        }
    }

    // ===================== LIST =====================
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

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label idLabel = new Label("📋 #" + r.getIdReservation());
        idLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 700; -fx-text-fill: #8b7a66;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label statusBadge = new Label(r.getStatut());
        statusBadge.getStyleClass().addAll("statusBadge", "status-" + r.getStatut());

        header.getChildren().addAll(idLabel, spacer, statusBadge);

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

    // ===================== MODE =====================
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