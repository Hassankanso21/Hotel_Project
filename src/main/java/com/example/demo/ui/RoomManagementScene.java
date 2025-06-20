package com.example.demo.ui;

import com.example.demo.ui.dialog.RoomCreationDialog;
import com.example.demo.ui.model.HotelRoom;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.util.Callback;
import javafx.util.Duration;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.function.Predicate;

public class RoomManagementScene {

    private final TableView<HotelRoom> tableView = new TableView<>();
    private final ObservableList<HotelRoom> roomList = FXCollections.observableArrayList();
    private final FilteredList<HotelRoom> filteredRooms;
    private final RestTemplate restTemplate = new RestTemplate();
    private final String jwtToken;

    // UI Components
    private TextField searchField;
    private ComboBox<String> categoryFilter;
    private ComboBox<String> availabilityFilter;
    private Label statusLabel;
    private ProgressIndicator loadingIndicator;
    private Button refreshBtn;
    private Button createBtn;
    private Button editBtn;
    private Button deleteBtn;
    private Button bulkDeleteBtn;

    public RoomManagementScene(String jwtToken) {
        this.jwtToken = jwtToken;
        this.filteredRooms = new FilteredList<>(roomList);
    }

    public VBox getContent() {
        // Create the main content container
        VBox mainContent = new VBox(20);
        mainContent.setPadding(new Insets(25));
        mainContent.getStyleClass().add("content-area");

        // Title section
        HBox titleSection = createTitleSection();

        // Search and filter section
        VBox searchSection = createSearchSection();

        // Statistics section
        HBox statsSection = createStatsSection();

        // Table section
        VBox tableSection = createTableSection();

        // Action buttons section
        HBox actionSection = createActionSection();

        // Status section
        HBox statusSection = createStatusSection();

        mainContent.getChildren().addAll(
                titleSection,
                searchSection,
                statsSection,
                tableSection,
                actionSection,
                statusSection
        );

        // Wrap everything in a ScrollPane
        ScrollPane scrollPane = new ScrollPane(mainContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(false);
        scrollPane.getStyleClass().add("room-management-scroll");
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        // Create wrapper VBox
        VBox wrapper = new VBox(scrollPane);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        // Load data asynchronously
        loadRoomsAsync();

        return wrapper;
    }

    private HBox createTitleSection() {
        HBox titleSection = new HBox(15);
        titleSection.setAlignment(Pos.CENTER_LEFT);
        titleSection.setPadding(new Insets(0, 0, 10, 0));

        Label title = new Label("🏢 Room Management");
        title.getStyleClass().addAll("section-title", "room-management-title");

        refreshBtn = new Button("🔄");
        refreshBtn.getStyleClass().addAll("action-button", "refresh-button");
        refreshBtn.setTooltip(new Tooltip("Refresh room data"));
        refreshBtn.setOnAction(e -> loadRoomsAsync());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label roomCount = new Label();
        roomCount.getStyleClass().add("room-count-label");

        // Update room count when list changes
        roomList.addListener((javafx.collections.ListChangeListener<HotelRoom>) change -> {
            Platform.runLater(() -> {
                roomCount.setText("Total Rooms: " + roomList.size());
            });
        });

        titleSection.getChildren().addAll(title, spacer, roomCount, refreshBtn);
        return titleSection;
    }

    private VBox createSearchSection() {
        VBox searchSection = new VBox(10);
        searchSection.getStyleClass().add("search-section");
        searchSection.setPadding(new Insets(15));

        Label searchLabel = new Label("🔍 Search & Filter");
        searchLabel.getStyleClass().add("subsection-title");

        HBox searchControls = new HBox(15);
        searchControls.setAlignment(Pos.CENTER_LEFT);

        // Search field
        searchField = new TextField();
        searchField.setPromptText("Search by room number, category...");
        searchField.getStyleClass().add("search-field");
        searchField.setPrefWidth(250);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> updateFilters());

        // Category filter
        categoryFilter = new ComboBox<>();
        categoryFilter.setPromptText("Filter by Category");
        categoryFilter.getStyleClass().add("filter-combo");
        categoryFilter.setPrefWidth(150);
        categoryFilter.valueProperty().addListener((obs, oldVal, newVal) -> updateFilters());

        // Availability filter
        availabilityFilter = new ComboBox<>();
        availabilityFilter.getItems().addAll("All", "Available", "Occupied");
        availabilityFilter.setValue("All");
        availabilityFilter.getStyleClass().add("filter-combo");
        availabilityFilter.setPrefWidth(120);
        availabilityFilter.valueProperty().addListener((obs, oldVal, newVal) -> updateFilters());

        Button clearFiltersBtn = new Button("Clear Filters");
        clearFiltersBtn.getStyleClass().addAll("secondary-button");
        clearFiltersBtn.setOnAction(e -> clearFilters());

        searchControls.getChildren().addAll(
                searchField, categoryFilter, availabilityFilter, clearFiltersBtn
        );

        searchSection.getChildren().addAll(searchLabel, searchControls);
        return searchSection;
    }

