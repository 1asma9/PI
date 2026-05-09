package edu.connexion3a8.controllers;

import edu.connexion3a8.entities.Blog;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;

import java.io.File;
import java.time.format.DateTimeFormatter;

public class BlogCardController {

    @FXML private ImageView blogImage;
    @FXML private Text blogTitre;
    @FXML private Text blogExtrait;
    @FXML private Label blogDate;
    @FXML private Label blogStatus;
    @FXML private Button viewBtn;
    @FXML private Button editBtn;
    @FXML private Button deleteBtn;

    private Blog blog;
    private BlogListController parentController;

    public void setData(Blog blog, BlogListController parentController) {
        this.blog = blog;
        this.parentController = parentController;

        // Set titre
        blogTitre.setText(blog.getTitre());

        // Set extrait
        String extrait = blog.getExtrait();
        if (extrait != null && extrait.length() > 100) {
            extrait = extrait.substring(0, 97) + "...";
        }
        blogExtrait.setText(extrait != null ? extrait : "Pas d'extrait disponible");

        // Set date
        if (blog.getDate_publication() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy");
            blogDate.setText(blog.getDate_publication().format(formatter));
        } else if (blog.getDate_creation() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy");
            blogDate.setText(blog.getDate_creation().format(formatter));
        } else {
            blogDate.setText("Date inconnue");
        }

        // Set status
        if (blog.isStatus()) {
            blogStatus.setText("● Publié");
            blogStatus.setStyle("-fx-text-fill: #4CAF50;");
        } else {
            blogStatus.setText("● Brouillon");
            blogStatus.setStyle("-fx-text-fill: #FFA726;");
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
                // Keep default image
                System.out.println("Impossible de charger l'image: " + e.getMessage());
            }
        }
    }

    @FXML
    public void viewBlog() {
        System.out.println("Viewing blog: " + blog.getTitre());
        if (parentController != null) {
            parentController.openBlogDetailView(blog);
        } else {
            System.err.println("Parent controller is null!");
        }
    }

    @FXML
    public void editBlog() {
        if (parentController != null) {
            parentController.openEditBlogForm(blog);
        }
    }

    @FXML
    public void deleteBlog() {
        if (parentController != null) {
            parentController.showDeleteConfirmation(blog);
        }
    }
}