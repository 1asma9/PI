package edu.destination.controllers;

import edu.destination.entities.Destination;
import edu.destination.entities.Voyage;
import edu.destination.services.DestinationService;
import edu.destination.services.VoyageService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.List;

public class AdminVoyageFormController {

    @FXML private TextField txtPointDepart, txtPointArrivee;
    @FXML private TextField txtPrix;
    @FXML private DatePicker dateDepart, dateArrivee;
    @FXML private ComboBox<Destination> comboDestination;
    @FXML private CheckBox chkPaid;
    @FXML private Button btnSave, btnCancel;

    @FXML private Label errPointDepart, errPointArrivee;
    @FXML private Label errPrix, errDateDepart, errDateArrivee, errDestination;

    private final VoyageService service = new VoyageService();
    private final DestinationService destinationService = new DestinationService();
    private Voyage voyage;

    public void setVoyage(Voyage voyage) {
        this.voyage = voyage;
        if (voyage != null) {
            txtPointDepart.setText(voyage.getPointDepart());
            txtPointArrivee.setText(voyage.getPointArrivee());
            txtPrix.setText(String.valueOf(voyage.getPrix()));
            if (voyage.getDateDepart() != null) dateDepart.setValue(voyage.getDateDepart());
            if (voyage.getDateArrivee() != null) dateArrivee.setValue(voyage.getDateArrivee());
            chkPaid.setSelected(voyage.getPaid() == 1);

            for (Destination d : comboDestination.getItems()) {
                if (d.getId() == voyage.getDestinationId()) {
                    comboDestination.getSelectionModel().select(d);
                    break;
                }
            }
        }
    }

    @FXML
    private void initialize() {
        List<Destination> destinations = destinationService.getData();
        comboDestination.getItems().addAll(destinations);

        btnSave.setOnAction(e -> save());
        btnCancel.setOnAction(e -> closeWindow());
    }

    private void save() {
        clearErrors();
        if (!validateFields()) return;

        try {
            if (voyage == null) voyage = new Voyage();

            voyage.setPointDepart(txtPointDepart.getText().trim());
            voyage.setPointArrivee(txtPointArrivee.getText().trim());
            voyage.setPrix(Double.parseDouble(txtPrix.getText().trim()));
            voyage.setDateDepart(dateDepart.getValue());
            voyage.setDateArrivee(dateArrivee.getValue());
            voyage.setPaid(chkPaid.isSelected() ? 1 : 0);

            Destination selected = comboDestination.getSelectionModel().getSelectedItem();
            voyage.setDestinationId(selected.getId());

            if (voyage.getId() == 0)
                service.addEntity(voyage);
            else
                service.update(voyage.getId(), voyage);

            closeWindow();

        } catch (SQLException ex) {
            showAlert("Erreur base de données", ex.getMessage());
        } catch (Exception ex) {
            showAlert("Erreur", ex.getMessage());
        }
    }

    private boolean validateFields() {
        boolean isValid = true;

        if (txtPointDepart.getText().trim().isEmpty()) {
            setError(txtPointDepart, errPointDepart, "Champ obligatoire");
            isValid = false;
        }

        if (txtPointArrivee.getText().trim().isEmpty()) {
            setError(txtPointArrivee, errPointArrivee, "Champ obligatoire");
            isValid = false;
        }

        try {
            double prix = Double.parseDouble(txtPrix.getText().trim());
            if (prix < 0) { setError(txtPrix, errPrix, "Doit être positif"); isValid = false; }
        } catch (NumberFormatException e) {
            setError(txtPrix, errPrix, "Nombre invalide");
            isValid = false;
        }

        if (dateDepart.getValue() == null) {
            errDateDepart.setText("Date obligatoire");
            isValid = false;
        }

        if (dateArrivee.getValue() == null) {
            errDateArrivee.setText("Date obligatoire");
            isValid = false;
        }

        if (dateDepart.getValue() != null && dateArrivee.getValue() != null) {
            if (dateArrivee.getValue().isBefore(dateDepart.getValue())) {
                errDateArrivee.setText("Doit être après le départ");
                isValid = false;
            }
        }

        if (comboDestination.getSelectionModel().getSelectedItem() == null) {
            errDestination.setText("Veuillez choisir une destination");
            isValid = false;
        }

        return isValid;
    }

    private void setError(Control field, Label label, String message) {
        if (field != null && !field.getStyleClass().contains("input-error"))
            field.getStyleClass().add("input-error");
        if (label != null) label.setText(message);
    }

    private void clearErrors() {
        Control[] fields = { txtPointDepart, txtPointArrivee, txtPrix, dateDepart, dateArrivee };
        Label[] labels = { errPointDepart, errPointArrivee, errPrix, errDateDepart, errDateArrivee };
        for (Control c : fields) if (c != null) c.getStyleClass().remove("input-error");
        for (Label l : labels) if (l != null) l.setText("");
        if (errDestination != null) errDestination.setText("");
    }

    private void closeWindow() {
        ((Stage) btnCancel.getScene().getWindow()).close();
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
    public void setDestinationId(int destinationId) {
        // Sélectionne automatiquement la destination dans le ComboBox
        for (Destination d : comboDestination.getItems()) {
            if (d.getId() == destinationId) {
                comboDestination.getSelectionModel().select(d);
                break;
            }
        }
        // Désactive le ComboBox pour qu'il ne soit pas modifiable
        comboDestination.setDisable(true);
    }
}