package com.example.demo.ui;

import com.example.demo.ui.model.Reservation;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;

public class UserReservationsScene {

    private final String username;
    private final String jwtToken;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObservableList<Reservation> reservations = FXCollections.observableArrayList();

    // Store references for updates
    private Label totalValueLabel;
    private Label upcomingValueLabel;
    private Label completedValueLabel;
    private Label reservationCountLabel;
    private TableView<Reservation> reservationsTable;
    private Button viewDetailsBtn;
    private Button cancelBtn;
    private Button payBtn;

    public UserReservationsScene(String username, String jwtToken) {
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

        // Stats Section
        HBox statsSection = createStatsSection();

        // Reservations Table Section
        VBox tableSection = createTableSection();

        // Control Buttons Section
        HBox controlsSection = createControlsSection();

        // Add all sections to main content
        mainContent.getChildren().addAll(titleSection, statsSection, tableSection, controlsSection);

        // Auto-load reservations
        loadReservations();

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

        System.out.println("Reservations scene-level ScrollPane created for entire content");

        return container;
    }

    private VBox createTitleSection() {
        VBox titleSection = new VBox(8);
        titleSection.setAlignment(Pos.CENTER);

        Label title = new Label("📋 My Reservations");
        title.getStyleClass().add("user-reservations-title");
        title.setAlignment(Pos.CENTER);
        title.setMaxWidth(Double.MAX_VALUE);

        Label subtitle = new Label("Manage your hotel bookings");
        subtitle.getStyleClass().add("user-reservations-subtitle");
        subtitle.setAlignment(Pos.CENTER);
        subtitle.setMaxWidth(Double.MAX_VALUE);

        titleSection.getChildren().addAll(title, subtitle);
        return titleSection;
    }

    private HBox createStatsSection() {
        HBox statsContainer = new HBox(15);
        statsContainer.setAlignment(Pos.CENTER);
        statsContainer.getStyleClass().add("user-stats-section");

        // Total Reservations Card
        VBox totalCard = createStatCard("Total Bookings", "0", "📊", "user-total-reservations-card");
        totalValueLabel = (Label) totalCard.getChildren().get(1); // Get the value label

        // Upcoming Reservations Card
        VBox upcomingCard = createStatCard("Upcoming", "0", "📅", "user-upcoming-reservations-card");
        upcomingValueLabel = (Label) upcomingCard.getChildren().get(1);

        // Past Reservations Card
        VBox completedCard = createStatCard("Completed", "0", "✅", "user-past-reservations-card");
        completedValueLabel = (Label) completedCard.getChildren().get(1);

        statsContainer.getChildren().addAll(totalCard, upcomingCard, completedCard);

        return statsContainer;
    }

    private VBox createStatCard(String title, String value, String icon, String cardClass) {
        VBox card = new VBox(8);
        card.getStyleClass().addAll("user-stat-card", cardClass);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(15));
        card.setMinWidth(150);
        card.setMaxWidth(180);

        // Icon
        Label iconLabel = new Label(icon);
        iconLabel.getStyleClass().add("user-stat-icon");

        // Value
        Label valueLabel = new Label(value);
        valueLabel.getStyleClass().add("user-stat-value");

        // Title
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("user-stat-title");

