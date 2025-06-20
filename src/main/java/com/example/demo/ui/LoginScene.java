package com.example.demo.ui;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
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
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

public class LoginScene {

    private final RestTemplate restTemplate = new RestTemplate();

    public static String JWT_TOKEN = null;
    public static String LOGGED_IN_USERNAME = null;
    public static String LOGGED_IN_ROLE = null;

    public void show(Stage primaryStage) {
        // Create main layout
        BorderPane root = createMainLayout();

        // Create scene
        Scene scene = new Scene(root, 900, 650);
        scene.getStylesheets().add(getClass().getResource("/css/user_booking_style.css").toExternalForm());

        // Configure stage
        setupStage(primaryStage, scene);

        // Add entrance animation
        addEntranceAnimation(root);
    }

    private BorderPane createMainLayout() {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("login-main-container");

        // Create left panel (branding/info)
        VBox leftPanel = createLeftPanel();

        // Create right panel (login form)
        VBox rightPanel = createRightPanel();

        root.setLeft(leftPanel);
        root.setRight(rightPanel);

        return root;
    }

    private VBox createLeftPanel() {
        VBox leftPanel = new VBox(20);
        leftPanel.setAlignment(Pos.CENTER);
        leftPanel.setPadding(new Insets(40));
        leftPanel.getStyleClass().add("login-left-panel");

        // Logo section
        VBox logoSection = createLogoSection();

        // Welcome message
        VBox welcomeSection = createWelcomeSection();

        // Features section
        VBox featuresSection = createFeaturesSection();

        leftPanel.getChildren().addAll(logoSection, welcomeSection, featuresSection);

        return leftPanel;
    }

    private VBox createLogoSection() {
        VBox logoSection = new VBox(10);
        logoSection.setAlignment(Pos.CENTER);

        try {
            ImageView logoView = new ImageView(new Image(getClass().getResourceAsStream("/images/img.png")));
            logoView.setFitHeight(80);
            logoView.setPreserveRatio(true);
            logoView.getStyleClass().add("login-logo-image");
            logoSection.getChildren().add(logoView);
        } catch (Exception e) {
            // Fallback logo
            Label fallbackLogo = new Label("🏨");
            fallbackLogo.getStyleClass().add("login-logo-fallback");
            logoSection.getChildren().add(fallbackLogo);
        }

        Label appTitle = new Label("Hotel Management System");
        appTitle.getStyleClass().add("login-app-title");

        Label appSubtitle = new Label("Professional Booking Solution");
        appSubtitle.getStyleClass().add("login-app-subtitle");

        logoSection.getChildren().addAll(appTitle, appSubtitle);
        return logoSection;
    }

    private VBox createWelcomeSection() {
        VBox welcomeSection = new VBox(8);
        welcomeSection.setAlignment(Pos.CENTER);

        Label welcomeTitle = new Label("Welcome Back!");
        welcomeTitle.getStyleClass().add("login-welcome-title");

        Label welcomeText = new Label("Sign in to access your dashboard and manage your hotel operations efficiently.");
        welcomeText.getStyleClass().add("login-welcome-text");
        welcomeText.setWrapText(true);

        welcomeSection.getChildren().addAll(welcomeTitle, welcomeText);
        return welcomeSection;
    }

    private VBox createFeaturesSection() {
        VBox featuresSection = new VBox(12);
        featuresSection.setAlignment(Pos.CENTER_LEFT);
        featuresSection.getStyleClass().add("login-features-section");

        Label featuresTitle = new Label("✨ Key Features");
        featuresTitle.getStyleClass().add("login-features-title");

        VBox featuresList = new VBox(8);
        featuresList.setAlignment(Pos.CENTER_LEFT);

        String[] features = {
                "🏢 Room Management & Availability",
                "📋 Reservation System",
                "👥 User Account Management",
                "📊 Real-time Analytics",
                "💳 Payment Processing",
                "🔒 Secure Access Control"
        };

        for (String feature : features) {
            Label featureLabel = new Label(feature);
            featureLabel.getStyleClass().add("login-feature-item");
            featuresList.getChildren().add(featureLabel);
        }

        featuresSection.getChildren().addAll(featuresTitle, featuresList);
        return featuresSection;
    }

