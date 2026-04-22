import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;

public class OffsetsView {

    private VBox offsetsList;

    public Pane getView() {
        VBox root = new VBox(30);
        root.setPadding(new Insets(40));
        root.setAlignment(Pos.TOP_LEFT);

        Label pageTitle = new Label("Carbon OffsetsTracker");
        pageTitle.getStyleClass().add("label-title");
        
        Label subTitle = new Label("Log activities that reduce your net footprint like planting trees");
        subTitle.getStyleClass().add("label-subheader");

        // Offset Logging Form
        HBox formBox = new HBox(15);
        formBox.setAlignment(Pos.CENTER_LEFT);
        formBox.getStyleClass().add("glass-pane");
        formBox.setPadding(new Insets(20));

        TextField typeField = new TextField();
        typeField.setPromptText("Offset Type (e.g. Tree planted)");
        typeField.getStyleClass().add("text-field-glass");
        typeField.setPrefWidth(200);

        TextField co2Field = new TextField();
        co2Field.setPromptText("CO2 Reduced (kg)");
        co2Field.getStyleClass().add("text-field-glass");
        co2Field.setPrefWidth(140);

        TextField costField = new TextField();
        costField.setPromptText("Cost ($) (Optional)");
        costField.getStyleClass().add("text-field-glass");
        costField.setPrefWidth(140);

        Button submitBtn = new Button("Log Offset");
        submitBtn.getStyleClass().add("button-primary");

        Label errorLabel = new Label("");
        errorLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");

        submitBtn.setOnAction(e -> {
            try {
                String type = typeField.getText().trim();
                String co2Str = co2Field.getText().trim();
                String costStr = costField.getText().trim();

                if (type.isEmpty() || co2Str.isEmpty()) {
                    errorLabel.setText("Please enter Type and CO2 amount");
                    return;
                }

                double co2 = Double.parseDouble(co2Str);
                double cost = costStr.isEmpty() ? 0.0 : Double.parseDouble(costStr);

                if (co2 <= 0 || cost < 0) {
                    errorLabel.setText("Values must be positive");
                    return;
                }

                saveOffset(type, co2, cost);
                typeField.clear();
                co2Field.clear();
                costField.clear();
                errorLabel.setText("");
                loadOffsets();
            } catch (NumberFormatException ex) {
                errorLabel.setText("Invalid number format");
            }
        });

        formBox.getChildren().addAll(typeField, co2Field, costField, submitBtn, errorLabel);

        // Display Logged Offsets
        Label listTitle = new Label("My Offset History");
        listTitle.getStyleClass().add("label-header");

        offsetsList = new VBox(15);
        ScrollPane scroll = new ScrollPane(offsetsList);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        loadOffsets();

        root.getChildren().addAll(pageTitle, subTitle, formBox, listTitle, scroll);
        return root;
    }

    private void saveOffset(String type, double co2, double cost) {
        String query = "INSERT INTO Offsets (user_id, offset_type, co2_offset_kg, cost, offset_date) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, SessionManager.getUserId());
            stmt.setString(2, type);
            stmt.setDouble(3, co2);
            stmt.setDouble(4, cost);
            stmt.setString(5, LocalDate.now().toString());
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadOffsets() {
        offsetsList.getChildren().clear();
        String query = "SELECT offset_id, offset_type, co2_offset_kg, cost, offset_date, verified " +
                       "FROM Offsets WHERE user_id = ? ORDER BY offset_date DESC, offset_id DESC";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, SessionManager.getUserId());
            ResultSet rs = stmt.executeQuery();
            
            boolean hasOffsets = false;
            while (rs.next()) {
                hasOffsets = true;
                int offsetId = rs.getInt("offset_id");
                String type = rs.getString("offset_type");
                double co2 = rs.getDouble("co2_offset_kg");
                double cost = rs.getDouble("cost");
                String date = rs.getString("offset_date");
                boolean verified = rs.getBoolean("verified");

                HBox row = new HBox(15);
                row.setAlignment(Pos.CENTER_LEFT);
                row.setPadding(new Insets(15));
                row.getStyleClass().add("glass-pane");
                
                VBox textInfo = new VBox(5);
                Label titleLabel = new Label(String.format("%s (-%.2f kg CO2)", type, co2));
                titleLabel.getStyleClass().add("label-header");
                titleLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #2ecc71;"); // Green since it's an offset

                Label detailLabel = new Label(String.format("Logged %s | Cost: $%.2f | Verified: %s", date, cost, verified ? "Yes" : "No"));
                detailLabel.getStyleClass().add("label-subheader");

                textInfo.getChildren().addAll(titleLabel, detailLabel);

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                Button delBtn = new Button("Delete");
                delBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #e74c3c; -fx-border-color: #e74c3c; -fx-border-radius: 5px; -fx-cursor: hand;");
                delBtn.setOnAction(e -> deleteOffset(offsetId));

                row.getChildren().addAll(textInfo, spacer, delBtn);
                offsetsList.getChildren().add(row);
            }
            if (!hasOffsets) {
                Label emptyLabel = new Label("No offsets logged yet. Help the environment today!");
                emptyLabel.getStyleClass().add("label-normal");
                offsetsList.getChildren().add(emptyLabel);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deleteOffset(int offsetId) {
        String query = "DELETE FROM Offsets WHERE offset_id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, offsetId);
            stmt.executeUpdate();
            loadOffsets();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
