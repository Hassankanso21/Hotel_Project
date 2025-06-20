package com.example.demo.ui;

import com.example.demo.ui.dialog.BookingDialog;
import com.example.demo.ui.model.HotelRoom;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.util.Callback;
import javafx.util.Duration;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;

public class UnifiedBookingScene {

    private final TableView<HotelRoom> roomTableView = new TableView<>();
    private final ObservableList<HotelRoom> roomList = FXCollections.observableArrayList();
    private final FilteredList<HotelRoom> filteredRooms;
    private final RestTemplate restTemplate = new RestTemplate();
    private final String jwtToken;

    // UI Components
    private ComboBox<String> categoryComboBox;
    private DatePicker checkInDate;
    private DatePicker checkOutDate;
    private Spinner<Integer> minPriceSpinner;
    private Spinner<Integer> maxPriceSpinner;
    private Button searchBtn;
    private Button bookBtn;
    private Button clearBtn;
    private Label statusLabel;
    private Label stayDurationLabel;
    private Label totalCostLabel;
    private ProgressIndicator loadingIndicator;

    public UnifiedBookingScene(String jwtToken) {
        this.jwtToken = jwtToken;
        this.filteredRooms = new FilteredList<>(roomList);
    }

    public VBox getContent() {
        // Create main content container
        VBox mainContent = new VBox(25);
        mainContent.setPadding(new Insets(30));
        mainContent.getStyleClass().add("content-area");

        // Header section
        VBox headerSection = createHeaderSection();

        // Search section
        VBox searchSection = createSearchSection();

        // Results section
        VBox resultsSection = createResultsSection();

        // Action section
        HBox actionSection = createActionSection();

        // Status section
        HBox statusSection = createStatusSection();

        mainContent.getChildren().addAll(
                headerSection,
                searchSection,
                resultsSection,
                actionSection,
                statusSection
        );

        // Wrap in ScrollPane
        ScrollPane scrollPane = new ScrollPane(mainContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(false);
        scrollPane.getStyleClass().add("booking-scroll");
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        // Create wrapper
        VBox wrapper = new VBox(scrollPane);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        // Load all rooms initially
        loadAllRoomsAsync();

        return wrapper;
    }

    private VBox createHeaderSection() {
        VBox headerSection = new VBox(10);
        headerSection.setAlignment(Pos.CENTER);
        headerSection.setPadding(new Insets(0, 0, 20, 0));

        Label title = new Label("🏨 Hotel Reservation System");
        title.getStyleClass().addAll("section-title", "booking-title");

        Label subtitle = new Label("Find and book your perfect room");
        subtitle.getStyleClass().add("booking-subtitle");

        headerSection.getChildren().addAll(title, subtitle);
        return headerSection;
    }

    private VBox createSearchSection() {
        VBox searchSection = new VBox(15);
        searchSection.getStyleClass().add("search-section");
        searchSection.setPadding(new Insets(20));

        Label searchTitle = new Label("🔍 Search Criteria");
        searchTitle.getStyleClass().add("subsection-title");

        // First row: Category and Dates
        HBox firstRow = new HBox(20);
        firstRow.setAlignment(Pos.CENTER_LEFT);

        VBox categoryBox = createControlGroup("Category", createCategoryComboBox());
        VBox checkInBox = createControlGroup("Check-in Date", createCheckInDatePicker());
        VBox checkOutBox = createControlGroup("Check-out Date", createCheckOutDatePicker());

        firstRow.getChildren().addAll(categoryBox, checkInBox, checkOutBox);

        // Second row: Price Range and Stay Info
        HBox secondRow = new HBox(20);
        secondRow.setAlignment(Pos.CENTER_LEFT);

        VBox priceBox = createPriceRangeBox();
        VBox stayInfoBox = createStayInfoBox();

        secondRow.getChildren().addAll(priceBox, stayInfoBox);

        // Action buttons row
        HBox buttonRow = createSearchButtonsRow();

        searchSection.getChildren().addAll(searchTitle, firstRow, secondRow, buttonRow);
        return searchSection;
    }

    private VBox createControlGroup(String labelText, Control control) {
        VBox group = new VBox(5);
        group.setAlignment(Pos.CENTER_LEFT);

        Label label = new Label(labelText);
        label.getStyleClass().add("control-label");

        group.getChildren().addAll(label, control);
        return group;
    }

    private ComboBox<String> createCategoryComboBox() {
        categoryComboBox = new ComboBox<>();
        categoryComboBox.getItems().addAll("Any Category", "Standard", "Deluxe", "Suite", "Presidential");
        categoryComboBox.setValue("Any Category");
        categoryComboBox.getStyleClass().add("search-combo");
        categoryComboBox.setPrefWidth(150);
        return categoryComboBox;
    }

    private DatePicker createCheckInDatePicker() {
        checkInDate = new DatePicker(LocalDate.now());
        checkInDate.getStyleClass().add("search-date-picker");
        checkInDate.setPrefWidth(150);

        // Add listener to update stay duration
        checkInDate.valueProperty().addListener((obs, oldVal, newVal) -> updateStayInfo());

        return checkInDate;
    }

    private DatePicker createCheckOutDatePicker() {
        checkOutDate = new DatePicker(LocalDate.now().plusDays(1));
        checkOutDate.getStyleClass().add("search-date-picker");
        checkOutDate.setPrefWidth(150);

        // Add listener to update stay duration
        checkOutDate.valueProperty().addListener((obs, oldVal, newVal) -> updateStayInfo());

        return checkOutDate;
    }

    private VBox createPriceRangeBox() {
        VBox priceBox = new VBox(5);
        priceBox.setAlignment(Pos.CENTER_LEFT);

        Label priceLabel = new Label("Price Range (per night)");
        priceLabel.getStyleClass().add("control-label");

        HBox priceControls = new HBox(10);
        priceControls.setAlignment(Pos.CENTER_LEFT);

        minPriceSpinner = new Spinner<>(0, 1000, 0, 25);
        minPriceSpinner.setEditable(true);
        minPriceSpinner.getStyleClass().add("price-spinner");
        minPriceSpinner.setPrefWidth(100);

        Label toLabel = new Label("to");
        toLabel.getStyleClass().add("to-label");

        maxPriceSpinner = new Spinner<>(0, 1000, 500, 25);
        maxPriceSpinner.setEditable(true);
        maxPriceSpinner.getStyleClass().add("price-spinner");
        maxPriceSpinner.setPrefWidth(100);

        priceControls.getChildren().addAll(
                new Label("$"), minPriceSpinner, toLabel, new Label("$"), maxPriceSpinner
        );

        priceBox.getChildren().addAll(priceLabel, priceControls);
        return priceBox;
    }

    private VBox createStayInfoBox() {
        VBox stayInfoBox = new VBox(5);
        stayInfoBox.setAlignment(Pos.CENTER_LEFT);
        stayInfoBox.getStyleClass().add("stay-info-box");

        Label stayInfoTitle = new Label("Stay Information");
        stayInfoTitle.getStyleClass().add("control-label");

        stayDurationLabel = new Label("Duration: 1 night");
        stayDurationLabel.getStyleClass().add("stay-duration-label");

        totalCostLabel = new Label("Estimated Total: $0");
        totalCostLabel.getStyleClass().add("total-cost-label");

        stayInfoBox.getChildren().addAll(stayInfoTitle, stayDurationLabel, totalCostLabel);
        return stayInfoBox;
    }

    private HBox createSearchButtonsRow() {
        HBox buttonRow = new HBox(15);
        buttonRow.setAlignment(Pos.CENTER);
        buttonRow.setPadding(new Insets(10, 0, 0, 0));

        searchBtn = new Button("🔍 Search Rooms");
        searchBtn.getStyleClass().addAll("primary-button", "search-button");
        searchBtn.setOnAction(e -> performSearch());

        clearBtn = new Button("Clear Filters");
        clearBtn.getStyleClass().addAll("secondary-button", "clear-button");
        clearBtn.setOnAction(e -> clearSearchCriteria());

        Button refreshBtn = new Button("🔄 Refresh");
        refreshBtn.getStyleClass().addAll("secondary-button", "refresh-button");
        refreshBtn.setOnAction(e -> loadAllRoomsAsync());

        buttonRow.getChildren().addAll(searchBtn, clearBtn, refreshBtn);
        return buttonRow;
    }

    private VBox createResultsSection() {
        VBox resultsSection = new VBox(15);

        Label resultsTitle = new Label("📋 Available Rooms");
        resultsTitle.getStyleClass().add("subsection-title");

        setupTableView();

        resultsSection.getChildren().addAll(resultsTitle, roomTableView);
        return resultsSection;
    }

    private void setupTableView() {
        roomTableView.getStyleClass().add("booking-table");
        roomTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        roomTableView.setPrefHeight(350);
        roomTableView.setMinHeight(300);
        roomTableView.setRowFactory(createRowFactory());

        // Room Number Column
        TableColumn<HotelRoom, String> numberCol = new TableColumn<>("Room");
        numberCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getRoomNumber()));
        numberCol.setPrefWidth(80);
        numberCol.getStyleClass().add("room-number-column");

