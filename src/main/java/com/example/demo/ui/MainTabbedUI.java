package com.example.demo.ui;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.application.Application;
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

public class MainTabbedUI extends Application {

    private static String jwtToken;
    private static String username;
    private static String role;

    private TabPane tabPane;
    private Stage primaryStage;

    public MainTabbedUI(String jwtToken, String username, String role) {
        MainTabbedUI.jwtToken = jwtToken;
        MainTabbedUI.username = username;
        MainTabbedUI.role = role;
    }

    public MainTabbedUI() {}  // Required for Application.launch()

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;

        // Setup main layout
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
        root.getStyleClass().add("main-container");

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
        topBar.setPadding(new Insets(12, 20, 12, 20));
        topBar.getStyleClass().add("top-bar");

        // Logo section
        HBox logoSection = createLogoSection();

        // User info section
        VBox userInfoSection = createUserInfoSection();

        // Action buttons section
        HBox actionSection = createActionSection();

        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        topBar.getChildren().addAll(logoSection, userInfoSection, spacer, actionSection);

        return topBar;
    }

    private HBox createLogoSection() {
        HBox logoSection = new HBox(10);
        logoSection.setAlignment(Pos.CENTER_LEFT);

        try {
            ImageView logoView = new ImageView(new Image(getClass().getResourceAsStream("/images/img.png")));
            logoView.setFitHeight(50);
            logoView.setPreserveRatio(true);
            logoView.getStyleClass().add("logo-image");

            VBox logoContainer = new VBox();
            logoContainer.setAlignment(Pos.CENTER);
            logoContainer.getChildren().add(logoView);

            Label appTitle = new Label("Hotel Management");
            appTitle.getStyleClass().add("app-title");

            logoSection.getChildren().addAll(logoContainer, appTitle);
        } catch (Exception e) {
            // Fallback if image not found
            Label fallbackLogo = new Label("🏨");
            fallbackLogo.getStyleClass().addAll("logo-fallback");

            Label appTitle = new Label("Hotel Management");
            appTitle.getStyleClass().add("app-title");

            logoSection.getChildren().addAll(fallbackLogo, appTitle);
        }

        return logoSection;
    }

    private VBox createUserInfoSection() {
        VBox userInfo = new VBox(2);
        userInfo.setAlignment(Pos.CENTER_LEFT);

        Label welcomeLabel = new Label("Welcome back!");
        welcomeLabel.getStyleClass().add("welcome-label");

        Label userLabel = new Label(username);
        userLabel.getStyleClass().add("username-label");

        Label roleLabel = new Label("Role: " + role.toUpperCase());
        roleLabel.getStyleClass().add("role-label");

        userInfo.getChildren().addAll(welcomeLabel, userLabel, roleLabel);

        return userInfo;
    }

    private HBox createActionSection() {
        HBox actionSection = new HBox(10);
        actionSection.setAlignment(Pos.CENTER_RIGHT);

        // Refresh button
        Button refreshBtn = new Button("🔄");
        refreshBtn.getStyleClass().addAll("action-button", "refresh-button");
        refreshBtn.setTooltip(new Tooltip("Refresh current tab"));
        refreshBtn.setOnAction(e -> refreshCurrentTab());

        // Settings button (placeholder for future features)
        Button settingsBtn = new Button("⚙️");
        settingsBtn.getStyleClass().addAll("action-button", "settings-button");
        settingsBtn.setTooltip(new Tooltip("Settings"));
        settingsBtn.setOnAction(e -> showSettingsDialog());

        // Logout button
        Button logoutBtn = new Button("Logout");
        logoutBtn.getStyleClass().add("logout-button");
        logoutBtn.setTooltip(new Tooltip("Sign out of your account"));
        logoutBtn.setOnAction(e -> handleLogout());

        actionSection.getChildren().addAll(refreshBtn, settingsBtn, logoutBtn);

        return actionSection;
    }

    private TabPane createTabPane() {
        TabPane tabPane = new TabPane();
        tabPane.getStyleClass().add("main-tab-pane");

        // Create tabs based on role
        System.out.println("Creating tabs for role: " + role); // Debug output
        if (role != null && role.toUpperCase().contains("ADMIN")) {
            createAdminTabs(tabPane);
        } else {
            createUserTabs(tabPane);
        }

        // Add tab change listener for animations
        tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab != null) {
                animateTabTransition(newTab);
            }
        });

        return tabPane;
    }

    private void createAdminTabs(TabPane tabPane) {
        // Dashboard Tab
        Tab dashboardTab = createStyledTab("📊 Dashboard", "dashboard-tab");
        try {
            dashboardTab.setContent(new AdminDashboardScene(jwtToken).getContent());
        } catch (Exception e) {
            System.err.println("Error creating AdminDashboardScene: " + e.getMessage());
            dashboardTab.setContent(createErrorContent("Dashboard"));
        }
        dashboardTab.setClosable(false);

        // Room Management Tab
        Tab manageRoomsTab = createStyledTab("🏢 Manage Rooms", "rooms-tab");
        try {
            manageRoomsTab.setContent(new RoomManagementScene(jwtToken).getContent());
        } catch (Exception e) {
            System.err.println("Error creating RoomManagementScene: " + e.getMessage());
            manageRoomsTab.setContent(createErrorContent("Room Management"));
        }
        manageRoomsTab.setClosable(false);

        // Search & Booking Tab
        Tab searchTab = createStyledTab("🔍 Search Rooms", "search-tab");
        try {
            searchTab.setContent(new UnifiedBookingScene(jwtToken).getContent());
        } catch (Exception e) {
            System.err.println("Error creating UnifiedBookingScene: " + e.getMessage());
            searchTab.setContent(createErrorContent("Search Rooms"));
        }
        searchTab.setClosable(false);

        // Reservations Tab
        Tab reservationsTab = createStyledTab("📋 Reservations", "reservations-tab");
        try {
            reservationsTab.setContent(new ReservationsScene(jwtToken).getContent());
        } catch (Exception e) {
            System.err.println("Error creating ReservationsScene: " + e.getMessage());
            reservationsTab.setContent(createErrorContent("Reservations"));
        }
        reservationsTab.setClosable(false);

        tabPane.getTabs().addAll(dashboardTab, manageRoomsTab, searchTab, reservationsTab);
    }

    private void createUserTabs(TabPane tabPane) {
        // Search & Booking Tab
        Tab searchTab = createStyledTab("🔍 Search Rooms", "search-tab");
        try {
            searchTab.setContent(new UnifiedBookingScene(jwtToken).getContent());
        } catch (Exception e) {
            System.err.println("Error creating UnifiedBookingScene: " + e.getMessage());
            searchTab.setContent(createErrorContent("Search Rooms"));
        }
        searchTab.setClosable(false);

        // My Reservations Tab
        Tab reservationsTab = createStyledTab("📋 My Reservations", "reservations-tab");
        try {
            reservationsTab.setContent(new ReservationsScene(jwtToken).getContent());
        } catch (Exception e) {
            System.err.println("Error creating ReservationsScene: " + e.getMessage());
            reservationsTab.setContent(createErrorContent("My Reservations"));
        }
        reservationsTab.setClosable(false);

        tabPane.getTabs().addAll(searchTab, reservationsTab);
    }

    private Tab createStyledTab(String title, String styleClass) {
        Tab tab = new Tab(title);
        tab.getStyleClass().add(styleClass);

        // Add loading placeholder initially
        ProgressIndicator loading = new ProgressIndicator();
        loading.getStyleClass().add("tab-loading");
        VBox loadingContainer = new VBox(loading);
        loadingContainer.setAlignment(Pos.CENTER);
        loadingContainer.getStyleClass().add("tab-loading-container");

        return tab;
    }

    private HBox createStatusBar() {
        HBox statusBar = new HBox(15);
        statusBar.setAlignment(Pos.CENTER_LEFT);
        statusBar.setPadding(new Insets(8, 20, 8, 20));
        statusBar.getStyleClass().add("status-bar");

        Label statusLabel = new Label("Ready");
        statusLabel.getStyleClass().add("status-label");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label connectionStatus = new Label("🟢 Connected");
        connectionStatus.getStyleClass().add("connection-status");

        Label timeLabel = new Label();
        timeLabel.getStyleClass().add("time-label");
        updateTimeLabel(timeLabel);

        statusBar.getChildren().addAll(statusLabel, spacer, connectionStatus, timeLabel);

        return statusBar;
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

        Label errorDetail = new Label("Please check the console for details");
        errorDetail.getStyleClass().add("error-detail");

        errorContainer.getChildren().addAll(errorIcon, errorMessage, errorDetail);
        return errorContainer;
    }

    private void updateTimeLabel(Label timeLabel) {
        // Update time every second
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
        stage.setTitle("Hotel Management System - " + role);
        stage.setScene(scene);
        stage.setMinWidth(1000);
        stage.setMinHeight(600);

        // Set fullscreen mode
        stage.setFullScreen(true);
        stage.setFullScreenExitHint("Press ESC to exit fullscreen mode");

        // Optional: Prevent exiting fullscreen with ESC key (uncomment if needed)
        // stage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);

        // Add application icon if available
        try {
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/icon.png")));
        } catch (Exception e) {
            // Icon not found, continue without it
        }

        // Handle window closing
        stage.setOnCloseRequest(e -> {
            handleApplicationExit();
        });

        stage.show();
    }

    private void addEntranceAnimation(BorderPane root) {
        FadeTransition fadeIn = new FadeTransition(Duration.millis(500), root);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);

        ScaleTransition scaleIn = new ScaleTransition(Duration.millis(500), root);
        scaleIn.setFromX(0.9);
        scaleIn.setFromY(0.9);
        scaleIn.setToX(1.0);
        scaleIn.setToY(1.0);

        fadeIn.play();
        scaleIn.play();
    }

    private void animateTabTransition(Tab tab) {
        if (tab.getContent() != null) {
            FadeTransition fade = new FadeTransition(Duration.millis(200), tab.getContent());
            fade.setFromValue(0.8);
            fade.setToValue(1.0);
            fade.play();
        }
    }

    private void refreshCurrentTab() {
        Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
        if (selectedTab != null) {
            // Add refresh logic here - reload tab content
            showStatusMessage("Refreshing " + selectedTab.getText() + "...");

            // Simulate refresh with a brief animation
            if (selectedTab.getContent() != null) {
                FadeTransition fadeOut = new FadeTransition(Duration.millis(150), selectedTab.getContent());
                fadeOut.setFromValue(1.0);
                fadeOut.setToValue(0.7);

                FadeTransition fadeIn = new FadeTransition(Duration.millis(150), selectedTab.getContent());
                fadeIn.setFromValue(0.7);
                fadeIn.setToValue(1.0);

                fadeOut.setOnFinished(e -> fadeIn.play());
                fadeOut.play();
            }
        }
    }

    private void showSettingsDialog() {
        Alert settingsAlert = new Alert(Alert.AlertType.INFORMATION);
        settingsAlert.setTitle("Settings");
        settingsAlert.setHeaderText("Application Settings");
        settingsAlert.setContentText("Settings panel coming soon!\n\nCurrent Configuration:\n" +
                "User: " + username + "\n" +
                "Role: " + role + "\n" +
                "Theme: Dark Mode");

        settingsAlert.getDialogPane().getStylesheets().add(
                getClass().getResource("/css/user_booking_style.css").toExternalForm()
        );
        settingsAlert.getDialogPane().getStyleClass().add("alert-dialog");

        settingsAlert.showAndWait();
    }

    private void handleLogout() {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Logout");
        confirmAlert.setHeaderText("Are you sure you want to logout?");
        confirmAlert.setContentText("You will be redirected to the login screen.");

        confirmAlert.getDialogPane().getStylesheets().add(
                getClass().getResource("/css/user_booking_style.css").toExternalForm()
        );
        confirmAlert.getDialogPane().getStyleClass().add("alert-dialog");

        if (confirmAlert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            performLogout();
        }
    }

    private void performLogout() {
        // Clear session data
        LoginScene.JWT_TOKEN = null;
        LoginScene.LOGGED_IN_USERNAME = null;
        LoginScene.LOGGED_IN_ROLE = null;

        // Add logout animation
        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), primaryStage.getScene().getRoot());
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> {
            new LoginScene().show(primaryStage);
        });
        fadeOut.play();
    }

    private void handleApplicationExit() {
        Alert exitAlert = new Alert(Alert.AlertType.CONFIRMATION);
        exitAlert.setTitle("Exit Application");
        exitAlert.setHeaderText("Are you sure you want to exit?");
        exitAlert.setContentText("Any unsaved changes will be lost.");

        exitAlert.getDialogPane().getStylesheets().add(
                getClass().getResource("/css/user_booking_style.css").toExternalForm()
        );

        if (exitAlert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            Platform.exit();
        }
    }

    private void showStatusMessage(String message) {
        // This would update the status bar - implementation depends on your needs
        System.out.println("Status: " + message);
    }
}