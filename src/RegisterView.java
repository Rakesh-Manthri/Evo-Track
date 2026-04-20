import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Pane;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class RegisterView {

    public Pane getView() {
        VBox root = new VBox(15);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(30));

        VBox glassPane = new VBox(15);
        glassPane.setAlignment(Pos.CENTER);
        glassPane.setMaxWidth(450);
        glassPane.getStyleClass().add("glass-pane");

        Label titleLabel = new Label("Create Account");
        titleLabel.getStyleClass().add("label-title");
        
        Label subLabel = new Label("Join EcoCampus to track your footprint");
        subLabel.getStyleClass().add("label-subheader");

        TextField nameField = new TextField();
        nameField.setPromptText("Full Name");
        nameField.getStyleClass().add("text-field-glass");
        nameField.setMaxWidth(350);

        TextField emailField = new TextField();
        emailField.setPromptText("Email Address");
        emailField.getStyleClass().add("text-field-glass");
        emailField.setMaxWidth(350);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.getStyleClass().add("text-field-glass");
        passwordField.setMaxWidth(350);
        
        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll("individual", "office");
        typeCombo.setValue("individual");
        typeCombo.getStyleClass().add("combo-box-glass");
        typeCombo.setMaxWidth(350);

        Label errorLabel = new Label("");
        errorLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");

        Button registerBtn = new Button("Register");
        registerBtn.getStyleClass().add("button-primary");
        registerBtn.setMaxWidth(350);

        Button backBtn = new Button("Back to Login");
        backBtn.getStyleClass().add("button-secondary");
        backBtn.setMaxWidth(350);

        registerBtn.setOnAction(e -> {
            String name = nameField.getText().trim();
            String email = emailField.getText().trim();
            String pwd = passwordField.getText();
            String accType = typeCombo.getValue();
            
            if (name.isEmpty() || email.isEmpty() || pwd.isEmpty()) {
                errorLabel.setText("Please fill all fields");
                return;
            }
            if (registerUser(name, email, pwd, accType)) {
                ViewManager.showLogin();
            } else {
                errorLabel.setText("Registration failed. Email may already exist.");
            }
        });

        backBtn.setOnAction(e -> ViewManager.showLogin());

        glassPane.getChildren().addAll(titleLabel, subLabel, nameField, emailField, typeCombo, passwordField, registerBtn, backBtn, errorLabel);
        root.getChildren().add(glassPane);
        return root;
    }

    private boolean registerUser(String name, String email, String password, String accType) {
        String query = "INSERT INTO Users (name, email, password_hash, account_type) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, name);
            stmt.setString(2, email);
            stmt.setString(3, Integer.toHexString(password.hashCode())); // Simple hash demo
            stmt.setString(4, accType);
            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
