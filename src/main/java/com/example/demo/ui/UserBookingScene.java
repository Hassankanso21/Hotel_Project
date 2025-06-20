package com.example.demo.ui;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class UserBookingScene {

    private final String username;
    private final String jwtToken;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObservableList<String> categories = FXCollections.observableArrayList("Any", "Standard", "Deluxe", "Suite");

    // Store references for updates
    private TableView<Map<String, Object>> resultsTable;
    private Label resultsCount;
    private Button bookButton;
    private DatePicker checkInDate;
    private DatePicker checkOutDate;

    public UserBookingScene(String username, String jwtToken) {
        this.username = username;
        this.jwtToken = jwtToken;
    }

    public VBox getContent() {
        // Create the main content
        VBox mainContent = new VBox(20);
        mainContent.setPadding(new Insets(25));
        mainContent.setAlignment(Pos.TOP_CENTER);
        mainContent.getStyleClass().add("user-content-area");

        // Title Section
        VBox titleSection = createTitleSection();

        // Search Form
        VBox searchForm = createSearchForm();

        // Results Section (this creates the table)
        VBox resultsSection = createResultsSection();

        // Book Button Section (this needs table to be created first)
        HBox bookButtonSection = createBookButtonSection();

        // Add ALL sections to main content
        mainContent.getChildren().addAll(titleSection, searchForm, resultsSection, bookButtonSection);

        // Set up the table listener AFTER everything is created
        setupTableListener();

        // Wrap the entire content in a ScrollPane for scene-level scrolling
        ScrollPane sceneScrollPane = new ScrollPane(mainContent);
        sceneScrollPane.setFitToWidth(true);
        sceneScrollPane.setFitToHeight(false); // Allow vertical scrolling
        sceneScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        sceneScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        sceneScrollPane.getStyleClass().add("user-scene-scroll");

        // Create a container VBox to return
        VBox container = new VBox();
        container.getChildren().add(sceneScrollPane);
        VBox.setVgrow(sceneScrollPane, Priority.ALWAYS);

        System.out.println("Scene-level ScrollPane created for entire content");

        return container;
    }

    private void setupTableListener() {
        if (resultsTable != null && bookButton != null) {
            resultsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                bookButton.setDisable(newSelection == null);
                System.out.println("Table selection changed: " + (newSelection != null ? "selected" : "none"));
            });
            System.out.println("Table listener set up successfully");
        } else {
            System.out.println("ERROR: Table or button is null - Table: " + resultsTable + ", Button: " + bookButton);
        }
    }

    private VBox createTitleSection() {
        VBox titleSection = new VBox(8);
        titleSection.setAlignment(Pos.CENTER);

        Label title = new Label("🔍 Search & Book Rooms");
        title.getStyleClass().add("user-booking-title");
        title.setAlignment(Pos.CENTER);
        title.setMaxWidth(Double.MAX_VALUE);

        Label subtitle = new Label("Find the perfect room for your stay");
        subtitle.getStyleClass().add("user-booking-subtitle");
        subtitle.setAlignment(Pos.CENTER);
        subtitle.setMaxWidth(Double.MAX_VALUE);

        titleSection.getChildren().addAll(title, subtitle);
        return titleSection;
    }

    private VBox createSearchForm() {
        VBox formContainer = new VBox(20);
        formContainer.getStyleClass().add("user-search-form");
        formContainer.setAlignment(Pos.TOP_CENTER);

        Label formTitle = new Label("Search Criteria");
        formTitle.getStyleClass().add("user-results-title");

        // Form controls
        ComboBox<String> categoryCombo = new ComboBox<>(categories);
        categoryCombo.getSelectionModel().selectFirst();
        categoryCombo.getStyleClass().add("user-combo-box");

        checkInDate = new DatePicker(LocalDate.now());
        checkInDate.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(LocalDate.now()));
            }
        });
        checkInDate.getStyleClass().add("user-date-picker");

        checkOutDate = new DatePicker(LocalDate.now().plusDays(1));
        checkOutDate.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                LocalDate checkIn = checkInDate.getValue();
                LocalDate minDate = (checkIn != null) ? checkIn.plusDays(1) : LocalDate.now().plusDays(1);
                setDisable(empty || date.isBefore(minDate));
            }
        });
        checkOutDate.getStyleClass().add("user-date-picker");

        Button searchBtn = new Button("🔍 Search Available Rooms");
        searchBtn.getStyleClass().add("user-search-button");

        // Form layout
        GridPane form = new GridPane();
        form.setHgap(20);
        form.setVgap(15);
        form.setAlignment(Pos.CENTER);

        Label catLabel = new Label("Room Category:");
        Label inLabel = new Label("Check-in Date:");
        Label outLabel = new Label("Check-out Date:");

        catLabel.getStyleClass().add("user-form-label");
        inLabel.getStyleClass().add("user-form-label");
        outLabel.getStyleClass().add("user-form-label");

        form.add(catLabel, 0, 0);
        form.add(categoryCombo, 1, 0);
        form.add(inLabel, 0, 1);
        form.add(checkInDate, 1, 1);
        form.add(outLabel, 0, 2);
        form.add(checkOutDate, 1, 2);

        // Center the search button
        HBox buttonContainer = new HBox();
        buttonContainer.setAlignment(Pos.CENTER);
        buttonContainer.getChildren().add(searchBtn);

        formContainer.getChildren().addAll(formTitle, form, buttonContainer);

        // Search button action
        searchBtn.setOnAction(e -> {
            String selectedCategory = categoryCombo.getValue().equals("Any") ? null : categoryCombo.getValue();
            LocalDate in = checkInDate.getValue();
            LocalDate out = checkOutDate.getValue();

            if (in == null || out == null || !in.isBefore(out)) {
                showStyledAlert("Invalid Dates", "Check-in date must be before check-out date.", Alert.AlertType.WARNING);
                return;
            }

            searchRooms(selectedCategory, in, out);
        });

        return formContainer;
    }

    private VBox createResultsSection() {
        VBox resultsSection = new VBox(15);
        resultsSection.getStyleClass().add("user-results-section");

        HBox headerBox = new HBox(15);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        Label resultsTitle = new Label("Available Rooms");
        resultsTitle.getStyleClass().add("user-results-title");

        resultsCount = new Label("Search to see available rooms");
        resultsCount.getStyleClass().add("user-results-count");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        headerBox.getChildren().addAll(resultsTitle, resultsCount, spacer);

        // Create table WITHOUT ScrollPane since we have scene-level scrolling
        resultsTable = createResultsTable();
        resultsTable.setPrefHeight(300); // Fixed height for table
        resultsTable.setMaxHeight(300);

        resultsSection.getChildren().addAll(headerBox, resultsTable);

        System.out.println("Results section created with direct table (no table ScrollPane)");

        return resultsSection;
    }

    private TableView<Map<String, Object>> createResultsTable() {
        TableView<Map<String, Object>> table = new TableView<>();
        table.getStyleClass().add("user-results-table");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        ObservableList<Map<String, Object>> searchResults = FXCollections.observableArrayList();
        table.setItems(searchResults);

        // Room ID Column
        TableColumn<Map<String, Object>, String> idCol = new TableColumn<>("Room ID");
        idCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get("id").toString()));
        idCol.getStyleClass().add("user-room-id-column");
        idCol.setMaxWidth(100);
        idCol.setMinWidth(80);

        // Category Column
        TableColumn<Map<String, Object>, String> catCol = new TableColumn<>("Category");
        catCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get("category").toString()));
        catCol.getStyleClass().add("user-category-column");

        // Price Column
        TableColumn<Map<String, Object>, String> priceCol = new TableColumn<>("Price per Night");
        priceCol.setCellValueFactory(data -> new SimpleStringProperty("$" + data.getValue().get("pricePerNight").toString()));
        priceCol.getStyleClass().add("user-price-column");
        priceCol.setMaxWidth(150);
        priceCol.setMinWidth(120);

        // Features Column
        TableColumn<Map<String, Object>, String> featuresCol = new TableColumn<>("Features");
        featuresCol.setCellValueFactory(data -> {
            Map<String, Object> room = data.getValue();
            StringBuilder features = new StringBuilder();

            if (Boolean.TRUE.equals(room.get("hasWifi"))) features.append("📶 WiFi ");
            if (Boolean.TRUE.equals(room.get("hasAirConditioning"))) features.append("❄️ AC ");
            if (Boolean.TRUE.equals(room.get("hasBreakfast"))) features.append("🍳 Breakfast ");

            return new SimpleStringProperty(features.toString().trim());
        });

        table.getColumns().addAll(idCol, catCol, priceCol, featuresCol);

        // Set placeholder for empty table
        Label placeholder = new Label("🏨 No rooms found\nTry searching for available rooms");
        placeholder.getStyleClass().add("user-empty-message");
        placeholder.setStyle("-fx-text-alignment: center;");
        table.setPlaceholder(placeholder);

        return table;
    }

    private HBox createBookButtonSection() {
        HBox buttonSection = new HBox();
        buttonSection.setAlignment(Pos.CENTER);
        buttonSection.setPadding(new Insets(15, 0, 0, 0));
        buttonSection.getStyleClass().add("user-controls-section"); // Add styling

        bookButton = new Button("📋 Book Selected Room");
        bookButton.getStyleClass().add("user-book-button");
        bookButton.setDisable(true);

        // Set minimum height to ensure visibility
        bookButton.setMinHeight(50);
        bookButton.setPrefHeight(50);

        bookButton.setOnAction(e -> {
            Map<String, Object> selected = resultsTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showStyledAlert("No Selection", "Please select a room to book.", Alert.AlertType.WARNING);
                return;
            }

            LocalDate checkIn = checkInDate.getValue();
            LocalDate checkOut = checkOutDate.getValue();

            if (checkIn == null || checkOut == null) {
                showStyledAlert("Missing Dates", "Please set check-in and check-out dates.", Alert.AlertType.WARNING);
                return;
            }

            showBookingConfirmation(selected, checkIn, checkOut);
        });

        buttonSection.getChildren().add(bookButton);

        // Set minimum height for the section too
        buttonSection.setMinHeight(70);
        buttonSection.setPrefHeight(70);

        // Debug: Print to console to verify this method is called
        System.out.println("Book button section created with button: " + bookButton.getText());

        return buttonSection;
    }

    private void searchRooms(String category, LocalDate checkIn, LocalDate checkOut) {
        try {
            UriComponentsBuilder builder = UriComponentsBuilder
                    .fromHttpUrl("http://localhost:8080/api/rooms/search")
                    .queryParam("checkIn", checkIn)
                    .queryParam("checkOut", checkOut);

            if (category != null) {
                builder.queryParam("category", category);
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(jwtToken);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<Map[]> response = restTemplate.exchange(
                    builder.toUriString(), HttpMethod.GET, entity, Map[].class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                resultsTable.getItems().setAll(response.getBody());

                // Update results count
                int count = response.getBody().length;
                resultsCount.setText(count + " room" + (count != 1 ? "s" : "") + " found");

            } else {
                resultsTable.getItems().clear();
                resultsCount.setText("No rooms found");
            }
        } catch (Exception ex) {
            showStyledAlert("Search Error", "Error searching for rooms: " + ex.getMessage(), Alert.AlertType.ERROR);
            resultsTable.getItems().clear();
            resultsCount.setText("Search failed");
        }
    }

    private void showBookingConfirmation(Map<String, Object> room, LocalDate checkIn, LocalDate checkOut) {
        Long roomId = Long.parseLong(room.get("id").toString());
        String category = room.get("category").toString();
        String price = room.get("pricePerNight").toString();

        // Calculate total cost
        long days = java.time.temporal.ChronoUnit.DAYS.between(checkIn, checkOut);
        double totalCost = Double.parseDouble(price) * days;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Booking");
        confirm.setHeaderText("Please confirm your reservation");
        confirm.setContentText(
                "🏨 Room ID: " + roomId + "\n" +
                        "🏷️ Category: " + category + "\n" +
                        "💰 Price per night: $" + price + "\n" +
                        "📅 Check-in: " + checkIn + "\n" +
                        "📅 Check-out: " + checkOut + "\n" +
                        "🗓️ Duration: " + days + " night" + (days != 1 ? "s" : "") + "\n" +
                        "💵 Total Cost: $" + String.format("%.2f", totalCost) + "\n\n" +
                        "Proceed with booking?"
        );

        // Style the confirmation dialog
        confirm.getDialogPane().getStylesheets().add(
                getClass().getResource("/css/user_booking_style.css").toExternalForm()
        );
        confirm.getDialogPane().getStyleClass().add("user-confirmation-dialog");

        confirm.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                performBooking(roomId, checkIn, checkOut);
            }
        });
    }

    private void performBooking(Long roomId, LocalDate checkIn, LocalDate checkOut) {
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("roomId", roomId);
            requestBody.put("customerName", username);
            requestBody.put("checkInDate", checkIn.toString());
            requestBody.put("checkOutDate", checkOut.toString());
            requestBody.put("paymentStatus", false);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(jwtToken);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(
                    "http://localhost:8080/reservations", request, String.class);

            if (response.getStatusCode() == HttpStatus.CREATED) {
                showStyledAlert("Booking Successful! 🎉",
                        "Your reservation has been confirmed!\n\n" +
                                "You can view your booking details in the 'My Reservations' tab.\n" +
                                "Thank you for choosing our hotel!",
                        Alert.AlertType.INFORMATION);

                // Clear the search results to encourage new search
                resultsTable.getItems().clear();
                resultsCount.setText("Search to see available rooms");
            } else {
                showStyledAlert("Booking Failed",
                        "Failed to create booking. Status: " + response.getStatusCode(),
                        Alert.AlertType.ERROR);
            }
        } catch (Exception ex) {
            showStyledAlert("Booking Error",
                    "An error occurred while processing your booking:\n" + ex.getMessage(),
                    Alert.AlertType.ERROR);
        }
    }

    private void showStyledAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        // Apply styling
        alert.getDialogPane().getStylesheets().add(
                getClass().getResource("/css/user_booking_style.css").toExternalForm()
        );
        alert.getDialogPane().getStyleClass().add("user-confirmation-dialog");

        alert.showAndWait();
    }
}