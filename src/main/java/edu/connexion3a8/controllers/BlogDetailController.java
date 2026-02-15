package edu.connexion3a8.controllers;

import edu.connexion3a8.entities.Blog;
import edu.connexion3a8.entities.Commentaire;
import edu.connexion3a8.services.CommentaireService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class BlogDetailController implements Initializable {

    @FXML private Text blogTitle;
    @FXML private Text blogExcerpt;
    @FXML private Text blogContent;
    @FXML private Label blogAuthor;
    @FXML private Label blogDate;
    @FXML private Label blogStatus;
    @FXML private ImageView blogCoverImage;

    // Comments Section
    @FXML private VBox commentsContainer;
    @FXML private VBox emptyCommentsState;
    @FXML private Label commentCountLabel;
    @FXML private TextField commentUserInput;
    @FXML private TextField commentImageInput;
    @FXML private TextArea commentContentInput;
    @FXML private Button submitCommentBtn;
    @FXML private Button backBtn;

    private Blog currentBlog;
    private CommentaireService commentaireService;
    private BlogListController parentController;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        commentaireService = new CommentaireService();
    }

    public void setData(Blog blog, BlogListController parentController) {
        this.currentBlog = blog;
        this.parentController = parentController;

        // Set blog data
        blogTitle.setText(blog.getTitre());
        blogExcerpt.setText(blog.getExtrait());
        blogContent.setText(blog.getContenu());

        // Set author
        blogAuthor.setText("Par " + (blog.getAuthor_id() != null ? "Admin" : "Anonyme"));

        // Set date
        if (blog.getDate_publication() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy");
            blogDate.setText(blog.getDate_publication().format(formatter));
        } else if (blog.getDate_creation() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy");
            blogDate.setText(blog.getDate_creation().format(formatter));
        }

        // Set status
        if (blog.isStatus()) {
            blogStatus.setText("● Publié");
            blogStatus.setStyle("-fx-text-fill: #4CAF50;");
        } else {
            blogStatus.setText("● Brouillon");
            blogStatus.setStyle("-fx-text-fill: #FFA726;");
        }

        // Set cover image
        if (blog.getImage_couverture() != null && !blog.getImage_couverture().isEmpty()) {
            try {
                File imageFile = new File(blog.getImage_couverture());
                if (imageFile.exists()) {
                    Image image = new Image(imageFile.toURI().toString());
                    blogCoverImage.setImage(image);
                } else {
                    Image image = new Image(blog.getImage_couverture());
                    blogCoverImage.setImage(image);
                }
            } catch (Exception e) {
                System.out.println("Impossible de charger l'image: " + e.getMessage());
            }
        }

        // Load comments
        loadComments();
    }

    private void loadComments() {
        commentsContainer.getChildren().clear();

        try {
            // Récupérer tous les commentaires
            List<Commentaire> comments = commentaireService.afficher();

            // Update comment count
            commentCountLabel.setText("(" + comments.size() + ")");

            if (comments.isEmpty()) {
                emptyCommentsState.setVisible(true);
            } else {
                emptyCommentsState.setVisible(false);

                for (Commentaire comment : comments) {
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/CommentCard.fxml"));
                        HBox commentCard = loader.load();

                        CommentCardController cardController = loader.getController();
                        cardController.setData(comment, this);

                        commentsContainer.getChildren().add(commentCard);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger les commentaires", Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void submitComment() {
        String userName = commentUserInput.getText().trim();
        String content = commentContentInput.getText().trim();
        String imageUrl = commentImageInput.getText().trim();

        // Validation
        if (userName.isEmpty()) {
            showAlert("Erreur", "Veuillez entrer votre nom", Alert.AlertType.WARNING);
            return;
        }

        if (content.isEmpty()) {
            showAlert("Erreur", "Veuillez entrer un commentaire", Alert.AlertType.WARNING);
            return;
        }

        if (content.length() < 3) {
            showAlert("Erreur", "Le commentaire doit contenir au moins 3 caractères", Alert.AlertType.WARNING);
            return;
        }

        try {
            // Créer le nouveau commentaire
            Commentaire newComment = new Commentaire();
            newComment.setContenu(content);
            newComment.setNomuser(userName);
            newComment.setImg(imageUrl.isEmpty() ? null : imageUrl);

            // Date actuelle (sera gérée par la base de données avec CURRENT_TIMESTAMP)
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            newComment.setDate(LocalDateTime.now().format(formatter));

            newComment.setLikesCount(0);
            newComment.setLiked(false);

            // Ajouter le commentaire
            commentaireService.ajouter(newComment);

            // Clear form
            commentUserInput.clear();
            commentContentInput.clear();
            commentImageInput.clear();

            // Reload comments
            loadComments();

            showAlert("Succès", "Commentaire publié avec succès!", Alert.AlertType.INFORMATION);

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors de la publication du commentaire", Alert.AlertType.ERROR);
        }
    }

    public void deleteCommentById(int commentId) {
        try {
            commentaireService.supprimer(commentId);
            loadComments();
            showAlert("Succès", "Commentaire supprimé avec succès!", Alert.AlertType.INFORMATION);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors de la suppression du commentaire", Alert.AlertType.ERROR);
        }
    }

    public void updateCommentLike(int commentId, boolean isLiked) {
        try {
            if (isLiked) {
                commentaireService.ajouterLike(commentId);
            } else {
                commentaireService.retirerLike(commentId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void goBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/BlogList.fxml"));
            Pane blogListPane = loader.load();

            // Get the content area from parent and replace content
            Pane contentArea = (Pane) backBtn.getScene().lookup("#content_area");
            if (contentArea != null) {
                contentArea.getChildren().clear();
                contentArea.getChildren().add(blogListPane);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}