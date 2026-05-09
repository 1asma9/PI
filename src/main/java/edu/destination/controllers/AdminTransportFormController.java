package edu.destination.controllers;

import edu.destination.entities.Transport;
import edu.destination.entities.Voyage;
import edu.destination.services.TransportService;
import edu.destination.services.VoyageService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.List;

public class AdminTransportFormController {

    @FXML private ComboBox<String> txtTypeTransport;
    @FXML private ComboBox<Voyage> comboVoyage;  // ← Voyage au lieu de Destination
    @FXML private Button btnSave, btnCancel;
    @FXML private Label errType, errVoyage;

    private final TransportService transportService = new TransportService();
    private final VoyageService voyageService = new VoyageService();
    private Transport transport;

    public void setTransport(Transport transport) {
        this.transport = transport;
        if (transport != null) {
            txtTypeTransport.setValue(transport.getTypeTransport());
            for (Voyage v : comboVoyage.getItems()) {
                if (v.getId() == transport.getVoyageId()) {
                    comboVoyage.getSelectionModel().select(v);
                    break;
                }
            }
        }
    }

    @FXML
    private void initialize() {
        txtTypeTransport.getItems().addAll("Voiture", "Avion", "Train", "Bus");
        txtTypeTransport.setPromptText("Choisir un transport");

        List<Voyage> voyages = voyageService.getData();
        comboVoyage.getItems().addAll(voyages);

        btnSave.setOnAction(e -> save());
        btnCancel.setOnAction(e -> ((Stage) btnCancel.getScene().getWindow()).close());
    }

    private void save() {
        clearErrors();
        if (!validateFields()) return;

        try {
            if (transport == null) transport = new Transport();

            transport.setTypeTransport(txtTypeTransport.getValue());

            Voyage selectedVoyage = comboVoyage.getSelectionModel().getSelectedItem();
            transport.setVoyageId(selectedVoyage.getId());

            if (transport.getId() == 0)
                transportService.addEntity(transport);
            else
                transportService.update(transport.getId(), transport);

            ((Stage) btnSave.getScene().getWindow()).close();

        } catch (SQLException ex) {
            showAlert("Erreur base de données", ex.getMessage());
        }
    }

    private boolean validateFields() {
        boolean isValid = true;

        if (txtTypeTransport.getValue() == null || txtTypeTransport.getValue().isEmpty()) {
            setError(errType, "Veuillez choisir un type de transport");
            isValid = false;
        }

        if (comboVoyage.getSelectionModel().getSelectedItem() == null) {
            setError(errVoyage, "Veuillez choisir un voyage");
            isValid = false;
        }

        return isValid;
    }

    private void setError(Label label, String message) {
        if (label != null) label.setText(message);
    }

    private void clearErrors() {
        if (errType != null) errType.setText("");
        if (errVoyage != null) errVoyage.setText("");
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
    public void setVoyageId(int voyageId) {
        for (Voyage v : comboVoyage.getItems()) {
            if (v.getId() == voyageId) {
                comboVoyage.getSelectionModel().select(v);
                break;
            }
        }
        comboVoyage.setDisable(true);
    }
}