    private HBox createStatsSection() {
        HBox statsSection = new HBox(20);
        statsSection.setAlignment(Pos.CENTER);
        statsSection.setPadding(new Insets(10, 0, 10, 0));

        VBox totalCard = createStatCard("Total Rooms", "0", "🏢", "total-rooms-stat");
        VBox availableCard = createStatCard("Available", "0", "✅", "available-rooms-stat");
        VBox occupiedCard = createStatCard("Occupied", "0", "🔒", "occupied-rooms-stat");
        VBox revenueCard = createStatCard("Avg. Price", "$0", "💰", "revenue-stat");

        // Update stats when room list changes
        roomList.addListener((javafx.collections.ListChangeListener<HotelRoom>) change -> {
            Platform.runLater(() -> updateStatistics(totalCard, availableCard, occupiedCard, revenueCard));
        });

        statsSection.getChildren().addAll(totalCard, availableCard, occupiedCard, revenueCard);
        return statsSection;
    }

    private VBox createStatCard(String title, String value, String icon, String styleClass) {
        VBox card = new VBox(5);
        card.getStyleClass().addAll("stat-card", styleClass);
        card.setAlignment(Pos.CENTER);
        card.setPrefWidth(150);

        Label iconLabel = new Label(icon);
        iconLabel.getStyleClass().add("stat-icon");

        Label valueLabel = new Label(value);
        valueLabel.getStyleClass().add("stat-value");

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("stat-title");

        card.getChildren().addAll(iconLabel, valueLabel, titleLabel);
        return card;
    }

    private VBox createTableSection() {
        VBox tableSection = new VBox(10);

        Label tableTitle = new Label("📋 Room Details");
        tableTitle.getStyleClass().add("subsection-title");

        setupTableView();

        // Set reasonable height for table but allow it to be taller if needed
        tableView.setPrefHeight(350);
        tableView.setMinHeight(300);

        tableSection.getChildren().addAll(tableTitle, tableView);
        return tableSection;
    }

