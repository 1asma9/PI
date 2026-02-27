package edu.connexion3a8.controllers;

import edu.connexion3a8.entities.Blog;
import edu.connexion3a8.entities.Commentaire;
import edu.connexion3a8.services.CommentaireService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class FrontendBlogDetailController implements Initializable {

    @FXML private ImageView blogImage;
    @FXML private Text blogTitle;  // ⚠️ IMPORTANT: Text, pas Label
    @FXML private Text blogDate;
    @FXML private Text blogExcerpt;
    @FXML private Text blogContent;
    @FXML private Text commentCount;
    @FXML private Label blogCategory;

    @FXML private TextField commentUserInput;
    @FXML private TextField commentImageInput;
    @FXML private TextArea commentContentInput;
    @FXML private VBox commentsContainer;

    private Blog currentBlog;
    private CommentaireService commentaireService;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        commentaireService = new CommentaireService();
    }

    public void setData(Blog blog, FrontendBlogController frontendBlogController) {
        this.currentBlog = blog;

        System.out.println("Setting blog data: " + blog.getTitre());

        // Titre
        blogTitle.setText(blog.getTitre());

        // Extrait
        if (blog.getExtrait() != null && !blog.getExtrait().isEmpty()) {
            blogExcerpt.setText(blog.getExtrait());
        }

        // Contenu
        if (blog.getContenu() != null && !blog.getContenu().isEmpty()) {
            blogContent.setText(blog.getContenu());
        }

        // Date - CORRECTION ICI
        if (blogDate != null && blog.getDate_creation() != null) {
            // Convertir LocalDateTime en String
            String dateStr = blog.getDate_creation().toString();
            blogDate.setText(formatDate(dateStr));
        }

        // Image
        loadImage(blog.getImage_couverture());

        // Charger les commentaires
        loadComments();
    }

    private void loadImage(String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) {
            return;
        }

        try {
            Image image;
            if (imagePath.startsWith("http://") || imagePath.startsWith("https://")) {
                image = new Image(imagePath, true);
            } else if (imagePath.startsWith("/")) {
                image = new Image("file:" + imagePath, true);
            } else {
                image = new Image(getClass().getResourceAsStream(imagePath));
            }
            blogImage.setImage(image);
        } catch (Exception e) {
            System.err.println("Erreur chargement image: " + e.getMessage());
        }
    }

    private String formatDate(String dateStr) {
        try {
            String[] months = {"January", "February", "March", "April", "May", "June",
                    "July", "August", "September", "October", "November", "December"};
            String[] parts = dateStr.split("-");
            if (parts.length >= 3) {
                int month = Integer.parseInt(parts[1]);
                int day = Integer.parseInt(parts[2].substring(0, 2));
                int year = Integer.parseInt(parts[0]);
                return months[month - 1] + " " + day + ", " + year;
            }
        } catch (Exception e) {
            // Fallback
        }
        return dateStr;
    }

    private void loadComments() {
        if (currentBlog == null) {
            return;
        }

        try {
            List<Commentaire> comments = commentaireService.afficherParBlog(currentBlog.getId());

            commentsContainer.getChildren().clear();

            // Mettre à jour le compteur
            if (commentCount != null) {
                commentCount.setText("(" + comments.size() + ")");
            }

            // Afficher chaque commentaire
            for (Commentaire comment : comments) {
                try {
                    FXMLLoader loader = new FXMLLoader(
                            getClass().getResource("/fxml/FrontendCommentCard.fxml")
                    );
                    VBox commentCard = loader.load();

                    FrontendCommentCardController controller = loader.getController();
                    controller.setData(comment, this);

                    commentsContainer.getChildren().add(commentCard);

                } catch (IOException e) {
                    System.err.println("Erreur chargement commentaire: " + e.getMessage());
                }
            }

            if (comments.isEmpty()) {
                Label noComments = new Label("No comments yet. Be the first to comment!");
                noComments.setStyle("-fx-text-fill: #999; -fx-font-size: 14px; -fx-padding: 20px;");
                commentsContainer.getChildren().add(noComments);
            }

        } catch (SQLException e) {
            System.err.println("Erreur chargement commentaires: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void submitComment() {
        String userName = commentUserInput.getText().trim();
        String content = commentContentInput.getText().trim();
        String imageUrl = commentImageInput.getText().trim();

        // Validation
        if (userName.isEmpty()) {
            showAlert("Error", "Please enter your name", Alert.AlertType.WARNING);
            return;
        }

        if (content.isEmpty()) {
            showAlert("Error", "Please enter a comment", Alert.AlertType.WARNING);
            return;
        }

        if (content.length() < 3) {
            showAlert("Error", "Comment must be at least 3 characters", Alert.AlertType.WARNING);
            return;
        }

        try {
            Commentaire newComment = new Commentaire();
            newComment.setContenu(content);
            newComment.setNomuser(userName);
            newComment.setImg(imageUrl.isEmpty() ? null : imageUrl);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            newComment.setDate(LocalDateTime.now().format(formatter));

            newComment.setLikesCount(0);
            newComment.setLiked(false);

            // Ajouter le commentaire
            commentaireService.ajouter(newComment, currentBlog.getId());

            // Clear form
            commentUserInput.clear();
            commentContentInput.clear();
            commentImageInput.clear();

            // Reload comments
            loadComments();

            showAlert("Success", "Comment posted successfully!", Alert.AlertType.INFORMATION);

        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().contains("trop de contenu inapproprié")) {
                showAlert("Moderation",
                        "Your comment contains inappropriate content and was rejected.\n" +
                                "Please rephrase your message respectfully.",
                        Alert.AlertType.WARNING);
            } else {
                showAlert("Error", "Error posting comment", Alert.AlertType.ERROR);
            }
            e.printStackTrace();
        }
    }

    public void deleteCommentById(int commentId) {
        try {
            commentaireService.supprimer(commentId);
            loadComments();
            showAlert("Success", "Comment deleted", Alert.AlertType.INFORMATION);
        } catch (SQLException e) {
            showAlert("Error", "Could not delete comment", Alert.AlertType.ERROR);
            e.printStackTrace();
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

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}