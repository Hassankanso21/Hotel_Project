// ✅ Step 1 is handled by using roomId in Reservation.java
// No need to create getRoom() method, we will use roomId to fetch the full room in the dialog.

// ✅ Step 2: RoomDetailsDialog.java
package com.example.demo.ui.dialog;

import com.example.demo.ui.model.HotelRoom;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.springframework.web.client.RestTemplate;

public class RoomDetailsDialog {

    private final Long roomId;

    public RoomDetailsDialog(Long roomId) {
        this.roomId = roomId;
    }

    public void show() {
        HotelRoom room;
        try {
            RestTemplate restTemplate = new RestTemplate();
            room = restTemplate.getForObject("http://localhost:8080/api/rooms/" + roomId, HotelRoom.class);
        } catch (Exception e) {
            showAlert("Failed to load room details: " + e.getMessage());
            return;
        }

        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Room Details");

        TextField numberField = new TextField(room.getRoomNumber());
        TextField categoryField = new TextField(room.getCategory());
        CheckBox availableCheck = new CheckBox("Available");
        availableCheck.setSelected(room.isAvailable());
        Spinner<Double> priceSpinner = new Spinner<>(0.0, 10000.0, room.getPricePerNight(), 10.0);
        priceSpinner.setEditable(true);

        Button saveButton = new Button("Save Changes");
        saveButton.setOnAction(e -> {
            room.setRoomNumber(numberField.getText());
            room.setCategory(categoryField.getText());
            room.setAvailable(availableCheck.isSelected());
            room.setPricePerNight(priceSpinner.getValue());

            try {
                RestTemplate restTemplate = new RestTemplate();
                restTemplate.put("http://localhost:8080/api/rooms/" + roomId, room);
                showAlert("Room updated successfully.");
                dialog.close();
            } catch (Exception ex) {
                showAlert("Failed to update room: " + ex.getMessage());
            }
        });

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10));
        grid.setVgap(10);
        grid.setHgap(10);

        grid.add(new Label("Room Number:"), 0, 0);
        grid.add(numberField, 1, 0);
        grid.add(new Label("Category:"), 0, 1);
        grid.add(categoryField, 1, 1);
        grid.add(new Label("Available:"), 0, 2);
        grid.add(availableCheck, 1, 2);
        grid.add(new Label("Price per Night:"), 0, 3);
        grid.add(priceSpinner, 1, 3);
        grid.add(saveButton, 1, 4);

        Scene scene = new Scene(grid);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Room Info");
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
