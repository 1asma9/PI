package hebergement.controllers;

import hebergement.entities.Hebergement;
import hebergement.entities.Reservation;
import hebergement.services.HebergementService;
import hebergement.services.ReservationService;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.util.Callback;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReservationsAdminController {

    private final ReservationService rs = new ReservationService();
    private final HebergementService hs = new HebergementService();

    // ✅ Map (hebergementId -> imagePath) pour afficher l'image
    private final Map<Integer, String> hebImageMap = new HashMap<>();

    @FXML private TableView<Reservation> tableRes;

    @FXML private TableColumn<Reservation, Number> idCol;
    @FXML private TableColumn<Reservation, String> hebCol;     // ✅ on garde hebCol mais devient "Image"
    @FXML private TableColumn<Reservation, String> clientCol;
    @FXML private TableColumn<Reservation, String> datesCol;
    @FXML private TableColumn<Reservation, Number> totalCol;
    @FXML private TableColumn<Reservation, String> statutCol;
    @FXML private TableColumn<Reservation, Void> actionsCol;

    @FXML private ComboBox<Hebergement> cbHebergementFilter;
    @FXML private Label lblInfo;

    @FXML
    public void initialize() {

        // ✅ Fixe largeur (optionnel)
        tableRes.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        idCol.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getId()));
        clientCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getClientNom()));
        datesCol.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getDateDebut() + " → " + d.getValue().getDateFin()
        ));
        totalCol.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().getTotal()));
        statutCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStatut()));

        loadHebImageMap();

        hebCol.setCellValueFactory(d -> {
            int hebId = d.getValue().getHebergementId();
            String path = hebImageMap.get(hebId);
            return new SimpleStringProperty(path);
        });

        hebCol.setCellFactory(col -> new TableCell<>() {

            private final ImageView iv = new ImageView();

            {
                iv.setFitWidth(90);
                iv.setFitHeight(55);
                iv.setPreserveRatio(true);
                iv.setSmooth(true);
            }

            @Override
            protected void updateItem(String path, boolean empty) {
                super.updateItem(path, empty);

                if (empty) {
                    setGraphic(null);
                    setText(null);
                    return;
                }

                if (path == null || path.isBlank()) {
                    setGraphic(null);
                    setText("—");
                    return;
                }

                try {
                    Image img = new Image(new File(path).toURI().toString(), 90, 55, true, true);
                    iv.setImage(img);
                    setGraphic(iv);
                    setText(null);
                } catch (Exception e) {
                    setGraphic(null);
                    setText("Image ?");
                }
            }
        });

        actionsCol.setCellFactory(getActions());

        loadHebergementsFilter();
        loadAll();
    }

    private void loadHebImageMap() {
        hebImageMap.clear();
        try {
            List<Hebergement> list = hs.getData();
            for (Hebergement h : list) {
                hebImageMap.put(h.getId(), h.getImagePath());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadHebergementsFilter() {
        try {
            List<Hebergement> list = hs.getData();
            cbHebergementFilter.setItems(FXCollections.observableArrayList(list));

            cbHebergementFilter.setCellFactory(lv -> new ListCell<>() {
                @Override protected void updateItem(Hebergement item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? "" : ("#" + item.getId() + " - " + item.getDescription()));
                }
            });

            cbHebergementFilter.setButtonCell(new ListCell<>() {
                @Override protected void updateItem(Hebergement item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? "" : ("#" + item.getId() + " - " + item.getDescription()));
                }
            });

        } catch (Exception e) {
            showError("Erreur", "Impossible de charger les hébergements: " + e.getMessage());
        }
    }

    @FXML
    public void loadAll() {
        try {
            loadHebImageMap();

            List<Reservation> list = rs.getData();
            tableRes.setItems(FXCollections.observableArrayList(list));
            lblInfo.setText("Total: " + list.size());
        } catch (Exception e) {
            showError("Erreur", e.getMessage());
        }
    }

    @FXML
    public void filter() {
        Hebergement h = cbHebergementFilter.getValue();
        if (h == null) {
            showInfo("Filtre", "Choisir un hébergement.");
            return;
        }
        try {
            loadHebImageMap();

            List<Reservation> list = rs.getByHebergement(h.getId());
            tableRes.setItems(FXCollections.observableArrayList(list));
            lblInfo.setText("Total: " + list.size());
        } catch (Exception e) {
            showError("Erreur", e.getMessage());
        }
    }

    // ==================== Actions column ====================

    private Callback<TableColumn<Reservation, Void>, TableCell<Reservation, Void>> getActions() {
        return col -> new TableCell<>() {

            private final Button btnConfirm = new Button("Confirmer");
            private final Button btnCancel = new Button("Annuler");
            private final Button btnDelete = new Button("Supprimer");
            private final HBox box = new HBox(8, btnConfirm, btnCancel, btnDelete);

            {
                btnConfirm.getStyleClass().add("primary");
                btnCancel.getStyleClass().add("danger");
                btnDelete.getStyleClass().add("danger");

                btnConfirm.setOnAction(e -> {
                    Reservation r = getTableView().getItems().get(getIndex());
                    updateStatut(r, "CONFIRME");
                });

                btnCancel.setOnAction(e -> {
                    Reservation r = getTableView().getItems().get(getIndex());
                    updateStatut(r, "ANNULE");
                });

                btnDelete.setOnAction(e -> {
                    Reservation r = getTableView().getItems().get(getIndex());
                    deleteReservation(r);
                });
            }

            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        };
    }

    private void updateStatut(Reservation r, String statut) {
        if (!confirm("Confirmation", "Changer statut réservation #" + r.getId() + " → " + statut + " ?")) return;

        try {
            rs.updateStatus(r.getId(), statut);

            if (cbHebergementFilter.getValue() != null) filter();
            else loadAll();

        } catch (Exception e) {
            showError("Erreur", e.getMessage());
        }
    }

    private void deleteReservation(Reservation r) {
        if (!confirm("Confirmation", "Supprimer réservation #" + r.getId() + " ?")) return;

        try {
            rs.deleteEntity(r);

            if (cbHebergementFilter.getValue() != null) filter();
            else loadAll();

        } catch (Exception e) {
            showError("Erreur", e.getMessage());
        }
    }

    // ==================== Utils ====================

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

    private boolean confirm(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        return a.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }
}
