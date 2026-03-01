package edu.connexion3a8.controllers;

import edu.connexion3a8.entities.Blog;
import edu.connexion3a8.services.BlogService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.ArrayList;

public class BlogListController implements Initializable {

    @FXML private Pane content_area;
    @FXML private GridPane BlogListContainer;
    @FXML private TextField blogSearchInput;
    @FXML private ComboBox<String> sortComboBox;
    @FXML private Button addBlogBtn;

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

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        blogService = new BlogService();

        // Initialize ComboBox
        sortComboBox.getItems().addAll("Date (Récent)", "Date (Ancien)", "Titre (A-Z)", "Titre (Z-A)");
        sortComboBox.setValue("Date (Récent)");

        // Load blogs
        loadBlogs();

        // Setup search listener
        blogSearchInput.textProperty().addListener((observable, oldValue, newValue) -> {
            searchBlogs(newValue);
        });

        // Setup sort listener
        sortComboBox.setOnAction(event -> sortBlogs());

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
            // Charger le Dashboard
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Dashboard.fxml"));
            Parent dashboardRoot = loader.load();

            // Créer la popup avec un fond semi-transparent
            Stage dashboardStage = new Stage();
            dashboardStage.initModality(Modality.APPLICATION_MODAL);
            dashboardStage.initStyle(StageStyle.TRANSPARENT); // Fenêtre transparente

            // Wrapper avec effet d'ombre
            StackPane wrapper = new StackPane();
            wrapper.setStyle("-fx-background-color: rgba(0, 0, 0, 0.5);"); // Fond sombre transparent

            // Ajouter le dashboard au centre
            dashboardRoot.setStyle(
                    "-fx-background-color: white; " +
                            "-fx-background-radius: 15px; " +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 30, 0, 0, 10);"
            );

            wrapper.getChildren().add(dashboardRoot);
            StackPane.setMargin(dashboardRoot, new javafx.geometry.Insets(20));

            // Créer la scène
            Scene scene = new Scene(wrapper, 1250, 850);
            scene.setFill(javafx.scene.paint.Color.TRANSPARENT);

            dashboardStage.setScene(scene);
            dashboardStage.centerOnScreen();

            // Fermer en cliquant sur le fond sombre
            wrapper.setOnMouseClicked(event -> {
                if (event.getTarget() == wrapper) {
                    dashboardStage.close();
                }
            });

            // Animation d'entrée (optionnel)
            dashboardRoot.setOpacity(0);
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

    private void loadBlogs() {
        BlogListContainer.getChildren().clear();

        try {
            List<Blog> blogs = blogService.afficher();

            int column = 0;
            int row = 0;

            for (Blog blog : blogs) {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/BlogCard.fxml"));
                    Pane blogCard = loader.load();

                    BlogCardController cardController = loader.getController();
                    cardController.setData(blog, this);

                    BlogListContainer.add(blogCard, column, row);

                    column++;
                    if (column == 3) {
                        column = 0;
                        row++;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger les blogs", Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void openAddBlogForm(MouseEvent event) {
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
    public void closeBlogFormModal(MouseEvent event) {
        blogFormModal.setVisible(false);
        clearForm();
    }

    @FXML
    public void saveBlog(MouseEvent event) {
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

            closeBlogFormModal(event);
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
    public void closeDeleteConfirmModal(MouseEvent event) {
        deleteConfirmModal.setVisible(false);
    }

    @FXML
    public void confirmDelete(MouseEvent event) {
        try {
            blogService.supprimer(currentBlogIdToDelete);
            showAlert("Succès", "Blog supprimé avec succès!", Alert.AlertType.INFORMATION);
            closeDeleteConfirmModal(event);
            loadBlogs();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors de la suppression du blog", Alert.AlertType.ERROR);
        }
    }


    private void searchBlogs(String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            loadBlogs();
            return;
        }

        BlogListContainer.getChildren().clear();

        try {
            // Récupérer tous les blogs et filtrer localement
            List<Blog> allBlogs = blogService.afficher();
            List<Blog> filteredBlogs = new ArrayList<>();

            String searchLower = searchText.toLowerCase();

            for (Blog blog : allBlogs) {
                // Rechercher dans le titre ou le contenu
                if ((blog.getTitre() != null && blog.getTitre().toLowerCase().contains(searchLower)) ||
                        (blog.getContenu() != null && blog.getContenu().toLowerCase().contains(searchLower)) ||
                        (blog.getExtrait() != null && blog.getExtrait().toLowerCase().contains(searchLower))) {
                    filteredBlogs.add(blog);
                }
            }

            int column = 0;
            int row = 0;

            for (Blog blog : filteredBlogs) {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/BlogCard.fxml"));
                    Pane blogCard = loader.load();

                    BlogCardController cardController = loader.getController();
                    cardController.setData(blog, this);

                    BlogListContainer.add(blogCard, column, row);

                    column++;
                    if (column == 3) {
                        column = 0;
                        row++;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sortBlogs() {
        loadBlogs();
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
