package com.example.demo.ui.dialog;

import com.example.demo.ui.model.HotelRoom;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

public class RoomCreationDialog {

    private static String jwtToken;

    public static void show(HotelRoom roomToEdit, Runnable onSaveCallback) {
        show(roomToEdit, onSaveCallback, null);
    }

    public static void show(HotelRoom roomToEdit, Runnable onSaveCallback, String token) {
        jwtToken = token;

        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initStyle(StageStyle.DECORATED);
        dialog.setTitle(roomToEdit == null ? "🏢 Create New Room" : "✏️ Edit Room");
        dialog.setResizable(true); // Allow resizing
        dialog.setMinWidth(450);
        dialog.setMinHeight(400);

        // Main container
        VBox mainContainer = new VBox();
        mainContainer.getStyleClass().add("dialog-container");

        // Header section
        VBox headerSection = createHeaderSection(roomToEdit);

        // Form section
        VBox formSection = createFormSection(roomToEdit, dialog, onSaveCallback);

        mainContainer.getChildren().addAll(headerSection, formSection);

        // Wrap main container in ScrollPane
        ScrollPane scrollPane = new ScrollPane(mainContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(false);
        scrollPane.getStyleClass().add("dialog-scroll");
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        // Create wrapper for ScrollPane
        VBox wrapper = new VBox(scrollPane);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        // Create scene with styling
        Scene scene = new Scene(wrapper, 470, 600); // Increased width slightly to accommodate scrollbar

        // Apply CSS styling
        try {
            scene.getStylesheets().add(
                    RoomCreationDialog.class.getResource("/css/user_booking_style.css").toExternalForm()
            );
        } catch (Exception e) {
            System.err.println("Could not load CSS file: " + e.getMessage());
        }

        dialog.setScene(scene);
        dialog.centerOnScreen();
        dialog.showAndWait();
    }

    private static VBox createHeaderSection(HotelRoom roomToEdit) {
        VBox headerSection = new VBox(10);
        headerSection.getStyleClass().add("dialog-header");
        headerSection.setAlignment(Pos.CENTER);
        headerSection.setPadding(new Insets(25, 20, 20, 20));

        Label titleLabel = new Label(roomToEdit == null ? "Create New Room" : "Edit Room Details");
        titleLabel.getStyleClass().add("dialog-title");

        Label subtitleLabel = new Label(roomToEdit == null ?
                "Add a new room to the hotel inventory" :
                "Modify existing room information");
        subtitleLabel.getStyleClass().add("dialog-subtitle");

        headerSection.getChildren().addAll(titleLabel, subtitleLabel);
        return headerSection;
    }

    private static VBox createFormSection(HotelRoom roomToEdit, Stage dialog, Runnable onSaveCallback) {
        VBox formSection = new VBox(20);
        formSection.getStyleClass().add("dialog-form");
        formSection.setPadding(new Insets(20, 30, 30, 30));
        formSection.setMaxWidth(Double.MAX_VALUE);

        // Room Number Field
        VBox roomNumberGroup = createFieldGroup("Room Number", "Enter room number (e.g., 101, 201A)");
        TextField numberField = new TextField();
        numberField.getStyleClass().add("dialog-field");
        numberField.setPromptText("Room number");
        numberField.setMaxWidth(Double.MAX_VALUE);
        roomNumberGroup.getChildren().add(numberField);

        // Category Selection
        VBox categoryGroup = createFieldGroup("Room Category", "Select the type of room");
        ComboBox<String> categoryBox = new ComboBox<>();
        categoryBox.getItems().addAll("Standard", "Deluxe", "Suite", "Presidential");
        categoryBox.getStyleClass().add("dialog-combo");
        categoryBox.setPromptText("Choose category");
        categoryBox.setMaxWidth(Double.MAX_VALUE);
        categoryGroup.getChildren().add(categoryBox);

        // Price Field
        VBox priceGroup = createFieldGroup("Price per Night", "Set the nightly rate for this room");
        HBox priceContainer = new HBox(10);
        priceContainer.setAlignment(Pos.CENTER_LEFT);

        Label currencyLabel = new Label("$");
        currencyLabel.getStyleClass().add("currency-label");

        Spinner<Double> priceSpinner = new Spinner<>(0.0, 2000.0, 100.0, 5.0);
        priceSpinner.setEditable(true);
        priceSpinner.getStyleClass().add("dialog-spinner");
        priceSpinner.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(priceSpinner, Priority.ALWAYS);

        priceContainer.getChildren().addAll(currencyLabel, priceSpinner);
        priceGroup.getChildren().add(priceContainer);

        // Availability Checkbox
        VBox availabilityGroup = createFieldGroup("Availability", "Set initial room availability");
        CheckBox availabilityBox = new CheckBox("Room is available for booking");
        availabilityBox.getStyleClass().add("dialog-checkbox");
        availabilityBox.setSelected(true);
        availabilityGroup.getChildren().add(availabilityBox);

        // Room Features (Visual enhancement)
        VBox featuresGroup = createFieldGroup("Room Features", "Additional room information");
        TextArea featuresArea = new TextArea();
        featuresArea.getStyleClass().add("dialog-textarea");
        featuresArea.setPromptText("Optional: Describe room features, amenities, or special notes...");
        featuresArea.setPrefRowCount(3);
        featuresArea.setMaxHeight(80);
        featuresArea.setMaxWidth(Double.MAX_VALUE);
        featuresGroup.getChildren().add(featuresArea);

        // Fill form if editing
        if (roomToEdit != null) {
            numberField.setText(roomToEdit.getRoomNumber());
            numberField.setEditable(false);
            categoryBox.setValue(roomToEdit.getCategory());
            priceSpinner.getValueFactory().setValue(roomToEdit.getPricePerNight());
            availabilityBox.setSelected(roomToEdit.isAvailable());
        }

        // Validation
        setupValidation(numberField, categoryBox, priceSpinner);

        // Action Buttons
        HBox buttonSection = createButtonSection(
                roomToEdit, dialog, onSaveCallback,
                numberField, categoryBox, priceSpinner, availabilityBox, featuresArea
        );

        formSection.getChildren().addAll(
                roomNumberGroup,
                categoryGroup,
                priceGroup,
                availabilityGroup,
                featuresGroup,
                buttonSection
        );

        return formSection;
    }

    private static VBox createFieldGroup(String title, String description) {
        VBox group = new VBox(8);
        group.getStyleClass().add("field-group");

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("field-title");

        Label descLabel = new Label(description);
        descLabel.getStyleClass().add("field-description");

        group.getChildren().addAll(titleLabel, descLabel);
        return group;
    }

    private static void setupValidation(TextField numberField, ComboBox<String> categoryBox,
                                        Spinner<Double> priceSpinner) {
        // Real-time validation
        numberField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.matches("[A-Za-z0-9]*")) {
                numberField.setText(oldVal);
            }
        });

        // Limit room number length
        numberField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal.length() > 10) {
                numberField.setText(oldVal);
            }
        });
    }

    private static HBox createButtonSection(HotelRoom roomToEdit, Stage dialog, Runnable onSaveCallback,
                                            TextField numberField, ComboBox<String> categoryBox,
                                            Spinner<Double> priceSpinner, CheckBox availabilityBox,
                                            TextArea featuresArea) {
        HBox buttonSection = new HBox(15);
        buttonSection.setAlignment(Pos.CENTER);
        buttonSection.setPadding(new Insets(20, 0, 0, 0));

        Button saveBtn = new Button(roomToEdit == null ? "💾 Create Room" : "💾 Save Changes");
        saveBtn.getStyleClass().addAll("dialog-button", "save-button");
        saveBtn.setPrefWidth(150);

        Button cancelBtn = new Button("❌ Cancel");
        cancelBtn.getStyleClass().addAll("dialog-button", "cancel-button");
        cancelBtn.setPrefWidth(120);

        // Progress indicator for loading state
        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.getStyleClass().add("dialog-progress");
        progressIndicator.setVisible(false);
        progressIndicator.setMaxSize(25, 25);

        saveBtn.setOnAction(e -> {
            if (validateForm(numberField, categoryBox)) {
                saveRoom(roomToEdit, dialog, onSaveCallback,
                        numberField, categoryBox, priceSpinner, availabilityBox,
                        saveBtn, cancelBtn, progressIndicator);
            }
        });

        cancelBtn.setOnAction(e -> dialog.close());

        buttonSection.getChildren().addAll(saveBtn, cancelBtn, progressIndicator);
        return buttonSection;
    }

    private static boolean validateForm(TextField numberField, ComboBox<String> categoryBox) {
        StringBuilder errors = new StringBuilder();

        if (numberField.getText().trim().isEmpty()) {
            errors.append("• Room number is required\n");
        }

        if (categoryBox.getValue() == null) {
            errors.append("• Room category must be selected\n");
        }

        if (errors.length() > 0) {
            showStyledAlert(Alert.AlertType.WARNING, "Validation Error",
                    "Please fix the following issues:", errors.toString());
            return false;
        }

        return true;
    }

    private static void saveRoom(HotelRoom roomToEdit, Stage dialog, Runnable onSaveCallback,
                                 TextField numberField, ComboBox<String> categoryBox,
                                 Spinner<Double> priceSpinner, CheckBox availabilityBox,
                                 Button saveBtn, Button cancelBtn, ProgressIndicator progressIndicator) {

        // Show loading state
        saveBtn.setDisable(true);
        cancelBtn.setDisable(true);
        progressIndicator.setVisible(true);

        Task<Void> saveTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                String number = numberField.getText().trim();
                String category = categoryBox.getValue();
                Double price = priceSpinner.getValue();
                boolean available = availabilityBox.isSelected();

                HotelRoom room = new HotelRoom();
                room.setRoomNumber(number);
                room.setCategory(category);
                room.setPricePerNight(price);
                room.setAvailable(available);

                RestTemplate restTemplate = new RestTemplate();
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);

                if (jwtToken != null && !jwtToken.trim().isEmpty()) {
                    headers.setBearerAuth(jwtToken);
                }

                HttpEntity<HotelRoom> entity = new HttpEntity<>(room, headers);

                if (roomToEdit == null) {
                    // Create new room
                    restTemplate.exchange(
                            "http://localhost:8080/api/rooms",
                            HttpMethod.POST,
                            entity,
                            HotelRoom.class
                    );
                } else {
                    // Update existing room
                    room.setId(roomToEdit.getId());
                    restTemplate.exchange(
                            "http://localhost:8080/api/rooms/" + room.getId(),
                            HttpMethod.PUT,
                            entity,
                            HotelRoom.class
                    );
                }

                return null;
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    progressIndicator.setVisible(false);

                    String successMessage = roomToEdit == null ?
                            "Room created successfully!" :
                            "Room updated successfully!";

                    showStyledAlert(Alert.AlertType.INFORMATION, "Success", null, successMessage);

                    if (onSaveCallback != null) {
                        onSaveCallback.run();
                    }
                    dialog.close();
                });
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    saveBtn.setDisable(false);
                    cancelBtn.setDisable(false);
                    progressIndicator.setVisible(false);

                    String errorMessage = "Failed to " + (roomToEdit == null ? "create" : "update") +
                            " room: " + getException().getMessage();

                    showStyledAlert(Alert.AlertType.ERROR, "Error", "Operation Failed", errorMessage);
                });
            }
        };

        Thread saveThread = new Thread(saveTask);
        saveThread.setDaemon(true);
        saveThread.start();
    }

    private static void showStyledAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);

        // Apply styling
        try {
            alert.getDialogPane().getStylesheets().add(
                    RoomCreationDialog.class.getResource("/css/user_booking_style.css").toExternalForm()
            );
            alert.getDialogPane().getStyleClass().add("alert-dialog");
        } catch (Exception e) {
            System.err.println("Could not load CSS for alert: " + e.getMessage());
        }

        alert.showAndWait();
    }
}