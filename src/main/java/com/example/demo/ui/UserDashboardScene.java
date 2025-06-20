package com.example.demo.ui;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;

public class UserDashboardScene {

    private final String username;
    private final String jwtToken;
    private final String role;
    private TabPane tabPane;
    private Stage primaryStage;

    public UserDashboardScene(String username, String jwtToken) {
        this.username = username;
        this.jwtToken = jwtToken;
        this.role = LoginScene.LOGGED_IN_ROLE;
    }

    public void show(Stage primaryStage) {
        this.primaryStage = primaryStage;

        // Create main layout
        BorderPane root = createMainLayout();

        // Create scene with improved sizing
        Scene scene = new Scene(root, 1200, 750);
        scene.getStylesheets().add(getClass().getResource("/css/user_booking_style.css").toExternalForm());

        // Configure stage
        setupStage(primaryStage, scene);

        // Add entrance animation
        addEntranceAnimation(root);
    }

    private BorderPane createMainLayout() {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("user-main-container");

        // Create top bar
        HBox topBar = createTopBar();

        // Create tab pane
        tabPane = createTabPane();

        // Create status bar
        HBox statusBar = createStatusBar();

        root.setTop(topBar);
        root.setCenter(tabPane);
        root.setBottom(statusBar);

        return root;
    }

    private HBox createTopBar() {
        HBox topBar = new HBox(20);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(15, 25, 15, 25));
        topBar.getStyleClass().add("user-top-bar");

        // Logo section
        HBox logoSection = createLogoSection();

        // Welcome section
        VBox welcomeSection = createWelcomeSection();

        // Action buttons section
        HBox actionSection = createActionSection();

        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        topBar.getChildren().addAll(logoSection, welcomeSection, spacer, actionSection);

