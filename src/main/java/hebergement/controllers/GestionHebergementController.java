package hebergement.controllers;

import hebergement.entities.Hebergement;
import hebergement.services.ChambreService;
import hebergement.services.HebergementService;
import javafx.collections.*;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Rectangle;

import java.util.Comparator;
import java.util.List;

public class GestionHebergementController {

    @FXML private TextField tfSearch;
    @FXML private ComboBox<String> cbType, cbTri, cbOrdre;
    @FXML private TableView<Hebergement> tableView;
    @FXML private TableColumn<Hebergement, Integer> colId;
    @FXML private TableColumn<Hebergement, String>  colImage, colDescription,
            colType, colChambres;
    @FXML private TableColumn<Hebergement, Double>  colPrix;
    @FXML private TableColumn<Hebergement, Void>    colActions;
    @FXML private Label lblTotal;

    private final HebergementService hs = new HebergementService();
    private final ChambreService     cs = new ChambreService();
    private ObservableList<Hebergement> allData = FXCollections.observableArrayList();
    private FilteredList<Hebergement>   filtered;

    @FXML
    public void initialize() {
        cbType.setItems(FXCollections.observableArrayList(
                "Tous", "hotel", "villa", "appartement"
        ));
        cbType.setValue("Tous");

        cbTri.setItems(FXCollections.observableArrayList("ID", "Prix"));
        cbTri.setValue("ID");

        cbOrdre.setItems(FXCollections.observableArrayList("▲ Croissant", "▼ Décroissant"));
        cbOrdre.setValue("▲ Croissant");

        setupColumns();
        loadData();

        tfSearch.textProperty().addListener((o, ov, nv) -> filterTable());
        cbType.valueProperty().addListener((o, ov, nv)  -> filterTable());
        cbTri.valueProperty().addListener((o, ov, nv)   -> filterTable());
        cbOrdre.valueProperty().addListener((o, ov, nv) -> filterTable());
    }

