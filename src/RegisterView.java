import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class RegisterView {

    public Pane getView() {
        StackPane root = new StackPane();
        root.getStyleClass().add("root-pane");

        HBox splitLayout = new HBox(0);
        splitLayout.setFillHeight(true);
        VBox.setVgrow(splitLayout, Priority.ALWAYS);

        // ── Left: Brand panel (mirrored from Login) ────────────────────────
        VBox brandPanel = new VBox(12);
        brandPanel.setAlignment(Pos.CENTER);
        brandPanel.setPrefWidth(380);
        brandPanel.setPadding(new Insets(60, 36, 60, 48));
        brandPanel.setStyle(
            "-fx-background-color: rgba(0,255,140,0.04);" +
            "-fx-border-color: rgba(0,255,140,0.08);" +
            "-fx-border-width: 0 1 0 0;"
        );

        Label logoIcon = new Label("🌿");
        logoIcon.setStyle("-fx-font-size: 52px;");
        Label logoText = new Label("EvoTrack");
        logoText.getStyleClass().add("label-brand");
        Label tagline = new Label("Track your carbon footprint and\nbuild sustainable habits.");
        tagline.getStyleClass().add("label-subheader");
        tagline.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        VBox benefitsList = new VBox(10);
        benefitsList.setPadding(new Insets(20, 0, 0, 0));
        benefitsList.getChildren().addAll(
            benefitItem("✅", "Free to join — no credit card"),
            benefitItem("🔒", "Your data stays private"),
            benefitItem("🌍", "Join thousands of eco-trackers"),
            benefitItem("📊", "Instant insights from day one")
        );

        brandPanel.getChildren().addAll(logoIcon, logoText, tagline, benefitsList);

        // ── Right: Registration form ───────────────────────────────────────
        VBox formOuter = new VBox(0);
        formOuter.setAlignment(Pos.CENTER);
        HBox.setHgrow(formOuter, Priority.ALWAYS);
        formOuter.setPadding(new Insets(50, 50, 50, 50));

        VBox glassCard = new VBox(16);
        glassCard.setAlignment(Pos.CENTER_LEFT);
        glassCard.setMaxWidth(380);
        glassCard.getStyleClass().add("glass-pane");

        Label title = new Label("Create account");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #e0ffe8;");
        Label sub = new Label("Join EvoTrack and start tracking today.");
        sub.getStyleClass().add("label-subheader");

        // Name
        VBox nameGroup = fieldGroup("Full Name");
        TextField nameField = new TextField();
        nameField.setId("reg-name");
        nameField.setPromptText("John Doe");
        nameField.getStyleClass().add("text-field-glass");
        nameField.setMaxWidth(Double.MAX_VALUE);
        nameGroup.getChildren().add(nameField);

        // Email
        VBox emailGroup = fieldGroup("Email Address");
        TextField emailField = new TextField();
        emailField.setId("reg-email");
        emailField.setPromptText("you@example.com");
        emailField.getStyleClass().add("text-field-glass");
        emailField.setMaxWidth(Double.MAX_VALUE);
        emailGroup.getChildren().add(emailField);

        // Password
        VBox pwGroup = fieldGroup("Password");
        PasswordField passwordField = new PasswordField();
        passwordField.setId("reg-password");
        passwordField.setPromptText("Min. 8 characters");
        passwordField.getStyleClass().add("text-field-glass");
        passwordField.setMaxWidth(Double.MAX_VALUE);
        pwGroup.getChildren().add(passwordField);

        // Account type
        VBox typeGroup = fieldGroup("Account Type");
        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll("individual", "office");
        typeCombo.setValue("individual");
        typeCombo.getStyleClass().add("combo-box-glass");
        typeCombo.setMaxWidth(Double.MAX_VALUE);
        typeGroup.getChildren().add(typeCombo);

        Label errorLabel = new Label("");
        errorLabel.getStyleClass().add("error-label");

        Button registerBtn = new Button("Create Account →");
        registerBtn.setId("register-btn");
        registerBtn.getStyleClass().add("button-primary");
        registerBtn.setMaxWidth(Double.MAX_VALUE);

        HBox orRow = new HBox();
        orRow.setAlignment(Pos.CENTER);
        Label orLabel = new Label("— or —");
        orLabel.getStyleClass().add("label-muted");
        orRow.getChildren().add(orLabel);

        Button backBtn = new Button("Back to Sign In");
        backBtn.setId("back-login-btn");
        backBtn.getStyleClass().add("button-secondary");
        backBtn.setMaxWidth(Double.MAX_VALUE);

        registerBtn.setOnAction(e -> {
            String name   = nameField.getText().trim();
            String email  = emailField.getText().trim();
            String pwd    = passwordField.getText();
            String type   = typeCombo.getValue();

            if (name.isEmpty() || email.isEmpty() || pwd.isEmpty()) {
                errorLabel.setText("Please fill in all fields.");
                return;
            }
            if (pwd.length() < 4) {
                errorLabel.setText("Password must be at least 4 characters.");
                return;
            }
            if (registerUser(name, email, pwd, type)) {
                ViewManager.showLogin();
            } else {
                errorLabel.setText("Registration failed — email may already be in use.");
            }
        });

        backBtn.setOnAction(e -> ViewManager.showLogin());

        glassCard.getChildren().addAll(
            title, sub,
            nameGroup, emailGroup, pwGroup, typeGroup,
            errorLabel,
            registerBtn, orRow, backBtn
        );

        formOuter.getChildren().add(glassCard);

        splitLayout.getChildren().addAll(brandPanel, formOuter);

        VBox wrapper = new VBox(splitLayout);
        wrapper.setFillWidth(true);
        wrapper.getStyleClass().add("root-pane");

        root.getChildren().add(wrapper);
        return root;
    }

    private VBox fieldGroup(String label) {
        VBox g = new VBox(6);
        Label lbl = new Label(label);
        lbl.getStyleClass().add("label-muted");
        g.getChildren().add(lbl);
        return g;
    }

    private HBox benefitItem(String icon, String text) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        Label ico = new Label(icon);
        ico.setStyle("-fx-font-size: 14px;");
        Label txt = new Label(text);
        txt.setStyle("-fx-font-size: 13px; -fx-text-fill: rgba(190,230,210,0.65);");
        row.getChildren().addAll(ico, txt);
        return row;
    }

    private boolean registerUser(String name, String email, String password, String accType) {
        String query = "INSERT INTO Users (name, email, password_hash, account_type) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, name);
            stmt.setString(2, email);
            stmt.setString(3, Integer.toHexString(password.hashCode()));
            stmt.setString(4, accType);
            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
