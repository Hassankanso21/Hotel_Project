package com.example.demo.ui.dialog;

import com.example.demo.ui.model.HotelRoom;
import com.example.demo.ui.model.Reservation;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class BookingDialog {

    private static String lastCustomerName;

    public static String getLastCustomerName() {
        return lastCustomerName;
    }

    public static boolean show(Long roomId, LocalDate checkIn, LocalDate checkOut, String jwtToken) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initStyle(StageStyle.UNDECORATED);
        dialog.setTitle("Admin Booking Form");

        // Create main container
        VBox mainContainer = createMainContainer();

        // Create header (fixed at top)
        VBox header = createHeader(dialog);

        // Create scrollable content
        ScrollPane scrollPane = createScrollableContent(roomId, checkIn, checkOut, jwtToken, dialog);

        mainContainer.getChildren().addAll(header, scrollPane);

        // Adjust scene size to be more flexible
        Scene scene = new Scene(mainContainer, 500, Math.min(600, 450)); // Allow up to 600px height
        scene.getStylesheets().add(BookingDialog.class.getResource("/css/user_booking_style.css").toExternalForm());
        dialog.setScene(scene);

        // Set minimum and maximum sizes
        dialog.setMinWidth(500);
        dialog.setMinHeight(400);
        dialog.setMaxHeight(700); // Limit maximum height

        // Center on screen
        dialog.centerOnScreen();

        // Add entrance animation
        addEntranceAnimation(mainContainer);

        final boolean[] success = {false};
        dialog.setUserData(success);

        dialog.showAndWait();

        return success[0];
    }

    private static VBox createMainContainer() {
        VBox container = new VBox();
        container.getStyleClass().add("dialog-container");
        container.setAlignment(Pos.TOP_CENTER);
        return container;
    }

    private static VBox createHeader(Stage dialog) {
        VBox header = new VBox(8);
        header.getStyleClass().add("dialog-header");
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(20, 20, 15, 20));

        // Close button
        Button closeBtn = new Button("✕");
        closeBtn.getStyleClass().addAll("dialog-close-button");
        closeBtn.setOnAction(e -> dialog.close());

        HBox closeContainer = new HBox();
        closeContainer.setAlignment(Pos.CENTER_RIGHT);
        closeContainer.getChildren().add(closeBtn);

        // Title
        Label title = new Label("🏨 Admin Booking Form");
        title.getStyleClass().add("dialog-title");

        Label subtitle = new Label("Create a new reservation");
        subtitle.getStyleClass().add("dialog-subtitle");

        header.getChildren().addAll(closeContainer, title, subtitle);
        return header;
    }

    private static ScrollPane createScrollableContent(Long roomId, LocalDate checkIn, LocalDate checkOut, String jwtToken, Stage dialog) {
        // Create the scrollable content container
        VBox scrollableContent = new VBox(20);
        scrollableContent.setPadding(new Insets(20));

        // Create form section content
        VBox formSection = createFormSection(roomId, checkIn, checkOut, jwtToken, dialog);
        scrollableContent.getChildren().add(formSection);

        // Create ScrollPane
        ScrollPane scrollPane = new ScrollPane(scrollableContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER); // Hide horizontal scrollbar
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED); // Show vertical scrollbar when needed

        // Set preferred viewport height to ensure buttons are always visible
        scrollPane.setPrefViewportHeight(350); // Adjust this value as needed
        scrollPane.setMaxHeight(450); // Maximum scroll area height

        // Style the scroll pane
        scrollPane.getStyleClass().add("dialog-scroll-pane");

        // Make scrolling smoother
        scrollPane.setVvalue(0); // Start at top

        // VBox to grow and fill space
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        return scrollPane;
    }

    private static VBox createFormSection(Long roomId, LocalDate checkIn, LocalDate checkOut, String jwtToken, Stage dialog) {
        VBox formSection = new VBox(20);
        formSection.getStyleClass().add("dialog-form");

        // Room Information Card
        VBox roomInfoCard = createRoomInfoCard(roomId, checkIn, checkOut, jwtToken);

        // Customer Details Card
        VBox customerCard = createCustomerCard();

        // Booking Summary Card
        VBox summaryCard = createBookingSummaryCard(checkIn, checkOut);

        // Action Buttons (always visible at bottom)
        HBox buttonSection = createButtonSection(roomId, checkIn, checkOut, jwtToken, dialog, customerCard);

        formSection.getChildren().addAll(roomInfoCard, customerCard, summaryCard, buttonSection);
        return formSection;
    }

    private static VBox createRoomInfoCard(Long roomId, LocalDate checkIn, LocalDate checkOut, String jwtToken) {
        VBox card = new VBox(12);
        card.getStyleClass().add("field-group");

        Label cardTitle = new Label("📋 Room Information");
        cardTitle.getStyleClass().add("field-title");

        // Try to fetch room details
        try {
            HotelRoom room = fetchRoomDetails(roomId, jwtToken);
            if (room != null) {
                GridPane roomGrid = new GridPane();
                roomGrid.setHgap(15);
                roomGrid.setVgap(8);

                Label roomIdLabel = new Label("Room ID:");
                roomIdLabel.getStyleClass().add("form-label");
                Label roomIdValue = new Label(room.getId().toString());
                roomIdValue.getStyleClass().add("form-value");

                Label categoryLabel = new Label("Category:");
                categoryLabel.getStyleClass().add("form-label");
                Label categoryValue = new Label(room.getCategory());
                categoryValue.getStyleClass().add("form-value");

                Label priceLabel = new Label("Price per Night:");
                priceLabel.getStyleClass().add("form-label");
                Label priceValue = new Label("$" + room.getPricePerNight());
                priceValue.getStyleClass().add("form-value");

                roomGrid.add(roomIdLabel, 0, 0);
                roomGrid.add(roomIdValue, 1, 0);
                roomGrid.add(categoryLabel, 0, 1);
                roomGrid.add(categoryValue, 1, 1);
                roomGrid.add(priceLabel, 0, 2);
                roomGrid.add(priceValue, 1, 2);

                card.getChildren().addAll(cardTitle, roomGrid);
            } else {
                Label roomInfo = new Label("Room ID: " + roomId);
                roomInfo.getStyleClass().add("form-value");
                card.getChildren().addAll(cardTitle, roomInfo);
            }
        } catch (Exception e) {
            Label roomInfo = new Label("Room ID: " + roomId);
            roomInfo.getStyleClass().add("form-value");
            card.getChildren().addAll(cardTitle, roomInfo);
        }

        return card;
    }

    private static VBox createCustomerCard() {
        VBox card = new VBox(12);
        card.getStyleClass().add("field-group");

        Label cardTitle = new Label("👤 Customer Details");
        cardTitle.getStyleClass().add("field-title");

        Label nameLabel = new Label("Customer Name *");
        nameLabel.getStyleClass().add("form-label");

        TextField nameField = new TextField();
        nameField.getStyleClass().add("dialog-field");
        nameField.setPromptText("Enter customer full name");

        // Set last customer name if available
        if (lastCustomerName != null && !lastCustomerName.trim().isEmpty()) {
            nameField.setText(lastCustomerName);
        }

        Label helpText = new Label("Enter the full name as it appears on ID");
        helpText.getStyleClass().add("field-description");

        card.getChildren().addAll(cardTitle, nameLabel, nameField, helpText);
        card.setUserData(nameField); // Store reference for access
        return card;
    }

    private static VBox createBookingSummaryCard(LocalDate checkIn, LocalDate checkOut) {
        VBox card = new VBox(12);
        card.getStyleClass().add("field-group");

        Label cardTitle = new Label("📅 Booking Summary");
        cardTitle.getStyleClass().add("field-title");

        GridPane summaryGrid = new GridPane();
        summaryGrid.setHgap(15);
        summaryGrid.setVgap(8);

        Label checkInLabel = new Label("Check-in Date:");
        checkInLabel.getStyleClass().add("form-label");
        Label checkInValue = new Label(checkIn.format(java.time.format.DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy")));
        checkInValue.getStyleClass().add("form-value");

        Label checkOutLabel = new Label("Check-out Date:");
        checkOutLabel.getStyleClass().add("form-label");
        Label checkOutValue = new Label(checkOut.format(java.time.format.DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy")));
        checkOutValue.getStyleClass().add("form-value");

        long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
        Label durationLabel = new Label("Duration:");
        durationLabel.getStyleClass().add("form-label");
        Label durationValue = new Label(nights + " night" + (nights != 1 ? "s" : ""));
        durationValue.getStyleClass().add("form-value");

        Label paymentLabel = new Label("Payment Status:");
        paymentLabel.getStyleClass().add("form-label");
        Label paymentValue = new Label("✅ Paid (Admin Booking)");
        paymentValue.getStyleClass().add("form-value");
        paymentValue.setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: bold;");

        summaryGrid.add(checkInLabel, 0, 0);
        summaryGrid.add(checkInValue, 1, 0);
        summaryGrid.add(checkOutLabel, 0, 1);
        summaryGrid.add(checkOutValue, 1, 1);
        summaryGrid.add(durationLabel, 0, 2);
        summaryGrid.add(durationValue, 1, 2);
        summaryGrid.add(paymentLabel, 0, 3);
        summaryGrid.add(paymentValue, 1, 3);

        card.getChildren().addAll(cardTitle, summaryGrid);
        return card;
    }

    private static HBox createButtonSection(Long roomId, LocalDate checkIn, LocalDate checkOut, String jwtToken, Stage dialog, VBox customerCard) {
        HBox buttonSection = new HBox(15);
        buttonSection.setAlignment(Pos.CENTER);
        buttonSection.setPadding(new Insets(20, 0, 10, 0)); // Increased top padding for better spacing

        Button cancelBtn = new Button("❌ Cancel");
        cancelBtn.getStyleClass().addAll("dialog-button", "cancel-button");

        Button confirmBtn = new Button("✅ Confirm Booking");
        confirmBtn.getStyleClass().addAll("dialog-button", "save-button");

        // Get reference to name field
        TextField nameField = (TextField) customerCard.getUserData();

        // Enable/disable confirm button based on name input
        confirmBtn.setDisable(nameField.getText().trim().isEmpty());
        nameField.textProperty().addListener((obs, oldVal, newVal) -> {
            confirmBtn.setDisable(newVal.trim().isEmpty());

            // Add validation styling
            if (newVal.trim().isEmpty()) {
                nameField.getStyleClass().remove("success");
                nameField.getStyleClass().add("error");
            } else {
                nameField.getStyleClass().remove("error");
                nameField.getStyleClass().add("success");
            }
        });

        // Button actions
        cancelBtn.setOnAction(e -> {
            addExitAnimation(dialog);
        });

        confirmBtn.setOnAction(e -> {
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                showStyledAlert(Alert.AlertType.ERROR, "Validation Error", "Customer name is required.");
                nameField.requestFocus();
                return;
            }

            // Show loading state
            confirmBtn.setText("⏳ Processing...");
            confirmBtn.setDisable(true);
            cancelBtn.setDisable(true);

            try {
                boolean success = performBooking(roomId, name, checkIn, checkOut, jwtToken);
                if (success) {
                    boolean[] result = (boolean[]) dialog.getUserData();
                    result[0] = true;
                    lastCustomerName = name;

                    // Show success animation
                    confirmBtn.setText("✅ Success!");
                    confirmBtn.getStyleClass().add("success-button");

                    // Delay before closing
                    javafx.animation.Timeline delay = new javafx.animation.Timeline(
                            new javafx.animation.KeyFrame(Duration.millis(1000), event -> dialog.close())
                    );
                    delay.play();
                }
            } catch (Exception ex) {
                // Reset button state on error
                confirmBtn.setText("✅ Confirm Booking");
                confirmBtn.setDisable(false);
                cancelBtn.setDisable(false);
            }
        });

        buttonSection.getChildren().addAll(cancelBtn, confirmBtn);
        return buttonSection;
    }

    private static HotelRoom fetchRoomDetails(Long roomId, String jwtToken) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + jwtToken);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<HotelRoom> response = restTemplate.exchange(
                    "http://localhost:8080/api/rooms/" + roomId,
                    HttpMethod.GET,
                    entity,
                    HotelRoom.class
            );
            return response.getBody();
        } catch (Exception e) {
            return null;
        }
    }

    private static boolean performBooking(Long roomId, String customerName, LocalDate checkIn, LocalDate checkOut, String jwtToken) {
        try {
            Reservation reservation = new Reservation();
            reservation.setCustomerName(customerName);
            reservation.setRoomId(roomId);
            reservation.setCheckInDate(checkIn);
            reservation.setCheckOutDate(checkOut);
            reservation.setPaymentStatus(true); // Admin bookings are paid

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + jwtToken);

            HttpEntity<Reservation> entity = new HttpEntity<>(reservation, headers);
            RestTemplate restTemplate = new RestTemplate();

            restTemplate.postForObject("http://localhost:8080/reservations", entity, Reservation.class);

            // Fetch full room details for receipt
            HotelRoom fullRoom = fetchRoomDetails(roomId, jwtToken);
            showReceiptAlert(fullRoom, customerName, checkIn, checkOut);

            return true;

        } catch (HttpClientErrorException.Conflict conflictEx) {
            showStyledAlert(Alert.AlertType.ERROR, "Booking Conflict", "Room is already booked for the selected dates.\n\nPlease choose different dates or another room.");
            return false;
        } catch (Exception ex) {
            ex.printStackTrace();
            showStyledAlert(Alert.AlertType.ERROR, "Booking Error", "Failed to create booking:\n" + ex.getMessage());
            return false;
        }
    }

    private static void addEntranceAnimation(VBox container) {
        // Fade in
        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), container);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);

        // Scale in
        ScaleTransition scaleIn = new ScaleTransition(Duration.millis(300), container);
        scaleIn.setFromX(0.8);
        scaleIn.setFromY(0.8);
        scaleIn.setToX(1.0);
        scaleIn.setToY(1.0);

        fadeIn.play();
        scaleIn.play();
    }

    private static void addExitAnimation(Stage dialog) {
        VBox container = (VBox) dialog.getScene().getRoot();

        FadeTransition fadeOut = new FadeTransition(Duration.millis(200), container);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> dialog.close());
        fadeOut.play();
    }

    private static void showStyledAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        // Apply styling
        try {
            alert.getDialogPane().getStylesheets().add(
                    BookingDialog.class.getResource("/css/user_booking_style.css").toExternalForm()
            );
            alert.getDialogPane().getStyleClass().add("user-confirmation-dialog");
        } catch (Exception e) {
            // Fallback if CSS not found
        }

        alert.showAndWait();
    }

    private static void showReceiptAlert(HotelRoom room, String customerName, LocalDate checkIn, LocalDate checkOut) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Booking Confirmed");
        alert.setHeaderText("🎉 Booking successful!");
        alert.setContentText("The reservation has been confirmed and saved to the system.");

        ButtonType showReceiptBtn = new ButtonType("📄 Show Receipt", ButtonBar.ButtonData.OK_DONE);
        ButtonType closeBtn = new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(showReceiptBtn, closeBtn);

        // Apply styling
        try {
            alert.getDialogPane().getStylesheets().add(
                    BookingDialog.class.getResource("/css/user_booking_style.css").toExternalForm()
            );
            alert.getDialogPane().getStyleClass().add("user-confirmation-dialog");
        } catch (Exception e) {
            // Fallback if CSS not found
        }

        alert.showAndWait().ifPresent(type -> {
            if (type == showReceiptBtn) {
                ReceiptDialog.show(room, customerName, checkIn, checkOut);
            }
        });
    }
}