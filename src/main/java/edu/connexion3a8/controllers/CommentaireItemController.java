package edu.connexion3a8.controllers;

import edu.connexion3a8.entities.Commentaire;
import edu.connexion3a8.services.CommentaireService;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.sql.SQLException;

public class CommentaireItemController {

    @FXML private ImageView userImage;
    @FXML private Label nomUserLabel;
    @FXML private Label dateLabel;
    @FXML private Label contenuLabel;
    @FXML private Label likesCountLabel;
    @FXML private Button likeBtn;

    private Commentaire commentaire;
    private CommentaireService service = new CommentaireService();

    public void setData(Commentaire commentaire) {
        this.commentaire = commentaire;

        nomUserLabel.setText(commentaire.getNomuser());
        dateLabel.setText(commentaire.getDate());
        contenuLabel.setText(commentaire.getContenu());
        likesCountLabel.setText(String.valueOf(commentaire.getLikesCount()));

        if (commentaire.getImg() != null) {
            userImage.setImage(new Image(commentaire.getImg()));
        }

        updateLikeIcon();
    }

    private void updateLikeIcon() {
        if (commentaire.isLiked()) {
            likeBtn.setText("❤️");
        } else {
            likeBtn.setText("♡");
        }
    }

    @FXML
    private void handleLike() {
        try {
            if (commentaire.isLiked()) {
                service.retirerLike(commentaire.getId());
                commentaire.setLiked(false);
                commentaire.setLikesCount(commentaire.getLikesCount() - 1);
            } else {
                service.ajouterLike(commentaire.getId());
                commentaire.setLiked(true);
                commentaire.setLikesCount(commentaire.getLikesCount() + 1);
            }

            likesCountLabel.setText(String.valueOf(commentaire.getLikesCount()));
            updateLikeIcon();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}