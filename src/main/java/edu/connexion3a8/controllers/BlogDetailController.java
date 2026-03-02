package edu.connexion3a8.controllers;

import edu.connexion3a8.services.BlogTranslation;
import edu.connexion3a8.services.TranslationService;

import edu.connexion3a8.entities.Blog;
import edu.connexion3a8.entities.Commentaire;
import edu.connexion3a8.services.*;
import javafx.event.ActionEvent;
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

    @FXML
    private Text blogTitle;
    @FXML
    private Text blogExcerpt;
    @FXML
    private Text blogContent;
    @FXML
    private Label blogAuthor;
    @FXML
    private Label blogDate;
    @FXML
    private Label blogStatus;
    @FXML
    private ImageView blogCoverImage;

    // Comments Section
    @FXML
    private VBox commentsContainer;
    @FXML
    private VBox emptyCommentsState;
    @FXML
    private Label commentCountLabel;
    @FXML
    private TextField commentUserInput;
    @FXML
    private TextField commentImageInput;
    @FXML
    private TextArea commentContentInput;
    @FXML
    private Button submitCommentBtn;
    @FXML
    private Button backBtn;

    @FXML private ComboBox<String> languageComboBox;
    @FXML private Button translateBtn;
    @FXML private Button resetBtn;


    private Blog currentBlog;
    private CommentaireService commentaireService;
    private BlogListController parentController;
    private Blog originalBlog;  // Pour sauvegarder l'original
    private TranslationService translationService;

    //@Override
    /*public void initialize(URL url, ResourceBundle resourceBundle) {
        commentaireService = new CommentaireService();
    }*/


    public void setData(Blog blog, BlogListController parentController) {
        this.currentBlog = blog;
        this.parentController = parentController;
        this.originalBlog = blog;  // Sauvegarder pour reset

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
            // CHANGÉ : Utiliser afficherParBlog au lieu de afficher
            List<Commentaire> comments = commentaireService.afficherParBlog(currentBlog.getId());

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

            // Date actuelle
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            newComment.setDate(LocalDateTime.now().format(formatter));

            newComment.setLikesCount(0);
            newComment.setLiked(false);

            // CHANGÉ : Ajouter le commentaire avec l'ID du blog
            commentaireService.ajouter(newComment, currentBlog.getId());

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


    @Override
    public void initialize(URL url, ResourceBundle rb) {
        translationService = new TranslationService();
        commentaireService = new CommentaireService();
    }

    @FXML
    public void handleTranslate() {
        String selectedLanguage = languageComboBox.getValue();

        if (selectedLanguage == null || selectedLanguage.isEmpty()) {
            showAlert("Erreur", "Veuillez choisir une langue", Alert.AlertType.WARNING);
            return;
        }

        // Extraire le code de langue entre parenthèses
        // Ex: "English (EN)" -> "en"
        String languageCode;
        try {
            int startIndex = selectedLanguage.indexOf("(") + 1;
            int endIndex = selectedLanguage.indexOf(")");
            languageCode = selectedLanguage.substring(startIndex, endIndex).toLowerCase();
        } catch (Exception e) {
            showAlert("Erreur", "Format de langue invalide", Alert.AlertType.ERROR);
            return;
        }

        try {
            // Afficher un loader
            translateBtn.setDisable(true);
            translateBtn.setText("Traduction...");

            // Traduire le blog
            BlogTranslation translation = translationService.translateBlog(currentBlog, languageCode);

            // Afficher la traduction
            blogTitle.setText(translation.getTitre());
            blogExcerpt.setText(translation.getExtrait());
            blogContent.setText(translation.getContenu());

            // Réactiver le bouton
            translateBtn.setDisable(false);
            translateBtn.setText("Traduire");

            // Message de succès (optionnel)
            showAlert("Succès", "Blog traduit avec succès en " + selectedLanguage, Alert.AlertType.INFORMATION);

        } catch (Exception e) {
            e.printStackTrace();
            translateBtn.setDisable(false);
            translateBtn.setText("Traduire");
            showAlert("Erreur", "Impossible de traduire le blog: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void resetTranslation() {
        if (originalBlog != null) {
            // Restaurer la version originale
            blogTitle.setText(originalBlog.getTitre());
            blogExcerpt.setText(originalBlog.getExtrait());
            blogContent.setText(originalBlog.getContenu());

            showAlert("Info", "Version originale restaurée", Alert.AlertType.INFORMATION);
        }
    }

    @FXML
    private Label sentimentLabel;

    private void loadSentimentScore() {
        try {
            List<Commentaire> comments = commentaireService.afficherParBlog(currentBlog.getId());

            if (comments.size() > 0) {
                SentimentAnalysisService sentimentService = new SentimentAnalysisService();
                BlogSentimentScore score = sentimentService.analyzeBlogComments(comments);

                // Afficher le score
                sentimentLabel.setText(score.getEmoji() + " Score : " + score.getGlobalScore() + "/100");

                // Mettre à jour en BD
                BlogService.updateSentiment(currentBlog.getId(), score.getGlobalScore(), score.getEmoji());
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}