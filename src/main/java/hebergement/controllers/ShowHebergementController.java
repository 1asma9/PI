package hebergement.controllers;

import hebergement.entities.Disponibilite;
import hebergement.entities.Hebergement;
import hebergement.entities.TypeHebergement;
import hebergement.services.DisponibiliteService;
import hebergement.services.HebergementService;
import hebergement.services.TypeHebergementService;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.util.Callback;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ShowHebergementController {

    private final HebergementService hs = new HebergementService();
    private final DisponibiliteService ds = new DisponibiliteService();
    private final TypeHebergementService ts = new TypeHebergementService();

    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // Hébergement table
    @FXML private TableView<Hebergement> tableHeb;
    @FXML private TableColumn<Hebergement, String> imgCol;   // ✅ Image au lieu de ID
    @FXML private TableColumn<Hebergement, String> descCol;
    @FXML private TableColumn<Hebergement, String> adrCol;
    @FXML private TableColumn<Hebergement, Number> prixCol;
    @FXML private TableColumn<Hebergement, Void> actionsCol;
    @FXML private Label lblHebInfo;

    // Disponibilite table
    @FXML private TableView<Disponibilite> tableDispo;
    @FXML private TableColumn<Disponibilite, Number> dispoIdCol;
    @FXML private TableColumn<Disponibilite, String> debutCol;
    @FXML private TableColumn<Disponibilite, String> finCol;
    @FXML private TableColumn<Disponibilite, Boolean> disponibleCol;
    @FXML private TableColumn<Disponibilite, Void> dispoActionsCol;
    @FXML private Label lblDispoInfo;

    @FXML
    public void initialize() {
        tableHeb.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableDispo.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // ✅ Colonne Image (miniature)
        imgCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getImagePath()));
        imgCol.setCellFactory(col -> new TableCell<>() {

            private final ImageView iv = new ImageView();

            {
                iv.setFitWidth(70);
                iv.setFitHeight(45);
                iv.setPreserveRatio(true);
                iv.setSmooth(true);
            }

            @Override
            protected void updateItem(String path, boolean empty) {
                super.updateItem(path, empty);

                if (empty || path == null || path.isBlank()) {
                    setGraphic(null);
                } else {
                    try {
                        iv.setImage(new Image("file:" + path, 70, 45, true, true));
                        setGraphic(iv);
                    } catch (Exception e) {
                        setGraphic(null);
                    }
                }
            }
        });

        // colonnes Hébergement
        descCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDescription()));
        adrCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getAdresse()));
        prixCol.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().getPrix()));
        actionsCol.setCellFactory(getHebActions());

        // colonnes Disponibilite
        dispoIdCol.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getId()));
        debutCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDateDebut().format(fmt)));
        finCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDateFin().format(fmt)));
        disponibleCol.setCellValueFactory(d -> new SimpleBooleanProperty(d.getValue().isDisponible()).asObject());
        dispoActionsCol.setCellFactory(getDispoActions());

        // ✅ synchronisation : sélection hébergement -> charge dispos
        tableHeb.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            if (selected != null) loadDispos(selected.getId());
        });

        refresh();
    }

    private void refresh() {
        try {
            List<Hebergement> list = hs.getData();
            tableHeb.setItems(FXCollections.observableArrayList(list));
            lblHebInfo.setText("Total: " + list.size());

            tableDispo.setItems(FXCollections.observableArrayList());
            lblDispoInfo.setText("Sélectionne un hébergement.");

        } catch (Exception e) {
            showError("Erreur", e.getMessage());
        }
    }

    private void loadDispos(int hebId) {
        try {
            List<Disponibilite> list = ds.getByHebergement(hebId);
            tableDispo.setItems(FXCollections.observableArrayList(list));
            lblDispoInfo.setText("Total: " + list.size());
        } catch (Exception e) {
            showError("Erreur disponibilités", e.getMessage());
        }
    }

    // ===================== ACTIONS HEBERGEMENT =====================

    private Callback<TableColumn<Hebergement, Void>, TableCell<Hebergement, Void>> getHebActions() {
        return col -> new TableCell<>() {
            private final Button btnEdit = new Button("Modifier");
            private final Button btnDel = new Button("Supprimer");
            private final HBox box = new HBox(8, btnEdit, btnDel);

            {
                btnEdit.getStyleClass().add("primary");
                btnDel.getStyleClass().add("danger");

                btnEdit.setOnAction(e -> {
                    Hebergement h = getTableView().getItems().get(getIndex());
                    openEditHebPopup(h);
                });

                btnDel.setOnAction(e -> {
                    Hebergement h = getTableView().getItems().get(getIndex());
                    deleteHeb(h);
                });
            }

            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        };
    }

    private void deleteHeb(Hebergement h) {
        if (!confirm("Confirmation", "Supprimer hébergement ID=" + h.getId() + " ?")) return;

        try {
            hs.deleteEntity(h);
            refresh();
        } catch (Exception e) {
            showError("Suppression", e.getMessage());
        }
    }

    private void openEditHebPopup(Hebergement h) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Modifier Hébergement");

        ButtonType BTN_SAVE = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(BTN_SAVE, ButtonType.CANCEL);

        TextField tfDesc = new TextField(h.getDescription());
        TextField tfAdr = new TextField(h.getAdresse());
        TextField tfPrix = new TextField(String.valueOf(h.getPrix()));

        ComboBox<TypeHebergement> cbType = new ComboBox<>();
        try {
            cbType.setItems(FXCollections.observableArrayList(ts.getData()));
            TypeHebergement current = cbType.getItems().stream()
                    .filter(t -> t.getId() == h.getTypeId())
                    .findFirst().orElse(null);
            cbType.setValue(current);
        } catch (Exception ignored) {}

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

        GridPane gp = new GridPane();
        gp.setHgap(10); gp.setVgap(10);
        gp.addRow(0, new Label("Description:"), tfDesc);
        gp.addRow(1, new Label("Adresse:"), tfAdr);
        gp.addRow(2, new Label("Prix:"), tfPrix);
        gp.addRow(3, new Label("Type:"), cbType);

        dialog.getDialogPane().setContent(gp);

        Button saveBtn = (Button) dialog.getDialogPane().lookupButton(BTN_SAVE);
        saveBtn.addEventFilter(javafx.event.ActionEvent.ACTION, ev -> {

            String desc = tfDesc.getText().trim();
            String adr = tfAdr.getText().trim();
            String prixS = tfPrix.getText().trim();
            TypeHebergement type = cbType.getValue();

            if (desc.isEmpty() || adr.isEmpty() || prixS.isEmpty()) {
                showInfo("Validation", "Tous les champs sont obligatoires.");
                ev.consume(); return;
            }
            if (type == null) {
                showInfo("Validation", "Choisir un type.");
                ev.consume(); return;
            }

            double prix;
            try {
                prix = Double.parseDouble(prixS);
            } catch (Exception ex) {
                showInfo("Validation", "Prix invalide.");
                ev.consume(); return;
            }
            if (prix <= 0) {
                showInfo("Validation", "Prix doit être > 0.");
                ev.consume(); return;
            }

            try {
                // ✅ update + garder l'image (IMPORTANT)
                Hebergement upd = new Hebergement(desc, adr, prix, type.getId());
                upd.setImagePath(h.getImagePath()); // ✅ ne pas perdre image_path

                hs.update(h.getId(), upd);
                refresh();
            } catch (Exception ex) {
                showError("Erreur update", ex.getMessage());
                ev.consume();
            }
        });

        dialog.showAndWait();
    }

    // ===================== ACTIONS DISPONIBILITE =====================

    private Callback<TableColumn<Disponibilite, Void>, TableCell<Disponibilite, Void>> getDispoActions() {
        return col -> new TableCell<>() {
            private final Button btnEdit = new Button("Modifier");
            private final Button btnDel = new Button("Supprimer");
            private final HBox box = new HBox(8, btnEdit, btnDel);

            {
                btnEdit.getStyleClass().add("primary");
                btnDel.getStyleClass().add("danger");

                btnEdit.setOnAction(e -> {
                    Disponibilite d = getTableView().getItems().get(getIndex());
                    openEditDispoPopup(d);
                });

                btnDel.setOnAction(e -> {
                    Disponibilite d = getTableView().getItems().get(getIndex());
                    deleteDispo(d);
                });
            }

            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        };
    }

    private void deleteDispo(Disponibilite d) {
        if (!confirm("Confirmation", "Supprimer disponibilité ID=" + d.getId() + " ?")) return;

        try {
            ds.deleteEntity(d);
            Hebergement selected = tableHeb.getSelectionModel().getSelectedItem();
            if (selected != null) loadDispos(selected.getId());
        } catch (Exception e) {
            showError("Suppression", e.getMessage());
        }
    }

    private void openEditDispoPopup(Disponibilite d) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Modifier Disponibilité");

        ButtonType BTN_SAVE = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(BTN_SAVE, ButtonType.CANCEL);

        DatePicker dpDebut = new DatePicker(d.getDateDebut());
        DatePicker dpFin = new DatePicker(d.getDateFin());
        CheckBox chk = new CheckBox("Disponible");
        chk.setSelected(d.isDisponible());

        GridPane gp = new GridPane();
        gp.setHgap(10); gp.setVgap(10);
        gp.addRow(0, new Label("Début:"), dpDebut);
        gp.addRow(1, new Label("Fin:"), dpFin);
        gp.addRow(2, new Label(""), chk);

        dialog.getDialogPane().setContent(gp);

        Button saveBtn = (Button) dialog.getDialogPane().lookupButton(BTN_SAVE);
        saveBtn.addEventFilter(javafx.event.ActionEvent.ACTION, ev -> {

            LocalDate debut = dpDebut.getValue();
            LocalDate fin = dpFin.getValue();

            if (debut == null || fin == null) {
                showInfo("Validation", "Choisir début et fin.");
                ev.consume(); return;
            }
            if (fin.isBefore(debut)) {
                showInfo("Validation", "Date fin doit être >= date début.");
                ev.consume(); return;
            }

            try {
                Disponibilite upd = new Disponibilite(
                        d.getHebergementId(),
                        debut,
                        fin,
                        chk.isSelected()
                );
                ds.update(d.getId(), upd);

                Hebergement selected = tableHeb.getSelectionModel().getSelectedItem();
                if (selected != null) loadDispos(selected.getId());

            } catch (Exception ex) {
                showError("Erreur update", ex.getMessage());
                ev.consume();
            }
        });

        dialog.showAndWait();
    }

    // ===================== UTILITAIRES =====================

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
