package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import tools.AlertHelper;
import tools.SessionManager;

import java.io.IOException;
import java.net.URL;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import java.util.ResourceBundle;

public class AdminLayoutController implements Initializable {

    @FXML private StackPane contentArea;
    @FXML private VBox destinationSubMenu;
    @FXML private Button btnToggleDestination;
    @FXML private VBox blogSubMenu;
    @FXML private Button btnToggleBlog;
    @FXML private Label pageTitle;
    private Node blogContentNode;

    @FXML private TableView<ArticleRow> articleTable;
    @FXML private TableColumn<ArticleRow, ArticleRow> imageColumn;
    @FXML private TableColumn<ArticleRow, ArticleRow> titleColumn;
    @FXML private TableColumn<ArticleRow, String> authorColumn;
    @FXML private TableColumn<ArticleRow, String> categoryColumn;
    @FXML private TableColumn<ArticleRow, String> dateColumn;
    @FXML private TableColumn<ArticleRow, String> statusColumn;
    @FXML private TableColumn<ArticleRow, ArticleRow> actionsColumn;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupArticleTable();
        if (!contentArea.getChildren().isEmpty()) {
            blogContentNode = contentArea.getChildren().get(0);
        }
    }

    private void setupArticleTable() {
        if (articleTable == null) {
            return;
        }
        articleTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        imageColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue()));
        imageColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(ArticleRow row, boolean empty) {
                super.updateItem(row, empty);
                if (empty || row == null) {
                    setGraphic(null);
                    return;
                }

                ImageView imageView = new ImageView(createPlaceholderImage(row.placeholderColor));
                imageView.setFitWidth(60);
                imageView.setFitHeight(60);
                imageView.setPreserveRatio(false);

                Rectangle clip = new Rectangle(60, 60);
                clip.setArcWidth(14);
                clip.setArcHeight(14);
                imageView.setClip(clip);

                setGraphic(new StackPane(imageView));
            }
        });

        titleColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue()));
        titleColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(ArticleRow row, boolean empty) {
                super.updateItem(row, empty);
                if (empty || row == null) {
                    setGraphic(null);
                    return;
                }
                Label title = new Label(row.title);
                title.getStyleClass().add("article-title");
                Label slug = new Label(row.slug);
                slug.getStyleClass().add("article-slug");
                setGraphic(new VBox(2, title, slug));
            }
        });

        authorColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().author));
        categoryColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().category));
        dateColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().date));

        statusColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().status));
        statusColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setGraphic(null);
                    return;
                }
                Label badge = new Label(status);
                badge.getStyleClass().add("status-badge");
                if ("Publié".equalsIgnoreCase(status)) {
                    badge.getStyleClass().add("status-badge-published");
                } else {
                    badge.getStyleClass().add("status-badge-draft");
                }
                setGraphic(badge);
            }
        });

        actionsColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue()));
        actionsColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(ArticleRow row, boolean empty) {
                super.updateItem(row, empty);
                if (empty || row == null) {
                    setGraphic(null);
                    return;
                }
                Button viewBtn = new Button("👁");
                viewBtn.getStyleClass().addAll("action-btn", "action-btn-eye");

                Button closeBtn = new Button("✕");
                closeBtn.getStyleClass().addAll("action-btn", "action-btn-close");

                Button deleteBtn = new Button("🗑");
                deleteBtn.getStyleClass().addAll("action-btn", "action-btn-delete");

                HBox actions = new HBox(8, viewBtn, closeBtn, deleteBtn);
                actions.setAlignment(Pos.CENTER_LEFT);
                setGraphic(actions);
            }
        });

        articleTable.setItems(FXCollections.observableArrayList(
                new ArticleRow("Top 10 des destinations 2026", "top-10-destinations-2026", "Admin", "Voyage", "07/05/2026", "Publié", Color.web("#3b82f6")),
                new ArticleRow("Séjour en Tunisie : guide complet", "sejour-tunisie-guide-complet", "Sarra", "Destination", "06/05/2026", "Publié", Color.web("#f97316")),
                new ArticleRow("Escapades weekend à petit budget", "escapades-weekend-budget", "Ahmed", "Conseils", "05/05/2026", "Publié", Color.web("#10b981")),
                new ArticleRow("Bons plans hébergement été", "bons-plans-hebergement-ete", "Yassine", "Hébergement", "04/05/2026", "Brouillon", Color.web("#64748b"))
        ));
    }

    private WritableImage createPlaceholderImage(Color color) {
        WritableImage image = new WritableImage(60, 60);
        PixelWriter pixelWriter = image.getPixelWriter();
        for (int x = 0; x < 60; x++) {
            for (int y = 0; y < 60; y++) {
                pixelWriter.setColor(x, y, color);
            }
        }
        return image;
    }

    // =============== DROPDOWN ===============
    @FXML
    void toggleDestinationMenu() {
        boolean visible = destinationSubMenu.isVisible();
        destinationSubMenu.setVisible(!visible);
        destinationSubMenu.setManaged(!visible);
        btnToggleDestination.setText(visible ? "🌍 Gestion Destination ▶" : "🌍 Gestion Destination ▼");
    }

    @FXML
    void toggleBlogMenu() {
        boolean visible = blogSubMenu.isVisible();
        blogSubMenu.setVisible(!visible);
        blogSubMenu.setManaged(!visible);
        btnToggleBlog.setText(visible ? "📝 Gestion Blog ▶" : "📝 Gestion Blog ▼");
    }

    // =============== DESTINATION ===============
    @FXML
    void goDestination() {
        chargerPage("/AdminDestinationView.fxml");
    }

    @FXML
    void goVoyage() {
        chargerPage("/AdminVoyageView.fxml");
    }

    @FXML
    void goTransport() {
        chargerPage("/AdminTransportView.fxml");
    }

    @FXML
    void goImage() {
        chargerPage("/AdminImageView.fxml");
    }

    // =============== NAVIGATION ===============
    @FXML
    void goDashboard() {
        chargerPage("/app/dashboard.fxml");
    }

    @FXML
    void goReservationsAdmin() {
        chargerPage("/app/reservations.fxml");
    }

    @FXML
    void goList() {
        chargerPage("/app/list.fxml");
    }

    @FXML
    void goAdd() {
        chargerPage("/app/add.fxml");
    }

    @FXML
    void goUtilisateurs() {
        chargerPage("/app/add_user.fxml");
    }

    @FXML
    void goChat() {
        chargerPage("/app/chat.fxml");
    }

    @FXML
    void goBlogList() {
        chargerPage("/fxml/BlogList.fxml");
        if (pageTitle != null) {
            pageTitle.setText("Liste blogs");
        }
    }

    @FXML
    void goBlogCommentReports() {
        chargerPage("/app/ModerationDashboard.fxml");
        if (pageTitle != null) {
            pageTitle.setText("Signalements commentaires");
        }
    }

    @FXML
    void goBlogAnalyses() {
        chargerPage("/fxml/Dashboard.fxml");
        if (pageTitle != null) {
            pageTitle.setText("Analyses");
        }
    }

    @FXML
    void goAdminReclamations() {
        chargerPage("/admin_reclamations.fxml");
    }

    @FXML
    void goActiviteBack() {
        chargerPage("/affichage_activites_back.fxml");
    }

    @FXML
    void goLogout() {
        if (AlertHelper.showConfirmation("Déconnexion", "Êtes-vous sûr de vouloir vous déconnecter ?")) {
            SessionManager.logout();
            System.exit(0);
        }
    }

    // =============== ANCIENS NOMS (compatibilité) ===============
    @FXML
    void afficherDashboard() {
        chargerPage("/app/dashboard.fxml");
    }

    @FXML
    void afficherAjouter() {
        chargerPage("/app/add.fxml");
    }

    @FXML
    void afficherGererHebergements() {
        chargerPage("/app/list.fxml");
    }

    @FXML
    void afficherReservationsAdmin() {
        chargerPage("/app/reservations.fxml");
    }

    @FXML
    void afficherUtilisateurs() {
        chargerPage("/app/add_user.fxml");
    }

    @FXML
    void afficherAdminReclamations() {
        chargerPage("/admin_reclamations.fxml");
    }

    @FXML
    void afficherAdminAvis() {
        chargerPage("/admin_avis.fxml");
    }

    @FXML
    void afficherStatistiques() {
        chargerPage("/app/dashboard.fxml");
    }

    @FXML
    void switchRole() {
        SessionManager.login(SessionManager.getCurrentUserId(), SessionManager.getUsername(), false);
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/app/main_layout.fxml"));
            contentArea.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void deconnexion() {
        if (AlertHelper.showConfirmation("Déconnexion", "Êtes-vous sûr de vouloir vous déconnecter ?")) {
            SessionManager.logout();
            System.exit(0);
        }
    }

    // =============== CHARGEMENT ===============
    private void chargerPage(String fxmlPath) {
        if (pageTitle != null) {
            pageTitle.setText(resolvePageTitle(fxmlPath));
        }
        try {
            URL resource = getClass().getResource(fxmlPath);
            if (resource == null) {
                resource = getClass().getClassLoader().getResource(
                        fxmlPath.startsWith("/") ? fxmlPath.substring(1) : fxmlPath
                );
            }
            if (resource == null) {
                Label errorLabel = new Label("❌ Page introuvable: " + fxmlPath);
                errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 14px;");
                contentArea.getChildren().setAll(errorLabel);
                System.err.println("❌ Introuvable: " + fxmlPath);
                return;
            }
            FXMLLoader loader = new FXMLLoader(resource);
            Parent newContent = loader.load();
            contentArea.getChildren().setAll(newContent);
        } catch (IOException e) {
            Label errorLabel = new Label("Erreur chargement: " + fxmlPath + "\n" + e.getMessage());
            errorLabel.setStyle("-fx-text-fill: red;");
            contentArea.getChildren().setAll(errorLabel);
            System.err.println("Erreur: " + fxmlPath + " → " + e.getMessage());
        }
    }

    private String resolvePageTitle(String fxmlPath) {
        if ("/app/dashboard.fxml".equals(fxmlPath)) return "Dashboard";
        if ("/app/reservations.fxml".equals(fxmlPath)) return "Réservations";
        if ("/app/list.fxml".equals(fxmlPath)) return "Gestion Hébergement";
        if ("/admin_reclamations.fxml".equals(fxmlPath)) return "Réclamations";
        return "Vianova Dashboard";
    }

    private void showBlogContent(String title) {
        if (blogContentNode != null) {
            contentArea.getChildren().setAll(blogContentNode);
        }
        if (pageTitle != null) {
            pageTitle.setText(title);
        }
    }

    public static class ArticleRow {
        private final String title;
        private final String slug;
        private final String author;
        private final String category;
        private final String date;
        private final String status;
        private final Color placeholderColor;

        public ArticleRow(String title, String slug, String author, String category, String date, String status, Color placeholderColor) {
            this.title = title;
            this.slug = slug;
            this.author = author;
            this.category = category;
            this.date = date;
            this.status = status;
            this.placeholderColor = placeholderColor;
        }
    }
}
