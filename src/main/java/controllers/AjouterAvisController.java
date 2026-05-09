package controllers;

import entities.Avis;
import entities.Reclamation;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import services.AvisService;
import services.ReclamationService;
import tools.MyConnection;
import tools.SessionManager;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;

public class AjouterAvisController implements Initializable {

    @FXML private ComboBox<String> comboCategorie;
    @FXML private Button star1, star2, star3, star4, star5;
    @FXML private Label lblNoteValeur;
    @FXML private TextArea txtCommentaire;
    @FXML private Label errCategorie, errNote, errCommentaire, lblResultat, lblCharCount;

    private AvisService avisService = new AvisService();
    private int currentUserId = SessionManager.getCurrentUserId();
    private int noteSelectionnee = 0;
    private int typeIdSelectionne = 1;
    private Button[] stars;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        stars = new Button[]{star1, star2, star3, star4, star5};

        // Charger catégories depuis typeavis
        chargerCategories();

        // Compteur caractères
        if (txtCommentaire != null) {
            txtCommentaire.textProperty().addListener((obs, o, n) -> {
                if (n.length() > 500) txtCommentaire.setText(o);
                if (lblCharCount != null) lblCharCount.setText(n.length() + " / 500");
                if (errCommentaire != null && !n.trim().isEmpty()) errCommentaire.setText("");
            });
        }
    }

    private void chargerCategories() {
        try {
            javafx.collections.ObservableList<String> cats = FXCollections.observableArrayList();
            String q = "SELECT id, nom FROM typeavis ORDER BY id";
            try (Statement st = MyConnection.getInstance().getCnx().createStatement();
                 ResultSet rs = st.executeQuery(q)) {
                while (rs.next()) {
                    cats.add(rs.getInt("id") + "||" + rs.getString("nom"));
                }
            }
            // Afficher seulement le nom dans le ComboBox
            if (comboCategorie != null) {
                comboCategorie.setItems(cats);
                comboCategorie.setButtonCell(new ListCell<String>() {
                    @Override protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null || empty) { setText("Choisir une catégorie"); return; }
                        setText(item.split("\\|\\|")[1]);
                    }
                });
                comboCategorie.setCellFactory(lv -> new ListCell<String>() {
                    @Override protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null || empty) { setText(null); return; }
                        setText(item.split("\\|\\|")[1]);
                    }
                });
                if (!cats.isEmpty()) comboCategorie.setValue(cats.get(0));
            }
        } catch (SQLException e) {
            System.err.println("Erreur catégories: " + e.getMessage());
        }
    }

    @FXML
    void selectNote(javafx.event.ActionEvent e) {
        Button src = (Button) e.getSource();
        int note = 0;
        if (src == star1) note = 1;
        else if (src == star2) note = 2;
        else if (src == star3) note = 3;
        else if (src == star4) note = 4;
        else if (src == star5) note = 5;
        noteSelectionnee = note;
        if (errNote != null) errNote.setText("");

        // Colorier les étoiles
        String[] labels = {"Très mauvais", "Mauvais", "Correct", "Bien", "Excellent !"};
        for (int i = 0; i < stars.length; i++) {
            if (stars[i] != null) {
                boolean active = i < note;
                stars[i].setStyle("-fx-background-color:transparent; -fx-text-fill:" +
                    (active ? "#c9a24a" : "#d9cdbf") +
                    "; -fx-font-size:32px; -fx-cursor:hand; -fx-padding:0;");
            }
        }
        if (lblNoteValeur != null)
            lblNoteValeur.setText(note + "/5 — " + labels[note - 1]);
    }

    @FXML
    void enregistrer() {
        if (errCategorie != null) errCategorie.setText("");
        if (errNote      != null) errNote.setText("");
        if (errCommentaire!= null) errCommentaire.setText("");

        boolean valid = true;

        // Valider catégorie
        if (comboCategorie == null || comboCategorie.getValue() == null) {
            if (errCategorie != null) errCategorie.setText("⚠ Choisissez une catégorie.");
            valid = false;
        } else {
            try { typeIdSelectionne = Integer.parseInt(comboCategorie.getValue().split("\\|\\|")[0]); }
            catch (Exception ex) { typeIdSelectionne = 1; }
        }

        // Valider note
        if (noteSelectionnee == 0) {
            if (errNote != null) errNote.setText("⚠ Cliquez sur une étoile pour noter.");
            valid = false;
        }

        // Valider commentaire
        String comment = txtCommentaire != null ? txtCommentaire.getText().trim() : "";
        if (comment.isEmpty()) {
            if (errCommentaire != null) errCommentaire.setText("⚠ Le commentaire est obligatoire.");
            valid = false;
        } else if (comment.length() < 10) {
            if (errCommentaire != null) errCommentaire.setText("⚠ Minimum 10 caractères.");
            valid = false;
        }

        if (!valid) return;

        try {
            // Créer l'avis avec le vrai type_id
            Avis a = new Avis(currentUserId, noteSelectionnee, comment);
            a.setTypeId(typeIdSelectionne);
            avisService.addEntity(a);

            // Auto-réclamation si note <= 2
            if (noteSelectionnee <= 2) {
                ReclamationService rs2 = new ReclamationService();
                Reclamation reclauto = new Reclamation(currentUserId,
                    "Avis négatif – Note " + noteSelectionnee + "/5",
                    "Réclamation automatique suite à un avis négatif (" + noteSelectionnee + "/5). Commentaire : " + comment);
                reclauto.setPriorite(noteSelectionnee == 1 ? "Urgente" : "Haute");
                reclauto.setTypeId(typeIdSelectionne);
                rs2.addEntity(reclauto);
                if (lblResultat != null) {
                    lblResultat.setText("✅ Avis publié ! ⚠️ Note basse — une réclamation a été créée automatiquement.");
                    lblResultat.setStyle("-fx-text-fill:#c9a24a; -fx-font-weight:700;");
                }
            } else {
                if (lblResultat != null) {
                    lblResultat.setText("✅ Avis publié avec succès !");
                    lblResultat.setStyle("-fx-text-fill:#1c7a44; -fx-font-weight:700;");
                }
            }

            new Thread(() -> {
                try { Thread.sleep(1500); } catch (Exception ex) {}
                javafx.application.Platform.runLater(this::retourListe);
            }).start();

        } catch (SQLException ex) {
            if (lblResultat != null) {
                lblResultat.setText("❌ Erreur : " + ex.getMessage());
                lblResultat.setStyle("-fx-text-fill:#e53e3e;");
            }
        }
    }

    @FXML void annuler() { retourListe(); }

    private void retourListe() {
        try {
            // Demander l'ouverture de l'onglet Feedbacks
            hebergement.controllers.MonEspaceController.requestOpenFeedbacksTab();

            hebergement.controllers.ClientLayoutController layout =
                hebergement.controllers.ClientLayoutController.getInstance();
            if (layout != null) {
                layout.goMonEspace();
            } else {
                javafx.scene.Node anyNode = null;
                if (anyNode == null) try { anyNode = txtCommentaire; } catch (Exception e2) {}
                if (anyNode == null) try { anyNode = lblNoteValeur; } catch (Exception e2) {}
                if (anyNode != null && anyNode.getScene() != null) {
                    Parent root = FXMLLoader.load(getClass().getResource("/app/MonEspace.fxml"));
                    anyNode.getScene().setRoot(root);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}