package edu.connexion3a8.controllers;

import edu.connexion3a8.entities.Blog;
import edu.connexion3a8.services.BlogService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class FrontendBlogController implements Initializable {

    @FXML private GridPane blogGridContainer;
    @FXML private ComboBox<String> destinationFilter;

    private BlogService blogService;
    private List<Blog> allBlogs;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        blogService = new BlogService();

        destinationFilter.getItems().addAll(
                "All Continents",
                "Africa",
                "Asia",
                "Europe",
                "North America",
                "South America",
                "Oceania",
                "Antarctica"
        );
        destinationFilter.setValue("All Continents");
        destinationFilter.setOnAction(event -> filterByDestination());

        loadPublishedBlogs();
    }

    private void loadPublishedBlogs() {
        blogGridContainer.getChildren().clear();

        try {
            allBlogs = blogService.afficher();

            List<Blog> publishedBlogs = new ArrayList<>();
            for (Blog blog : allBlogs) {
                if (blog.isStatus()) {
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
                Parent blogCard = loader.load();

                FrontendBlogCardController cardController = loader.getController();
                cardController.setData(blog, this);

                blogGridContainer.add(blogCard, column, row);

                column++;
                if (column == 3) {
                    column = 0;
                    row++;
                }
            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("Erreur lors du chargement de la carte: " + e.getMessage());
            }
        }
    }

    private void filterByDestination() {
        String selected = destinationFilter.getValue();
        if (selected == null || selected.equals("All Continents")) {
            loadPublishedBlogs();
            return;
        }

        List<Blog> filteredBlogs = new ArrayList<>();
        for (Blog blog : allBlogs) {
            if (blog.isStatus() && matchesContinent(blog, selected)) {
                filteredBlogs.add(blog);
            }
        }

        displayBlogs(filteredBlogs);
    }

    private boolean matchesContinent(Blog blog, String continent) {
        String text = getBlogSearchText(blog);
        return switch (continent) {
            case "Africa" -> containsAny(text, "africa", "morocco", "tunisia", "egypt", "kenya", "south africa", "ghana", "senegal");
            case "Asia" -> containsAny(text, "asia", "japan", "china", "india", "thailand", "indonesia", "korea", "vietnam");
            case "Europe" -> containsAny(text, "europe", "france", "italy", "spain", "germany", "greece", "portugal", "uk");
            case "North America" -> containsAny(text, "north america", "usa", "canada", "mexico", "new york", "california");
            case "South America" -> containsAny(text, "south america", "brazil", "argentina", "chile", "peru", "colombia");
            case "Oceania" -> containsAny(text, "oceania", "australia", "new zealand", "fiji", "sydney", "melbourne");
            case "Antarctica" -> containsAny(text, "antarctica");
            default -> false;
        };
    }

    private String getBlogSearchText(Blog blog) {
        String titre = blog.getTitre() != null ? blog.getTitre().toLowerCase() : "";
        String contenu = blog.getContenu() != null ? blog.getContenu().toLowerCase() : "";
        String extrait = blog.getExtrait() != null ? blog.getExtrait().toLowerCase() : "";
        String slug = blog.getSlug() != null ? blog.getSlug().toLowerCase() : "";
        return titre + " " + contenu + " " + extrait + " " + slug;
    }

    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    public void openBlogDetailView(Blog blog) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/FrontendBlogDetail.fxml"));
            Parent detailView = loader.load();

            FrontendBlogDetailController detailController = loader.getController();
            detailController.setData(blog, this);

            Scene detailScene = new Scene(detailView, 1366, 750);
            Stage stage = (Stage) blogGridContainer.getScene().getWindow();
            stage.setScene(detailScene);

        } catch (IOException e) {
            System.err.println("ERROR: Could not load detail view!");
            e.printStackTrace();
        }
    }

    public void goBackToList() {
        loadPublishedBlogs();
    }
}
