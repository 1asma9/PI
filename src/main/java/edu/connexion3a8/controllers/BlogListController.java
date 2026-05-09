package edu.connexion3a8.controllers;

import edu.connexion3a8.entities.Blog;
import edu.connexion3a8.services.BlogService;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class BlogListController implements Initializable {

    @FXML
    private Pane content_area;
    @FXML private TableView<Blog> blogTable;
    @FXML private TableColumn<Blog, Blog> imageColumn;
    @FXML private TableColumn<Blog, Blog> titleColumn;
    @FXML private TableColumn<Blog, String> authorColumn;
    @FXML private TableColumn<Blog, String> categoryColumn;
    @FXML private TableColumn<Blog, String> dateColumn;
    @FXML private TableColumn<Blog, String> statusColumn;
    @FXML private TableColumn<Blog, Blog> actionsColumn;
    @FXML private TextField blogSearchInput;
    @FXML private ComboBox<String> sortComboBox;
    @FXML private ComboBox<String> statusFilterComboBox;
    @FXML private Button addBlogBtn;
    @FXML private Text totalStatsLabel;
    @FXML private Text publishedStatsLabel;
    @FXML private Text draftStatsLabel;
    @FXML private Text viewsStatsLabel;

    // Form Modal Elements
    @FXML private HBox blogFormModal;
    @FXML private TextField titreInput;
    @FXML private TextField slugInput;
    @FXML private TextArea extraitInput;
    @FXML private TextArea contenuInput;
    @FXML private TextField imageCouvertureInput;
    @FXML private CheckBox statusCheckbox;
    @FXML private Button saveBlogBtn;
    @FXML private Text modalTitle;

    // Error Labels
    @FXML private Label titreError;
    @FXML private Label slugError;
    @FXML private Label extraitError;
    @FXML private Label contenuError;

    // Delete Modal
    @FXML private HBox deleteConfirmModal;

    private BlogService blogService;
    private Blog selectedBlog;
    private int currentBlogIdToDelete;
    private final ObservableList<Blog> blogObservableList = FXCollections.observableArrayList();
    private final List<Blog> allBlogs = new ArrayList<>();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        blogService = new BlogService();

        // Initialize ComboBox
        sortComboBox.getItems().addAll("Date (Récent)", "Date (Ancien)", "Titre (A-Z)", "Titre (Z-A)", "Plus vues");
        sortComboBox.setValue("Date (Récent)");
        statusFilterComboBox.getItems().addAll("Tous", "Publiés", "Brouillons", "En attente");
        statusFilterComboBox.setValue("Tous");

        configureTable();
        // Load blogs
        loadBlogs();

        // Setup search listener
        blogSearchInput.textProperty().addListener((observable, oldValue, newValue) -> {
            applyFilters();
        });

        // Setup sort listener
        sortComboBox.setOnAction(event -> sortBlogs());
        statusFilterComboBox.setOnAction(event -> applyFilters());

        // Hide modals initially
        if (blogFormModal != null) {
            blogFormModal.setVisible(false);
        }
        if (deleteConfirmModal != null) {
            deleteConfirmModal.setVisible(false);
        }
    }

    @FXML
    public void showDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Dashboard.fxml"));
            Parent dashboardRoot = loader.load();

            Stage dashboardStage = new Stage();
            dashboardStage.initModality(Modality.APPLICATION_MODAL);
            dashboardStage.initStyle(StageStyle.TRANSPARENT);

            StackPane wrapper = new StackPane();
            wrapper.setStyle("-fx-background-color: rgba(0, 0, 0, 0.5);");

            dashboardRoot.setStyle(
                    "-fx-background-color: white; " +
                            "-fx-background-radius: 15px; " +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 30, 0, 0, 10);"
            );

            wrapper.getChildren().add(dashboardRoot);
            StackPane.setMargin(dashboardRoot, new javafx.geometry.Insets(20));

            Scene scene = new Scene(wrapper, 1250, 850);
            scene.setFill(javafx.scene.paint.Color.TRANSPARENT);

            dashboardStage.setScene(scene);
            dashboardStage.centerOnScreen();

            wrapper.setOnMouseClicked(event -> {
                if (event.getTarget() == wrapper) {
                    dashboardStage.close();
                }
            });

            javafx.animation.FadeTransition fadeIn = new javafx.animation.FadeTransition(
                    javafx.util.Duration.millis(300),
                    dashboardRoot
            );
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.play();
            dashboardStage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir le Dashboard", Alert.AlertType.ERROR);
        }
    }

    private void configureTable() {
        if (blogTable == null) return;
        blogTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        blogTable.setItems(blogObservableList);

        imageColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue()));
        imageColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Blog blog, boolean empty) {
                super.updateItem(blog, empty);
                if (empty || blog == null) {
                    setGraphic(null);
                    return;
                }
                ImageView imageView = new ImageView(createPlaceholderImage(Color.web("#222244")));
                imageView.setFitWidth(44);
                imageView.setFitHeight(44);
                imageView.setPreserveRatio(true);
                setGraphic(new StackPane(imageView));
            }
        });

        titleColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue()));
        titleColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Blog blog, boolean empty) {
                super.updateItem(blog, empty);
                if (empty || blog == null) {
                    setGraphic(null);
                    return;
                }
                Label title = new Label(blog.getTitre());
                title.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
                Label subtitle = new Label(blog.getSlug());
                subtitle.setStyle("-fx-text-fill: rgba(255,255,255,0.6);");
                setGraphic(new VBox(2, title, subtitle));
            }
        });

        authorColumn.setCellValueFactory(data -> {
            String author = data.getValue().getAuthor_nom();
            if (author == null || author.isBlank()) {
                author = data.getValue().getAuthor_id();
            }
            if (author == null || author.isBlank()) {
                author = "Anonyme";
            }
            return new ReadOnlyStringWrapper(author);
        });
        categoryColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getSlug()));
        dateColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(formatDate(data.getValue())));
        statusColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(resolveWorkflowStatusLabel(data.getValue())));

        actionsColumn.setPrefWidth(240);
        actionsColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue()));
        actionsColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Blog blog, boolean empty) {
                super.updateItem(blog, empty);
                if (empty || blog == null) {
                    setGraphic(null);
                    return;
                }
                Button viewBtn = createIconButton("👁", "#2563eb", "Voir l'article", e -> openBlogDetailView(blog));
                Button editBtn = createIconButton("✏", "#059669", "Modifier", e -> openEditBlogForm(blog));
                Button publishDraftBtn = createIconButton(blog.isStatus() ? "🕓" : "🚀",
                        blog.isStatus() ? "#f59e0b" : "#16a34a",
                        blog.isStatus() ? "Revenir en brouillon" : "Publier",
                        e -> applyWorkflow(blog, blog.isStatus() ? "draft" : "publish"));
                Button requestBtn = createIconButton("📤", "#7c3aed", "Demander publication", e -> applyWorkflow(blog, "request"));
                Button deleteBtn = createIconButton("🗑", "#ef4444", "Supprimer", e -> showDeleteConfirmation(blog));
                HBox actions = new HBox(6, viewBtn, editBtn, publishDraftBtn, requestBtn, deleteBtn);
                actions.setStyle("-fx-alignment: CENTER_LEFT; -fx-padding: 0 10 0 0;");
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                setGraphic(actions);
            }
        });
    }

    private Button createIconButton(String icon, String color, String tooltipText, EventHandler<ActionEvent> handler) {
        Button button = new Button();
        button.setStyle("-fx-background-color: " + color + "; -fx-border-radius: 999px; -fx-background-radius: 999px; -fx-cursor: hand;");
        button.setMinSize(34, 34);
        button.setPrefSize(34, 34);
        button.setMaxSize(34, 34);
        button.setPadding(Insets.EMPTY);
        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-text-fill: white; -fx-font-size: 15px;");
        button.setGraphic(iconLabel);
        button.setText(null);
        button.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        button.setFocusTraversable(false);
        button.setOnAction(handler);
        if (tooltipText != null && !tooltipText.isBlank()) {
            button.setTooltip(new Tooltip(tooltipText));
        }
        return button;
    }

    private String resolveWorkflowStatusLabel(Blog blog) {
        if (blog.isStatus()) return "PUBLIÉ";
        if (blog.isPublicationRequested()) return "EN_ATTENTE";
        return "BROUILLON";
    }

    private void applyWorkflow(Blog blog, String action) {
        try {
            blogService.applyWorkflowAction(blog.getId(), action);
            loadBlogs();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'appliquer le workflow", Alert.AlertType.ERROR);
        }
    }

    private void loadBlogs() {
        try {
            List<Blog> blogs = blogService.afficher();
            allBlogs.clear();
            allBlogs.addAll(blogs);
            applyFilters();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger les blogs", Alert.AlertType.ERROR);
        }
    }

    private void applyFilters() {
        String search = blogSearchInput == null ? "" : blogSearchInput.getText().trim().toLowerCase();
        List<Blog> filtered = allBlogs.stream()
                .filter(blog -> matchesSearch(blog, search))
                .filter(this::matchesStatusFilter)
                .sorted((a, b) -> compareBySortOption(a, b))
                .toList();
        blogObservableList.setAll(filtered);
        updateStats(filtered);
    }

    private boolean matchesSearch(Blog blog, String search) {
        if (search.isEmpty()) return true;
        return (blog.getTitre() != null && blog.getTitre().toLowerCase().contains(search)) ||
                (blog.getExtrait() != null && blog.getExtrait().toLowerCase().contains(search)) ||
                (blog.getContenu() != null && blog.getContenu().toLowerCase().contains(search)) ||
                (blog.getSlug() != null && blog.getSlug().toLowerCase().contains(search));
    }

    private boolean matchesStatusFilter(Blog blog) {
        String status = statusFilterComboBox == null ? "Tous" : statusFilterComboBox.getValue();
        if (status == null || "Tous".equals(status)) return true;
        if ("Publiés".equals(status)) return blog.isStatus();
        if ("En attente".equals(status)) return !blog.isStatus() && blog.isPublicationRequested();
        return !blog.isStatus() && !blog.isPublicationRequested(); // Brouillons
    }

    private int compareBySortOption(Blog a, Blog b) {
        String option = sortComboBox == null ? "" : sortComboBox.getValue();
        String at = a.getTitre() == null ? "" : a.getTitre();
        String bt = b.getTitre() == null ? "" : b.getTitre();
        if ("Titre (A-Z)".equals(option)) return at.compareToIgnoreCase(bt);
        if ("Titre (Z-A)".equals(option)) return bt.compareToIgnoreCase(at);
        if ("Plus vues".equals(option)) return Integer.compare(b.getRatingCount(), a.getRatingCount());
        if ("Date (Ancien)".equals(option)) return compareDate(a, b);
        return compareDate(b, a); // Date recent by default
    }

    private int compareDate(Blog a, Blog b) {
        LocalDateTime da = a.getDate_publication() != null ? a.getDate_publication() : a.getDate_creation();
        LocalDateTime db = b.getDate_publication() != null ? b.getDate_publication() : b.getDate_creation();
        if (da == null || db == null) return 0;
        return da.compareTo(db);
    }

    private void updateStats(List<Blog> filtered) {
        if (totalStatsLabel != null) totalStatsLabel.setText(String.valueOf(allBlogs.size()));
        long published = filtered.stream().filter(Blog::isStatus).count();
        long pending = filtered.stream().filter(b -> !b.isStatus() && b.isPublicationRequested()).count();
        long drafts = filtered.stream().filter(b -> !b.isStatus() && !b.isPublicationRequested()).count();
        if (publishedStatsLabel != null) publishedStatsLabel.setText(String.valueOf(published));
        if (draftStatsLabel != null) draftStatsLabel.setText(String.valueOf(drafts));
        if (viewsStatsLabel != null) {
            viewsStatsLabel.setText(String.valueOf(pending));
        }
    }

    private String formatDate(Blog blog) {
        LocalDateTime date = blog.getDate_publication() != null ? blog.getDate_publication() : blog.getDate_creation();
        if (date == null) return "-";
        return DateTimeFormatter.ofPattern("dd/MM/yyyy").format(date);
    }

    private Image createPlaceholderImage(Color color) {
        int size = 60;
        WritableImage image = new WritableImage(size, size);
        PixelWriter writer = image.getPixelWriter();
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                writer.setColor(x, y, color);
            }
        }
        return image;
    }

    @FXML
    public void openAddBlogForm() {
        selectedBlog = null;
        clearForm();
        modalTitle.setText("Ajouter un Blog");
        blogFormModal.setVisible(true);
    }

    public void openEditBlogForm(Blog blog) {
        selectedBlog = blog;
        modalTitle.setText("Modifier le Blog");

        // Fill form with blog data
        titreInput.setText(blog.getTitre());
        slugInput.setText(blog.getSlug());
        extraitInput.setText(blog.getExtrait());
        contenuInput.setText(blog.getContenu());
        imageCouvertureInput.setText(blog.getImage_couverture());
        statusCheckbox.setSelected(blog.isStatus());

        blogFormModal.setVisible(true);
    }

    @FXML
    public void closeBlogFormModal() {
        blogFormModal.setVisible(false);
        clearForm();
    }

    @FXML
    public void saveBlog() {
        if (!validateForm()) {
            return;
        }

        Blog blog = selectedBlog != null ? selectedBlog : new Blog();

        blog.setTitre(titreInput.getText().trim());
        blog.setSlug(slugInput.getText().trim());
        blog.setExtrait(extraitInput.getText().trim());
        blog.setContenu(contenuInput.getText().trim());
        blog.setImage_couverture(imageCouvertureInput.getText().trim());
        blog.setStatus(statusCheckbox.isSelected());
        blog.setPublicationRequested(false);
        blog.setAuthor_id("1"); // À remplacer par l'ID de l'utilisateur connecté

        try {
            if (selectedBlog == null) {
                // Add new blog
                blog.setDate_creation(LocalDateTime.now());
                if (blog.isStatus()) {
                    blog.setDate_publication(LocalDateTime.now());
                }
                blogService.ajouter(blog);
                showAlert("Succès", "Blog ajouté avec succès!", Alert.AlertType.INFORMATION);
            } else {
                // Update existing blog
                if (blog.isStatus() && selectedBlog.getDate_publication() == null) {
                    blog.setDate_publication(LocalDateTime.now());
                }
                blogService.modifier(blog);
                showAlert("Succès", "Blog modifié avec succès!", Alert.AlertType.INFORMATION);
            }

            closeBlogFormModal();
            loadBlogs();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors de l'enregistrement du blog", Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void browseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File file = fileChooser.showOpenDialog(imageCouvertureInput.getScene().getWindow());
        if (file != null) {
            imageCouvertureInput.setText(file.getAbsolutePath());
        }
    }

    public void showDeleteConfirmation(Blog blog) {
        currentBlogIdToDelete = blog.getId();
        deleteConfirmModal.setVisible(true);
    }

    @FXML
    public void closeDeleteConfirmModal() {
        deleteConfirmModal.setVisible(false);
    }

    @FXML
    public void confirmDelete() {
        try {
            blogService.supprimer(currentBlogIdToDelete);
            showAlert("Succès", "Blog supprimé avec succès!", Alert.AlertType.INFORMATION);
            closeDeleteConfirmModal();
            loadBlogs();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors de la suppression du blog", Alert.AlertType.ERROR);
        }
    }


    private void sortBlogs() {
        applyFilters();
    }

    private boolean validateForm() {
        boolean isValid = true;

        // Validation titre
        if (titreInput.getText().trim().isEmpty()) {
            if (titreError != null) {
                titreError.setText("Le titre est obligatoire");
                titreError.setVisible(true);
            }
            isValid = false;
        } else {
            if (titreError != null) titreError.setVisible(false);
        }

        // Validation slug
        if (slugInput.getText().trim().isEmpty()) {
            if (slugError != null) {
                slugError.setText("Le slug est obligatoire");
                slugError.setVisible(true);
            }
            isValid = false;
        } else {
            if (slugError != null) slugError.setVisible(false);
        }

        // Validation extrait
        String extrait = extraitInput.getText().trim();
        if (extrait.isEmpty()) {
            if (extraitError != null) {
                extraitError.setText("L'extrait est obligatoire");
                extraitError.setVisible(true);
            }
            isValid = false;
        } else if (extrait.length() > 150) {
            if (extraitError != null) {
                extraitError.setText("L'extrait doit faire maximum 150 caractères");
                extraitError.setVisible(true);
            }
            isValid = false;
        } else {
            if (extraitError != null) extraitError.setVisible(false);
        }

        // Validation contenu
        if (contenuInput.getText().trim().isEmpty()) {
            if (contenuError != null) {
                contenuError.setText("Le contenu est obligatoire");
                contenuError.setVisible(true);
            }
            isValid = false;
        } else {
            if (contenuError != null) contenuError.setVisible(false);
        }

        if (!isValid && titreError == null) {
            // Si les labels n'existent pas, utiliser une alerte
            showAlert("Validation", "Veuillez remplir tous les champs obligatoires", Alert.AlertType.WARNING);
        }

        return isValid;
    }

    public void openBlogDetailView(Blog blog) {
        try {
            System.out.println("Opening blog detail for: " + blog.getTitre());

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/BlogDetailView.fxml"));
            Pane blogDetailPane = loader.load();

            BlogDetailController detailController = loader.getController();
            detailController.setData(blog, this);

            // Trouver le content_area
            Pane contentArea = (Pane) content_area.getScene().lookup("#content_area");

            if (contentArea != null) {
                contentArea.getChildren().clear();
                contentArea.getChildren().add(blogDetailPane);
                System.out.println("Blog detail loaded successfully!");
            } else {
                System.err.println("Content area not found!");
                // Alternative : remplacer directement dans le parent actuel
                content_area.getChildren().clear();
                content_area.getChildren().add(blogDetailPane);
            }

        } catch (IOException e) {
            System.err.println("Error loading blog detail: " + e.getMessage());
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir les détails du blog: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void clearForm() {
        // Vider les champs de formulaire
        if (titreInput != null) titreInput.clear();
        if (slugInput != null) slugInput.clear();
        if (extraitInput != null) extraitInput.clear();
        if (contenuInput != null) contenuInput.clear();
        if (imageCouvertureInput != null) imageCouvertureInput.clear();
        if (statusCheckbox != null) statusCheckbox.setSelected(false);

        // Masquer les messages d'erreur (seulement s'ils existent)
        if (titreError != null) titreError.setVisible(false);
        if (slugError != null) slugError.setVisible(false);
        if (extraitError != null) extraitError.setVisible(false);
        if (contenuError != null) contenuError.setVisible(false);

        // Réinitialiser le compteur (seulement s'il existe)
        //if (extraitCounter != null) {
           // extraitCounter.setText("0/150");
       // }
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
