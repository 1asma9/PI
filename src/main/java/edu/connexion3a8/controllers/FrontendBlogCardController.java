package edu.connexion3a8.controllers;

import edu.connexion3a8.entities.Blog;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.scene.control.Label;

import java.io.File;
import java.time.format.DateTimeFormatter;

public class FrontendBlogCardController {

    @FXML private ImageView blogImage;
    @FXML private Text blogTitle;
    @FXML private Text blogExcerpt;
    @FXML private Label blogDate;

    private Blog blog;
    private FrontendBlogController parentController;

    public void setData(Blog blog, FrontendBlogController parentController) {
        this.blog = blog;
        this.parentController = parentController;

        // Set title
        blogTitle.setText(blog.getTitre());

        // Set excerpt (limité à 120 caractères)
        String extrait = blog.getExtrait();
        if (extrait != null && extrait.length() > 120) {
            extrait = extrait.substring(0, 117) + "...";
        }
        blogExcerpt.setText(extrait != null ? extrait : "");

        // Set date
        if (blog.getDate_publication() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy");
            blogDate.setText(blog.getDate_publication().format(formatter));
        } else if (blog.getDate_creation() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy");
            blogDate.setText(blog.getDate_creation().format(formatter));
        } else {
            blogDate.setText("");
        }

        // Set image
        if (blog.getImage_couverture() != null && !blog.getImage_couverture().isEmpty()) {
            try {
                File imageFile = new File(blog.getImage_couverture());
                if (imageFile.exists()) {
                    Image image = new Image(imageFile.toURI().toString());
                    blogImage.setImage(image);
                } else {
                    // Try as URL
                    Image image = new Image(blog.getImage_couverture());
                    blogImage.setImage(image);
                }
            } catch (Exception e) {
                System.out.println("Impossible de charger l'image: " + e.getMessage());
            }
        }
    }

    @FXML
    public void openBlogDetail(MouseEvent event) {
        System.out.println("Card clicked! Opening blog: " + blog.getTitre()); // Debug

        if (parentController != null) {
            System.out.println("Parent controller found, opening detail view..."); // Debug
            parentController.openBlogDetailView(blog);
        } else {
            System.err.println("ERROR: Parent controller is null!"); // Debug
        }
    }
}