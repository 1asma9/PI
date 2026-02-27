package hebergement.controllers;

import hebergement.entities.Disponibilite;
import hebergement.entities.Hebergement;
import hebergement.entities.TypeHebergement;
import hebergement.services.DisponibiliteService;
import hebergement.services.HebergementService;
import hebergement.services.TypeHebergementService;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.util.Callback;

import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;

// ===== Excel (POI) =====
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

// ===== PDF (OpenPDF) =====
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
public class ShowHebergementController {

    private final HebergementService hs = new HebergementService();
    private final DisponibiliteService ds = new DisponibiliteService();
    private final TypeHebergementService ts = new TypeHebergementService();

    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // ✅ DATA for filters
    private final ObservableList<Hebergement> masterHeb = FXCollections.observableArrayList();
    private FilteredList<Hebergement> filteredHeb;

    // ===== filters UI
    @FXML private TextField tfSearch;
    @FXML private ComboBox<TypeHebergement> cbTypeFilter;
    @FXML private TextField tfMinPrix;
    @FXML private TextField tfMaxPrix;

    // Hébergement table
    @FXML private TableView<Hebergement> tableHeb;
    @FXML private TableColumn<Hebergement, String> imgCol;
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

        // ✅ Image path
        imgCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getImagePath()));

        // ✅ Premium rounded image cell
        imgCol.setCellFactory(col -> new TableCell<Hebergement, String>() {
            private final ImageView iv = new ImageView();
            private final StackPane wrap = new StackPane(iv);

            {
                iv.setFitWidth(64);
                iv.setFitHeight(44);
                iv.setPreserveRatio(false);
                iv.setSmooth(true);

                wrap.getStyleClass().add("tableImgWrap");
                iv.getStyleClass().add("tableImg");
                wrap.setAlignment(Pos.CENTER);
            }

            @Override
            protected void updateItem(String path, boolean empty) {
                super.updateItem(path, empty);

                if (empty || path == null || path.isBlank()) {
                    setGraphic(null);
                    setText(null);
                    return;
                }

                try {
                    iv.setImage(new Image("file:" + path, 64, 44, false, true));
                    setGraphic(wrap);
                    setText(null);
                } catch (Exception e) {
                    setGraphic(null);
                    setText("—");
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

        // ✅ sélection hébergement -> charge dispos
        tableHeb.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            if (selected != null) loadDispos(selected.getId());
        });

        // ✅ setup filters pipeline
        filteredHeb = new FilteredList<>(masterHeb, h -> true);
        SortedList<Hebergement> sorted = new SortedList<>(filteredHeb);
        sorted.comparatorProperty().bind(tableHeb.comparatorProperty());
        tableHeb.setItems(sorted);

        // ✅ types filter
        try {
            cbTypeFilter.setItems(FXCollections.observableArrayList(ts.getData()));
            cbTypeFilter.setCellFactory(lv -> new ListCell<>() {
                @Override protected void updateItem(TypeHebergement item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? "" : item.getLibelle());
                }
            });
            cbTypeFilter.setButtonCell(new ListCell<>() {
                @Override protected void updateItem(TypeHebergement item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? "" : item.getLibelle());
                }
            });
        } catch (Exception ignored) {}

        // listeners => re-filter
        if (tfSearch != null) tfSearch.textProperty().addListener((obs, o, n) -> applyFilters());
        if (cbTypeFilter != null) cbTypeFilter.valueProperty().addListener((obs, o, n) -> applyFilters());
        if (tfMinPrix != null) tfMinPrix.textProperty().addListener((obs, o, n) -> applyFilters());
        if (tfMaxPrix != null) tfMaxPrix.textProperty().addListener((obs, o, n) -> applyFilters());

        refresh();
    }

    private void refresh() {
        try {
            List<Hebergement> list = hs.getData();
            masterHeb.setAll(list); // ✅ important (for filters)
            lblHebInfo.setText("Total: " + list.size());

            tableDispo.setItems(FXCollections.observableArrayList());
            lblDispoInfo.setText("Sélectionne un hébergement.");

            applyFilters();
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

    // ===================== FILTERS =====================

    @FXML
    private void resetFilters() {
        if (tfSearch != null) tfSearch.clear();
        if (cbTypeFilter != null) cbTypeFilter.getSelectionModel().clearSelection();
        if (tfMinPrix != null) tfMinPrix.clear();
        if (tfMaxPrix != null) tfMaxPrix.clear();
        applyFilters();
    }

    private void applyFilters() {
        String q = tfSearch == null || tfSearch.getText() == null ? "" : tfSearch.getText().trim().toLowerCase();
        TypeHebergement type = cbTypeFilter == null ? null : cbTypeFilter.getValue();

        Double min = parseDoubleOrNull(tfMinPrix == null ? null : tfMinPrix.getText());
        Double max = parseDoubleOrNull(tfMaxPrix == null ? null : tfMaxPrix.getText());

        filteredHeb.setPredicate(h -> {
            if (h == null) return false;

            boolean matchText = q.isEmpty()
                    || (h.getDescription() != null && h.getDescription().toLowerCase().contains(q))
                    || (h.getAdresse() != null && h.getAdresse().toLowerCase().contains(q));

            boolean matchType = true;
            if (type != null) matchType = (h.getTypeId() == type.getId());

            boolean matchPrix = true;
            if (min != null) matchPrix = h.getPrix() >= min;
            if (max != null) matchPrix = matchPrix && h.getPrix() <= max;

            return matchText && matchType && matchPrix;
        });

        lblHebInfo.setText("Affichés: " + filteredHeb.size() + " / Total: " + masterHeb.size());
    }

    private Double parseDoubleOrNull(String s) {
        if (s == null) return null;
        s = s.trim();
        if (s.isEmpty()) return null;
        try { return Double.parseDouble(s); }
        catch (Exception e) { return null; }
    }

    // ===================== EXPORT HEBERGEMENTS =====================

    @FXML
    private void exportHebExcel() {
        File file = chooseSaveFile("Exporter Hébergements (Excel)", "hebergements.xlsx", "Excel", "*.xlsx");
        if (file == null) return;

        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Hebergements");

            int r = 0;
            org.apache.poi.ss.usermodel.Row header = sheet.createRow(r++);
            header.createCell(0).setCellValue("ID");
            header.createCell(1).setCellValue("Description");
            header.createCell(2).setCellValue("Adresse");
            header.createCell(3).setCellValue("Prix");
            header.createCell(4).setCellValue("TypeId");
            header.createCell(5).setCellValue("ImagePath");

            for (Hebergement h : filteredHeb) {
                org.apache.poi.ss.usermodel.Row row = sheet.createRow(r++);
                row.createCell(0).setCellValue(h.getId());
                row.createCell(1).setCellValue(nvl(h.getDescription()));
                row.createCell(2).setCellValue(nvl(h.getAdresse()));
                row.createCell(3).setCellValue(h.getPrix());
                row.createCell(4).setCellValue(h.getTypeId());
                row.createCell(5).setCellValue(nvl(h.getImagePath()));
            }

            for (int i = 0; i <= 5; i++) sheet.autoSizeColumn(i);

            try (FileOutputStream out = new FileOutputStream(file)) {
                wb.write(out);
            }

            showInfo("Export Excel", "Fichier généré : " + file.getAbsolutePath());
        } catch (Exception e) {
            showError("Export Excel", e.getMessage());
        }
    }

    @FXML
    private void exportHebPdf() {
        File file = chooseSaveFile("Exporter Hébergements (PDF)", "hebergements.pdf", "PDF", "*.pdf");
        if (file == null) return;

        try {
            Document doc = new Document(PageSize.A4.rotate(), 24, 24, 24, 24);
            PdfWriter.getInstance(doc, new FileOutputStream(file));
            doc.open();

            com.lowagie.text.Font titleFont =
                    new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 16, com.lowagie.text.Font.BOLD);

            Paragraph title = new Paragraph("Liste des Hébergements", titleFont);
            title.setAlignment(Element.ALIGN_LEFT);
            doc.add(title);
            doc.add(new Paragraph(" "));

            PdfPTable t = new PdfPTable(6);
            t.setWidthPercentage(100);
            t.setWidths(new float[]{1.1f, 3.2f, 3.2f, 1.3f, 1.1f, 3.2f});

            addPdfHeader(t, "ID");
            addPdfHeader(t, "Description");
            addPdfHeader(t, "Adresse");
            addPdfHeader(t, "Prix");
            addPdfHeader(t, "TypeId");
            addPdfHeader(t, "ImagePath");

            for (Hebergement h : filteredHeb) {
                t.addCell(String.valueOf(h.getId()));
                t.addCell(nvl(h.getDescription()));
                t.addCell(nvl(h.getAdresse()));
                t.addCell(String.valueOf(h.getPrix()));
                t.addCell(String.valueOf(h.getTypeId()));
                t.addCell(nvl(h.getImagePath()));
            }

            doc.add(t);
            doc.close();

            showInfo("Export PDF", "Fichier généré : " + file.getAbsolutePath());
        } catch (Exception e) {
            showError("Export PDF", e.getMessage());
        }
    }

    private File chooseSaveFile(String title, String defaultName, String desc, String pattern) {
        FileChooser fc = new FileChooser();
        fc.setTitle(title);
        fc.setInitialFileName(defaultName);
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter(desc + " (" + pattern + ")", pattern));
        return fc.showSaveDialog(tableHeb.getScene().getWindow());
    }

    private void addPdfHeader(PdfPTable t, String text) {
        PdfPCell c = new PdfPCell(new Phrase(text));
        c.setBackgroundColor(new java.awt.Color(243, 236, 226));
        c.setPadding(6);
        t.addCell(c);
    }

    private String nvl(String s) { return s == null ? "" : s; }

    // ===================== ACTIONS HEBERGEMENT =====================

    private Callback<TableColumn<Hebergement, Void>, TableCell<Hebergement, Void>> getHebActions() {
        return col -> new TableCell<>() {
            private final Button btnEdit = new Button("✏");
            private final Button btnDel  = new Button("🗑");
            private final HBox box = new HBox(8, btnEdit, btnDel);

            {
                btnEdit.getStyleClass().setAll("iconBtn", "btnEdit");
                btnDel.getStyleClass().setAll("iconBtn", "btnDelete");
                box.setAlignment(Pos.CENTER);

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
                setText(null);
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
        TextField tfAdr  = new TextField(h.getAdresse());
        TextField tfPrix = new TextField(String.valueOf(h.getPrix()));

        tfDesc.getStyleClass().add("input");
        tfAdr.getStyleClass().add("input");
        tfPrix.getStyleClass().add("input");

        ComboBox<TypeHebergement> cbType = new ComboBox<>();
        cbType.getStyleClass().add("input");

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
        saveBtn.getStyleClass().add("btnGold");

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
            try { prix = Double.parseDouble(prixS); }
            catch (Exception ex) {
                showInfo("Validation", "Prix invalide.");
                ev.consume(); return;
            }

            if (prix <= 0) {
                showInfo("Validation", "Prix doit être > 0.");
                ev.consume(); return;
            }

            try {
                Hebergement upd = new Hebergement(desc, adr, prix, type.getId());
                upd.setImagePath(h.getImagePath());
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
            private final Button btnEdit = new Button("✏");
            private final Button btnDel  = new Button("🗑");
            private final HBox box = new HBox(8, btnEdit, btnDel);

            {
                btnEdit.getStyleClass().setAll("iconBtn", "btnEdit");
                btnDel.getStyleClass().setAll("iconBtn", "btnDelete");
                box.setAlignment(Pos.CENTER);

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
                setText(null);
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
        DatePicker dpFin   = new DatePicker(d.getDateFin());
        CheckBox chk = new CheckBox("Disponible");
        chk.setSelected(d.isDisponible());

        dpDebut.getStyleClass().add("input");
        dpFin.getStyleClass().add("input");

        GridPane gp = new GridPane();
        gp.setHgap(10); gp.setVgap(10);
        gp.addRow(0, new Label("Début:"), dpDebut);
        gp.addRow(1, new Label("Fin:"), dpFin);
        gp.addRow(2, new Label(""), chk);

        dialog.getDialogPane().setContent(gp);

        Button saveBtn = (Button) dialog.getDialogPane().lookupButton(BTN_SAVE);
        saveBtn.getStyleClass().add("btnGold");

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
                Disponibilite upd = new Disponibilite(d.getHebergementId(), debut, fin, chk.isSelected());
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