    private void setupColumns() {
        // Colonne ID
        colId.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null); setText(null); return;
                }
                int id = ((Hebergement) getTableRow().getItem()).getId();
                Label badge = new Label("#" + id);
                badge.setStyle("-fx-background-color:#1e1e2a; -fx-text-fill:#818cf8;" +
                        "-fx-background-radius:6; -fx-padding:3 9; -fx-font-size:12px; -fx-font-weight:700;");
                setGraphic(badge); setText(null);
                setStyle("-fx-alignment:CENTER;");
            }
        });

        // Colonne Image
        colImage.setCellFactory(col -> new TableCell<>() {
            private final ImageView iv = new ImageView();
            {
                iv.setFitWidth(64); iv.setFitHeight(50);
                Rectangle clip = new Rectangle(64, 50);
                clip.setArcWidth(8); clip.setArcHeight(8);
                iv.setClip(clip);
            }
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null); return;
                }
                Hebergement h = (Hebergement) getTableRow().getItem();
                if (h.getImagePath() != null && !h.getImagePath().isBlank()) {
                    try {
                        String url = h.getImagePath().startsWith("http") ?
                                h.getImagePath() : "file:///" + h.getImagePath().replace("\\", "/");
                        iv.setImage(new Image(url, true));
                        setGraphic(iv);
                    } catch (Exception e) { setGraphic(fallbackLabel()); }
                } else { setGraphic(fallbackLabel()); }
                setStyle("-fx-alignment:CENTER;");
            }
            private Label fallbackLabel() {
                Label l = new Label("🏨");
                l.setStyle("-fx-background-color:#1e1e2a; -fx-background-radius:8;" +
                        "-fx-padding:8 12; -fx-font-size:20px;");
                return l;
            }
        });

        // Colonne Description
        colDescription.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(d.getValue().getDescription()));
        colDescription.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); return; }
                String txt = item.length() > 45 ? item.substring(0, 45) + "…" : item;
                setText(txt);
                setStyle("-fx-text-fill:#e2e8f0; -fx-font-size:13px; -fx-font-weight:600;");
            }
        });

        // Colonne Prix
        colPrix.setCellValueFactory(d ->
                new javafx.beans.property.SimpleDoubleProperty(d.getValue().getPrix()).asObject());
        colPrix.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); return; }
                setText(String.format("%.2f TND", item));
                setStyle("-fx-text-fill:#4ade80; -fx-font-size:13px; -fx-font-weight:700; -fx-alignment:CENTER;");
            }
        });

        // Colonne Type
        colType.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(d.getValue().getTypeLibelle()));
        colType.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); setText(null); return; }
                Label badge = new Label(item);
                badge.setStyle("-fx-background-color:#2e2050; -fx-text-fill:#a78bfa;" +
                        "-fx-background-radius:6; -fx-padding:3 9; -fx-font-size:12px; -fx-font-weight:600;");
                setGraphic(badge); setText(null);
                setStyle("-fx-alignment:CENTER;");
            }
        });

        // Colonne Chambres
        colChambres.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null); return;
                }
                Hebergement h = (Hebergement) getTableRow().getItem();
                try {
                    int count = cs.getByHebergement(h.getId()).size();
                    if (count > 0) {
                        Label badge = new Label(count + " chambre" + (count > 1 ? "s" : ""));
                        badge.setStyle("-fx-background-color:#1e1e2a; -fx-text-fill:#7dd3fc;" +
                                "-fx-border-color:#0e2a4a; -fx-border-radius:6; -fx-background-radius:6;" +
                                "-fx-padding:3 9; -fx-font-size:11px; -fx-font-weight:600;");
                        setGraphic(badge);
                    } else {
                        Label dash = new Label("—");
                        dash.setStyle("-fx-text-fill:#4b5563; -fx-font-size:12px;");
                        setGraphic(dash);
                    }
                } catch (Exception e) { setGraphic(null); }
                setStyle("-fx-alignment:CENTER;");
            }
        });

        // Colonne Actions
        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button btnVoir     = new Button("👁 Voir");
            private final Button btnModifier = new Button("✏️ Modifier");
            private final Button btnChambre  = new Button("+ Chambre");
            private final Button btnSuppr    = new Button("Supprimer");
            private final HBox box = new HBox(6, btnVoir, btnModifier, btnChambre, btnSuppr);
            {
                box.setStyle("-fx-alignment:CENTER;");
                btnVoir.setStyle(
                        "-fx-background-color:#1a1a28; -fx-text-fill:#818cf8;" +
                                "-fx-border-color:#2e2e50; -fx-border-radius:6; -fx-background-radius:6;" +
                                "-fx-font-size:11px; -fx-cursor:hand; -fx-padding:4 10;");
                btnModifier.setStyle(
                        "-fx-background-color:#1a1a28; -fx-text-fill:#fbbf24;" +
                                "-fx-border-color:#4a3010; -fx-border-radius:6; -fx-background-radius:6;" +
                                "-fx-font-size:11px; -fx-cursor:hand; -fx-padding:4 10;");
                btnChambre.setStyle(
                        "-fx-background-color:#1a1a28; -fx-text-fill:#4ade80;" +
                                "-fx-border-color:#1a3d28; -fx-border-radius:6; -fx-background-radius:6;" +
                                "-fx-font-size:11px; -fx-cursor:hand; -fx-padding:4 10;");
                btnSuppr.setStyle(
                        "-fx-background-color:#1a1a28; -fx-text-fill:#f87171;" +
                                "-fx-border-color:#3d1f1f; -fx-border-radius:6; -fx-background-radius:6;" +
                                "-fx-font-size:11px; -fx-cursor:hand; -fx-padding:4 10;");

                btnVoir.setOnAction(e -> {
                    if (getIndex() < getTableView().getItems().size())
                        voirHebergement(getTableView().getItems().get(getIndex()));
                });
                btnModifier.setOnAction(e -> {
                    if (getIndex() < getTableView().getItems().size())
                        modifierHebergement(getTableView().getItems().get(getIndex()));
                });
                btnChambre.setOnAction(e -> openAddChambre());
                btnSuppr.setOnAction(e -> {
                    if (getIndex() < getTableView().getItems().size())
                        supprimerHebergement(getTableView().getItems().get(getIndex()));
                });
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    private void loadData() {
        try {
            allData.setAll(hs.getData());
            filtered = new FilteredList<>(allData, h -> true);
            SortedList<Hebergement> sorted = new SortedList<>(filtered);
            sorted.comparatorProperty().bind(tableView.comparatorProperty());
            tableView.setItems(sorted);
            updateTotal();
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void filterTable() {
        if (filtered == null) return;
        String q    = tfSearch.getText().toLowerCase();
        String type = cbType.getValue();

        filtered.setPredicate(h -> {
            boolean matchText = q.isEmpty() ||
                    (h.getDescription() != null && h.getDescription().toLowerCase().contains(q)) ||
                    (h.getTypeLibelle() != null && h.getTypeLibelle().toLowerCase().contains(q));
            boolean matchType = type == null || type.equals("Tous") ||
                    (h.getTypeLibelle() != null && h.getTypeLibelle().equalsIgnoreCase(type));
            return matchText && matchType;
        });

        String tri   = cbTri.getValue();
        String ordre = cbOrdre.getValue();
        List<Hebergement> list = new java.util.ArrayList<>(filtered);
        Comparator<Hebergement> comp = "Prix".equals(tri) ?
                Comparator.comparingDouble(Hebergement::getPrix) :
                Comparator.comparingInt(Hebergement::getId);
        if ("▼ Décroissant".equals(ordre)) comp = comp.reversed();
        list.sort(comp);
        tableView.setItems(FXCollections.observableArrayList(list));
        updateTotal();
    }

    @FXML
    private void resetFilters() {
        tfSearch.clear();
        cbType.setValue("Tous");
        cbTri.setValue("ID");
        cbOrdre.setValue("▲ Croissant");
        filtered.setPredicate(h -> true);
        tableView.setItems(new SortedList<>(filtered));
        updateTotal();
    }

    private void updateTotal() {
        int n = tableView.getItems().size();
        lblTotal.setText(n + " résultat" + (n > 1 ? "s" : ""));
    }

    // ===== ACTIONS =====

    @FXML private void openAdd() {
        AdminLayoutController layout = AdminLayoutController.getInstance();
        if (layout != null) layout.loadPage("/app/add.fxml", "Ajouter Hébergement");
    }

    @FXML private void openAddChambre() {
        AdminLayoutController layout = AdminLayoutController.getInstance();
        if (layout != null) layout.loadPage("/app/AddChambre.fxml", "Ajouter Chambre");
    }

    @FXML private void openAddType() {
        showInfo("Type", "Fonctionnalité à venir...");
    }

    @FXML private void openEdit() {
        Hebergement sel = tableView.getSelectionModel().getSelectedItem();
        if (sel == null) { showInfo("Info", "Sélectionne un hébergement."); return; }
        modifierHebergement(sel);
    }

    @FXML private void deleteSelected() {
        Hebergement sel = tableView.getSelectionModel().getSelectedItem();
        if (sel == null) { showInfo("Info", "Sélectionne un hébergement."); return; }
        supprimerHebergement(sel);
    }

    @FXML private void openStats() {
        showInfo("Statistiques", "Fonctionnalité à venir...");
    }

    private void voirHebergement(Hebergement h) {
        showInfo("Hébergement #" + h.getId(),
                "Description : " + h.getDescription() +
                        "\nAdresse : " + h.getAdresse() +
                        "\nPrix : " + h.getPrix() + " TND");
    }

    // ✅ MODIFIER — charge add.fxml et passe l'hébergement au controller
    private void modifierHebergement(Hebergement h) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/app/add.fxml"));
            Parent root = loader.load();

            // ✅ Passer l'hébergement sélectionné
            AddHebergementController controller = loader.getController();
            controller.setHebergementToEdit(h);

            AdminLayoutController layout = AdminLayoutController.getInstance();
            if (layout != null) layout.loadPageWithRoot(root, "Modifier Hébergement");

        } catch (Exception e) {
            e.printStackTrace();
            showError("Impossible d'ouvrir le formulaire : " + e.getMessage());
        }
    }

    private void supprimerHebergement(Hebergement h) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer \"" + h.getDescription() + "\" ?",
                ButtonType.YES, ButtonType.NO);
        alert.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                try {
                    hs.deleteEntity(h);
                    allData.remove(h);
                    updateTotal();
                } catch (Exception e) {
                    showError("Erreur suppression : " + e.getMessage());
                }
            }
        });
    }

    private void showInfo(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title); a.setHeaderText(null); a.setContentText(msg);
        a.showAndWait();
    }

    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Erreur"); a.setHeaderText(null); a.setContentText(msg);
        a.showAndWait();
    }
}