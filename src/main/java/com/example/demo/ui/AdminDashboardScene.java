package com.example.demo.ui;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

public class AdminDashboardScene {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String jwtToken;

    // Statistics data holders
    private long totalRooms = 0;
    private long availableRooms = 0;
    private long totalReservations = 0;
    private long activeReservations = 0;

    public AdminDashboardScene(String jwtToken) {
        this.jwtToken = jwtToken;
    }

    public VBox getContent() {
        VBox mainLayout = new VBox(20);
        mainLayout.setPadding(new Insets(30));
        mainLayout.setAlignment(Pos.TOP_CENTER);
        mainLayout.getStyleClass().add("content-area");

        // Dashboard title
        Label title = createStyledTitle();

        // Statistics cards container
        HBox statsContainer = createStatsContainer();

        // Chart section
        VBox chartSection = createChartSection();

        // Add loading indicator
        ProgressIndicator loadingIndicator = new ProgressIndicator();
        loadingIndicator.getStyleClass().add("loading-indicator");
        loadingIndicator.setVisible(true);

        mainLayout.getChildren().addAll(title, loadingIndicator);

        // Load data asynchronously
        loadDashboardData(mainLayout, statsContainer, chartSection, loadingIndicator);

        return mainLayout;
    }

    private Label createStyledTitle() {
        Label title = new Label("📊 Admin Dashboard");
        title.getStyleClass().addAll("section-title", "dashboard-title");
        return title;
    }

    private HBox createStatsContainer() {
        HBox container = new HBox(20);
        container.setAlignment(Pos.CENTER);
        container.setPadding(new Insets(0, 0, 20, 0));

        return container;
    }

    private VBox createStatCard(String title, String value, String iconText, String cardClass) {
        VBox card = new VBox(8);
        card.getStyleClass().addAll("stat-card", cardClass);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(20));
        card.setMinWidth(180);
        card.setMaxWidth(200);

        // Icon
        Label icon = new Label(iconText);
        icon.getStyleClass().add("stat-icon");

        // Value
        Label valueLabel = new Label(value);
        valueLabel.getStyleClass().add("stat-value");

        // Title
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("stat-title");

        card.getChildren().addAll(icon, valueLabel, titleLabel);
        return card;
    }

    private VBox createChartSection() {
        VBox chartSection = new VBox(15);
        chartSection.setAlignment(Pos.CENTER);
        chartSection.getStyleClass().add("chart-section");

        Label chartTitle = new Label("Room Availability Overview");
        chartTitle.getStyleClass().addAll("chart-section-title");

        PieChart pieChart = createStyledPieChart();

        chartSection.getChildren().addAll(chartTitle, pieChart);
        return chartSection;
    }

    private PieChart createStyledPieChart() {
        PieChart pieChart = new PieChart();
        pieChart.setTitle("");
        pieChart.setLabelsVisible(true);
        pieChart.setLegendVisible(true);
        pieChart.getStyleClass().add("dashboard-pie-chart");
        pieChart.setMaxSize(400, 400);
        pieChart.setMinSize(300, 300);

        return pieChart;
    }

    private void loadDashboardData(VBox mainLayout, HBox statsContainer, VBox chartSection, ProgressIndicator loadingIndicator) {
        Task<Void> dataTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                // Fetch all data
                fetchStatistics();
                return null;
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    // Hide loading indicator
                    loadingIndicator.setVisible(false);

                    // Create and populate stat cards
                    populateStatsContainer(statsContainer);

                    // Update chart
                    updatePieChart(chartSection);

                    // Add components to main layout
                    mainLayout.getChildren().addAll(statsContainer, chartSection);
                });
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    loadingIndicator.setVisible(false);
                    showErrorState(mainLayout);
                });
            }
        };

        Thread dataThread = new Thread(dataTask);
        dataThread.setDaemon(true);
        dataThread.start();
    }

    private void fetchStatistics() throws Exception {
        totalRooms = restTemplate.exchange(
                "http://localhost:8080/api/rooms/count",
                HttpMethod.GET,
                getEntity(),
                Long.class
        ).getBody();

        availableRooms = restTemplate.exchange(
                "http://localhost:8080/api/rooms/available/count",
                HttpMethod.GET,
                getEntity(),
                Long.class
        ).getBody();

        totalReservations = restTemplate.exchange(
                "http://localhost:8080/reservations/count",
                HttpMethod.GET,
                getEntity(),
                Long.class
        ).getBody();

        activeReservations = restTemplate.exchange(
                "http://localhost:8080/reservations/active/count",
                HttpMethod.GET,
                getEntity(),
                Long.class
        ).getBody();
    }

    private void populateStatsContainer(HBox container) {
        // Calculate occupancy rate
        double occupancyRate = totalRooms > 0 ?
                ((double)(totalRooms - availableRooms) / totalRooms) * 100 : 0;

        VBox roomsCard = createStatCard("Total Rooms", String.valueOf(totalRooms), "🏢", "rooms-card");
        VBox availableCard = createStatCard("Available", String.valueOf(availableRooms), "✅", "available-card");
        VBox reservationsCard = createStatCard("Total Bookings", String.valueOf(totalReservations), "📋", "reservations-card");
        VBox activeCard = createStatCard("Active Bookings", String.valueOf(activeReservations), "🔥", "active-card");
        VBox occupancyCard = createStatCard("Occupancy Rate", String.format("%.1f%%", occupancyRate), "📊", "occupancy-card");

        container.getChildren().addAll(roomsCard, availableCard, reservationsCard, activeCard, occupancyCard);
    }

    private void updatePieChart(VBox chartSection) {
        PieChart pieChart = (PieChart) chartSection.getChildren().get(1);

        long occupiedRooms = totalRooms - availableRooms;

        if (totalRooms > 0) {
            PieChart.Data availableSlice = new PieChart.Data("Available (" + availableRooms + ")", availableRooms);
            PieChart.Data occupiedSlice = new PieChart.Data("Occupied (" + occupiedRooms + ")", occupiedRooms);

            pieChart.getData().addAll(availableSlice, occupiedSlice);

            // Apply colors after the chart is rendered
            Platform.runLater(() -> {
                if (availableSlice.getNode() != null) {
                    availableSlice.getNode().setStyle("-fx-pie-color: #00ff99;");
                }
                if (occupiedSlice.getNode() != null) {
                    occupiedSlice.getNode().setStyle("-fx-pie-color: #ff4d4d;");
                }
            });
        } else {
            // No data available
            PieChart.Data noDataSlice = new PieChart.Data("No Data", 1);
            pieChart.getData().add(noDataSlice);
            Platform.runLater(() -> {
                if (noDataSlice.getNode() != null) {
                    noDataSlice.getNode().setStyle("-fx-pie-color: #cccccc;");
                }
            });
        }
    }

    private void showErrorState(VBox mainLayout) {
        VBox errorContainer = new VBox(15);
        errorContainer.setAlignment(Pos.CENTER);
        errorContainer.getStyleClass().add("error-container");

        Label errorIcon = new Label("⚠️");
        errorIcon.getStyleClass().add("error-icon");

        Label errorMessage = new Label("Unable to load dashboard data");
        errorMessage.getStyleClass().add("error-message");

        Label errorDetail = new Label("Please check your connection and try again");
        errorDetail.getStyleClass().add("error-detail");

        errorContainer.getChildren().addAll(errorIcon, errorMessage, errorDetail);
        mainLayout.getChildren().add(errorContainer);
    }

    private HttpEntity<String> getEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + jwtToken);
        return new HttpEntity<>(headers);
    }
}