        // Category Column with icon
        TableColumn<HotelRoom, String> categoryCol = new TableColumn<>("Category");
        categoryCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCategory()));
        categoryCol.setCellFactory(createCategoryCellFactory());
        categoryCol.setPrefWidth(120);

        // Availability Column
        TableColumn<HotelRoom, Boolean> availCol = new TableColumn<>("Status");
        availCol.setCellValueFactory(data -> new SimpleBooleanProperty(data.getValue().isAvailable()).asObject());
        availCol.setCellFactory(createAvailabilityCellFactory());
        availCol.setPrefWidth(100);

        // Price Column
        TableColumn<HotelRoom, Double> priceCol = new TableColumn<>("Price/Night");
        priceCol.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().getPricePerNight()).asObject());
        priceCol.setCellFactory(createPriceCellFactory());
        priceCol.setPrefWidth(120);

        // Total Cost Column
        TableColumn<HotelRoom, Double> totalCol = new TableColumn<>("Total Cost");
        totalCol.setCellValueFactory(data -> new SimpleDoubleProperty(calculateTotalCost(data.getValue())).asObject());
        totalCol.setCellFactory(createTotalCostCellFactory());
        totalCol.setPrefWidth(120);

        // Actions Column
        TableColumn<HotelRoom, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setCellFactory(createActionsCellFactory());
        actionsCol.setPrefWidth(100);
        actionsCol.setSortable(false);

        roomTableView.getColumns().addAll(numberCol, categoryCol, availCol, priceCol, totalCol, actionsCol);
        roomTableView.setItems(filteredRooms);

        // Selection handling
        roomTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            updateBookButtonState();
            updateTotalCostDisplay();
        });
    }

    private Callback<TableView<HotelRoom>, TableRow<HotelRoom>> createRowFactory() {
        return tv -> {
            TableRow<HotelRoom> row = new TableRow<>();
            row.itemProperty().addListener((obs, oldRoom, newRoom) -> {
                if (newRoom != null) {
                    row.getStyleClass().removeAll("available-room-row", "occupied-room-row");
                    if (newRoom.isAvailable()) {
                        row.getStyleClass().add("available-room-row");
                    } else {
                        row.getStyleClass().add("occupied-room-row");
                    }
                } else {
                    row.getStyleClass().removeAll("available-room-row", "occupied-room-row");
                }
            });
            return row;
        };
    }

    private Callback<TableColumn<HotelRoom, String>, TableCell<HotelRoom, String>> createCategoryCellFactory() {
        return column -> new TableCell<HotelRoom, String>() {
            @Override
            protected void updateItem(String category, boolean empty) {
                super.updateItem(category, empty);
                if (empty || category == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    String icon = getCategoryIcon(category);
                    setText(icon + " " + category);
                    setStyle("-fx-font-weight: bold;");
                }
            }
        };
    }

    private Callback<TableColumn<HotelRoom, Boolean>, TableCell<HotelRoom, Boolean>> createAvailabilityCellFactory() {
        return column -> new TableCell<HotelRoom, Boolean>() {
            @Override
            protected void updateItem(Boolean available, boolean empty) {
                super.updateItem(available, empty);
                if (empty || available == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label statusLabel = new Label(available ? "Available" : "Occupied");
                    statusLabel.getStyleClass().add(available ? "status-available" : "status-occupied");
                    setGraphic(statusLabel);
                    setText(null);
                }
            }
        };
    }

    private Callback<TableColumn<HotelRoom, Double>, TableCell<HotelRoom, Double>> createPriceCellFactory() {
        return column -> new TableCell<HotelRoom, Double>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                if (empty || price == null) {
                    setText(null);
                } else {
                    setText(String.format("$%.2f", price));
                    setStyle("-fx-alignment: CENTER-RIGHT; -fx-font-weight: bold; -fx-text-fill: #2ecc71;");
                }
            }
        };
    }

    private Callback<TableColumn<HotelRoom, Double>, TableCell<HotelRoom, Double>> createTotalCostCellFactory() {
        return column -> new TableCell<HotelRoom, Double>() {
            @Override
            protected void updateItem(Double totalCost, boolean empty) {
                super.updateItem(totalCost, empty);
                if (empty || totalCost == null) {
                    setText(null);
                } else {
                    setText(String.format("$%.2f", totalCost));
                    setStyle("-fx-alignment: CENTER-RIGHT; -fx-font-weight: bold; -fx-text-fill: #3498db;");
                }
            }
        };
    }

    private Callback<TableColumn<HotelRoom, Void>, TableCell<HotelRoom, Void>> createActionsCellFactory() {
        return column -> new TableCell<HotelRoom, Void>() {
            private final Button bookButton = new Button("📅");

            {
                bookButton.getStyleClass().addAll("mini-button", "book-button");
                bookButton.setTooltip(new Tooltip("Book this room"));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HotelRoom room = getTableView().getItems().get(getIndex());
                    bookButton.setDisable(!room.isAvailable() || !isValidDateRange());
                    bookButton.setOnAction(e -> bookRoom(room));
                    setGraphic(bookButton);
                }
            }
        };
    }

    private HBox createActionSection() {
        HBox actionSection = new HBox(15);
        actionSection.setAlignment(Pos.CENTER);
        actionSection.setPadding(new Insets(15, 0, 0, 0));

        bookBtn = new Button("📅 Book Selected Room");
        bookBtn.getStyleClass().addAll("primary-button", "book-selected-button");
        bookBtn.setDisable(true);
        bookBtn.setOnAction(e -> bookSelectedRoom());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button helpBtn = new Button("❓ Need Help?");
        helpBtn.getStyleClass().addAll("secondary-button", "help-button");
        helpBtn.setOnAction(e -> showHelpDialog());

        actionSection.getChildren().addAll(bookBtn, spacer, helpBtn);
        return actionSection;
    }

    private HBox createStatusSection() {
        HBox statusSection = new HBox(10);
        statusSection.setAlignment(Pos.CENTER_LEFT);
        statusSection.setPadding(new Insets(10, 0, 0, 0));
        statusSection.getStyleClass().add("status-section");

        statusLabel = new Label("Ready to search");
        statusLabel.getStyleClass().add("status-label");

        loadingIndicator = new ProgressIndicator();
        loadingIndicator.getStyleClass().add("loading-indicator");
        loadingIndicator.setVisible(false);
        loadingIndicator.setMaxSize(20, 20);

        statusSection.getChildren().addAll(statusLabel, loadingIndicator);
        return statusSection;
    }

    private String getCategoryIcon(String category) {
        switch (category.toLowerCase()) {
            case "standard": return "🛏️";
            case "deluxe": return "🏨";
            case "suite": return "🏛️";
            case "presidential": return "👑";
            default: return "🏠";
        }
    }

    private double calculateTotalCost(HotelRoom room) {
        long nights = getStayDuration();
        return room.getPricePerNight() * nights;
    }

    private long getStayDuration() {
        if (checkInDate.getValue() != null && checkOutDate.getValue() != null &&
                checkOutDate.getValue().isAfter(checkInDate.getValue())) {
            return ChronoUnit.DAYS.between(checkInDate.getValue(), checkOutDate.getValue());
        }
        return 1;
    }

    private boolean isValidDateRange() {
        return checkInDate.getValue() != null && checkOutDate.getValue() != null &&
                checkOutDate.getValue().isAfter(checkInDate.getValue());
    }

    private void updateStayInfo() {
        long nights = getStayDuration();
        stayDurationLabel.setText("Duration: " + nights + " night" + (nights > 1 ? "s" : ""));
        updateTotalCostDisplay();
    }

    private void updateTotalCostDisplay() {
        HotelRoom selectedRoom = roomTableView.getSelectionModel().getSelectedItem();
        if (selectedRoom != null) {
            double totalCost = calculateTotalCost(selectedRoom);
            totalCostLabel.setText("Estimated Total: $" + String.format("%.2f", totalCost));
        } else {
            totalCostLabel.setText("Estimated Total: $0");
        }
    }

    private void updateBookButtonState() {
        HotelRoom selected = roomTableView.getSelectionModel().getSelectedItem();
        boolean canBook = selected != null && selected.isAvailable() && isValidDateRange();
        bookBtn.setDisable(!canBook);
    }

    private void performSearch() {
        if (!isValidDateRange()) {
            showError("Please select valid check-in and check-out dates.");
            return;
        }

        setLoading(true);
        updateStatus("Searching for available rooms...");

        Task<Void> searchTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                searchRooms();
                return null;
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    setLoading(false);
                    updateStatus("Search completed - " + filteredRooms.size() + " rooms found");

                    // Add fade-in animation
                    FadeTransition fade = new FadeTransition(Duration.millis(300), roomTableView);
                    fade.setFromValue(0.5);
                    fade.setToValue(1.0);
                    fade.play();
                });
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    setLoading(false);
                    updateStatus("Search failed");
                    showError("Error searching rooms: " + getException().getMessage());
                });
            }
        };

        Thread searchThread = new Thread(searchTask);
        searchThread.setDaemon(true);
        searchThread.start();
    }

    private void searchRooms() {
        try {
            String selectedCategory = categoryComboBox.getValue();
            String category = "Any Category".equals(selectedCategory) ? null : selectedCategory;

            UriComponentsBuilder builder = UriComponentsBuilder
                    .fromHttpUrl("http://localhost:8080/api/rooms/search")
                    .queryParam("minPrice", minPriceSpinner.getValue())
                    .queryParam("maxPrice", maxPriceSpinner.getValue())
                    .queryParam("checkIn", checkInDate.getValue())
                    .queryParam("checkOut", checkOutDate.getValue());

            if (category != null) {
                builder.queryParam("category", category);
            }

            URI uri = builder.build().toUri();
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + jwtToken);
            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

            ResponseEntity<HotelRoom[]> response = restTemplate.exchange(
                    uri, HttpMethod.GET, requestEntity, HotelRoom[].class);

            Platform.runLater(() -> {
                roomList.clear();
                if (response.getBody() != null) {
                    roomList.addAll(Arrays.asList(response.getBody()));
                }
            });
        } catch (Exception ex) {
            throw new RuntimeException("Failed to search rooms: " + ex.getMessage(), ex);
        }
    }

    private void loadAllRoomsAsync() {
        setLoading(true);
        updateStatus("Loading all available rooms...");

        Task<Void> loadTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                loadAllRooms();
                return null;
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    setLoading(false);
                    updateStatus("Rooms loaded successfully - " + roomList.size() + " rooms available");
                });
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    setLoading(false);
                    updateStatus("Failed to load rooms");
                    showError("Failed to load rooms: " + getException().getMessage());
                });
            }
        };

        Thread loadThread = new Thread(loadTask);
        loadThread.setDaemon(true);
        loadThread.start();
    }

    private void loadAllRooms() {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + jwtToken);
            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

            ResponseEntity<HotelRoom[]> response = restTemplate.exchange(
                    "http://localhost:8080/api/rooms", HttpMethod.GET, requestEntity, HotelRoom[].class);

            Platform.runLater(() -> {
                roomList.clear();
                if (response.getBody() != null) {
                    roomList.addAll(Arrays.asList(response.getBody()));
                }
            });
        } catch (Exception e) {
            throw new RuntimeException("Failed to load rooms: " + e.getMessage(), e);
        }
    }

    private void clearSearchCriteria() {
        categoryComboBox.setValue("Any Category");
        checkInDate.setValue(LocalDate.now());
        checkOutDate.setValue(LocalDate.now().plusDays(1));
        minPriceSpinner.getValueFactory().setValue(0);
        maxPriceSpinner.getValueFactory().setValue(500);

        loadAllRoomsAsync();
        updateStatus("Search criteria cleared");
    }

    private void bookRoom(HotelRoom room) {
        if (room != null && room.isAvailable() && isValidDateRange()) {
            BookingDialog.show(room.getId(), checkInDate.getValue(), checkOutDate.getValue(), jwtToken);
            // Refresh rooms after booking attempt
            Platform.runLater(() -> {
                try {
                    Thread.sleep(500); // Small delay to allow booking to process
                    loadAllRoomsAsync();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }
    }

    private void bookSelectedRoom() {
        HotelRoom selected = roomTableView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            bookRoom(selected);
        }
    }

    private void showHelpDialog() {
        Alert helpAlert = new Alert(Alert.AlertType.INFORMATION);
        helpAlert.setTitle("Booking Help");
        helpAlert.setHeaderText("How to Book a Room");
        helpAlert.setContentText(
                "1. Select your check-in and check-out dates\n" +
                        "2. Choose your preferred room category\n" +
                        "3. Set your price range if needed\n" +
                        "4. Click 'Search Rooms' to find available options\n" +
                        "5. Select a room from the table\n" +
                        "6. Click 'Book Selected Room' or use the inline book button\n\n" +
                        "Need more help? Contact our support team!"
        );

        helpAlert.getDialogPane().getStylesheets().add(
                getClass().getResource("/css/user_booking_style.css").toExternalForm()
        );
        helpAlert.getDialogPane().getStyleClass().add("alert-dialog");

        helpAlert.showAndWait();
    }

    private void setLoading(boolean loading) {
        loadingIndicator.setVisible(loading);
        searchBtn.setDisable(loading);
    }

    private void updateStatus(String message) {
        statusLabel.setText(message);
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Booking Error");
        alert.setContentText(message);

        alert.getDialogPane().getStylesheets().add(
                getClass().getResource("/css/user_booking_style.css").toExternalForm()
        );
        alert.getDialogPane().getStyleClass().add("alert-dialog");

        alert.showAndWait();
    }
}