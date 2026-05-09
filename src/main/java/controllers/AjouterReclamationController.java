package controllers;

import entities.Reclamation;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import services.ReclamationService;
import tools.MyConnection;
import tools.SessionManager;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;

public class AjouterReclamationController implements Initializable {

    @FXML private ComboBox<String> comboCategorie;
    @FXML private TextField txtTitre;
    @FXML private TextArea txtDescription;
    @FXML private Button btnBasse, btnMoyenne, btnHaute, btnUrgente;
    @FXML private Label errCategorie, errTitre, errPriorite, errDescription, lblResultat, lblCharCount;

    private ReclamationService reclamationService = new ReclamationService();
    private int currentUserId = SessionManager.getCurrentUserId();
    private String prioriteSelectionnee = "Moyenne";
    private int typeIdSelectionne = 1;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        chargerCategories();
        selectPrioriteValue("Moyenne");

        if (txtDescription != null) {
            txtDescription.textProperty().addListener((obs, o, n) -> {
                if (n.length() > 500) txtDescription.setText(o);
                if (lblCharCount != null) lblCharCount.setText(n.length() + " / 500");
                if (errDescription != null && !n.trim().isEmpty()) errDescription.setText("");
            });
        }
        if (txtTitre != null) {
            txtTitre.textProperty().addListener((obs, o, n) -> {
                if (errTitre != null && !n.trim().isEmpty()) errTitre.setText("");
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
            if (comboCategorie != null) {
                comboCategorie.setItems(cats);
                comboCategorie.setButtonCell(new ListCell<String>() {
                    @Override protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        setText(item == null || empty ? "Choisir une catégorie" : item.split("\\|\\|")[1]);
                    }
                });
                comboCategorie.setCellFactory(lv -> new ListCell<String>() {
                    @Override protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        setText(item == null || empty ? null : item.split("\\|\\|")[1]);
                    }
                });
                if (!cats.isEmpty()) comboCategorie.setValue(cats.get(0));
            }
        } catch (SQLException e) {
            System.err.println("Erreur catégories: " + e.getMessage());
        }
    }

    @FXML
    void selectPriorite(javafx.event.ActionEvent e) {
        Button src = (Button) e.getSource();
        String val = "Moyenne";
        if (src == btnBasse)        val = "Basse";
        else if (src == btnMoyenne) val = "Moyenne";
        else if (src == btnHaute)   val = "Haute";
        else if (src == btnUrgente) val = "Urgente";
        selectPrioriteValue(val);
    }

    private void selectPrioriteValue(String val) {
        prioriteSelectionnee = val;
        if (errPriorite != null) errPriorite.setText("");
        String def = "-fx-border-width:1.5; -fx-border-radius:10; -fx-background-radius:10; -fx-font-weight:700; -fx-cursor:hand;";
        if (btnBasse   != null) btnBasse.setStyle("-fx-background-color:#f0fdf4; -fx-text-fill:#16a34a; -fx-border-color:#bbf7d0; " + def);
        if (btnMoyenne != null) btnMoyenne.setStyle("-fx-background-color:#eff6ff; -fx-text-fill:#1d4ed8; -fx-border-color:#bfdbfe; " + def);
        if (btnHaute   != null) btnHaute.setStyle("-fx-background-color:#fff7ed; -fx-text-fill:#c2410c; -fx-border-color:#fed7aa; " + def);
        if (btnUrgente != null) btnUrgente.setStyle("-fx-background-color:#fff1f2; -fx-text-fill:#be123c; -fx-border-color:#fecdd3; " + def);

        String activeStyle = "-fx-background-color:#c9a24a; -fx-text-fill:white; -fx-border-color:#b8933f; " + def +
            " -fx-effect:dropshadow(gaussian,rgba(201,162,74,0.4),8,0,0,3);";
        if ("Basse".equals(val)   && btnBasse   != null) btnBasse.setStyle(activeStyle);
        if ("Moyenne".equals(val) && btnMoyenne != null) btnMoyenne.setStyle(activeStyle);
        if ("Haute".equals(val)   && btnHaute   != null) btnHaute.setStyle(activeStyle);
        if ("Urgente".equals(val) && btnUrgente != null) btnUrgente.setStyle(activeStyle);
    }

    @FXML
    void enregistrer() {
        if (errCategorie   != null) errCategorie.setText("");
        if (errTitre       != null) errTitre.setText("");
        if (errPriorite    != null) errPriorite.setText("");
        if (errDescription != null) errDescription.setText("");

        boolean valid = true;

        if (comboCategorie == null || comboCategorie.getValue() == null) {
            if (errCategorie != null) errCategorie.setText("⚠ Choisissez une catégorie.");
            valid = false;
        } else {
            try { typeIdSelectionne = Integer.parseInt(comboCategorie.getValue().split("\\|\\|")[0]); }
            catch (Exception ex) { typeIdSelectionne = 1; }
        }

        String titre = txtTitre != null ? txtTitre.getText().trim() : "";
        if (titre.isEmpty()) {
            if (errTitre != null) errTitre.setText("⚠ Le titre est obligatoire.");
            valid = false;
        } else if (titre.length() < 5) {
            if (errTitre != null) errTitre.setText("⚠ Minimum 5 caractères.");
            valid = false;
        }

        String desc = txtDescription != null ? txtDescription.getText().trim() : "";
        if (desc.isEmpty()) {
            if (errDescription != null) errDescription.setText("⚠ La description est obligatoire.");
            valid = false;
        } else if (desc.length() < 10) {
            if (errDescription != null) errDescription.setText("⚠ Minimum 10 caractères.");
            valid = false;
        }

        if (!valid) return;

        try {
            Reclamation r = new Reclamation(currentUserId, titre, desc);
            r.setPriorite(prioriteSelectionnee);
            r.setTypeId(typeIdSelectionne);
            reclamationService.addEntity(r);

            if (lblResultat != null) {
                lblResultat.setText("✅ Réclamation envoyée avec succès !");
                lblResultat.setStyle("-fx-text-fill:#1c7a44; -fx-font-weight:700;");
            }

            new Thread(() -> {
                try { Thread.sleep(1500); } catch (Exception ex) {}
                javafx.application.Platform.runLater(this::retourListe);
            }).start();

        } catch (SQLException e) {
            if (lblResultat != null) {
                lblResultat.setText("❌ Erreur : " + e.getMessage());
                lblResultat.setStyle("-fx-text-fill:#e53e3e;");
            }
        }
    }

    @FXML void annuler() { retourListe(); }

    private void retourListe() {
        try {
            hebergement.controllers.ClientLayoutController layout =
                hebergement.controllers.ClientLayoutController.getInstance();
            if (layout != null) {
                layout.goMonEspace();
            } else {
                javafx.scene.Node anyNode = null;
                if (anyNode == null) try { anyNode = txtTitre; } catch (Exception e2) {}
                if (anyNode == null) try { anyNode = txtDescription; } catch (Exception e2) {}
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