package controllers;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import entities.Reclamation;
import entities.Avis;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.chart.*;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.geometry.Pos;
import javafx.util.Duration;
import services.ReclamationService;
import services.AvisService;
import tools.AlertHelper;

import java.io.*;
import java.nio.file.*;
import java.net.URL;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.awt.Desktop;

public class DashboardStatsController implements Initializable {

    // KPI Labels
    @FXML
    private Label lblTotalReclamations;
    @FXML
    private Label lblEvolutionReclamations;
    @FXML
    private Label lblEnAttente;
    @FXML
    private Label lblPourcentageAttente;
    @FXML
    private Label lblTraitees;
    @FXML
    private Label lblTauxTraitement;
    @FXML
    private Label lblNoteMoyenne;
    @FXML
    private Label lblTotalAvis;

    // Statistiques détaillées
    @FXML
    private Label lblTempsReponse;
    @FXML
    private Label lblReclamationsMois;
    @FXML
    private Label lblAvisMois;
    @FXML
    private Label lblTauxSatisfaction;
    @FXML
    private Label lblResolues;
    @FXML
    private Label lblUsersActifs;

    // Graphiques
    @FXML
    private PieChart pieChartStatuts;
    @FXML
    private LineChart<String, Number> lineChartEvolution;
    @FXML
    private BarChart<String, Number> barChartNotes;

    // Container pour top users
    @FXML
    private VBox containerTopUsers;

    // Nouveaux éléments pour l'historique
    @FXML
    private VBox containerHistoriquePDF;
    @FXML
    private VBox messageAucunRapport;
    @FXML
    private Label lblNombreRapports;

    private ReclamationService reclamationService = new ReclamationService();
    private AvisService avisService = new AvisService();

    private List<Reclamation> toutesReclamations;
    private List<Avis> tousAvis;