        return topBar;
    }

    private HBox createLogoSection() {
        HBox logoSection = new HBox(15);
        logoSection.setAlignment(Pos.CENTER_LEFT);

        try {
            ImageView logoView = new ImageView(new Image(getClass().getResourceAsStream("/images/img.png")));
            logoView.setFitHeight(45);
            logoView.setPreserveRatio(true);
            logoView.getStyleClass().add("user-logo-image");

            Label appTitle = new Label("Hotel Booking");
            appTitle.getStyleClass().add("user-app-title");

            logoSection.getChildren().addAll(logoView, appTitle);
        } catch (Exception e) {
            // Fallback if image not found
            Label fallbackLogo = new Label("🏨");
            fallbackLogo.getStyleClass().add("user-logo-fallback");

            Label appTitle = new Label("Hotel Booking");
            appTitle.getStyleClass().add("user-app-title");

            logoSection.getChildren().addAll(fallbackLogo, appTitle);
        }

        return logoSection;
    }

    private VBox createWelcomeSection() {
        VBox welcomeSection = new VBox(2);
        welcomeSection.setAlignment(Pos.CENTER_LEFT);

        // Time-based greeting
        int hour = java.time.LocalTime.now().getHour();
        String greeting = getTimeBasedGreeting(hour);

        Label greetingLabel = new Label(greeting + "!");
        greetingLabel.getStyleClass().add("user-greeting-label");

        Label userLabel = new Label(username);
        userLabel.getStyleClass().add("user-username-label");

        Label roleLabel = new Label("Guest Account");
        roleLabel.getStyleClass().add("user-role-label");

        welcomeSection.getChildren().addAll(greetingLabel, userLabel, roleLabel);

        return welcomeSection;
    }

    private String getTimeBasedGreeting(int hour) {
        if (hour < 12) return "🌅 Good Morning";
        else if (hour < 17) return "☀️ Good Afternoon";
        else return "🌙 Good Evening";
    }

    private HBox createActionSection() {
        HBox actionSection = new HBox(12);
        actionSection.setAlignment(Pos.CENTER_RIGHT);

        // Help button
        Button helpBtn = new Button("❓");
        helpBtn.getStyleClass().addAll("user-action-button", "help-button");
        helpBtn.setTooltip(createStyledTooltip("Get help and support"));
        helpBtn.setOnAction(e -> showHelpDialog());

        // Profile button
        Button profileBtn = new Button("👤");
        profileBtn.getStyleClass().addAll("user-action-button", "profile-button");
        profileBtn.setTooltip(createStyledTooltip("View your profile"));
        profileBtn.setOnAction(e -> showProfileDialog());

        // Logout button
        Button logoutBtn = new Button("Logout");
        logoutBtn.getStyleClass().add("user-logout-button");
        logoutBtn.setTooltip(createStyledTooltip("Sign out of your account"));
        logoutBtn.setOnAction(e -> handleLogout());

        actionSection.getChildren().addAll(helpBtn, profileBtn, logoutBtn);

        return actionSection;
    }

    private Tooltip createStyledTooltip(String text) {
        Tooltip tooltip = new Tooltip(text);
        tooltip.getStyleClass().add("tooltip");
        return tooltip;
    }

    private TabPane createTabPane() {
        TabPane tabPane = new TabPane();
        tabPane.getStyleClass().add("user-tab-pane");

        // Search & Book Tab
        Tab searchTab = createStyledTab("🔍 Search & Book", "user-search-tab");
        try {
            VBox searchContent = new UserBookingScene(username, jwtToken).getContent();
            searchContent.getStyleClass().add("user-content-area");
            searchTab.setContent(searchContent);
        } catch (Exception e) {
            System.err.println("Error creating UserBookingScene: " + e.getMessage());
            searchTab.setContent(createErrorContent("Search & Book"));
        }
        searchTab.setClosable(false);

        // My Reservations Tab
        Tab reservationsTab = createStyledTab("📋 My Reservations", "user-reservations-tab");
        try {
            VBox reservationContent = new UserReservationsScene(username, jwtToken).getContent();
            reservationContent.getStyleClass().add("user-content-area");
            reservationsTab.setContent(reservationContent);
        } catch (Exception e) {
            System.err.println("Error creating UserReservationsScene: " + e.getMessage());
            reservationsTab.setContent(createErrorContent("My Reservations"));
        }
        reservationsTab.setClosable(false);

        tabPane.getTabs().addAll(searchTab, reservationsTab);

        // Add tab change listener for animations
        tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab != null) {
                animateTabTransition(newTab);
            }
        });

        return tabPane;
    }

    private Tab createStyledTab(String title, String styleClass) {
        Tab tab = new Tab(title);
        tab.getStyleClass().add(styleClass);
        return tab;
    }

    private VBox createErrorContent(String tabName) {
        VBox errorContainer = new VBox(15);
        errorContainer.setAlignment(Pos.CENTER);
        errorContainer.getStyleClass().add("error-container");
        errorContainer.setPadding(new Insets(50));

        Label errorIcon = new Label("⚠️");
        errorIcon.getStyleClass().add("error-icon");

        Label errorMessage = new Label("Error loading " + tabName);
        errorMessage.getStyleClass().add("error-message");

        Label errorDetail = new Label("Please try refreshing or contact support");
        errorDetail.getStyleClass().add("error-detail");

        Button retryButton = new Button("🔄 Retry");
        retryButton.getStyleClass().addAll("user-action-button", "help-button");
        retryButton.setOnAction(e -> {
            // Trigger a refresh of the current scene
            show(primaryStage);
        });

        errorContainer.getChildren().addAll(errorIcon, errorMessage, errorDetail, retryButton);
        return errorContainer;
    }

    private HBox createStatusBar() {
        HBox statusBar = new HBox(15);
        statusBar.setAlignment(Pos.CENTER_LEFT);
        statusBar.setPadding(new Insets(8, 25, 8, 25));
        statusBar.getStyleClass().add("user-status-bar");

        Label statusLabel = new Label("Welcome to your booking dashboard");
        statusLabel.getStyleClass().add("user-status-label");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label connectionStatus = new Label("🟢 Connected");
        connectionStatus.getStyleClass().add("user-connection-status");

        Label timeLabel = new Label();
        timeLabel.getStyleClass().add("user-time-label");
        updateTimeLabel(timeLabel);

        statusBar.getChildren().addAll(statusLabel, spacer, connectionStatus, timeLabel);

        return statusBar;
    }

    private void updateTimeLabel(Label timeLabel) {
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.seconds(1), e -> {
                    timeLabel.setText(java.time.LocalDateTime.now().format(
                            java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")
                    ));
                })
        );
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void setupStage(Stage stage, Scene scene) {
        stage.setTitle("Hotel Booking - " + username);
        stage.setScene(scene);
        stage.setMinWidth(1000);
        stage.setMinHeight(600);

        // Add application icon if available
        try {
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/icon.png")));
        } catch (Exception e) {
            // Icon not found, continue without it
        }

        // Handle window closing
        stage.setOnCloseRequest(e -> {
            e.consume(); // Prevent default close
            handleApplicationExit();
        });

        stage.show();

        // Set full screen AFTER the stage is shown
        Platform.runLater(() -> {
            try {
                stage.setFullScreen(true);
                stage.setFullScreenExitHint("Press ESC to exit full screen");
                stage.setFullScreenExitKeyCombination(javafx.scene.input.KeyCombination.valueOf("ESC"));
                System.out.println("Full screen enabled in UserDashboardScene: " + stage.isFullScreen());
            } catch (Exception e) {
                System.err.println("Could not set full screen in UserDashboardScene: " + e.getMessage());
                // Fallback to maximized if full screen fails
                stage.setMaximized(true);
                System.out.println("Fallback to maximized: " + stage.isMaximized());
            }
        });
    }

    private void addEntranceAnimation(BorderPane root) {
        FadeTransition fadeIn = new FadeTransition(Duration.millis(600), root);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();
    }

    private void animateTabTransition(Tab tab) {
        if (tab.getContent() != null) {
            FadeTransition fade = new FadeTransition(Duration.millis(250), tab.getContent());
            fade.setFromValue(0.7);
            fade.setToValue(1.0);
            fade.play();
        }
    }

    private void showHelpDialog() {
        Alert helpAlert = new Alert(Alert.AlertType.INFORMATION);
        helpAlert.setTitle("Help & Support");
        helpAlert.setHeaderText("How can we help you?");
        helpAlert.setContentText(
                "🔍 Search & Book: Find available rooms and make reservations\n" +
                        "📋 My Reservations: View and manage your bookings\n\n" +
                        "📞 Need assistance? Contact our 24/7 support team:\n" +
                        "   Phone: +1 (555) 123-4567\n" +
                        "   Email: support@hotel.com\n\n" +
                        "💡 Pro Tips:\n" +
                        "• Book in advance for better rates!\n" +
                        "• Check our seasonal promotions\n" +
                        "• Contact us for special requests\n\n" +
                        "Thank you for choosing our hotel! 🏨"
        );

        styleDialog(helpAlert);
        helpAlert.showAndWait();
    }

    private void showProfileDialog() {
        Alert profileAlert = new Alert(Alert.AlertType.INFORMATION);
        profileAlert.setTitle("Profile Information");
        profileAlert.setHeaderText("Your Account Details");

        // Get current date info
        String memberSince = java.time.LocalDate.now().getYear() + "";
        String currentDate = java.time.LocalDateTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("MMMM dd, yyyy")
        );

        profileAlert.setContentText(
                "👤 Username: " + username + "\n" +
                        "🎭 Account Type: Guest\n" +
                        "📅 Member Since: " + memberSince + "\n" +
                        "⭐ Status: Active\n" +
                        "🗓️ Last Login: " + currentDate + "\n\n" +
                        "To update your profile information, change your password,\n" +
                        "or manage your account settings, please contact our\n" +
                        "support team.\n\n" +
                        "We value your business! 🌟"
        );

        styleDialog(profileAlert);
        profileAlert.showAndWait();
    }

    private void handleLogout() {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Logout");
        confirmAlert.setHeaderText("Are you sure you want to logout?");
        confirmAlert.setContentText(
                "You will be signed out and redirected to the login screen.\n\n" +
                        "Any unsaved changes will be lost.\n\n" +
                        "Thank you for using our hotel booking system!"
        );

        styleDialog(confirmAlert);

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                performLogout();
            }
        });
    }

    private void performLogout() {
        // Add logout animation
        FadeTransition fadeOut = new FadeTransition(Duration.millis(400), primaryStage.getScene().getRoot());
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> {
            // Clear session data
            LoginScene.JWT_TOKEN = null;
            LoginScene.LOGGED_IN_USERNAME = null;
            LoginScene.LOGGED_IN_ROLE = null;

            // Return to login screen
            new LoginScene().show(primaryStage);
        });
        fadeOut.play();
    }

    private void handleApplicationExit() {
        Alert exitAlert = new Alert(Alert.AlertType.CONFIRMATION);
        exitAlert.setTitle("Exit Application");
        exitAlert.setHeaderText("Are you sure you want to exit?");
        exitAlert.setContentText(
                "The application will close and you will be logged out.\n\n" +
                        "Thank you for using our hotel booking system!\n" +
                        "We hope to see you again soon! 🏨✨"
        );

        styleDialog(exitAlert);

        exitAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                Platform.exit();
            }
        });
    }

    private void styleDialog(Alert alert) {
        alert.getDialogPane().getStylesheets().add(
                getClass().getResource("/css/user_booking_style.css").toExternalForm()
        );
        alert.getDialogPane().getStyleClass().add("user-confirmation-dialog");
    }
}