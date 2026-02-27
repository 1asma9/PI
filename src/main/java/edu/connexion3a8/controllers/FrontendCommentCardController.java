package edu.connexion3a8.controllers;

import edu.connexion3a8.entities.Commentaire;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

public class FrontendCommentCardController {

    @FXML private ImageView userAvatar;
    @FXML private Text userName;
    @FXML private Text commentDate;
    @FXML private Text commentText;
    @FXML private Text likeIcon;
    @FXML private Label likesLabel;
    @FXML private Button deleteBtn;

    private Commentaire commentaire;
    private FrontendBlogDetailController parentController;

    public void setData(Commentaire commentaire, FrontendBlogDetailController parent) {
        this.commentaire = commentaire;
        this.parentController = parent;

        // Nom
        userName.setText(commentaire.getNomuser());

        // Contenu
        commentText.setText(commentaire.getContenu());

        // Date
        commentDate.setText(formatDate(commentaire.getDate()));

        // Likes
        likesLabel.setText(String.valueOf(commentaire.getLikesCount()));

        // Icône like
        if (commentaire.isLiked()) {
            likeIcon.setFill(Color.web("#FF6B6B"));
        } else {
            likeIcon.setFill(Color.web("#999999"));
        }

        // Avatar
        if (commentaire.getImg() != null && !commentaire.getImg().isEmpty()) {
            try {
                userAvatar.setImage(new Image(commentaire.getImg()));
            } catch (Exception e) {
                // Utiliser l'avatar par défaut
            }
        }
    }

    private String formatDate(String dateStr) {
        // Simple format: "2 hours ago", "1 day ago", etc.
        return dateStr;
    }

    @FXML
    public void toggleLike(MouseEvent event) {
        boolean newLikedState = !commentaire.isLiked();

        if (newLikedState) {
            commentaire.setLikesCount(commentaire.getLikesCount() + 1);
            likeIcon.setFill(Color.web("#FF6B6B"));
        } else {
            commentaire.setLikesCount(commentaire.getLikesCount() - 1);
            likeIcon.setFill(Color.web("#999999"));
        }

        commentaire.setLiked(newLikedState);
        likesLabel.setText(String.valueOf(commentaire.getLikesCount()));

        parentController.updateCommentLike(commentaire.getId(), newLikedState);
    }

    @FXML
    public void deleteComment() {
        parentController.deleteCommentById(commentaire.getId());
    }
}