package edu.connexion3a8.controllers;

import edu.connexion3a8.entities.Commentaire;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class CommentCardController {

    @FXML private ImageView userAvatar;
    @FXML private Text userName;
    @FXML private Text commentDate;
    @FXML private Text commentText;
    @FXML private Text likeIcon;
    @FXML private Label likesLabel;
    @FXML private Button deleteBtn;

    private Commentaire commentaire;
    private BlogDetailController parentController;

    public void setData(Commentaire commentaire, BlogDetailController parentController) {
        this.commentaire = commentaire;
        this.parentController = parentController;

        // Set user name
        userName.setText(commentaire.getNomuser());

        // Set comment text
        commentText.setText(commentaire.getContenu());

        // Set date (format relatif)
        commentDate.setText(formatRelativeDate(commentaire.getDate()));

        // Set likes count
        likesLabel.setText(String.valueOf(commentaire.getLikesCount()));

        // Set like icon color
        if (commentaire.isLiked()) {
            likeIcon.setFill(javafx.scene.paint.Color.web("#dc284c"));
        } else {
            likeIcon.setFill(javafx.scene.paint.Color.web("#999999"));
        }

        // Set user avatar
        if (commentaire.getImg() != null && !commentaire.getImg().isEmpty()) {
            try {
                File imageFile = new File(commentaire.getImg());
                if (imageFile.exists()) {
                    Image image = new Image(imageFile.toURI().toString());
                    userAvatar.setImage(image);
                } else {
                    Image image = new Image(commentaire.getImg());
                    userAvatar.setImage(image);
                }
            } catch (Exception e) {
                // Keep default avatar
                System.out.println("Impossible de charger l'avatar: " + e.getMessage());
            }
        }
    }

    @FXML
    public void toggleLike(MouseEvent event) {
        // Toggle liked state
        boolean newLikedState = !commentaire.isLiked();

        if (newLikedState) {
            // Like
            commentaire.setLikesCount(commentaire.getLikesCount() + 1);
            likeIcon.setFill(javafx.scene.paint.Color.web("#dc284c"));
        } else {
            // Unlike
            commentaire.setLikesCount(commentaire.getLikesCount() - 1);
            likeIcon.setFill(javafx.scene.paint.Color.web("#999999"));
        }

        commentaire.setLiked(newLikedState);
        likesLabel.setText(String.valueOf(commentaire.getLikesCount()));

        // Update in database
        if (parentController != null) {
            parentController.updateCommentLike(commentaire.getId(), newLikedState);
        }
    }

    @FXML
    public void deleteComment() {
        if (parentController != null) {
            parentController.deleteCommentById(commentaire.getId());
        }
    }

    private String formatRelativeDate(String dateStr) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime commentDateTime = LocalDateTime.parse(dateStr, formatter);
            LocalDateTime now = LocalDateTime.now();

            long minutes = ChronoUnit.MINUTES.between(commentDateTime, now);
            long hours = ChronoUnit.HOURS.between(commentDateTime, now);
            long days = ChronoUnit.DAYS.between(commentDateTime, now);

            if (minutes < 1) {
                return "À l'instant";
            } else if (minutes < 60) {
                return "Il y a " + minutes + " min";
            } else if (hours < 24) {
                return "Il y a " + hours + "h";
            } else if (days < 7) {
                return "Il y a " + days + " jour" + (days > 1 ? "s" : "");
            } else {
                DateTimeFormatter displayFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy");
                return commentDateTime.format(displayFormatter);
            }
        } catch (Exception e) {
            return dateStr;
        }
    }
}