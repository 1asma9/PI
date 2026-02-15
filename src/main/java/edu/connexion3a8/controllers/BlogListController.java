package edu.connexion3a8.controllers;

import edu.connexion3a8.entities.Blog;
import edu.connexion3a8.services.BlogService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
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

        // Reset errors
        titreError.setVisible(false);
        slugError.setVisible(false);
        extraitError.setVisible(false);
        contenuError.setVisible(false);

        // Validate Titre
        if (titreInput.getText().trim().isEmpty()) {
            titreError.setText("Le titre est obligatoire");
            titreError.setVisible(true);
            isValid = false;
        }

        // Validate Slug
        if (slugInput.getText().trim().isEmpty()) {
            slugError.setText("Le slug est obligatoire");
            slugError.setVisible(true);
            isValid = false;
        }

        // Validate Extrait
        if (extraitInput.getText().trim().isEmpty()) {
            extraitError.setText("L'extrait est obligatoire");
            extraitError.setVisible(true);
            isValid = false;
        } else if (extraitInput.getText().length() > 150) {
            extraitError.setText("L'extrait ne doit pas dépasser 150 caractères");
            extraitError.setVisible(true);
            isValid = false;
        }

        // Validate Contenu
        if (contenuInput.getText().trim().isEmpty()) {
            contenuError.setText("Le contenu est obligatoire");
            contenuError.setVisible(true);
            isValid = false;
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
        titreInput.clear();
        slugInput.clear();
        extraitInput.clear();
        contenuInput.clear();
        imageCouvertureInput.clear();
        statusCheckbox.setSelected(false);

        titreError.setVisible(false);
        slugError.setVisible(false);
        extraitError.setVisible(false);
        contenuError.setVisible(false);
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
