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
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.util.Callback;

import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// ===== Excel (Apache POI) =====
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

// ===== PDF (OpenPDF) =====
// ⚠️ IMPORTANT: on n'importe PAS com.lowagie.text.Font / TextField etc.
// pour éviter les conflits avec JavaFX (TextField) et POI (Font).
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

public class ReservationsAdminController {

    private final ReservationService rs = new ReservationService();
    private final HebergementService hs = new HebergementService();

    // (hebergementId -> imagePath)
    private final Map<Integer, String> hebImageMap = new HashMap<>();

    @FXML private TableView<Reservation> tableRes;

    @FXML private TableColumn<Reservation, Number> idCol;
    @FXML private TableColumn<Reservation, String> hebCol;     // image path
    @FXML private TableColumn<Reservation, String> clientCol;
    @FXML private TableColumn<Reservation, String> datesCol;
    @FXML private TableColumn<Reservation, Number> totalCol;
    @FXML private TableColumn<Reservation, String> statutCol;
    @FXML private TableColumn<Reservation, Void> actionsCol;

    @FXML private ComboBox<Hebergement> cbHebergementFilter;
    @FXML private Label lblInfo;

    @FXML
    public void initialize() {

        tableRes.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        idCol.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getId()));
        clientCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getClientNom()));
        datesCol.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getDateDebut() + " → " + d.getValue().getDateFin()
        ));
        totalCol.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().getTotal()));
        statutCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStatut()));

        // ✅ Badge statut (UI)
        statutCol.setCellFactory(col -> new TableCell<Reservation, String>() {
            @Override
            protected void updateItem(String statut, boolean empty) {
                super.updateItem(statut, empty);
                if (empty || statut == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }
                Label badge = new Label(statut.replace("_", " "));
                badge.getStyleClass().setAll("statusBadge", "status-" + statut);
                setGraphic(badge);
                setText(null);
            }
        });

        loadHebImageMap();

        hebCol.setCellValueFactory(d -> {
            int hebId = d.getValue().getHebergementId();
            String path = hebImageMap.get(hebId);
            return new SimpleStringProperty(path);
        });

        // ✅ Image arrondie (UI)
        hebCol.setCellFactory(col -> new TableCell<>() {
            private final ImageView iv = new ImageView();
            private final StackPane wrap = new StackPane(iv);

            {
                iv.setFitWidth(80);
                iv.setFitHeight(52);
                iv.setPreserveRatio(false);
                iv.setSmooth(true);

                wrap.getStyleClass().add("tableImgWrap");
                iv.getStyleClass().add("tableImg");
                wrap.setAlignment(Pos.CENTER);
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
                    Image img = new Image(new File(path).toURI().toString(), 80, 52, false, true);
                    iv.setImage(img);
                    setGraphic(wrap);
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

    // ==================== DATA LOAD ====================

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
            cbHebergementFilter.getStyleClass().add("input");

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

    // ==================== EXPORT EXCEL ====================

    @FXML
    private void exportResExcel() {
        File file = chooseSaveFile("Exporter Réservations (Excel)", "reservations.xlsx", "Excel", "*.xlsx");
        if (file == null) return;

        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Reservations");

            int r = 0;
            org.apache.poi.ss.usermodel.Row header = sheet.createRow(r++);
            header.createCell(0).setCellValue("ID");
            header.createCell(1).setCellValue("HebergementId");
            header.createCell(2).setCellValue("Client");
            header.createCell(3).setCellValue("Dates");
            header.createCell(4).setCellValue("Total");
            header.createCell(5).setCellValue("Statut");

            // export ce qui est affiché (filtré ou all)
            for (Reservation res : tableRes.getItems()) {
                org.apache.poi.ss.usermodel.Row row = sheet.createRow(r++);
                row.createCell(0).setCellValue(res.getId());
                row.createCell(1).setCellValue(res.getHebergementId());
                row.createCell(2).setCellValue(nvl(res.getClientNom()));
                row.createCell(3).setCellValue(res.getDateDebut() + " → " + res.getDateFin());
                row.createCell(4).setCellValue(res.getTotal());
                row.createCell(5).setCellValue(nvl(res.getStatut()));
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

    // ==================== EXPORT PDF PREMIUM ====================

    @FXML
    private void exportResPdf() {
        File file = chooseSaveFile("Exporter Réservations (PDF)", "reservations.pdf", "PDF", "*.pdf");
        if (file == null) return;

        try {
            Document doc = new Document(PageSize.A4.rotate(), 24, 24, 24, 24);
            PdfWriter.getInstance(doc, new FileOutputStream(file));
            doc.open();

            // Fonts (fully qualified to avoid ambiguity)
            com.lowagie.text.Font titleFont = new com.lowagie.text.Font(
                    com.lowagie.text.Font.HELVETICA, 18, com.lowagie.text.Font.BOLD
            );
            com.lowagie.text.Font subFont = new com.lowagie.text.Font(
                    com.lowagie.text.Font.HELVETICA, 11, com.lowagie.text.Font.NORMAL
            );

            // ✅ Title + date
            doc.add(new Paragraph("Liste des Réservations", titleFont));
            String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            doc.add(new Paragraph("Exporté le : " + dateStr, subFont));
            doc.add(new Paragraph(" "));

            // ✅ Table
            PdfPTable t = new PdfPTable(6);
            t.setWidthPercentage(100);
            t.setSpacingBefore(6);
            t.setWidths(new float[]{1.0f, 1.2f, 2.2f, 2.2f, 1.2f, 1.4f});

            addPdfHeaderPremium(t, "ID");
            addPdfHeaderPremium(t, "HebId");
            addPdfHeaderPremium(t, "Client");
            addPdfHeaderPremium(t, "Dates");
            addPdfHeaderPremium(t, "Total");
            addPdfHeaderPremium(t, "Statut");

            int i = 0;
            for (Reservation res : tableRes.getItems()) {
                boolean odd = (i % 2 == 1);

                t.addCell(cellPremium(String.valueOf(res.getId()), odd, null));
                t.addCell(cellPremium(String.valueOf(res.getHebergementId()), odd, null));
                t.addCell(cellPremium(nvl(res.getClientNom()), odd, null));
                t.addCell(cellPremium(res.getDateDebut() + " → " + res.getDateFin(), odd, null));
                t.addCell(cellPremium(String.valueOf(res.getTotal()), odd, null));

                // ✅ Status as colored badge cell
                t.addCell(statusBadgeCell(res.getStatut(), odd));

                i++;
            }

            doc.add(t);
            doc.close();

            showInfo("Export PDF", "PDF premium généré : " + file.getAbsolutePath());
        } catch (Exception e) {
            showError("Export PDF", e.getMessage());
        }
    }

    // ==================== ACTIONS COLUMN ====================

    private Callback<TableColumn<Reservation, Void>, TableCell<Reservation, Void>> getActions() {
        return col -> new TableCell<>() {

            private final Button btnConfirm = new Button("✓");
            private final Button btnCancel  = new Button("↩");
            private final Button btnDelete  = new Button("🗑");
            private final HBox box = new HBox(8, btnConfirm, btnCancel, btnDelete);

            {
                btnConfirm.getStyleClass().setAll("iconBtn", "btnAdd");
                btnCancel.getStyleClass().setAll("iconBtn", "btnDelete");
                btnDelete.getStyleClass().setAll("iconBtn", "btnDelete");
                box.setAlignment(Pos.CENTER);

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
                setText(null);
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

    // ==================== HELPERS (FILE + PDF STYLE) ====================

    private File chooseSaveFile(String title, String defaultName, String desc, String pattern) {
        FileChooser fc = new FileChooser();
        fc.setTitle(title);
        fc.setInitialFileName(defaultName);
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter(desc + " (" + pattern + ")", pattern));
        return fc.showSaveDialog(tableRes.getScene().getWindow());
    }

    private void addPdfHeaderPremium(PdfPTable t, String text) {
        com.lowagie.text.Font headerFont = new com.lowagie.text.Font(
                com.lowagie.text.Font.HELVETICA, 11, com.lowagie.text.Font.BOLD
        );

        PdfPCell c = new PdfPCell(new Phrase(text, headerFont));
        c.setBackgroundColor(new java.awt.Color(15, 42, 42)); // dark green
        c.setHorizontalAlignment(Element.ALIGN_CENTER);
        c.setVerticalAlignment(Element.ALIGN_MIDDLE);
        c.setPadding(8);
        c.setBorderColor(new java.awt.Color(220, 220, 220));
        t.addCell(c);
    }

    private PdfPCell cellPremium(String text, boolean odd, java.awt.Color bgOverride) {
        com.lowagie.text.Font f = new com.lowagie.text.Font(
                com.lowagie.text.Font.HELVETICA, 10, com.lowagie.text.Font.NORMAL
        );

        PdfPCell c = new PdfPCell(new Phrase(nvl(text), f));

        java.awt.Color bg = (bgOverride != null)
                ? bgOverride
                : (odd ? new java.awt.Color(250, 246, 239) : java.awt.Color.WHITE); // zebra

        c.setBackgroundColor(bg);
        c.setPadding(7);
        c.setBorderColor(new java.awt.Color(235, 235, 235));
        c.setVerticalAlignment(Element.ALIGN_MIDDLE);
        return c;
    }

    private PdfPCell statusBadgeCell(String statutRaw, boolean odd) {
        String statut = (statutRaw == null ? "" : statutRaw.trim().toUpperCase());

        java.awt.Color bg;
        java.awt.Color fg;

        switch (statut) {
            case "CONFIRME":
            case "CONFIRMEE":
                bg = new java.awt.Color(232, 255, 240);
                fg = new java.awt.Color(28, 122, 68);
                break;

            case "ANNULE":
            case "ANNULEE":
                bg = new java.awt.Color(253, 232, 231);
                fg = new java.awt.Color(180, 35, 24);
                break;

            case "EN_ATTENTE":
            default:
                bg = new java.awt.Color(255, 244, 223);
                fg = new java.awt.Color(154, 106, 17);
                break;
        }

        com.lowagie.text.Font badgeFont = new com.lowagie.text.Font(
                com.lowagie.text.Font.HELVETICA, 10, com.lowagie.text.Font.BOLD, fg
        );

        PdfPCell c = new PdfPCell(new Phrase(statut.replace("_", " "), badgeFont));
        c.setHorizontalAlignment(Element.ALIGN_CENTER);
        c.setVerticalAlignment(Element.ALIGN_MIDDLE);
        c.setPadding(7);
        c.setBorderColor(new java.awt.Color(235, 235, 235));
        c.setBackgroundColor(bg);
        return c;
    }

    private String nvl(String s) {
        return s == null ? "" : s;
    }

    // ==================== UI ALERTS ====================

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