    private VBox createRightPanel() {
        VBox rightPanel = new VBox();
        rightPanel.setAlignment(Pos.CENTER);
        rightPanel.setPadding(new Insets(40));
        rightPanel.getStyleClass().add("login-right-panel");

        // Login form container
        VBox loginForm = createLoginForm();

        rightPanel.getChildren().add(loginForm);
        return rightPanel;
    }

    private VBox createLoginForm() {
        VBox loginForm = new VBox(25);
        loginForm.setAlignment(Pos.CENTER);
        loginForm.setPadding(new Insets(40));
        loginForm.getStyleClass().add("login-form-container");
        loginForm.setMaxWidth(400);

        // Form header
        VBox headerSection = createFormHeader();

        // Input fields section
        VBox inputSection = createInputSection();

        // Buttons section
        VBox buttonSection = createButtonSection();

        // Footer section
        VBox footerSection = createFooterSection();

        loginForm.getChildren().addAll(headerSection, inputSection, buttonSection, footerSection);

        return loginForm;
    }

    private VBox createFormHeader() {
        VBox headerSection = new VBox(8);
        headerSection.setAlignment(Pos.CENTER);

        Label formTitle = new Label("Sign In");
        formTitle.getStyleClass().add("login-form-title");

        Label formSubtitle = new Label("Enter your credentials to access the system");
        formSubtitle.getStyleClass().add("login-form-subtitle");

        headerSection.getChildren().addAll(formTitle, formSubtitle);
        return headerSection;
    }

    private VBox createInputSection() {
        VBox inputSection = new VBox(20);
        inputSection.setAlignment(Pos.CENTER);

        // Username field
        VBox usernameGroup = new VBox(5);
        Label usernameLabel = new Label("Username");
        usernameLabel.getStyleClass().add("login-field-label");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Enter your username");
        usernameField.getStyleClass().add("login-text-field");
        usernameField.setPrefHeight(45);

        usernameGroup.getChildren().addAll(usernameLabel, usernameField);

        // Password field
        VBox passwordGroup = new VBox(5);
        Label passwordLabel = new Label("Password");
        passwordLabel.getStyleClass().add("login-field-label");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter your password");
        passwordField.getStyleClass().add("login-password-field");
        passwordField.setPrefHeight(45);

        passwordGroup.getChildren().addAll(passwordLabel, passwordField);

        // Error label
        Label errorLabel = new Label();
        errorLabel.getStyleClass().add("login-error-label");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        inputSection.getChildren().addAll(usernameGroup, passwordGroup, errorLabel);

        // Store references for event handlers
        inputSection.setUserData(Map.of(
                "usernameField", usernameField,
                "passwordField", passwordField,
                "errorLabel", errorLabel
        ));

        return inputSection;
    }

    private VBox createButtonSection() {
        VBox buttonSection = new VBox(15);
        buttonSection.setAlignment(Pos.CENTER);

        // Login button
        Button loginButton = new Button("🔐 Sign In");
        loginButton.getStyleClass().add("login-primary-button");
        loginButton.setPrefHeight(50);
        loginButton.setMaxWidth(Double.MAX_VALUE);

        // Loading indicator (initially hidden)
        ProgressIndicator loadingIndicator = new ProgressIndicator();
        loadingIndicator.getStyleClass().add("login-loading-indicator");
        loadingIndicator.setVisible(false);
        loadingIndicator.setManaged(false);
        loadingIndicator.setPrefSize(30, 30);

        // Demo credentials info
        VBox demoSection = createDemoSection();

        buttonSection.getChildren().addAll(loginButton, loadingIndicator, demoSection);

        // Set up login action
        setupLoginAction(loginButton, loadingIndicator);

        return buttonSection;
    }

