package edu.pidev.controllers;

import edu.pidev.entities.Activite;
import edu.pidev.services.ActiviteService;
import javafx.beans.property.*;
import javafx.collections.*;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class AffichageActivitesBackController {

    @FXML private TableView<Activite> tableActivites;
    @FXML private TableColumn<Activite, Integer> colId;
    @FXML private TableColumn<Activite, String> colNom;
    @FXML private TableColumn<Activite, String> colLieu;
    @FXML private TableColumn<Activite, String> colType;
    @FXML private TableColumn<Activite, Double> colPrix;
    @FXML private TableColumn<Activite, Integer> colDuree;
    @FXML private TableColumn<Activite, Double> colRating;

    @FXML private TextField tfSearch;
    @FXML private ComboBox<String> cbType;
    @FXML private ComboBox<String> cbLieu;

    private final ActiviteService service = new ActiviteService();

    private final ObservableList<Activite> master = FXCollections.observableArrayList();
    private FilteredList<Activite> filtered;

    @FXML
    public void initialize() {

        colId.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getIdActivite()).asObject());
        colNom.setCellValueFactory(d -> new SimpleStringProperty(safe(d.getValue().getNom())));
        colLieu.setCellValueFactory(d -> new SimpleStringProperty(safe(d.getValue().getLieu())));
        colType.setCellValueFactory(d -> new SimpleStringProperty(safe(d.getValue().getType())));
        colPrix.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().getPrix()).asObject());
        colDuree.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getDuree()).asObject());

        // ai_rating (si tu as getAiRating dans Activite)
        colRating.setCellValueFactory(d -> {
            try { return new SimpleDoubleProperty(d.getValue().getAiRating()).asObject(); }
            catch (Exception e) { return new SimpleDoubleProperty(0.0).asObject(); }
        });

        // formatters
        colPrix.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : String.format("%.0f DT", item));
            }
        });

        colRating.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : String.format("%.1f", item));
            }
        });

        reload();
        fillCombos();

        filtered = new FilteredList<>(master, a -> true);
        tableActivites.setItems(filtered);

        tfSearch.textProperty().addListener((o, a, b) -> applyFilters());
        cbType.setOnAction(e -> applyFilters());
        cbLieu.setOnAction(e -> applyFilters());
    }

    private void reload() {
        master.setAll(service.getAllActivites());
    }

    private void fillCombos() {
        cbType.getItems().clear();
        cbLieu.getItems().clear();

        cbType.getItems().add("Tous");
        cbLieu.getItems().add("Tous");

        master.stream().map(a -> safe(a.getType())).distinct().sorted()
                .filter(s -> !s.equals("-"))
                .forEach(cbType.getItems()::add);

        master.stream().map(a -> safe(a.getLieu())).distinct().sorted()
                .filter(s -> !s.equals("-"))
                .forEach(cbLieu.getItems()::add);

        cbType.setValue("Tous");
        cbLieu.setValue("Tous");
    }

    private void applyFilters() {
        String q = safe(tfSearch.getText()).toLowerCase();
        String type = safe(cbType.getValue());
        String lieu = safe(cbLieu.getValue());

        filtered.setPredicate(a -> {
            if (a == null) return false;

            boolean okText = q.isBlank()
                    || safe(a.getNom()).toLowerCase().contains(q)
                    || safe(a.getLieu()).toLowerCase().contains(q);

            boolean okType = type.equalsIgnoreCase("Tous") || safe(a.getType()).equalsIgnoreCase(type);
            boolean okLieu = lieu.equalsIgnoreCase("Tous") || safe(a.getLieu()).equalsIgnoreCase(lieu);

            return okText && okType && okLieu;
        });
    }

    @FXML
    private void onReset() {
        tfSearch.clear();
        cbType.setValue("Tous");
        cbLieu.setValue("Tous");
        applyFilters();
    }

    @FXML
    private void onAjouter() {
        switchScene("/ajout_activite.fxml", "/form_activite.css");
    }

    @FXML
    private void onModifier() {
        Activite selected = tableActivites.getSelectionModel().getSelectedItem();
        if (selected == null) {
            warn("Sélection requise", "Choisis une activité à modifier.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/modifier_activite.fxml"));
            Parent root = loader.load();

            ModifierActiviteController c = loader.getController();
            c.setActivite(selected);

            Stage stage = (Stage) tableActivites.getScene().getWindow();
            Scene scene = stage.getScene();
            if (scene == null) scene = new Scene(root);
            else scene.setRoot(root);

            scene.getStylesheets().clear();
            var css = getClass().getResource("/affichage.css");
            if (css != null) scene.getStylesheets().add(css.toExternalForm());

            stage.setScene(scene);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            error("Modification impossible", e.getMessage());
        }
    }

    @FXML
    private void onSupprimer() {
        Activite selected = tableActivites.getSelectionModel().getSelectedItem();
        if (selected == null) {
            warn("Sélection requise", "Choisis une activité à supprimer.");
            return;
        }

        Alert c = new Alert(Alert.AlertType.CONFIRMATION);
        c.setTitle("Confirmation");
        c.setHeaderText("Supprimer cette activité ?");
        c.setContentText("Activité : " + safe(selected.getNom()));

        var res = c.showAndWait();
        if (res.isPresent() && res.get() == ButtonType.OK) {
            service.deleteActivite(selected.getIdActivite());
            reload();
            fillCombos();
            applyFilters();
        }
    }

    @FXML
    private void goFrontOffice() {
        switchScene("/affichage_activites_front.fxml", "/affichage.css");
    }

    private void switchScene(String fxml, String cssPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();

            Stage stage = (Stage) tableActivites.getScene().getWindow();
            Scene scene = stage.getScene();
            if (scene == null) scene = new Scene(root);
            else scene.setRoot(root);

            scene.getStylesheets().clear();
            var css = getClass().getResource(cssPath);
            if (css != null) scene.getStylesheets().add(css.toExternalForm());

            stage.setScene(scene);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            error("FXML load failed", String.valueOf(e));
        }
    }

    private void warn(String h, String m) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle("Attention");
        a.setHeaderText(h);
        a.setContentText(m);
        a.showAndWait();
    }

    private void error(String h, String m) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Erreur");
        a.setHeaderText(h);
        a.setContentText(m);
        a.showAndWait();
    }

    private String safe(String s) {
        return (s == null || s.isBlank()) ? "" : s.trim();
    }
}