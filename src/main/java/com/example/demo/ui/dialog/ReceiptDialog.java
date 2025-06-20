package com.example.demo.ui.dialog;

import com.example.demo.ui.model.HotelRoom;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.print.PrinterJob;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.text.DecimalFormat;

public class ReceiptDialog {

    public static void show(HotelRoom room, String customerName, LocalDate checkIn, LocalDate checkOut) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Booking Receipt");

        ImageView logo = new ImageView(new Image("file:/C:/Users/khasa/OneDrive/Desktop/HotelProject2_Clean/src/main/resources/Images/img.png"));
        logo.setFitHeight(60);
        logo.setPreserveRatio(true);
        HBox logoBox = new HBox(logo);
        logoBox.setAlignment(Pos.CENTER);

        Label title = new Label("Booking Receipt");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        GridPane details = new GridPane();
        details.setVgap(10);
        details.setHgap(10);
        details.setPadding(new Insets(20));

        int nights = (int) ChronoUnit.DAYS.between(checkIn, checkOut);
        double price = room.getPricePerNight();
        double total = price * nights;
        DecimalFormat df = new DecimalFormat("$#,##0.00");

        int row = 0;
        details.add(new Label("Customer:"), 0, row);
        details.add(new Label(customerName), 1, row++);
        details.add(new Label("Room Number:"), 0, row);
        details.add(new Label(room.getRoomNumber()), 1, row++);
        details.add(new Label("Category:"), 0, row);
        details.add(new Label(room.getCategory()), 1, row++);
        details.add(new Label("Price per Night:"), 0, row);
        details.add(new Label(df.format(price)), 1, row++);
        details.add(new Label("Check-in Date:"), 0, row);
        details.add(new Label(checkIn.toString()), 1, row++);
        details.add(new Label("Check-out Date:"), 0, row);
        details.add(new Label(checkOut.toString()), 1, row++);
        details.add(new Label("Total Nights:"), 0, row);
        details.add(new Label(String.valueOf(nights)), 1, row++);
        details.add(new Label("Total Cost:"), 0, row);
        details.add(new Label(df.format(total)), 1, row++);

        Button closeBtn = new Button("Close");
        closeBtn.setOnAction(e -> dialog.close());

        Button printBtn = new Button("Print");
        printBtn.setOnAction(e -> {
            PrinterJob job = PrinterJob.createPrinterJob();
            if (job != null && job.showPrintDialog(dialog.getOwner())) {
                boolean success = job.printPage(details);
                if (success) {
                    job.endJob();
                }
            }
        });

        HBox btnBox = new HBox(10, printBtn, closeBtn);
        btnBox.setAlignment(Pos.CENTER);
        btnBox.setPadding(new Insets(10));

        VBox layout = new VBox(15, logoBox, title, details, btnBox);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-background-color: white; -fx-border-color: gray; -fx-border-width: 1px;");
        layout.setAlignment(Pos.TOP_CENTER);

        Scene scene = new Scene(layout);
        dialog.setScene(scene);
        dialog.showAndWait();
    }
}