    private VBox createDemoSection() {
        VBox demoSection = new VBox(8);
        demoSection.setAlignment(Pos.CENTER);
        demoSection.getStyleClass().add("login-demo-section");

        Label demoTitle = new Label("💡 Demo Credentials");
        demoTitle.getStyleClass().add("login-demo-title");

        VBox demoCredentials = new VBox(4);
        demoCredentials.setAlignment(Pos.CENTER);

        Label adminDemo = new Label("👑 Admin: admin / admin");
        adminDemo.getStyleClass().add("login-demo-credential");

        Label userDemo = new Label("👤 User: user / user");
        userDemo.getStyleClass().add("login-demo-credential");

        demoCredentials.getChildren().addAll(adminDemo, userDemo);
        demoSection.getChildren().addAll(demoTitle, demoCredentials);

        return demoSection;
    }

    private VBox createFooterSection() {
        VBox footerSection = new VBox(8);
        footerSection.setAlignment(Pos.CENTER);

        Label supportLabel = new Label("Need help? Contact support at:");
        supportLabel.getStyleClass().add("login-support-label");

        Label contactInfo = new Label("📧 support@hotel.com | 📞 +1 (555) 123-4567");
        contactInfo.getStyleClass().add("login-contact-info");

        footerSection.getChildren().addAll(supportLabel, contactInfo);
        return footerSection;
    }

    @SuppressWarnings("unchecked")
    private void setupLoginAction(Button loginButton, ProgressIndicator loadingIndicator) {
        loginButton.setOnAction(e -> {
            // Find the input section and get field references
            VBox inputSection = findInputSection(loginButton);
            if (inputSection == null) return;

            Map<String, Object> fieldMap = (Map<String, Object>) inputSection.getUserData();
            TextField usernameField = (TextField) fieldMap.get("usernameField");
            PasswordField passwordField = (PasswordField) fieldMap.get("passwordField");
            Label errorLabel = (Label) fieldMap.get("errorLabel");

            String username = usernameField.getText().trim();
            String password = passwordField.getText();

            // Clear previous error
            hideError(errorLabel);

            // Validate input
            if (username.isEmpty() || password.isEmpty()) {
                showError(errorLabel, "Please enter both username and password.");
                return;
            }

            // Show loading state
            showLoadingState(loginButton, loadingIndicator, true);

            // Perform login in background thread
            Platform.runLater(() -> performLogin(username, password, loginButton, loadingIndicator, errorLabel));
        });

        // Store reference for later setup of Enter key support
        loginButton.setUserData(Map.of("needsEnterKeySetup", true));
    }

    private VBox findInputSection(Button loginButton) {
        // Navigate up the scene graph to find the input section
        javafx.scene.Node parent = loginButton.getParent();
        while (parent != null) {
            if (parent instanceof VBox) {
                VBox vbox = (VBox) parent;
                for (javafx.scene.Node child : vbox.getChildren()) {
                    if (child instanceof VBox && child.getUserData() instanceof Map) {
                        return (VBox) child;
                    }
                }
            }
            parent = parent.getParent();
        }
        return null;
    }

    private void setupEnterKeySupport(Button loginButton) {
        // This method is now handled in setupStage after scene creation
        // Remove this method as it's causing the null pointer exception
    }

