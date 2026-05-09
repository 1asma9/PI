package edu.destination.controllers;

import edu.destination.entities.Voyage;
import edu.destination.services.VoyageService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.example.PI_Gestion_des_utilisateurs.entities.utilisateur;
import org.example.PI_Gestion_des_utilisateurs.services.utilisateur_service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class AdminVoyageDetailController {

    @FXML private Label  lblVoyageId;
    @FXML private Label  lblId;
    @FXML private Label  lblPointDepart;
    @FXML private Label  lblPointArrivee;
    @FXML private Label  lblDateDepart;
    @FXML private Label  lblDateArrivee;
    @FXML private Label  lblPrix;
    @FXML private Label  lblReservationCount;
    @FXML private VBox   usersContainer;
    @FXML private Button btnRetour;
    @FXML private Button btnModifier;
    @FXML private Button btnSupprimer;

    private Voyage currentVoyage;

    private final VoyageService      voyageService = new VoyageService();
    private final utilisateur_service uSvc          = new utilisateur_service();

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // ══════════════════════════════════════════════════════
    @FXML
    public void initialize() {
        if (btnRetour != null)
            btnRetour.setOnAction(e -> goBack());
    }

    // ══════════════════════════════════════════════════════
    // ENTRY POINT — appelé depuis AdminVoyageController
    // ══════════════════════════════════════════════════════
    public void setVoyage(Voyage v) {
        this.currentVoyage = v;
        fillInfos(v);
        loadUsers(v);

        if (btnModifier  != null) btnModifier.setOnAction(e  -> goModifier(v));
        if (btnSupprimer != null) btnSupprimer.setOnAction(e -> goSupprimer(v));
    }

    // ══════════════════════════════════════════════════════
    // INFOS VOYAGE
    // ══════════════════════════════════════════════════════
    private void fillInfos(Voyage v) {
        if (lblVoyageId    != null) lblVoyageId.setText("" + v.getId());
        if (lblId          != null) lblId.setText(String.valueOf(v.getId()));
        if (lblPointDepart != null) lblPointDepart.setText(nvl(v.getPointDepart(),  "—"));
        if (lblPointArrivee!= null) lblPointArrivee.setText(nvl(v.getPointArrivee(),"—"));
        if (lblDateDepart  != null) lblDateDepart.setText(
                v.getDateDepart()  != null ? v.getDateDepart().format(FMT)  : "—");
        if (lblDateArrivee != null) lblDateArrivee.setText(
                v.getDateArrivee() != null ? v.getDateArrivee().format(FMT) : "—");
        if (lblPrix        != null) lblPrix.setText(
                v.getPrix() > 0 ? String.format("%.0f EUR", v.getPrix()) : "—");
    }

    // ══════════════════════════════════════════════════════
    // LISTE DES UTILISATEURS QUI ONT RÉSERVÉ
    // ══════════════════════════════════════════════════════
    private void loadUsers(Voyage v) {
        if (usersContainer == null) return;
        usersContainer.getChildren().clear();

        List<Integer> userIds = voyageService.getUserIdsByVoyage(v.getId());

        if (lblReservationCount != null)
            lblReservationCount.setText(userIds.size() + " réservation(s)");

        if (userIds.isEmpty()) {
            Label empty = new Label("— Non réservé");
            empty.setStyle("-fx-text-fill:#6b7280;-fx-font-size:13px;-fx-padding:8 4 8 4;");
            usersContainer.getChildren().add(empty);
            return;
        }

        for (int userId : userIds) {
            // ✅ Utilise rechercherutilisateurParId qui retourne Optional<utilisateur>
            Optional<utilisateur> opt = uSvc.rechercherutilisateurParId(userId);
            opt.ifPresent(u -> {
                boolean isPaid = voyageService.isPaidByUser(v.getId(), userId);
                usersContainer.getChildren().add(buildUserRow(u, isPaid, v));
            });
        }
    }

    // ══════════════════════════════════════════════════════
    // ROW USER — style identique au template Twig
    // ══════════════════════════════════════════════════════
    private HBox buildUserRow(utilisateur u, boolean isPaid, Voyage v) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 14, 10, 14));
        row.setStyle(rowStyle(false));
        row.setOnMouseEntered(e -> row.setStyle(rowStyle(true)));
        row.setOnMouseExited(e  -> row.setStyle(rowStyle(false)));

        // ── Avatar initiales ──────────────────────────────
        StackPane avatar = new StackPane();
        avatar.setMinSize(36, 36);
        avatar.setMaxSize(36, 36);
        avatar.setStyle(
                "-fx-background-color:linear-gradient(from 0% 0% to 100% 100%, #f97316, #ea6c0a);" +
                        "-fx-background-radius:50%;"
        );
        String prenom = nvl(u.getPrenom(), "");
        String nom    = nvl(u.getNom(),    "");
        String init   = (!prenom.isEmpty() ? prenom.substring(0,1).toUpperCase() : "")
                + (!nom.isEmpty()    ? nom.substring(0,1).toUpperCase()    : "");
        Label initLbl = new Label(init);
        initLbl.setStyle("-fx-text-fill:white;-fx-font-size:13px;-fx-font-weight:700;");
        avatar.getChildren().add(initLbl);

        // ── Infos texte ───────────────────────────────────
        VBox infoBox = new VBox(4);
        HBox.setHgrow(infoBox, Priority.ALWAYS);

        Label nameLbl = new Label(prenom + " " + nom);
        nameLbl.setStyle("-fx-text-fill:#e2e8f0;-fx-font-size:14px;-fx-font-weight:700;");

        Label emailLbl = new Label(nvl(u.getEmail(), "—"));
        emailLbl.setStyle("-fx-text-fill:#6b7280;-fx-font-size:12px;");

        // Badge payé / non payé
        Label badge = new Label(isPaid ? "✅  Payé" : "⏳  Non payé");
        badge.setStyle(isPaid
                ? "-fx-background-color:#14532d;-fx-border-color:#16a34a;" +
                "-fx-border-radius:20;-fx-background-radius:20;-fx-border-width:1;" +
                "-fx-text-fill:#4ade80;-fx-font-size:11px;-fx-font-weight:700;" +
                "-fx-padding:2 8 2 8;"
                : "-fx-background-color:#3d1f1f;-fx-border-color:#ef4444;" +
                "-fx-border-radius:20;-fx-background-radius:20;-fx-border-width:1;" +
                "-fx-text-fill:#f87171;-fx-font-size:11px;-fx-font-weight:700;" +
                "-fx-padding:2 8 2 8;"
        );

        infoBox.getChildren().addAll(nameLbl, emailLbl, badge);

        // ── Bouton Annuler ────────────────────────────────
        Button btnAnnuler = new Button("✕  Annuler");
        btnAnnuler.setStyle(btnAnnulerStyle(false));
        btnAnnuler.setOnMouseEntered(e -> btnAnnuler.setStyle(btnAnnulerStyle(true)));
        btnAnnuler.setOnMouseExited(e  -> btnAnnuler.setStyle(btnAnnulerStyle(false)));
        btnAnnuler.setOnAction(e -> confirmAnnulation(u, v));

        row.getChildren().addAll(avatar, infoBox, btnAnnuler);
        return row;
    }

    // ══════════════════════════════════════════════════════
    // POPUP CONFIRMATION ANNULATION
    // ══════════════════════════════════════════════════════
    private void confirmAnnulation(utilisateur u, Voyage v) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Annuler la réservation");
        confirm.setHeaderText(null);
        confirm.setContentText(
                "Voulez-vous annuler la réservation de "
                        + nvl(u.getPrenom(), "") + " " + nvl(u.getNom(), "") + " ?"
        );
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                boolean ok = voyageService.annulerReservation(v.getId(), u.getId());
                if (ok) {
                    loadUsers(v); // Recharge la liste après annulation
                } else {
                    Alert err = new Alert(Alert.AlertType.ERROR);
                    err.setTitle("Erreur");
                    err.setHeaderText(null);
                    err.setContentText("Impossible d'annuler cette réservation.");
                    err.showAndWait();
                }
            }
        });
    }

    // ══════════════════════════════════════════════════════
    // NAVIGATION
    // ══════════════════════════════════════════════════════
    private void goBack() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/AdminVoyage.fxml"));
            btnRetour.getScene().setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void goModifier(Voyage v) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AdminVoyageForm.fxml"));
            Parent root = loader.load();
            AdminVoyageFormController ctrl = loader.getController();
            ctrl.setVoyage(v); // ✅ nom exact de la méthode
            btnModifier.getScene().setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void goSupprimer(Voyage v) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Supprimer le voyage");
        confirm.setHeaderText(null);
        confirm.setContentText("Supprimer définitivement le voyage " + v.getId() + " ?");
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                voyageService.deleteEntity(v);
                goBack();
            }
        });
    }

    // ══════════════════════════════════════════════════════
    // STYLES
    // ══════════════════════════════════════════════════════
    private String rowStyle(boolean hover) {
        return hover
                ? "-fx-background-color:#1e1e38;-fx-background-radius:10;" +
                "-fx-border-color:#3a3a4a;-fx-border-radius:10;-fx-border-width:1;"
                : "-fx-background-color:#1a1a2e;-fx-background-radius:10;" +
                "-fx-border-color:#2a2a3a;-fx-border-radius:10;-fx-border-width:1;";
    }

    private String btnAnnulerStyle(boolean hover) {
        return hover
                ? "-fx-background-color:#3d1f1f;-fx-border-color:#ef4444;" +
                "-fx-border-radius:6;-fx-background-radius:6;-fx-border-width:1;" +
                "-fx-text-fill:#f87171;-fx-font-size:12px;-fx-font-weight:600;" +
                "-fx-padding:6 14 6 14;-fx-cursor:hand;"
                : "-fx-background-color:#1a1a28;-fx-border-color:#3d1f1f;" +
                "-fx-border-radius:6;-fx-background-radius:6;-fx-border-width:1;" +
                "-fx-text-fill:#f87171;-fx-font-size:12px;-fx-font-weight:600;" +
                "-fx-padding:6 14 6 14;-fx-cursor:hand;";
    }

    // ══════════════════════════════════════════════════════
    // UTILITAIRE
    // ══════════════════════════════════════════════════════
    private String nvl(String s, String fallback) {
        return (s != null && !s.isBlank()) ? s : fallback;
    }
}