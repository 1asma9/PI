package edu.connexion3a8.controllers;

import edu.connexion3a8.services.AnalyticsService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.SQLException;
import java.util.Map;
import java.util.ResourceBundle;

public class DashboardController implements Initializable {

    @FXML private Label totalBlogsLabel;
    @FXML private Label totalViewsLabel;
    @FXML private Label totalCommentsLabel;
    @FXML private Label growthRateLabel;

    @FXML private LineChart<String, Number> viewsTrendChart;

    @FXML private TableView<BlogStat> topViewedTable;
    @FXML private TableColumn<BlogStat, String> topViewedTitleCol;
    @FXML private TableColumn<BlogStat, Integer> topViewedCountCol;

    @FXML private TableView<BlogStat> topCommentedTable;
    @FXML private TableColumn<BlogStat, String> topCommentedTitleCol;
    @FXML private TableColumn<BlogStat, Integer> topCommentedCountCol;

    @FXML private TableView<BlogRatingStat> topRatedTable;
    @FXML private TableColumn<BlogRatingStat, String> topRatedTitleCol;
    @FXML private TableColumn<BlogRatingStat, Double> topRatedScoreCol;

    private AnalyticsService analyticsService;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        analyticsService = new AnalyticsService();

        // Configurer les colonnes des tables
        topViewedTitleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        topViewedCountCol.setCellValueFactory(new PropertyValueFactory<>("count"));

        topCommentedTitleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        topCommentedCountCol.setCellValueFactory(new PropertyValueFactory<>("count"));

        topRatedTitleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        topRatedScoreCol.setCellValueFactory(new PropertyValueFactory<>("rating"));

        // Charger les données
        loadDashboardData();
    }

    @FXML
    public void refreshData() {
        loadDashboardData();
    }

    private void loadDashboardData() {
        try {
            // KPIs
            totalBlogsLabel.setText(String.valueOf(analyticsService.getTotalBlogs()));
            totalViewsLabel.setText(String.format("%,d", analyticsService.getTotalViews()));
            totalCommentsLabel.setText(String.valueOf(analyticsService.getTotalComments()));

            double growthRate = analyticsService.getViewsGrowthRate();
            growthRateLabel.setText(String.format("%+.1f%%", growthRate));

            // Graphique des vues
            loadViewsTrendChart();

            // Tableaux
            loadTopViewedBlogs();
            loadTopCommentedBlogs();
            loadTopRatedBlogs();

        } catch (SQLException e) {
            System.err.println("Erreur chargement dashboard: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadViewsTrendChart() throws SQLException {
        Map<String, Integer> trend = analyticsService.getGlobalViewsTrend(7);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Views");

        for (Map.Entry<String, Integer> entry : trend.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }

        viewsTrendChart.getData().clear();
        viewsTrendChart.getData().add(series);
    }

    private void loadTopViewedBlogs() throws SQLException {
        Map<String, Integer> topBlogs = analyticsService.getTopBlogsByViews(5);
        ObservableList<BlogStat> data = FXCollections.observableArrayList();

        for (Map.Entry<String, Integer> entry : topBlogs.entrySet()) {
            data.add(new BlogStat(entry.getKey(), entry.getValue()));
        }

        topViewedTable.setItems(data);
    }

    private void loadTopCommentedBlogs() throws SQLException {
        Map<String, Integer> topBlogs = analyticsService.getTopBlogsByComments(5);
        ObservableList<BlogStat> data = FXCollections.observableArrayList();

        for (Map.Entry<String, Integer> entry : topBlogs.entrySet()) {
            data.add(new BlogStat(entry.getKey(), entry.getValue()));
        }

        topCommentedTable.setItems(data);
    }

    private void loadTopRatedBlogs() throws SQLException {
        Map<String, Double> topBlogs = analyticsService.getTopBlogsByRating(5);
        ObservableList<BlogRatingStat> data = FXCollections.observableArrayList();

        for (Map.Entry<String, Double> entry : topBlogs.entrySet()) {
            data.add(new BlogRatingStat(entry.getKey(), entry.getValue()));
        }

        topRatedTable.setItems(data);
    }

    // Classes internes pour les données des tables
    public static class BlogStat {
        private String title;
        private Integer count;

        public BlogStat(String title, Integer count) {
            this.title = title;
            this.count = count;
        }

        public String getTitle() { return title; }
        public Integer getCount() { return count; }
    }

    public static class BlogRatingStat {
        private String title;
        private Double rating;

        public BlogRatingStat(String title, Double rating) {
            this.title = title;
            this.rating = rating;
        }

        public String getTitle() { return title; }
        public Double getRating() { return rating; }
    }
    @FXML
    public void closeDashboard() {
        // Récupérer la fenêtre actuelle et la fermer
        Stage stage = (Stage) totalBlogsLabel.getScene().getWindow();
        stage.close();
    }
}