    private void performLogin(String username, String password, Button loginButton,
                              ProgressIndicator loadingIndicator, Label errorLabel) {
        try {
            String url = "http://localhost:8080/authenticate";

            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("username", username);
            requestBody.put("password", password);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            Platform.runLater(() -> {
                showLoadingState(loginButton, loadingIndicator, false);

                if (response.getStatusCode() == HttpStatus.OK) {
                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        JsonNode root = mapper.readTree(response.getBody());

                        JWT_TOKEN = root.path("token").asText();
                        LOGGED_IN_ROLE = root.path("role").asText();
                        LOGGED_IN_USERNAME = username;

                        System.out.println("JWT: " + JWT_TOKEN);
                        System.out.println("Role: " + LOGGED_IN_ROLE);
                        System.out.println("Username: " + LOGGED_IN_USERNAME);

                        // Add success animation
                        addSuccessAnimation(loginButton, () -> {
                            if ("ROLE_ADMIN".equalsIgnoreCase(LOGGED_IN_ROLE)) {
                                new MainTabbedUI(JWT_TOKEN, LOGGED_IN_USERNAME, LOGGED_IN_ROLE)
                                        .start((Stage) loginButton.getScene().getWindow());
                            } else {
                                new UserDashboardScene(LOGGED_IN_USERNAME, JWT_TOKEN)
                                        .show((Stage) loginButton.getScene().getWindow());
                            }
                        });

                    } catch (Exception ex) {
                        showError(errorLabel, "Error processing login response.");
                        ex.printStackTrace();
                    }
                } else {
                    showError(errorLabel, "Invalid username or password.");
                }
            });

        } catch (Exception ex) {
            Platform.runLater(() -> {
                showLoadingState(loginButton, loadingIndicator, false);
                showError(errorLabel, "Connection failed: " + ex.getMessage());
                ex.printStackTrace();
            });
        }
    }

    private void showLoadingState(Button button, ProgressIndicator indicator, boolean loading) {
        if (loading) {
            button.setText("Signing in...");
            button.setDisable(true);
            indicator.setVisible(true);
            indicator.setManaged(true);
        } else {
            button.setText("🔐 Sign In");
            button.setDisable(false);
            indicator.setVisible(false);
            indicator.setManaged(false);
        }
    }

    private void showError(Label errorLabel, String message) {
        errorLabel.setText("❌ " + message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);

        // Add error animation
        ScaleTransition shake = new ScaleTransition(Duration.millis(100), errorLabel);
        shake.setFromX(1.0);
        shake.setToX(1.05);
        shake.setCycleCount(4);
        shake.setAutoReverse(true);
        shake.play();
    }

    private void hideError(Label errorLabel) {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }

    private void addSuccessAnimation(Button button, Runnable onComplete) {
        button.setText("✅ Success!");
        button.getStyleClass().add("login-success-button");

        ScaleTransition success = new ScaleTransition(Duration.millis(200), button);
        success.setFromX(1.0);
        success.setToX(1.1);
        success.setCycleCount(2);
        success.setAutoReverse(true);
        success.setOnFinished(e -> {
            // Delay before transition
            Platform.runLater(() -> {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
                onComplete.run();
            });
        });
        success.play();
    }

    private void setupStage(Stage stage, Scene scene) {
        stage.setTitle("Hotel Management System - Login");
        stage.setScene(scene);
        stage.setMinWidth(900);
        stage.setMinHeight(650);
        stage.setResizable(true);
        stage.centerOnScreen();

        // Add application icon if available
        try {
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/icon.png")));
        } catch (Exception e) {
            // Icon not found, continue without it
        }

        // Set up Enter key support now that the scene is created
        setupEnterKeySupport(scene);

        stage.show();
    }

    private void setupEnterKeySupport(Scene scene) {
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.ENTER) {
                // Find the login button and trigger it
                findAndTriggerLoginButton(scene);
            }
        });
    }

    private void findAndTriggerLoginButton(Scene scene) {
        // Find the login button in the scene graph
        javafx.scene.Node root = scene.getRoot();
        Button loginButton = findLoginButtonInNode(root);
        if (loginButton != null && !loginButton.isDisabled()) {
            loginButton.fire();
        }
    }

    private Button findLoginButtonInNode(javafx.scene.Node node) {
        if (node instanceof Button) {
            Button button = (Button) node;
            if (button.getText() != null && button.getText().contains("Sign In")) {
                return button;
            }
        } else if (node instanceof javafx.scene.Parent) {
            javafx.scene.Parent parent = (javafx.scene.Parent) node;
            for (javafx.scene.Node child : parent.getChildrenUnmodifiable()) {
                Button found = findLoginButtonInNode(child);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    private void addEntranceAnimation(BorderPane root) {
        // Fade in animation
        FadeTransition fadeIn = new FadeTransition(Duration.millis(800), root);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();

        // Scale animation for the form
        VBox rightPanel = (VBox) root.getRight();
        if (rightPanel != null) {
            ScaleTransition scaleIn = new ScaleTransition(Duration.millis(600), rightPanel);
            scaleIn.setFromX(0.8);
            scaleIn.setFromY(0.8);
            scaleIn.setToX(1.0);
            scaleIn.setToY(1.0);
            scaleIn.setDelay(Duration.millis(200));
            scaleIn.play();
        }
    }
}