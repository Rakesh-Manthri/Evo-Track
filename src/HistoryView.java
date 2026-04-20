import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class HistoryView {

    private VBox historyBox;

    public Pane getView() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(40));
        root.setAlignment(Pos.TOP_LEFT);

        Label pageTitle = new Label("Activity History");
        pageTitle.getStyleClass().add("label-title");

        Label subTitle = new Label("View or remove logged activities");
        subTitle.getStyleClass().add("label-subheader");

        historyBox = new VBox(15);
        historyBox.getStyleClass().add("glass-pane");

        ScrollPane scroll = new ScrollPane(historyBox);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        loadHistory();

        root.getChildren().addAll(pageTitle, subTitle, scroll);
        return root;
    }

    private void loadHistory() {
        historyBox.getChildren().clear();
        String query = "SELECT a.activity_id, a.activity_date, t.name, ac.category_name, a.quantity, a.unit, ec.co2_result " +
                       "FROM Activities a " +
                       "JOIN Activity_Types t ON a.activity_type_id = t.activity_type_id " +
                       "JOIN Activity_Categories ac ON t.category_id = ac.category_id " +
                       "LEFT JOIN Emission_Calculations ec ON a.activity_id = ec.activity_id " +
                       "WHERE a.user_id = ? ORDER BY a.activity_date DESC, a.activity_id DESC";
                       
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, SessionManager.getUserId());
            ResultSet rs = stmt.executeQuery();
            
            boolean hasRecords = false;
            while (rs.next()) {
                hasRecords = true;
                int activityId = rs.getInt("activity_id");
                String date = rs.getString("activity_date");
                String cat = rs.getString("category_name");
                String type = rs.getString("name");
                double qty = rs.getDouble("quantity");
                String unit = rs.getString("unit");
                double co2 = rs.getDouble("co2_result");
                
                HBox row = new HBox(15);
                row.setAlignment(Pos.CENTER_LEFT);
                row.setPadding(new Insets(10));
                row.setStyle("-fx-border-color: rgba(0,0,0,0.1); -fx-border-width: 0 0 1 0;");
                
                VBox textInfo = new VBox(5);
                Label catLabel = new Label(cat + " - " + type);
                catLabel.getStyleClass().add("label-header");
                catLabel.setStyle("-fx-font-size: 16px;");
                
                Label detailLabel = new Label(String.format("Logged on %s: %.1f %s ➔ +%.2f kg CO2", date, qty, unit, co2));
                detailLabel.getStyleClass().add("label-subheader");
                
                textInfo.getChildren().addAll(catLabel, detailLabel);
                
                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);
                
                Button delBtn = new Button("Delete");
                delBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-cursor: hand;");
                delBtn.setOnAction(e -> deleteActivity(activityId));
                
                row.getChildren().addAll(textInfo, spacer, delBtn);
                historyBox.getChildren().add(row);
            }
            if (!hasRecords) {
                Label emptyLabel = new Label("No activities logged yet.");
                emptyLabel.getStyleClass().add("label-normal");
                historyBox.getChildren().add(emptyLabel);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deleteActivity(int activityId) {
        String query = "DELETE FROM Activities WHERE activity_id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, activityId);
            stmt.executeUpdate();
            loadHistory(); // refresh
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
