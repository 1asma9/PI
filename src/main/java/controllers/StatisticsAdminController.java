package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.Label;
import services.ReclamationService;
import services.AvisService;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class StatisticsAdminController implements Initializable {

    @FXML
    private PieChart pieChartStatus;

    @FXML
    private BarChart<String, Number> barChartRatings;

    @FXML
    private Label lblTotalComplaints;

    @FXML
    private Label lblPendingComplaints;

    @FXML
    private Label lblAverageRating;

    @FXML
    private Label lblTotalReviews;

    private ReclamationService reclamationService = new ReclamationService();
    private AvisService avisService = new AvisService();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadStatistics();
    }

    private void loadStatistics() {
        try {
            // Load complaint statistics
            int totalComplaints = reclamationService.getTotalCount();
            int pendingComplaints = reclamationService.getCountByStatus("En attente");
            lblTotalComplaints.setText(String.valueOf(totalComplaints));
            lblPendingComplaints.setText(String.valueOf(pendingComplaints));

            // Load review statistics
            var allReviews = avisService.getAllEntities();
            int totalReviews = allReviews.size();
            double avgRating = avisService.getAverageRating();
            lblTotalReviews.setText(String.valueOf(totalReviews));
            lblAverageRating.setText(String.format("%.1f/5", avgRating));

            // Load charts
            loadComplaintStatusChart(pendingComplaints, totalComplaints - pendingComplaints);
            loadRatingsChart();

        } catch (SQLException e) {
            System.err.println("Error loading statistics: " + e.getMessage());
        }
    }

    private void loadComplaintStatusChart(int pending, int processed) {
        pieChartStatus.getData().clear();
        pieChartStatus.getData().add(new PieChart.Data("Pending", pending));
        pieChartStatus.getData().add(new PieChart.Data("Processed", processed));
    }

    private void loadRatingsChart() throws SQLException {
        barChartRatings.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Stars");

        for (int i = 1; i <= 5; i++) {
            int count = avisService.getCountByNote(i);
            series.getData().add(new XYChart.Data<>(i + "★", count));
        }

        barChartRatings.getData().add(series);
    }

    @FXML
    void retourMenu() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/admin_menu.fxml"));
            lblTotalComplaints.getScene().setRoot(root);
        } catch (IOException e) {
            System.err.println("Could not go to admin menu: " + e.getMessage());
        }
    }

    @FXML
    void goToMainMenu() {
        retourMenu();
    }
}
