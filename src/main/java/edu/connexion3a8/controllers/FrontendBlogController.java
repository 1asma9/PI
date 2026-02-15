package edu.connexion3a8.controllers;

import edu.connexion3a8.entities.Blog;
import edu.connexion3a8.services.BlogService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class FrontendBlogController implements Initializable {

    @FXML private GridPane blogGridContainer;
    @FXML private TextField searchInput;

    private BlogService blogService;
    private List<Blog> allBlogs;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        blogService = new BlogService();

        // Setup search listener
        searchInput.textProperty().addListener((observable, oldValue, newValue) -> {
            searchBlogs(newValue);
        });

        // Load all published blogs
        loadPublishedBlogs();
    }

    private void loadPublishedBlogs() {
        blogGridContainer.getChildren().clear();

        try {
            // Récupérer tous les blogs
            allBlogs = blogService.afficher();

            // Filtrer uniquement les blogs publiés
            List<Blog> publishedBlogs = new ArrayList<>();
            for (Blog blog : allBlogs) {
                if (blog.isStatus()) { // Seulement les blogs publiés
                    publishedBlogs.add(blog);
                }
            }

            displayBlogs(publishedBlogs);

        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Erreur lors du chargement des blogs: " + e.getMessage());
        }
    }

    private void displayBlogs(List<Blog> blogs) {
        blogGridContainer.getChildren().clear();

        int column = 0;
        int row = 0;

        for (Blog blog : blogs) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/FrontendBlogCard.fxml"));
                VBox blogCard = loader.load();

                FrontendBlogCardController cardController = loader.getController();
                cardController.setData(blog, this);

                blogGridContainer.add(blogCard, column, row);

                column++;
                if (column == 4) { // 4 colonnes
                    column = 0;
                    row++;
                }
            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("Erreur lors du chargement de la carte: " + e.getMessage());
            }
        }
    }

    private void searchBlogs(String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            loadPublishedBlogs();
            return;
        }

        List<Blog> filteredBlogs = new ArrayList<>();
        String searchLower = searchText.toLowerCase();

        for (Blog blog : allBlogs) {
            if (blog.isStatus() && // Seulement les blogs publiés
                    ((blog.getTitre() != null && blog.getTitre().toLowerCase().contains(searchLower)) ||
                            (blog.getContenu() != null && blog.getContenu().toLowerCase().contains(searchLower)) ||
                            (blog.getExtrait() != null && blog.getExtrait().toLowerCase().contains(searchLower)))) {
                filteredBlogs.add(blog);
            }
        }

        displayBlogs(filteredBlogs);
    }

    public void openBlogDetailView(Blog blog) {
        try {
            System.out.println("Loading detail view for: " + blog.getTitre()); // Debug

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/FrontendBlogDetail.fxml"));
            VBox detailView = loader.load();

            System.out.println("Detail view loaded successfully!"); // Debug

            FrontendBlogDetailController detailController = loader.getController();
            detailController.setData(blog, this);

            System.out.println("Setting data to detail controller..."); // Debug

            // Créer une nouvelle scène pour les détails
            Scene detailScene = new Scene(detailView, 1366, 750);

            // Obtenir le stage actuel
            Stage stage = (Stage) blogGridContainer.getScene().getWindow();
            stage.setScene(detailScene);

            System.out.println("Scene changed successfully!"); // Debug

        } catch (IOException e) {
            System.err.println("ERROR: Could not load detail view!"); // Debug
            e.printStackTrace();
        }
    }

    public void goBackToList() {
        loadPublishedBlogs();
    }
}