import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginView {

    public Pane getView() {
        StackPane root = new StackPane();
        root.getStyleClass().add("root-pane");

        // ── Decorative glow blobs (background orbs) ──────────────────────
        Pane blobs = new Pane();
        blobs.setMouseTransparent(true);

        Circle blob1 = new Circle(180);
        blob1.setStyle("-fx-fill: radial-gradient(center 50% 50%, radius 100%, rgba(0,180,100,0.13) 0%, transparent 100%);");
        blob1.setLayoutX(160); blob1.setLayoutY(200);

        Circle blob2 = new Circle(140);
        blob2.setStyle("-fx-fill: radial-gradient(center 50% 50%, radius 100%, rgba(0,130,230,0.10) 0%, transparent 100%);");
        blob2.setLayoutX(900); blob2.setLayoutY(500);

        Circle blob3 = new Circle(100);
        blob3.setStyle("-fx-fill: radial-gradient(center 50% 50%, radius 100%, rgba(140,0,230,0.08) 0%, transparent 100%);");
        blob3.setLayoutX(700); blob3.setLayoutY(120);

        blobs.getChildren().addAll(blob1, blob2, blob3);

        // ── Left brand panel ───────────────────────────────────────────────
        VBox brandPanel = new VBox(12);
        brandPanel.setAlignment(Pos.CENTER);
        brandPanel.setPrefWidth(420);
        brandPanel.setPadding(new Insets(60, 40, 60, 50));
        brandPanel.setStyle(
            "-fx-background-color: rgba(0,255,140,0.04);" +
            "-fx-border-color: rgba(0,255,140,0.08);" +
            "-fx-border-width: 0 1 0 0;"
        );

        Label logoIcon  = new Label("🌿");
        logoIcon.setStyle("-fx-font-size: 56px;");

        Label logoText = new Label("EvoTrack");
        logoText.getStyleClass().add("label-brand");

        Label tagline = new Label("Carbon Footprint & Sustainability Tracker");
        tagline.getStyleClass().add("label-subheader");
        tagline.setWrapText(true);
        tagline.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        // Feature pills
        HBox f1 = featurePill("📊", "Real-time Analytics");
        HBox f2 = featurePill("🎯", "Personalised Goals");
        HBox f3 = featurePill("🏅", "Earn Achievements");
        HBox f4 = featurePill("🌍", "Carbon Offsets");

        VBox features = new VBox(10, f1, f2, f3, f4);
        features.setPadding(new Insets(24, 0, 0, 0));

        brandPanel.getChildren().addAll(logoIcon, logoText, tagline, features);

        // ── Right login form panel ─────────────────────────────────────────
        VBox formPanel = new VBox(0);
        formPanel.setAlignment(Pos.CENTER);
        formPanel.setPrefWidth(460);
        formPanel.setPadding(new Insets(60, 50, 60, 50));

        VBox glassCard = new VBox(18);
        glassCard.setAlignment(Pos.CENTER_LEFT);
        glassCard.setMaxWidth(370);
        glassCard.getStyleClass().add("glass-pane");

        Label signInTitle = new Label("Sign in");
        signInTitle.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #e0ffe8;");

        Label signInSub = new Label("Welcome back — let's track your impact.");
        signInSub.getStyleClass().add("label-subheader");

        // Email field with label
        VBox emailGroup = inputGroup("Email address", "email-field");
        TextField emailField = (TextField) ((VBox) emailGroup).lookup(".text-field-glass");
        if (emailField == null) {
            emailField = new TextField();
            emailField.setId("email-field");
            emailField.setPromptText("you@example.com");
            emailField.getStyleClass().add("text-field-glass");
            emailField.setMaxWidth(Double.MAX_VALUE);
            emailGroup.getChildren().add(emailField);
        }

        // Password field
        VBox passwordGroup = new VBox(6);
        Label pwLabel = new Label("Password");
        pwLabel.getStyleClass().add("label-muted");
        PasswordField passwordField = new PasswordField();
        passwordField.setId("password-field");
        passwordField.setPromptText("••••••••");
        passwordField.getStyleClass().add("text-field-glass");
        passwordField.setMaxWidth(Double.MAX_VALUE);
        passwordGroup.getChildren().addAll(pwLabel, passwordField);

        Label errorLabel = new Label("");
        errorLabel.getStyleClass().add("error-label");

        // Buttons
        Button loginBtn = new Button("Sign In →");
        loginBtn.setId("login-btn");
        loginBtn.getStyleClass().add("button-primary");
        loginBtn.setMaxWidth(Double.MAX_VALUE);

        HBox orRow = new HBox();
        orRow.setAlignment(Pos.CENTER);
        Label orLabel = new Label("— or —");
        orLabel.getStyleClass().add("label-muted");
        orRow.getChildren().add(orLabel);

        Button registerBtn = new Button("Create an account");
        registerBtn.setId("register-btn");
        registerBtn.getStyleClass().add("button-secondary");
        registerBtn.setMaxWidth(Double.MAX_VALUE);

        // ── Wire up email field properly ──────────────────────────────────────
        TextField emailActual = new TextField();
        emailActual.setId("email-actual");
        emailActual.setPromptText("you@example.com");
        emailActual.getStyleClass().add("text-field-glass");
        emailActual.setMaxWidth(Double.MAX_VALUE);
        VBox emailBox = new VBox(6);
        Label emailLbl2 = new Label("Email address");
        emailLbl2.getStyleClass().add("label-muted");
        emailBox.getChildren().addAll(emailLbl2, emailActual);

        glassCard.getChildren().addAll(signInTitle, signInSub, emailBox, passwordGroup, errorLabel, loginBtn, orRow, registerBtn);

        loginBtn.setOnAction(e -> {
            String email = emailActual.getText().trim();
            String pwd = passwordField.getText();
            if (email.isEmpty() || pwd.isEmpty()) {
                errorLabel.setText("Please enter email and password.");
                return;
            }
            if (authenticate(email, pwd)) {
                ViewManager.showMainLayout();
            } else {
                errorLabel.setText("Invalid email or password. Try again.");
            }
        });

        registerBtn.setOnAction(e -> ViewManager.showRegister());

        formPanel.getChildren().add(glassCard);

        // ── Full-width split layout ────────────────────────────────────────
        HBox splitLayout = new HBox();
        splitLayout.setAlignment(Pos.CENTER);
        HBox.setHgrow(brandPanel, Priority.ALWAYS);
        HBox.setHgrow(formPanel, Priority.ALWAYS);
        splitLayout.getChildren().addAll(brandPanel, formPanel);
        splitLayout.setPrefHeight(Region.USE_COMPUTED_SIZE);
        VBox.setVgrow(splitLayout, Priority.ALWAYS);

        VBox mainWrapper = new VBox(splitLayout);
        mainWrapper.setFillWidth(true);
        mainWrapper.getStyleClass().add("root-pane");

        root.getChildren().addAll(blobs, mainWrapper);
        return root;
    }

    // ── Helper: feature pill chip ────────────────────────────────────────────
    private HBox featurePill(String icon, String text) {
        HBox pill = new HBox(10);
        pill.setAlignment(Pos.CENTER_LEFT);
        pill.setStyle(
            "-fx-background-color: rgba(0,255,163,0.06);" +
            "-fx-background-radius: 30;" +
            "-fx-border-color: rgba(0,255,163,0.18);" +
            "-fx-border-radius: 30;" +
            "-fx-border-width: 1;" +
            "-fx-padding: 8 18 8 14;"
        );
        Label ico = new Label(icon);
        ico.setStyle("-fx-font-size: 16px;");
        Label txt = new Label(text);
        txt.setStyle("-fx-font-size: 13px; -fx-text-fill: rgba(200,240,215,0.75); -fx-font-weight: 500;");
        pill.getChildren().addAll(ico, txt);
        return pill;
    }

    // ── Helper: input group with label ───────────────────────────────────────
    private VBox inputGroup(String labelText, String fieldId) {
        VBox group = new VBox(6);
        Label lbl = new Label(labelText);
        lbl.getStyleClass().add("label-muted");
        group.getChildren().add(lbl);
        return group;
    }

    // ── Authentication logic ─────────────────────────────────────────────────
    private boolean authenticate(String email, String password) {
        String query = "SELECT user_id, org_id, name, password_hash, account_type, role FROM Users WHERE email = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String hash = rs.getString("password_hash");
                if (hash.equals(Integer.toHexString(password.hashCode()))) {
                    Integer orgId = rs.getObject("org_id") != null ? rs.getInt("org_id") : null;
                    SessionManager.setSession(
                        rs.getInt("user_id"),
                        rs.getString("name"),
                        email,
                        orgId,
                        rs.getString("account_type"),
                        rs.getString("role")
                    );
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
