package com.example.demo.ui;

import com.example.demo.ui.model.HotelRoom;
import com.example.demo.ui.model.Reservation;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ReservationsScene {

    private final ObservableList<Reservation> masterList = FXCollections.observableArrayList();
    private final String jwtToken;
    private final RestTemplate restTemplate = new RestTemplate();

    public ReservationsScene(String jwtToken) {
        this.jwtToken = jwtToken;
    }

    public VBox getContent() {
        // Create main content container
        VBox mainContent = new VBox(15);
        mainContent.setPadding(new Insets(25));
        mainContent.getStyleClass().add("content-area");

        // Title section
        Label title = new Label("📋 Reservations Management");
        title.getStyleClass().addAll("section-title", "reservations-title");

        // Create table first so we can reference it
        TableView<Reservation> reservationTable = new TableView<>();

        // Search section
        VBox searchSection = createSearchSection(reservationTable);

        // Table section
        VBox tableSection = createTableSection(reservationTable);

        // Action buttons section
        HBox actionSection = createActionSection(reservationTable);

        // Status section
        HBox statusSection = createStatusSection();

        mainContent.getChildren().addAll(
                title,
                searchSection,
                tableSection,
                actionSection,
                statusSection
        );

        // Wrap in ScrollPane
        ScrollPane scrollPane = new ScrollPane(mainContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(false);
        scrollPane.getStyleClass().add("reservations-scroll");
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        // Create wrapper
        VBox wrapper = new VBox(scrollPane);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        return wrapper;
    }

    private VBox createSearchSection(TableView<Reservation> reservationTable) {
        VBox searchSection = new VBox(10);
        searchSection.getStyleClass().add("search-section");
        searchSection.setPadding(new Insets(15));

        Label searchLabel = new Label("🔍 Search Reservations");
        searchLabel.getStyleClass().add("subsection-title");

        TextField searchField = new TextField();
        searchField.setPromptText("Search by customer name or room ID...");
        searchField.getStyleClass().add("search-field");
        searchField.setPrefWidth(400);

        // Setup search functionality
        setupSearchFunctionality(searchField, reservationTable);

        searchSection.getChildren().addAll(searchLabel, searchField);
        return searchSection;
    }

    private VBox createTableSection(TableView<Reservation> reservationTable) {
        VBox tableSection = new VBox(10);

        Label tableTitle = new Label("📋 Reservation Details");
        tableTitle.getStyleClass().add("subsection-title");

        reservationTable.getStyleClass().add("reservations-table");
        reservationTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        reservationTable.setPrefHeight(400);
        reservationTable.setMinHeight(300);

        // Setup table columns
        setupTableColumns(reservationTable);

        tableSection.getChildren().addAll(tableTitle, reservationTable);
        return tableSection;
    }

    private void setupTableColumns(TableView<Reservation> reservationTable) {
        TableColumn<Reservation, Long> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(data -> new SimpleLongProperty(data.getValue().getId()).asObject());
        idCol.setPrefWidth(60);

        TableColumn<Reservation, String> nameCol = new TableColumn<>("Customer Name");
        nameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCustomerName()));
        nameCol.setPrefWidth(150);

        TableColumn<Reservation, String> roomCol = new TableColumn<>("Room ID");
        roomCol.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getRoomId())));
        roomCol.setPrefWidth(80);

        TableColumn<Reservation, String> inCol = new TableColumn<>("Check-in Date");
        inCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCheckInDate().toString()));
        inCol.setPrefWidth(120);

        TableColumn<Reservation, String> outCol = new TableColumn<>("Check-out Date");
        outCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCheckOutDate().toString()));
        outCol.setPrefWidth(120);

        TableColumn<Reservation, Boolean> payCol = new TableColumn<>("Payment Status");
        payCol.setCellValueFactory(data -> new SimpleBooleanProperty(data.getValue().isPaymentStatus()).asObject());
        payCol.setCellFactory(createPaymentStatusCellFactory());
        payCol.setPrefWidth(120);

        reservationTable.getColumns().addAll(idCol, nameCol, roomCol, inCol, outCol, payCol);
        reservationTable.setItems(masterList);
    }

    private Callback<TableColumn<Reservation, Boolean>, TableCell<Reservation, Boolean>> createPaymentStatusCellFactory() {
        return column -> new TableCell<Reservation, Boolean>() {
            @Override
            protected void updateItem(Boolean paid, boolean empty) {
                super.updateItem(paid, empty);
                if (empty || paid == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label statusLabel = new Label(paid ? "Paid" : "Pending");
                    statusLabel.getStyleClass().add(paid ? "payment-paid" : "payment-pending");
                    setGraphic(statusLabel);
                    setText(null);
                }
            }
        };
    }

    private void setupSearchFunctionality(TextField searchField, TableView<Reservation> reservationTable) {
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.trim().isEmpty()) {
                reservationTable.setItems(masterList);
            } else {
                String lower = newVal.toLowerCase();
                reservationTable.setItems(masterList.stream()
                        .filter(r -> r.getCustomerName().toLowerCase().contains(lower)
                                || String.valueOf(r.getRoomId()).contains(lower))
                        .collect(Collectors.toCollection(FXCollections::observableArrayList)));
            }
        });
    }

    private HBox createActionSection(TableView<Reservation> reservationTable) {
        HBox actionSection = new HBox(15);
        actionSection.setAlignment(Pos.CENTER_LEFT);
        actionSection.setPadding(new Insets(15, 0, 0, 0));

        Button loadBtn = new Button("🔄 Load Reservations");
        loadBtn.getStyleClass().addAll("primary-button", "load-button");

        Button cancelBtn = new Button("❌ Cancel Selected");
        cancelBtn.getStyleClass().addAll("danger-button", "cancel-button");
        cancelBtn.setDisable(true);

        Button viewBtn = new Button("👁️ View Room Details");
        viewBtn.getStyleClass().addAll("secondary-button", "view-button");
        viewBtn.setDisable(true);

        Button togglePaymentBtn = new Button("💰 Toggle Payment");
        togglePaymentBtn.getStyleClass().addAll("secondary-button", "payment-button");
        togglePaymentBtn.setDisable(true);

        // Setup button states based on selection
        reservationTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            boolean selected = newVal != null;
            cancelBtn.setDisable(!selected);
            viewBtn.setDisable(!selected);
            togglePaymentBtn.setDisable(!selected);
        });

        // Setup button actions
        setupButtonActions(loadBtn, cancelBtn, viewBtn, togglePaymentBtn, reservationTable);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button exportBtn = new Button("📊 Export Data");
        exportBtn.getStyleClass().addAll("secondary-button", "export-button");
        exportBtn.setOnAction(e -> exportReservationData());

        actionSection.getChildren().addAll(
                loadBtn, cancelBtn, viewBtn, togglePaymentBtn, spacer, exportBtn
        );

        return actionSection;
    }

    private void setupButtonActions(Button loadBtn, Button cancelBtn, Button viewBtn,
                                    Button togglePaymentBtn, TableView<Reservation> reservationTable) {

        loadBtn.setOnAction(e -> loadReservations(reservationTable));

        cancelBtn.setOnAction(e -> {
            Reservation selected = reservationTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                cancelReservation(selected, reservationTable);
            }
        });

        viewBtn.setOnAction(e -> {
            Reservation selected = reservationTable.getSelectionModel().getSelectedItem();
            if (selected != null && selected.getRoomId() != null) {
                fetchAndShowRoomDetails(selected.getRoomId());
            } else {
                showAlert("Room ID is missing for this reservation.");
            }
        });

        togglePaymentBtn.setOnAction(e -> {
            Reservation selected = reservationTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                togglePaymentStatus(selected, reservationTable);
            }
        });
    }

    private HBox createStatusSection() {
        HBox statusSection = new HBox(10);
        statusSection.setAlignment(Pos.CENTER_LEFT);
        statusSection.setPadding(new Insets(10, 0, 0, 0));
        statusSection.getStyleClass().add("status-section");

        Label statusLabel = new Label("Ready to load reservations");
        statusLabel.getStyleClass().add("status-label");

        ProgressIndicator loadingIndicator = new ProgressIndicator();
        loadingIndicator.getStyleClass().add("loading-indicator");
        loadingIndicator.setVisible(false);
        loadingIndicator.setMaxSize(20, 20);

        statusSection.getChildren().addAll(statusLabel, loadingIndicator);
        return statusSection;
    }

    private void loadReservations(TableView<Reservation> reservationTable) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(jwtToken);
            RequestEntity<Void> request = RequestEntity
                    .get(URI.create("http://localhost:8080/reservations"))
                    .headers(headers)
                    .build();

            ResponseEntity<Reservation[]> response = restTemplate.exchange(request, Reservation[].class);
            masterList.setAll(response.getBody() != null ? Arrays.asList(response.getBody()) : List.of());

            reservationTable.setItems(masterList);

            showAlert("Reservations loaded successfully: " + masterList.size() + " found.");
        } catch (Exception ex) {
            showAlert("Error loading reservations: " + ex.getMessage());
        }
    }

    private void cancelReservation(Reservation reservation, TableView<Reservation> reservationTable) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Cancellation");
        confirm.setHeaderText("Cancel Reservation");
        confirm.setContentText("Are you sure you want to cancel the reservation for " +
                reservation.getCustomerName() + "?");

        confirm.getDialogPane().getStylesheets().add(
                getClass().getResource("/css/user_booking_style.css").toExternalForm()
        );
        confirm.getDialogPane().getStyleClass().add("alert-dialog");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    HttpHeaders headers = new HttpHeaders();
                    headers.setBearerAuth(jwtToken);
                    HttpEntity<Void> entity = new HttpEntity<>(headers);

                    restTemplate.exchange(
                            "http://localhost:8080/reservations/" + reservation.getId(),
                            HttpMethod.DELETE,
                            entity,
                            Void.class
                    );

                    masterList.remove(reservation);
                    reservationTable.setItems(masterList);
                    showAlert("Reservation cancelled and room marked as available.");
                } catch (Exception ex) {
                    showAlert("Error cancelling reservation: " + ex.getMessage());
                }
            }
        });
    }

    private void togglePaymentStatus(Reservation reservation, TableView<Reservation> reservationTable) {
        try {
            reservation.setPaymentStatus(!reservation.isPaymentStatus());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(jwtToken);
            HttpEntity<Reservation> entity = new HttpEntity<>(reservation, headers);

            restTemplate.exchange(
                    "http://localhost:8080/reservations/" + reservation.getId(),
                    HttpMethod.PUT,
                    entity,
                    Void.class
            );

            reservationTable.refresh();
            showAlert("Payment status updated successfully.");
        } catch (Exception ex) {
            showAlert("Error updating payment status: " + ex.getMessage());
        }
    }

    private void fetchAndShowRoomDetails(Long roomId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(jwtToken);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<HotelRoom> response = restTemplate.exchange(
                    "http://localhost:8080/api/rooms/" + roomId,
                    HttpMethod.GET,
                    entity,
                    HotelRoom.class);

            HotelRoom room = response.getBody();
            if (room != null) {
                showRoomDetailsModal(room);
            } else {
                showAlert("Room not found in the system.");
            }
        } catch (HttpClientErrorException.NotFound nf) {
            showAlert("Room with ID " + roomId + " no longer exists in the system.");
        } catch (Exception ex) {
            showAlert("Failed to fetch room details: " + ex.getMessage());
        }
    }

    private void showRoomDetailsModal(HotelRoom room) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Room Details - Room " + room.getRoomNumber());

        VBox mainContent = new VBox(15);
        mainContent.setPadding(new Insets(25));
        mainContent.getStyleClass().add("room-details-dialog");

        Label titleLabel = new Label("🏨 Room Information");
        titleLabel.getStyleClass().add("dialog-title");

        // Room details form
        GridPane form = new GridPane();
        form.setHgap(15);
        form.setVgap(10);
        form.getStyleClass().add("room-form");

        Label roomNumberLabel = new Label("Room Number:");
        roomNumberLabel.getStyleClass().add("form-label");
        Label roomNumberValue = new Label(room.getRoomNumber());
        roomNumberValue.getStyleClass().add("form-value");

        Label categoryLabel = new Label("Category:");
        categoryLabel.getStyleClass().add("form-label");
        ComboBox<String> categoryBox = new ComboBox<>();
        categoryBox.getItems().addAll("Standard", "Deluxe", "Suite", "Presidential");
        categoryBox.setValue(room.getCategory());
        categoryBox.getStyleClass().add("form-combo");

        Label priceLabel = new Label("Price per Night:");
        priceLabel.getStyleClass().add("form-label");
        TextField priceField = new TextField(String.valueOf(room.getPricePerNight()));
        priceField.getStyleClass().add("form-field");

        Label availabilityLabel = new Label("Availability:");
        availabilityLabel.getStyleClass().add("form-label");
        CheckBox availabilityBox = new CheckBox("Available for booking");
        availabilityBox.setSelected(room.isAvailable());
        availabilityBox.getStyleClass().add("form-checkbox");

        form.add(roomNumberLabel, 0, 0);
        form.add(roomNumberValue, 1, 0);
        form.add(categoryLabel, 0, 1);
        form.add(categoryBox, 1, 1);
        form.add(priceLabel, 0, 2);
        form.add(priceField, 1, 2);
        form.add(availabilityLabel, 0, 3);
        form.add(availabilityBox, 1, 3);

        // Action buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);

        Button saveBtn = new Button("💾 Save Changes");
        saveBtn.getStyleClass().addAll("primary-button", "save-button");
        saveBtn.setOnAction(e -> {
            try {
                String category = categoryBox.getValue();
                double price = Double.parseDouble(priceField.getText());
                room.setCategory(category);
                room.setPricePerNight(price);
                room.setAvailable(availabilityBox.isSelected());

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.setBearerAuth(jwtToken);
                HttpEntity<HotelRoom> entity = new HttpEntity<>(room, headers);

                restTemplate.exchange(
                        "http://localhost:8080/api/rooms/" + room.getId(),
                        HttpMethod.PUT,
                        entity,
                        Void.class);

                dialog.close();
                showAlert("Room updated successfully.");
            } catch (NumberFormatException nfe) {
                showAlert("Please enter a valid price.");
            } catch (Exception ex) {
                showAlert("Error saving room: " + ex.getMessage());
            }
        });

        Button cancelBtn = new Button("❌ Cancel");
        cancelBtn.getStyleClass().addAll("secondary-button", "cancel-dialog-button");
        cancelBtn.setOnAction(e -> dialog.close());

        buttonBox.getChildren().addAll(saveBtn, cancelBtn);

        mainContent.getChildren().addAll(titleLabel, form, buttonBox);

        ScrollPane scrollPane = new ScrollPane(mainContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(false);

        Scene scene = new Scene(scrollPane, 400, 350);
        scene.getStylesheets().add(getClass().getResource("/css/user_booking_style.css").toExternalForm());

        dialog.setScene(scene);
        dialog.showAndWait();
    }

    private void exportReservationData() {
        showAlert("Export functionality coming soon!\n\nThis will allow you to export reservation data to CSV or PDF format.");
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(msg);

        alert.getDialogPane().getStylesheets().add(
                getClass().getResource("/css/user_booking_style.css").toExternalForm()
        );
        alert.getDialogPane().getStyleClass().add("alert-dialog");

        alert.showAndWait();
    }
}