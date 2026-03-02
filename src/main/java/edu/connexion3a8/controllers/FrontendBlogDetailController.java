package edu.connexion3a8.controllers;

import edu.connexion3a8.entities.BlogRating;

import edu.connexion3a8.entities.Blog;
import edu.connexion3a8.entities.Commentaire;
import edu.connexion3a8.services.BlogSentimentScore;
import edu.connexion3a8.services.BlogTranslation;
import edu.connexion3a8.services.CommentaireService;
import edu.connexion3a8.services.RatingService;
import edu.connexion3a8.services.SentimentAnalysisService;
import edu.connexion3a8.services.SmartRecommendationService;
import edu.connexion3a8.services.TranslationService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

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
    @FXML private Label blogAuthorName;

    @FXML private TextField commentUserInput;
    @FXML private TextField commentImageInput;
    @FXML private TextArea commentContentInput;
    @FXML private VBox commentsContainer;

    @FXML private Text ratingStar1, ratingStar2, ratingStar3, ratingStar4, ratingStar5;
    @FXML private Label currentRatingLabel;
    @FXML private TextField reviewUserNameInput;
    @FXML private TextArea reviewTextInput;
    @FXML private ComboBox<String> languageComboBox;
    @FXML private Button translateBtn;
    @FXML private Button resetBtn;
    @FXML private Label sentimentLabel;
    @FXML private Label sentimentBreakdownLabel;

    private Blog currentBlog;
    private Blog originalBlog;
    private CommentaireService commentaireService;

    private int selectedRating = 0;
    private RatingService ratingService;

    private SmartRecommendationService recommendationService;
    private TranslationService translationService;
    private FrontendBlogController parentController;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        commentaireService = new CommentaireService();
        ratingService = new RatingService();
        recommendationService = new SmartRecommendationService();
        translationService = new TranslationService();
    }

    public void setData(Blog blog, FrontendBlogController frontendBlogController) {
        this.currentBlog = blog;
        this.originalBlog = blog;
        this.parentController = frontendBlogController;

        System.out.println("Setting blog data: " + blog.getTitre());

        // Titre
        blogTitle.setText(blog.getTitre());

        // Extrait
        if (blog.getExtrait() != null && !blog.getExtrait().isEmpty()) {
            blogExcerpt.setText(blog.getExtrait());
        }

        // Slug
        if (blogCategory != null) {
            String slug = blog.getSlug();
            blogCategory.setText((slug != null && !slug.isBlank()) ? slug : "no-slug");
        }

        // Auteur
        if (blogAuthorName != null) {
            String authorName = blog.getAuthor_nom();
            if (authorName == null || authorName.isBlank()) {
                authorName = blog.getAuthor_id();
            }
            if (authorName == null || authorName.isBlank()) {
                authorName = "Unknown User";
            }
            blogAuthorName.setText("by " + authorName);
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
        loadSentimentScore();

        loadRecommendations();
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
                    Parent commentCard = loader.load();

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
            loadSentimentScore();

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
            loadSentimentScore();
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

    @FXML
    public void setRating1() { setRating(1); }
    @FXML
    public void setRating2() { setRating(2); }
    @FXML
    public void setRating3() { setRating(3); }
    @FXML
    public void setRating4() { setRating(4); }
    @FXML
    public void setRating5() { setRating(5); }

    private void setRating(int rating) {
        selectedRating = rating;
        updateStarDisplay();
        currentRatingLabel.setText(rating + "/5");
    }

    private void updateStarDisplay() {
        Text[] stars = {ratingStar1, ratingStar2, ratingStar3, ratingStar4, ratingStar5};

        for (int i = 0; i < stars.length; i++) {
            if (i < selectedRating) {
                stars[i].setFill(javafx.scene.paint.Color.web("#FFD700"));
            } else {
                stars[i].setFill(javafx.scene.paint.Color.web("#ddd"));
            }
        }
    }

    @FXML
    public void submitRating() {
        String userName = reviewUserNameInput.getText().trim();
        String reviewText = reviewTextInput.getText().trim();

        if (userName.isEmpty()) {
            showAlert("Error", "Please enter your name", Alert.AlertType.WARNING);
            return;
        }

        if (selectedRating == 0) {
            showAlert("Error", "Please select a rating", Alert.AlertType.WARNING);
            return;
        }

        try {
            // Vérifier si l'utilisateur a déjà noté
            if (ratingService.hasUserRated(currentBlog.getId(), userName)) {
                showAlert("Info", "You have already rated this blog", Alert.AlertType.INFORMATION);
                return;
            }

            // Créer la note
            BlogRating rating = new BlogRating(currentBlog.getId(), userName, selectedRating, reviewText);
            ratingService.addRating(rating);

            // Réinitialiser le formulaire
            selectedRating = 0;
            reviewUserNameInput.clear();
            reviewTextInput.clear();
            updateStarDisplay();
            currentRatingLabel.setText("0/5");

            showAlert("Success", "Thank you for your rating!", Alert.AlertType.INFORMATION);

            // Recharger les données du blog pour afficher la nouvelle moyenne
            // (optionnel: recharger la page)

        } catch (SQLException e) {
            showAlert("Error", "Could not submit rating", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    public void handleTranslate() {
        String selectedLanguage = languageComboBox.getValue();
        if (selectedLanguage == null || selectedLanguage.isEmpty()) {
            showAlert("Error", "Please choose a language", Alert.AlertType.WARNING);
            return;
        }

        String languageCode;
        try {
            int startIndex = selectedLanguage.indexOf("(") + 1;
            int endIndex = selectedLanguage.indexOf(")");
            languageCode = selectedLanguage.substring(startIndex, endIndex).toLowerCase();
        } catch (Exception e) {
            showAlert("Error", "Invalid language format", Alert.AlertType.ERROR);
            return;
        }

        try {
            translateBtn.setDisable(true);
            translateBtn.setText("Translating...");

            BlogTranslation translation = translationService.translateBlog(currentBlog, languageCode);

            blogTitle.setText(translation.getTitre());
            blogExcerpt.setText(translation.getExtrait());
            blogContent.setText(translation.getContenu());

            translateBtn.setDisable(false);
            translateBtn.setText("Translate");
            showAlert("Success", "Blog translated successfully to " + selectedLanguage, Alert.AlertType.INFORMATION);
        } catch (Exception e) {
            e.printStackTrace();
            translateBtn.setDisable(false);
            translateBtn.setText("Translate");
            showAlert("Error", "Could not translate blog: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void resetTranslation() {
        if (originalBlog != null) {
            blogTitle.setText(originalBlog.getTitre());
            blogExcerpt.setText(originalBlog.getExtrait());
            blogContent.setText(originalBlog.getContenu());
            showAlert("Info", "Original version restored", Alert.AlertType.INFORMATION);
        }
    }

    private void loadSentimentScore() {
        if (currentBlog == null || sentimentLabel == null) {
            return;
        }

        try {
            List<Commentaire> comments = commentaireService.afficherParBlog(currentBlog.getId());
            if (comments.isEmpty()) {
                sentimentLabel.setText("No comments yet");
                if (sentimentBreakdownLabel != null) {
                    sentimentBreakdownLabel.setText("");
                }
                return;
            }

            SentimentAnalysisService sentimentService = new SentimentAnalysisService();
            BlogSentimentScore score = sentimentService.analyzeBlogComments(comments);

            sentimentLabel.setText(score.getEmoji() + " " + score.getGlobalScore() + "/100");
            if (sentimentBreakdownLabel != null) {
                sentimentBreakdownLabel.setText(
                        "Positive: " + score.getPositive() +
                                " | Neutral: " + score.getNeutral() +
                                " | Negative: " + score.getNegative()
                );
            }
        } catch (SQLException e) {
            sentimentLabel.setText("Sentiment unavailable");
            if (sentimentBreakdownLabel != null) {
                sentimentBreakdownLabel.setText("");
            }
            System.err.println("Erreur sentiment: " + e.getMessage());
        }
    }

    private void loadRecommendations() {
        try {
            // Utiliser un identifiant utilisateur (par exemple basé sur le nom d'utilisateur ou session)
            String userIdentifier = "user_" + System.getProperty("user.name");

            List<Blog> recommendations = recommendationService.getPersonalizedRecommendations(
                    currentBlog.getId(),
                    userIdentifier,
                    3  // 3 recommandations
            );

            // Afficher les recommandations dans l'interface
            displayRecommendations(recommendations);

        } catch (SQLException e) {
            System.err.println("Erreur chargement recommandations: " + e.getMessage());
        }
    }

    private void displayRecommendations(List<Blog> recommendations) {
        // TODO: Créer une section "You might also like" et afficher les blogs recommandés
        // Utiliser FrontendBlogCard pour chaque recommandation
    }
    @FXML
    public void goBackToBlogList() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/FrontendBlogList.fxml"));
            Parent listView = loader.load();
            Scene listScene = new Scene(listView, 1366, 750);
            Stage stage = (Stage) blogTitle.getScene().getWindow();
            stage.setScene(listScene);
        } catch (IOException e) {
            System.err.println("Erreur retour vers la liste: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