    // Dossier pour stocker les rapports PDF
    private static final String DOSSIER_RAPPORTS = "rapports_pdf/";
    private List<File> rapportsPDF = new ArrayList<>();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        creerDossierRapports();
        chargerDonnees();
        calculerKPI();
        genererGraphiques();
        genererTopUsers();
        chargerHistoriqueRapports();
    }

    private void creerDossierRapports() {
        try {
            Path path = Paths.get(DOSSIER_RAPPORTS);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
            }
        } catch (IOException e) {
            System.err.println("Erreur création dossier : " + e.getMessage());
        }
    }

    private void chargerDonnees() {
        try {
            toutesReclamations = reclamationService.getAllEntities();
            tousAvis = avisService.getAllEntities();
        } catch (SQLException e) {
            System.err.println("Erreur chargement données : " + e.getMessage());
            toutesReclamations = new ArrayList<>();
            tousAvis = new ArrayList<>();
        }
    }

    private void calculerKPI() {
        DecimalFormat df = new DecimalFormat("#.#");

        // ===== RÉCLAMATIONS =====
        int total = toutesReclamations.size();
        long enAttente = toutesReclamations.stream()
                .filter(r -> "En attente".equals(r.getStatut()))
                .count();
        long traitees = toutesReclamations.stream()
                .filter(r -> "Traitée".equals(r.getStatut()))
                .count();

        lblTotalReclamations.setText(String.valueOf(total));
        lblEnAttente.setText(String.valueOf(enAttente));
        lblTraitees.setText(String.valueOf(traitees));

        // Pourcentages
        if (total > 0) {
            double pctAttente = (enAttente * 100.0) / total;
            double pctTraitees = (traitees * 100.0) / total;

            lblPourcentageAttente.setText(df.format(pctAttente) + "% du total");
            lblTauxTraitement.setText(df.format(pctTraitees) + "% traitées");
        }

        // Évolution (simulation)
        lblEvolutionReclamations.setText("+12% ce mois");

        // ===== AVIS =====
        int totalAvis = tousAvis.size();
        double noteMoyenne = tousAvis.stream()
                .mapToInt(Avis::getNote)
                .average()
                .orElse(0.0);

        lblNoteMoyenne.setText(df.format(noteMoyenne) + "/5");
        lblTotalAvis.setText("sur " + totalAvis + " avis");

        // ===== STATISTIQUES DÉTAILLÉES =====
        lblTempsReponse.setText("2.5 jours");
        lblReclamationsMois.setText(String.valueOf(total));
        lblAvisMois.setText(String.valueOf(totalAvis));

        // Taux de satisfaction (avis >= 4 étoiles)
        long avisSatisfaits = tousAvis.stream()
                .filter(a -> a.getNote() >= 4)
                .count();
        double tauxSatisfaction = totalAvis > 0 ? (avisSatisfaits * 100.0) / totalAvis : 0;
        lblTauxSatisfaction.setText(df.format(tauxSatisfaction) + "%");

        lblResolues.setText(String.valueOf(traitees));

        // Utilisateurs actifs
        Set<Integer> usersActifs = new HashSet<>();
        toutesReclamations.forEach(r -> usersActifs.add(r.getUserId()));
        tousAvis.forEach(a -> usersActifs.add(a.getUserId()));
        lblUsersActifs.setText(String.valueOf(usersActifs.size()));
    }

    private void genererGraphiques() {
        genererPieChartStatuts();
        genererLineChartEvolution();
        genererBarChartNotes();
    }

    private void genererPieChartStatuts() {
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();

        long enAttente = toutesReclamations.stream()
                .filter(r -> "En attente".equals(r.getStatut()))
                .count();
        long traitees = toutesReclamations.stream()
                .filter(r -> "Traitée".equals(r.getStatut()))
                .count();

        if (enAttente > 0) {
            pieChartData.add(new PieChart.Data("En attente (" + enAttente + ")", enAttente));
        }
        if (traitees > 0) {
            pieChartData.add(new PieChart.Data("Traitées (" + traitees + ")", traitees));
        }

        pieChartStatuts.setData(pieChartData);

        for (PieChart.Data data : pieChartData) {
            if (data.getName().contains("attente")) {
                data.getNode().setStyle("-fx-pie-color: #f0d4a0;");
            } else {
                data.getNode().setStyle("-fx-pie-color: #b9efcf;");
            }
        }
    }

    private void genererLineChartEvolution() {
        XYChart.Series<String, Number> seriesReclamations = new XYChart.Series<>();
        seriesReclamations.setName("Réclamations");

        XYChart.Series<String, Number> seriesAvis = new XYChart.Series<>();
        seriesAvis.setName("Avis");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");
        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            String dateStr = date.format(formatter);
            int nbReclamations = (int) (Math.random() * 10 + 5);
            int nbAvis = (int) (Math.random() * 8 + 3);
            seriesReclamations.getData().add(new XYChart.Data<>(dateStr, nbReclamations));
            seriesAvis.getData().add(new XYChart.Data<>(dateStr, nbAvis));
        }
        lineChartEvolution.getData().clear();
        lineChartEvolution.getData().addAll(seriesReclamations, seriesAvis);
    }

    private void genererBarChartNotes() {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Distribution");
        for (int note = 1; note <= 5; note++) {
            final int noteFinale = note;
            long count = tousAvis.stream()
                    .filter(a -> a.getNote() == noteFinale)
                    .count();
            series.getData().add(new XYChart.Data<>(note + " ⭐", count));
        }
        barChartNotes.getData().clear();
        barChartNotes.getData().add(series);
    }

    private void genererTopUsers() {
        Map<Integer, Integer> userActions = new HashMap<>();
        toutesReclamations.forEach(r -> userActions.put(r.getUserId(), userActions.getOrDefault(r.getUserId(), 0) + 1));
        tousAvis.forEach(a -> userActions.put(a.getUserId(), userActions.getOrDefault(a.getUserId(), 0) + 1));

        List<Map.Entry<Integer, Integer>> topUsers = userActions.entrySet().stream()
                .sorted(Map.Entry.<Integer, Integer>comparingByValue().reversed())
                .limit(5)
                .collect(Collectors.toList());

        containerTopUsers.getChildren().clear();
        int rank = 1;
        for (Map.Entry<Integer, Integer> entry : topUsers) {
            containerTopUsers.getChildren().add(creerLigneTopUser(rank++, entry.getKey(), entry.getValue()));
        }
    }

    private HBox creerLigneTopUser(int rank, int userId, int actions) {
        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color: #faf6ef; -fx-background-radius: 12; -fx-padding: 12;");
        Label lblRank = new Label(rank == 1 ? "🥇" : rank == 2 ? "🥈" : rank == 3 ? "🥉" : "👤");
        lblRank.setStyle("-fx-font-size: 24px;");
        VBox userInfo = new VBox(3);
        Label lblUser = new Label("Utilisateur #" + userId);
        lblUser.setStyle("-fx-font-size: 14px; -fx-font-weight: 900; -fx-text-fill: #0f2a2a;");
        Label lblActions = new Label(actions + " actions");
        lblActions.setStyle("-fx-font-size: 12px; -fx-text-fill: #6a7a73;");
        userInfo.getChildren().addAll(lblUser, lblActions);
        row.getChildren().addAll(lblRank, userInfo);
        return row;
    }

    @FXML
    void genererRapportPDF() {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
            String dateHeure = LocalDateTime.now().format(formatter);
            String nomFichier = DOSSIER_RAPPORTS + "Rapport_Stats_" + dateHeure + ".pdf";

            com.itextpdf.text.Document document = new com.itextpdf.text.Document(PageSize.A4);
            PdfWriter.getInstance(document, new FileOutputStream(nomFichier));
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, BaseColor.BLACK);
            Paragraph title = new Paragraph("RAPPORT STATISTIQUES", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(10);
            document.add(title);

            Paragraph subtitle = new Paragraph(
                    "Généré le " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm")),
                    FontFactory.getFont(FontFactory.HELVETICA, 12, BaseColor.GRAY));
            subtitle.setAlignment(Element.ALIGN_CENTER);
            subtitle.setSpacingAfter(20);
            document.add(subtitle);
            document.add(new com.itextpdf.text.pdf.draw.LineSeparator());
            document.add(new Paragraph("\n"));

            Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, BaseColor.BLACK);
            PdfPTable tableKPI = new PdfPTable(2);
            tableKPI.setWidthPercentage(100);

            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, BaseColor.WHITE);
            BaseColor goldColor = new BaseColor(201, 162, 74);

            PdfPCell h1 = new PdfPCell(new Phrase("Indicateur", headerFont));
            h1.setBackgroundColor(goldColor);
            h1.setPadding(8);
            PdfPCell h2 = new PdfPCell(new Phrase("Valeur", headerFont));
            h2.setBackgroundColor(goldColor);
            h2.setPadding(8);
            tableKPI.addCell(h1);
            tableKPI.addCell(h2);

            Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 11);
            ajouterLigneKPI(tableKPI, "Total Réclamations", lblTotalReclamations.getText(), cellFont);
            ajouterLigneKPI(tableKPI, "En Attente", lblEnAttente.getText(), cellFont);
            ajouterLigneKPI(tableKPI, "Traitées", lblTraitees.getText(), cellFont);
            ajouterLigneKPI(tableKPI, "Taux de Traitement", lblTauxTraitement.getText(), cellFont);
            ajouterLigneKPI(tableKPI, "Total Avis", lblTotalAvis.getText(), cellFont);
            ajouterLigneKPI(tableKPI, "Note Moyenne", lblNoteMoyenne.getText(), cellFont);
            ajouterLigneKPI(tableKPI, "Taux de Satisfaction", lblTauxSatisfaction.getText(), cellFont);

            document.add(new Paragraph("📊 INDICATEURS CLÉS", sectionFont));
            document.add(tableKPI);

            document.close();

            File fichierPDF = new File(nomFichier);
            rapportsPDF.add(fichierPDF);
            ajouterRapportDansHistorique(fichierPDF);
            AlertHelper.showSuccess("Rapport Généré", "Fichier : " + nomFichier);

        } catch (Exception e) {
            AlertHelper.showError("Erreur", "Impossible de générer le rapport PDF : " + e.getMessage());
        }
    }

    private void ajouterLigneKPI(PdfPTable table, String label, String valeur, Font font) {
        PdfPCell c1 = new PdfPCell(new Phrase(label, font));
        c1.setPadding(6);
        c1.setBackgroundColor(new BaseColor(250, 246, 239));
        PdfPCell c2 = new PdfPCell(new Phrase(valeur, font));
        c2.setPadding(6);
        table.addCell(c1);
        table.addCell(c2);
    }

    private void chargerHistoriqueRapports() {
        File dossier = new File(DOSSIER_RAPPORTS);
        if (dossier.exists() && dossier.isDirectory()) {
            File[] fichiers = dossier.listFiles((dir, name) -> name.endsWith(".pdf"));
            if (fichiers != null) {
                Arrays.sort(fichiers, Comparator.comparingLong(File::lastModified).reversed());
                rapportsPDF = new ArrayList<>(Arrays.asList(fichiers));
                containerHistoriquePDF.getChildren().clear();
                messageAucunRapport.setVisible(rapportsPDF.isEmpty());
                for (File f : fichiers)
                    ajouterRapportDansHistorique(f);
                lblNombreRapports.setText(rapportsPDF.size() + " rapport(s)");
            }
        }
    }

    private void ajouterRapportDansHistorique(File fichier) {
        HBox card = new HBox(15);
        card.setAlignment(Pos.CENTER_LEFT);
        card.getStyleClass().add("pdfCard");
        card.setPrefHeight(80);

        Label icone = new Label("📄");
        icone.getStyleClass().add("pdfIcon");
        VBox info = new VBox(5);
        HBox.setHgrow(info, Priority.ALWAYS);
        Label nom = new Label(fichier.getName());
        nom.getStyleClass().add("pdfNom");
        String dateStr = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date(fichier.lastModified()));
        Label lblDate = new Label("Créé le " + dateStr);
        lblDate.getStyleClass().add("pdfDate");
        Label lblTaille = new Label("Taille : " + String.format("%.2f KB", fichier.length() / 1024.0));
        lblTaille.getStyleClass().add("pdfTaille");
        info.getChildren().addAll(nom, lblDate, lblTaille);

        HBox btns = new HBox(8);
        btns.setAlignment(Pos.CENTER_RIGHT);
        Button btnVoir = new Button("👁 Voir");
        btnVoir.getStyleClass().add("btnGold");
        btnVoir.setOnAction(e -> ouvrirPDF(fichier));
        Button btnDel = new Button("🗑");
        btnDel.getStyleClass().addAll("btnDelete");
        btnDel.setOnAction(e -> supprimerRapport(fichier, card));
        btns.getChildren().addAll(btnVoir, btnDel);

        card.getChildren().addAll(icone, info, btns);
        containerHistoriquePDF.getChildren().add(0, card);
        messageAucunRapport.setVisible(false);
    }

    private void ouvrirPDF(File fichier) {
        try {
            if (Desktop.isDesktopSupported())
                Desktop.getDesktop().open(fichier);
            else
                AlertHelper.showInfo("Info", "Ouvrez manuellement : " + fichier.getAbsolutePath());
        } catch (Exception e) {
            AlertHelper.showError("Erreur", "Ouverture impossible : " + e.getMessage());
        }
    }

    private void supprimerRapport(File fichier, HBox card) {
        if (AlertHelper.showConfirmation("Suppression", "Supprimer " + fichier.getName() + " ?")) {
            if (fichier.delete()) {
                containerHistoriquePDF.getChildren().remove(card);
                rapportsPDF.remove(fichier);
                lblNombreRapports.setText(rapportsPDF.size() + " rapport(s)");
                messageAucunRapport.setVisible(rapportsPDF.isEmpty());
            } else
                AlertHelper.showError("Erreur", "Suppression échouée.");
        }
    }

    @FXML
    void actualiser() {
        chargerDonnees();
        calculerKPI();
        genererGraphiques();
        genererTopUsers();
        chargerHistoriqueRapports();
    }

    @FXML
    void retourMenu(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/admin_menu.fxml"));
            ((javafx.scene.Node) event.getSource()).getScene().setRoot(root);
        } catch (IOException e) {
            AlertHelper.showError("Erreur", "Impossible de retourner au menu : " + e.getMessage());
        }
    }
}