        card.getChildren().addAll(iconLabel, valueLabel, titleLabel);
        return card;
    }

    private VBox createTableSection() {
        VBox tableSection = new VBox(15);
        tableSection.getStyleClass().add("user-table-section");

        HBox headerBox = new HBox(15);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        Label tableTitle = new Label("Your Reservations");
        tableTitle.getStyleClass().add("user-table-title");

        reservationCountLabel = new Label("No reservations found");
        reservationCountLabel.getStyleClass().add("user-reservation-count");

        Button refreshBtn = new Button("🔄");
        refreshBtn.getStyleClass().addAll("user-action-button", "user-refresh-button");
        refreshBtn.setTooltip(createStyledTooltip("Refresh reservations"));
        refreshBtn.setOnAction(e -> loadReservations());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        headerBox.getChildren().addAll(tableTitle, reservationCountLabel, spacer, refreshBtn);

        // Create table WITHOUT ScrollPane since we have scene-level scrolling
        reservationsTable = createReservationsTable();
        reservationsTable.setPrefHeight(300); // Fixed height for table
        reservationsTable.setMaxHeight(300);

        tableSection.getChildren().addAll(headerBox, reservationsTable);

        System.out.println("Reservations table section created with direct table (no table ScrollPane)");

        return tableSection;
    }

    private TableView<Reservation> createReservationsTable() {
        TableView<Reservation> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setItems(reservations);
        table.getStyleClass().add("user-reservations-table");

        // Reservation ID Column
        TableColumn<Reservation, Long> idCol = new TableColumn<>("Booking ID");
        idCol.setCellValueFactory(data -> new SimpleLongProperty(data.getValue().getId()).asObject());
        idCol.getStyleClass().add("user-reservation-id-column");
        idCol.setMaxWidth(100);
        idCol.setMinWidth(80);

        // Room ID Column
        TableColumn<Reservation, String> roomCol = new TableColumn<>("Room");
        roomCol.setCellValueFactory(data -> new SimpleStringProperty("Room " + data.getValue().getRoomId()));
        roomCol.getStyleClass().add("user-room-id-column");
        roomCol.setMaxWidth(100);
        roomCol.setMinWidth(80);

        // Check-in Date Column
        TableColumn<Reservation, String> inCol = new TableColumn<>("Check-in");
        inCol.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getCheckInDate().format(java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy"))
        ));
        inCol.getStyleClass().add("user-checkin-column");

        // Check-out Date Column
        TableColumn<Reservation, String> outCol = new TableColumn<>("Check-out");
        outCol.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getCheckOutDate().format(java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy"))
        ));
        outCol.getStyleClass().add("user-checkout-column");

        // Duration Column
        TableColumn<Reservation, String> durationCol = new TableColumn<>("Duration");
        durationCol.setCellValueFactory(data -> {
            Reservation reservation = data.getValue();
            long days = ChronoUnit.DAYS.between(reservation.getCheckInDate(), reservation.getCheckOutDate());
            return new SimpleStringProperty(days + " night" + (days != 1 ? "s" : ""));
        });
        durationCol.getStyleClass().add("user-duration-column");
        durationCol.setMaxWidth(100);
        durationCol.setMinWidth(80);

        // Status Column
        TableColumn<Reservation, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(data -> {
            Reservation reservation = data.getValue();
            LocalDate today = LocalDate.now();
            LocalDate checkIn = reservation.getCheckInDate();
            LocalDate checkOut = reservation.getCheckOutDate();

            String status;
            if (today.isBefore(checkIn)) {
                status = "📅 Upcoming";
            } else if (today.isAfter(checkOut)) {
                status = "✅ Completed";
            } else {
                status = "🏨 Active";
            }

            return new SimpleStringProperty(status);
        });
        statusCol.getStyleClass().add("user-status-column");

        // Payment Status Column
        TableColumn<Reservation, String> paymentCol = new TableColumn<>("Payment");
        paymentCol.setCellValueFactory(data -> {
            boolean paid = data.getValue().isPaymentStatus();
            return new SimpleStringProperty(paid ? "💳 Paid" : "⏳ Pending");
        });
        paymentCol.setCellFactory(column -> new TableCell<Reservation, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    getStyleClass().removeAll("user-payment-paid", "user-payment-pending");
                } else {
                    setText(item);
                    if (item.contains("Paid")) {
                        getStyleClass().removeAll("user-payment-pending");
                        getStyleClass().add("user-payment-paid");
                    } else {
                        getStyleClass().removeAll("user-payment-paid");
                        getStyleClass().add("user-payment-pending");
                    }
                }
            }
        });
        paymentCol.getStyleClass().add("user-payment-column");
        paymentCol.setMaxWidth(100);
        paymentCol.setMinWidth(80);

        table.getColumns().addAll(idCol, roomCol, inCol, outCol, durationCol, statusCol, paymentCol);

        // Set placeholder for empty table
        Label placeholder = new Label("📋 No reservations found\nYour bookings will appear here");
        placeholder.getStyleClass().add("user-empty-message");
        placeholder.setStyle("-fx-text-alignment: center;");
        table.setPlaceholder(placeholder);

        return table;
    }

    private HBox createControlsSection() {
        HBox controlsSection = new HBox(15);
        controlsSection.setAlignment(Pos.CENTER);
        controlsSection.getStyleClass().add("user-controls-section");
        controlsSection.setPadding(new Insets(15, 0, 0, 0));

        viewDetailsBtn = new Button("👁️ View Details");
        viewDetailsBtn.getStyleClass().addAll("user-control-button", "user-view-button");
        viewDetailsBtn.setDisable(true);

        cancelBtn = new Button("❌ Cancel Booking");
        cancelBtn.getStyleClass().addAll("user-control-button", "user-cancel-button");
        cancelBtn.setDisable(true);

        payBtn = new Button("💳 Pay Now");
        payBtn.getStyleClass().addAll("user-control-button", "user-pay-button");
        payBtn.setDisable(true);

        // Selection listener
        reservationsTable.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            boolean hasSelection = selected != null;
            viewDetailsBtn.setDisable(!hasSelection);

            if (hasSelection) {
                LocalDate today = LocalDate.now();
                boolean canCancel = selected.getCheckInDate().isAfter(today);
                boolean needsPayment = !selected.isPaymentStatus();

                cancelBtn.setDisable(!canCancel);
                payBtn.setDisable(!needsPayment);
            } else {
                cancelBtn.setDisable(true);
                payBtn.setDisable(true);
            }
        });

        // Button actions
        viewDetailsBtn.setOnAction(e -> {
            Reservation selected = reservationsTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                showReservationDetails(selected);
            }
        });

        cancelBtn.setOnAction(e -> {
            Reservation selected = reservationsTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                cancelReservation(selected);
            }
        });

        payBtn.setOnAction(e -> {
            Reservation selected = reservationsTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                processPayment(selected);
            }
        });

        controlsSection.getChildren().addAll(viewDetailsBtn, cancelBtn, payBtn);

        return controlsSection;
    }

    private Tooltip createStyledTooltip(String text) {
        Tooltip tooltip = new Tooltip(text);
        tooltip.getStyleClass().add("tooltip");
        return tooltip;
    }

    private void loadReservations() {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(jwtToken);
            HttpEntity<Void> request = new HttpEntity<>(headers);

            ResponseEntity<Reservation[]> response = restTemplate.exchange(
                    "http://localhost:8080/api/reservations/customer/" + username,
                    HttpMethod.GET,
                    request,
                    Reservation[].class
            );

            List<Reservation> reservationList = response.getBody() != null ?
                    Arrays.asList(response.getBody()) : List.of();

            reservations.setAll(reservationList);
            updateStatistics(reservationList);
            updateReservationCount(reservationList.size());

        } catch (Exception ex) {
            showStyledAlert("Load Error",
                    "Failed to load reservations: " + ex.getMessage(),
                    Alert.AlertType.ERROR);
        }
    }

    private void updateStatistics(List<Reservation> reservationList) {
        LocalDate today = LocalDate.now();

        int total = reservationList.size();
        int upcoming = (int) reservationList.stream()
                .filter(r -> r.getCheckInDate().isAfter(today))
                .count();
        int completed = (int) reservationList.stream()
                .filter(r -> r.getCheckOutDate().isBefore(today))
                .count();

        // Update stat card values
        if (totalValueLabel != null) {
            totalValueLabel.setText(String.valueOf(total));
        }
        if (upcomingValueLabel != null) {
            upcomingValueLabel.setText(String.valueOf(upcoming));
        }
        if (completedValueLabel != null) {
            completedValueLabel.setText(String.valueOf(completed));
        }
    }

    private void updateReservationCount(int count) {
        if (reservationCountLabel != null) {
            String text = count == 0 ? "No reservations found" :
                    count + " reservation" + (count != 1 ? "s" : "") + " found";
            reservationCountLabel.setText(text);
        }
    }

    private void showReservationDetails(Reservation reservation) {
        Alert detailsAlert = new Alert(Alert.AlertType.INFORMATION);
        detailsAlert.setTitle("Reservation Details");
        detailsAlert.setHeaderText("Booking Information");

        long days = ChronoUnit.DAYS.between(reservation.getCheckInDate(), reservation.getCheckOutDate());
        LocalDate today = LocalDate.now();
        String status = today.isBefore(reservation.getCheckInDate()) ? "Upcoming" :
                today.isAfter(reservation.getCheckOutDate()) ? "Completed" : "Active";

        detailsAlert.setContentText(
                "🆔 Booking ID: " + reservation.getId() + "\n" +
                        "🏨 Room: " + reservation.getRoomId() + "\n" +
                        "📅 Check-in: " + reservation.getCheckInDate().format(
                        java.time.format.DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy")) + "\n" +
                        "📅 Check-out: " + reservation.getCheckOutDate().format(
                        java.time.format.DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy")) + "\n" +
                        "🗓️ Duration: " + days + " night" + (days != 1 ? "s" : "") + "\n" +
                        "📊 Status: " + status + "\n" +
                        "💳 Payment: " + (reservation.isPaymentStatus() ? "Paid" : "Pending") + "\n" +
                        "👤 Guest: " + reservation.getCustomerName() + "\n\n" +
                        "Need to make changes? Contact our support team!"
        );

        styleDialog(detailsAlert);
        detailsAlert.showAndWait();
    }

    private void cancelReservation(Reservation reservation) {
        // Check if cancellation is allowed
        LocalDate today = LocalDate.now();
        if (!reservation.getCheckInDate().isAfter(today)) {
            showStyledAlert("Cannot Cancel",
                    "This reservation cannot be cancelled as the check-in date has passed.",
                    Alert.AlertType.WARNING);
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Cancel Reservation");
        confirmAlert.setHeaderText("Are you sure you want to cancel this booking?");
        confirmAlert.setContentText(
                "🆔 Booking ID: " + reservation.getId() + "\n" +
                        "🏨 Room: " + reservation.getRoomId() + "\n" +
                        "📅 Check-in: " + reservation.getCheckInDate() + "\n\n" +
                        "This action cannot be undone.\n" +
                        "Refund policies may apply."
        );

        styleDialog(confirmAlert);

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                performCancellation(reservation);
            }
        });
    }

    private void performCancellation(Reservation reservation) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(jwtToken);
            HttpEntity<Void> request = new HttpEntity<>(headers);

            ResponseEntity<Void> response = restTemplate.exchange(
                    "http://localhost:8080/reservations/" + reservation.getId(),
                    HttpMethod.DELETE,
                    request,
                    Void.class
            );

            if (response.getStatusCode() == HttpStatus.NO_CONTENT) {
                reservations.remove(reservation);
                showStyledAlert("Cancellation Successful",
                        "Your reservation has been cancelled successfully.\n\n" +
                                "If you paid for this booking, a refund will be processed\n" +
                                "according to our cancellation policy.",
                        Alert.AlertType.INFORMATION);
                loadReservations(); // Refresh the list
            } else {
                showStyledAlert("Cancellation Failed",
                        "Failed to cancel reservation. Status: " + response.getStatusCode(),
                        Alert.AlertType.ERROR);
            }
        } catch (Exception ex) {
            showStyledAlert("Cancellation Error",
                    "Failed to cancel reservation: " + ex.getMessage(),
                    Alert.AlertType.ERROR);
        }
    }

    private void processPayment(Reservation reservation) {
        Alert paymentAlert = new Alert(Alert.AlertType.CONFIRMATION);
        paymentAlert.setTitle("Process Payment");
        paymentAlert.setHeaderText("Complete your booking payment");
        paymentAlert.setContentText(
                "🆔 Booking ID: " + reservation.getId() + "\n" +
                        "🏨 Room: " + reservation.getRoomId() + "\n" +
                        "📅 Check-in: " + reservation.getCheckInDate() + "\n" +
                        "📅 Check-out: " + reservation.getCheckOutDate() + "\n\n" +
                        "💳 Payment Status: Pending\n\n" +
                        "This will redirect you to our secure payment gateway.\n" +
                        "Proceed with payment?"
        );

        styleDialog(paymentAlert);

        paymentAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Simulate payment processing
                showStyledAlert("Payment Processing",
                        "Payment gateway integration would be handled here.\n\n" +
                                "In a real application, this would redirect to\n" +
                                "a secure payment processor.",
                        Alert.AlertType.INFORMATION);
            }
        });
    }

    private void showStyledAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        styleDialog(alert);
        alert.showAndWait();
    }

    private void styleDialog(Alert alert) {
        alert.getDialogPane().getStylesheets().add(
                getClass().getResource("/css/user_booking_style.css").toExternalForm()
        );
        alert.getDialogPane().getStyleClass().add("user-confirmation-dialog");
    }
}