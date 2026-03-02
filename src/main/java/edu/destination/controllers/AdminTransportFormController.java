package edu.destination.controllers;

import edu.destination.entities.Destination;
import edu.destination.entities.Transport;
import edu.destination.services.DestinationService;
import edu.destination.services.TransportService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.List;

public class AdminTransportFormController {

    @FXML private ComboBox<String> txtTypeTransport;
    @FXML private ComboBox<Destination> comboDestination;
    @FXML private Button btnSave, btnCancel;

    @FXML private Label errType, errDestination;

    private TransportService transportService = new TransportService();
    private DestinationService destinationService = new DestinationService();

    private Transport transport;

    public void setTransport(Transport transport) {
        this.transport = transport;

        if (transport != null) {
            txtTypeTransport.setValue(transport.getTypeTransport());

            for (Destination d : comboDestination.getItems()) {
                if (d.getIdDestination() == transport.getIdDestination()) {
                    comboDestination.getSelectionModel().select(d);
                    break;
                }
            }
        }
    }

    @FXML
    private void initialize() {
        // Choix fixes pour le type de transport
        txtTypeTransport.getItems().addAll("Voiture", "Avion", "Train", "Vélo", "Piéton");
        txtTypeTransport.setPromptText("Choisir un transport");

        // Charger toutes les destinations
        List<Destination> destinations = destinationService.getData();
        comboDestination.getItems().addAll(destinations);

        btnSave.setOnAction(e -> save());
        btnCancel.setOnAction(e -> ((Stage) btnCancel.getScene().getWindow()).close());
    }

    private void save() {
        clearErrors();
        if (!validateFields()) return;

        try {
            if (transport == null) transport = new Transport();

            transport.setTypeTransport(txtTypeTransport.getValue());

            Destination selectedDest = comboDestination.getSelectionModel().getSelectedItem();
            transport.setIdDestination(selectedDest.getIdDestination());

            if (transport.getIdTransport() == 0)
                transportService.addEntity(transport);
            else
                transportService.update(transport.getIdTransport(), transport);

            ((Stage) btnSave.getScene().getWindow()).close();

        } catch (SQLException ex) {
            showAlert("Erreur base de données", ex.getMessage());
        }
    }

    private boolean validateFields() {
        boolean isValid = true;

        // Type transport
        if (txtTypeTransport.getValue() == null || txtTypeTransport.getValue().isEmpty()) {
            setError(errType, "Veuillez choisir un type de transport");
            isValid = false;
        }

        // Destination sélectionnée
        Destination selectedDest = comboDestination.getSelectionModel().getSelectedItem();
        if (selectedDest == null) {
            setError(errDestination, "Veuillez choisir une destination");
            isValid = false;
        } else {
            // Vérifier qu'aucun autre transport n'a déjà cette destination
            List<Transport> allTransports = transportService.getData();
            for (Transport t : allTransports) {
                boolean sameDestination = t.getIdDestination() == selectedDest.getIdDestination();
                boolean differentTransport = transport == null || t.getIdTransport() != transport.getIdTransport();

                if (sameDestination && differentTransport) {
                    setError(errDestination, "Cette destination a déjà un transport");
                    isValid = false;
                    break;
                }
            }
        }

        return isValid;
    }

    private void setError(Label label, String message) {
        if (label != null) label.setText(message);
    }

    private void clearErrors() {
        txtTypeTransport.setStyle("");
        errType.setText("");
        errDestination.setText("");
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}