    private void setupTableView() {
        tableView.getStyleClass().add("room-table");
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableView.setRowFactory(createRowFactory());

        // ID Column
        TableColumn<HotelRoom, Long> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(data -> new SimpleLongProperty(data.getValue().getId()).asObject());
        idCol.setPrefWidth(60);
        idCol.getStyleClass().add("id-column");

        // Room Number Column
        TableColumn<HotelRoom, String> numberCol = new TableColumn<>("Room Number");
        numberCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getRoomNumber()));
        numberCol.setPrefWidth(120);

        // Category Column
        TableColumn<HotelRoom, String> categoryCol = new TableColumn<>("Category");
        categoryCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCategory()));
        categoryCol.setPrefWidth(130);

        // Availability Column with custom cell factory
        TableColumn<HotelRoom, Boolean> availCol = new TableColumn<>("Status");
        availCol.setCellValueFactory(data -> new SimpleBooleanProperty(data.getValue().isAvailable()).asObject());
        availCol.setCellFactory(createAvailabilityCellFactory());
        availCol.setPrefWidth(100);

        // Price Column with formatting
        TableColumn<HotelRoom, Double> priceCol = new TableColumn<>("Price/Night");
        priceCol.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().getPricePerNight()).asObject());
        priceCol.setCellFactory(createPriceCellFactory());
        priceCol.setPrefWidth(120);

        // Actions Column
        TableColumn<HotelRoom, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setCellFactory(createActionsCellFactory());
        actionsCol.setPrefWidth(150);
        actionsCol.setSortable(false);

        tableView.getColumns().addAll(idCol, numberCol, categoryCol, availCol, priceCol, actionsCol);

        // Set up sorted list
        SortedList<HotelRoom> sortedRooms = new SortedList<>(filteredRooms);
        sortedRooms.comparatorProperty().bind(tableView.comparatorProperty());
        tableView.setItems(sortedRooms);

        // Selection handling
        tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            updateButtonStates();
        });
    }

    private Callback<TableView<HotelRoom>, TableRow<HotelRoom>> createRowFactory() {
        return tv -> {
            TableRow<HotelRoom> row = new TableRow<>();
            row.itemProperty().addListener((obs, oldRoom, newRoom) -> {
                if (newRoom != null) {
                    if (newRoom.isAvailable()) {
                        row.getStyleClass().removeAll("occupied-row");
                        row.getStyleClass().add("available-row");
                    } else {
                        row.getStyleClass().removeAll("available-row");
                        row.getStyleClass().add("occupied-row");
                    }
                } else {
                    row.getStyleClass().removeAll("available-row", "occupied-row");
                }
            });
            return row;
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
                    setStyle("-fx-alignment: CENTER-RIGHT;");
                }
            }
        };
    }

    private Callback<TableColumn<HotelRoom, Void>, TableCell<HotelRoom, Void>> createActionsCellFactory() {
        return column -> new TableCell<HotelRoom, Void>() {
            private final HBox actionButtons = new HBox(5);
            private final Button editButton = new Button("✏️");
            private final Button deleteButton = new Button("🗑️");

            {
                editButton.getStyleClass().addAll("mini-button", "edit-button");
                deleteButton.getStyleClass().addAll("mini-button", "delete-button");
                editButton.setTooltip(new Tooltip("Edit room"));
                deleteButton.setTooltip(new Tooltip("Delete room"));

                actionButtons.setAlignment(Pos.CENTER);
                actionButtons.getChildren().addAll(editButton, deleteButton);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HotelRoom room = getTableView().getItems().get(getIndex());
                    editButton.setOnAction(e -> editRoom(room));
                    deleteButton.setOnAction(e -> deleteRoom(room));
                    setGraphic(actionButtons);
                }
            }
        };
    }

    private HBox createActionSection() {
        HBox actionSection = new HBox(15);
        actionSection.setAlignment(Pos.CENTER_LEFT);
        actionSection.setPadding(new Insets(15, 0, 0, 0));

        createBtn = new Button("➕ Create New Room");
        createBtn.getStyleClass().addAll("primary-button", "create-button");
        createBtn.setTooltip(new Tooltip("Create a new room"));
        createBtn.setOnAction(e -> createRoom());

        editBtn = new Button("✏️ Edit Selected");
        editBtn.getStyleClass().addAll("secondary-button", "edit-button");
        editBtn.setTooltip(new Tooltip("Edit the selected room"));
        editBtn.setOnAction(e -> editSelectedRoom());

        deleteBtn = new Button("🗑️ Delete Selected");
        deleteBtn.getStyleClass().addAll("danger-button", "delete-button");
        deleteBtn.setTooltip(new Tooltip("Delete the selected room"));
        deleteBtn.setOnAction(e -> deleteSelectedRoom());

        bulkDeleteBtn = new Button("🗑️ Delete Multiple");
        bulkDeleteBtn.getStyleClass().addAll("danger-button", "bulk-delete-button");
        bulkDeleteBtn.setTooltip(new Tooltip("Delete multiple selected rooms"));
        bulkDeleteBtn.setOnAction(e -> bulkDeleteRooms());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button exportBtn = new Button("📊 Export Data");
        exportBtn.getStyleClass().addAll("secondary-button", "export-button");
        exportBtn.setTooltip(new Tooltip("Export room data to file"));
        exportBtn.setOnAction(e -> exportRoomData());

        actionSection.getChildren().addAll(
                createBtn, editBtn, deleteBtn, bulkDeleteBtn, spacer, exportBtn
        );

        updateButtonStates();
        return actionSection;
    }

    private HBox createStatusSection() {
        HBox statusSection = new HBox(10);
        statusSection.setAlignment(Pos.CENTER_LEFT);
        statusSection.setPadding(new Insets(10, 0, 0, 0));
        statusSection.getStyleClass().add("status-section");

        statusLabel = new Label("Ready");
        statusLabel.getStyleClass().add("status-label");

        loadingIndicator = new ProgressIndicator();
        loadingIndicator.getStyleClass().add("loading-indicator");
        loadingIndicator.setVisible(false);
        loadingIndicator.setMaxSize(20, 20);

        statusSection.getChildren().addAll(statusLabel, loadingIndicator);
        return statusSection;
    }

    private void updateFilters() {
        String searchText = searchField.getText().toLowerCase().trim();
        String categoryValue = categoryFilter.getValue();
        String availabilityValue = availabilityFilter.getValue();

        filteredRooms.setPredicate(room -> {
            // Search text filter
            boolean matchesSearch = searchText.isEmpty() ||
                    room.getRoomNumber().toLowerCase().contains(searchText) ||
                    room.getCategory().toLowerCase().contains(searchText);

            // Category filter
            boolean matchesCategory = categoryValue == null ||
                    categoryValue.isEmpty() ||
                    room.getCategory().equals(categoryValue);

            // Availability filter
            boolean matchesAvailability = "All".equals(availabilityValue) ||
                    ("Available".equals(availabilityValue) && room.isAvailable()) ||
                    ("Occupied".equals(availabilityValue) && !room.isAvailable());

            return matchesSearch && matchesCategory && matchesAvailability;
        });

        updateStatusLabel("Showing " + filteredRooms.size() + " of " + roomList.size() + " rooms");
    }

    private void clearFilters() {
        searchField.clear();
        categoryFilter.setValue(null);
        availabilityFilter.setValue("All");
        updateStatusLabel("Filters cleared - showing all rooms");
    }

    private void updateStatistics(VBox totalCard, VBox availableCard, VBox occupiedCard, VBox revenueCard) {
        int total = roomList.size();
        long available = roomList.stream().mapToLong(room -> room.isAvailable() ? 1 : 0).sum();
        long occupied = total - available;
        double avgPrice = roomList.stream().mapToDouble(HotelRoom::getPricePerNight).average().orElse(0.0);

        ((Label) totalCard.getChildren().get(1)).setText(String.valueOf(total));
        ((Label) availableCard.getChildren().get(1)).setText(String.valueOf(available));
        ((Label) occupiedCard.getChildren().get(1)).setText(String.valueOf(occupied));
        ((Label) revenueCard.getChildren().get(1)).setText(String.format("$%.0f", avgPrice));
    }

    private void updateButtonStates() {
        boolean hasSelection = !tableView.getSelectionModel().getSelectedItems().isEmpty();
        boolean multipleSelection = tableView.getSelectionModel().getSelectedItems().size() > 1;

        editBtn.setDisable(!hasSelection || multipleSelection);
        deleteBtn.setDisable(!hasSelection || multipleSelection);
        bulkDeleteBtn.setDisable(!multipleSelection);
    }

    private void loadRoomsAsync() {
        setLoading(true);
        updateStatusLabel("Loading rooms...");

        Task<Void> loadTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                loadRooms();
                return null;
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    setLoading(false);
                    updateStatusLabel("Rooms loaded successfully");
                    updateCategoryFilter();
                    // Add fade-in animation
                    FadeTransition fade = new FadeTransition(Duration.millis(300), tableView);
                    fade.setFromValue(0.5);
                    fade.setToValue(1.0);
                    fade.play();
                });
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    setLoading(false);
                    updateStatusLabel("Failed to load rooms");
                    showAlert("Failed to load rooms: " + getException().getMessage(), Alert.AlertType.ERROR);
                });
            }
        };

        Thread loadThread = new Thread(loadTask);
        loadThread.setDaemon(true);
        loadThread.start();
    }

    private void loadRooms() {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(jwtToken);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<HotelRoom[]> response = restTemplate.exchange(
                    "http://localhost:8080/api/rooms",
                    HttpMethod.GET,
                    entity,
                    HotelRoom[].class
            );

            Platform.runLater(() -> {
                roomList.setAll(response.getBody() != null ?
                        Arrays.asList(response.getBody()) :
                        FXCollections.observableArrayList());
            });
        } catch (Exception ex) {
            throw new RuntimeException("Failed to load rooms: " + ex.getMessage(), ex);
        }
    }

    private void updateCategoryFilter() {
        String currentValue = categoryFilter.getValue();
        categoryFilter.getItems().clear();

        roomList.stream()
                .map(HotelRoom::getCategory)
                .distinct()
                .sorted()
                .forEach(category -> categoryFilter.getItems().add(category));

        if (currentValue != null && categoryFilter.getItems().contains(currentValue)) {
            categoryFilter.setValue(currentValue);
        }
    }

    private void createRoom() {
        try {
            RoomCreationDialog.show(null, this::loadRoomsAsync);
        } catch (Exception e) {
            // Fallback: Create a simple room creation dialog if RoomCreationDialog doesn't exist
            showSimpleRoomCreationDialog();
        }
    }

    private void showSimpleRoomCreationDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Create New Room");
        dialog.setHeaderText("Enter room details");

        // Create the custom dialog content
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField roomNumber = new TextField();
        roomNumber.setPromptText("Room Number");

        ComboBox<String> category = new ComboBox<>();
        category.getItems().addAll("Standard", "Deluxe", "Suite", "Presidential");
        category.setPromptText("Select Category");

        TextField price = new TextField();
        price.setPromptText("Price per night");

        CheckBox available = new CheckBox("Available");
        available.setSelected(true);

        grid.add(new Label("Room Number:"), 0, 0);
        grid.add(roomNumber, 1, 0);
        grid.add(new Label("Category:"), 0, 1);
        grid.add(category, 1, 1);
        grid.add(new Label("Price per night:"), 0, 2);
        grid.add(price, 1, 2);
        grid.add(available, 1, 3);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Style the dialog
        dialog.getDialogPane().getStylesheets().add(
                getClass().getResource("/css/user_booking_style.css").toExternalForm()
        );
        dialog.getDialogPane().getStyleClass().add("alert-dialog");

        // Enable/Disable OK button depending on whether all fields are filled
        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.setDisable(true);

        // Validation listener
        Runnable validation = () -> {
            boolean valid = !roomNumber.getText().trim().isEmpty() &&
                    category.getValue() != null &&
                    !price.getText().trim().isEmpty();
            okButton.setDisable(!valid);
        };

        roomNumber.textProperty().addListener((obs, oldVal, newVal) -> validation.run());
        category.valueProperty().addListener((obs, oldVal, newVal) -> validation.run());
        price.textProperty().addListener((obs, oldVal, newVal) -> validation.run());

        dialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    createRoomViaAPI(
                            roomNumber.getText().trim(),
                            category.getValue(),
                            Double.parseDouble(price.getText().trim()),
                            available.isSelected()
                    );
                } catch (NumberFormatException e) {
                    showAlert("Invalid price format. Please enter a valid number.", Alert.AlertType.ERROR);
                } catch (Exception e) {
                    showAlert("Failed to create room: " + e.getMessage(), Alert.AlertType.ERROR);
                }
            }
        });
    }

    private void createRoomViaAPI(String roomNumber, String category, double pricePerNight, boolean isAvailable) {
        setLoading(true);
        updateStatusLabel("Creating room...");

        Task<Void> createTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                // Create room object as JSON string
                String roomJson = String.format(
                        "{\"roomNumber\":\"%s\",\"category\":\"%s\",\"pricePerNight\":%.2f,\"available\":%b}",
                        roomNumber, category, pricePerNight, isAvailable
                );

                HttpHeaders headers = new HttpHeaders();
                headers.setBearerAuth(jwtToken);
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<String> entity = new HttpEntity<>(roomJson, headers);

                restTemplate.exchange(
                        "http://localhost:8080/api/rooms",
                        HttpMethod.POST,
                        entity,
                        HotelRoom.class
                );
                return null;
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    setLoading(false);
                    updateStatusLabel("Room created successfully");
                    showAlert("Room created successfully!", Alert.AlertType.INFORMATION);
                    loadRoomsAsync();
                });
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    setLoading(false);
                    updateStatusLabel("Failed to create room");
                    showAlert("Failed to create room: " + getException().getMessage(), Alert.AlertType.ERROR);
                });
            }
        };

        Thread createThread = new Thread(createTask);
        createThread.setDaemon(true);
        createThread.start();
    }

    private void editRoom(HotelRoom room) {
        try {
            RoomCreationDialog.show(room, this::loadRoomsAsync);
        } catch (Exception e) {
            // Fallback: Create a simple room edit dialog if RoomCreationDialog doesn't exist
            showSimpleRoomEditDialog(room);
        }
    }

    private void showSimpleRoomEditDialog(HotelRoom room) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit Room");
        dialog.setHeaderText("Edit room details for Room " + room.getRoomNumber());

        // Create the custom dialog content
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField roomNumber = new TextField(room.getRoomNumber());

        ComboBox<String> category = new ComboBox<>();
        category.getItems().addAll("Standard", "Deluxe", "Suite", "Presidential");
        category.setValue(room.getCategory());

        TextField price = new TextField(String.valueOf(room.getPricePerNight()));

        CheckBox available = new CheckBox("Available");
        available.setSelected(room.isAvailable());

        grid.add(new Label("Room Number:"), 0, 0);
        grid.add(roomNumber, 1, 0);
        grid.add(new Label("Category:"), 0, 1);
        grid.add(category, 1, 1);
        grid.add(new Label("Price per night:"), 0, 2);
        grid.add(price, 1, 2);
        grid.add(available, 1, 3);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Style the dialog
        dialog.getDialogPane().getStylesheets().add(
                getClass().getResource("/css/user_booking_style.css").toExternalForm()
        );
        dialog.getDialogPane().getStyleClass().add("alert-dialog");

        // Enable/Disable OK button depending on whether all fields are filled
        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);

        // Validation listener
        Runnable validation = () -> {
            boolean valid = !roomNumber.getText().trim().isEmpty() &&
                    category.getValue() != null &&
                    !price.getText().trim().isEmpty();
            okButton.setDisable(!valid);
        };

        roomNumber.textProperty().addListener((obs, oldVal, newVal) -> validation.run());
        category.valueProperty().addListener((obs, oldVal, newVal) -> validation.run());
        price.textProperty().addListener((obs, oldVal, newVal) -> validation.run());

        dialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    updateRoomViaAPI(
                            room.getId(),
                            roomNumber.getText().trim(),
                            category.getValue(),
                            Double.parseDouble(price.getText().trim()),
                            available.isSelected()
                    );
                } catch (NumberFormatException e) {
                    showAlert("Invalid price format. Please enter a valid number.", Alert.AlertType.ERROR);
                } catch (Exception e) {
                    showAlert("Failed to update room: " + e.getMessage(), Alert.AlertType.ERROR);
                }
            }
        });
    }

    private void updateRoomViaAPI(Long roomId, String roomNumber, String category, double pricePerNight, boolean isAvailable) {
        setLoading(true);
        updateStatusLabel("Updating room...");

        Task<Void> updateTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                // Create room object as JSON string
                String roomJson = String.format(
                        "{\"id\":%d,\"roomNumber\":\"%s\",\"category\":\"%s\",\"pricePerNight\":%.2f,\"available\":%b}",
                        roomId, roomNumber, category, pricePerNight, isAvailable
                );

                HttpHeaders headers = new HttpHeaders();
                headers.setBearerAuth(jwtToken);
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<String> entity = new HttpEntity<>(roomJson, headers);

                restTemplate.exchange(
                        "http://localhost:8080/api/rooms/" + roomId,
                        HttpMethod.PUT,
                        entity,
                        HotelRoom.class
                );
                return null;
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    setLoading(false);
                    updateStatusLabel("Room updated successfully");
                    showAlert("Room updated successfully!", Alert.AlertType.INFORMATION);
                    loadRoomsAsync();
                });
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    setLoading(false);
                    updateStatusLabel("Failed to update room");
                    showAlert("Failed to update room: " + getException().getMessage(), Alert.AlertType.ERROR);
                });
            }
        };

        Thread updateThread = new Thread(updateTask);
        updateThread.setDaemon(true);
        updateThread.start();
    }

    private void editSelectedRoom() {
        HotelRoom selected = tableView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            editRoom(selected);
        }
    }

    private void deleteRoom(HotelRoom room) {
        Alert confirm = createStyledAlert(Alert.AlertType.CONFIRMATION,
                "Confirm Deletion",
                "Are you sure you want to delete room " + room.getRoomNumber() + "?",
                "This action cannot be undone.");

        confirm.showAndWait().ifPresent(type -> {
            if (type == ButtonType.OK) {
                performDeleteRoom(room);
            }
        });
    }

    private void deleteSelectedRoom() {
        HotelRoom selected = tableView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            deleteRoom(selected);
        }
    }

    private void bulkDeleteRooms() {
        var selectedRooms = tableView.getSelectionModel().getSelectedItems();
        if (selectedRooms.isEmpty()) return;

        Alert confirm = createStyledAlert(Alert.AlertType.CONFIRMATION,
                "Confirm Bulk Deletion",
                "Are you sure you want to delete " + selectedRooms.size() + " rooms?",
                "This action cannot be undone.");

        confirm.showAndWait().ifPresent(type -> {
            if (type == ButtonType.OK) {
                setLoading(true);
                updateStatusLabel("Deleting rooms...");

                Task<Void> deleteTask = new Task<Void>() {
                    @Override
                    protected Void call() throws Exception {
                        for (HotelRoom room : selectedRooms) {
                            performDeleteRoom(room);
                        }
                        return null;
                    }

                    @Override
                    protected void succeeded() {
                        Platform.runLater(() -> {
                            setLoading(false);
                            updateStatusLabel("Rooms deleted successfully");
                            loadRoomsAsync();
                        });
                    }

                    @Override
                    protected void failed() {
                        Platform.runLater(() -> {
                            setLoading(false);
                            updateStatusLabel("Failed to delete some rooms");
                            showAlert("Failed to delete rooms: " + getException().getMessage(), Alert.AlertType.ERROR);
                        });
                    }
                };

                Thread deleteThread = new Thread(deleteTask);
                deleteThread.setDaemon(true);
                deleteThread.start();
            }
        });
    }

    private void performDeleteRoom(HotelRoom room) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(jwtToken);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            restTemplate.exchange(
                    "http://localhost:8080/api/rooms/" + room.getId(),
                    HttpMethod.DELETE,
                    entity,
                    Void.class);

            Platform.runLater(() -> roomList.remove(room));
        } catch (Exception ex) {
            throw new RuntimeException("Failed to delete room: " + ex.getMessage(), ex);
        }
    }

    private void exportRoomData() {
        // Placeholder for export functionality
        showAlert("Export functionality coming soon!", Alert.AlertType.INFORMATION);
    }

    private void setLoading(boolean loading) {
        loadingIndicator.setVisible(loading);
        refreshBtn.setDisable(loading);
    }

    private void updateStatusLabel(String message) {
        statusLabel.setText(message);
    }

    private Alert createStyledAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);

        // Apply styles
        alert.getDialogPane().getStylesheets().add(
                getClass().getResource("/css/user_booking_style.css").toExternalForm()
        );
        alert.getDialogPane().getStyleClass().add("alert-dialog");

        return alert;
    }

    private void showAlert(String message, Alert.AlertType type) {
        Alert alert = createStyledAlert(type, "Information", null, message);
        alert.showAndWait();
    }
}