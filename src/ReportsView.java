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
import java.time.LocalDate;

public class ReportsView {

    private VBox reportsList;

    public Pane getView() {
        VBox root = new VBox(30);
        root.setPadding(new Insets(40));
        root.setAlignment(Pos.TOP_LEFT);

        Label pageTitle = new Label("Export & Save Reports");
        pageTitle.getStyleClass().add("label-title");
        
        Label subTitle = new Label("Generate official snapshots of your carbon data.");
        subTitle.getStyleClass().add("label-subheader");

        HBox topBox = new HBox(20);
        Button generateBtn = new Button("Generate Current Report snapshot");
        generateBtn.getStyleClass().add("button-primary");
        Label statusLabel = new Label("");
        statusLabel.setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: bold;");

        generateBtn.setOnAction(e -> {
            if (generateReport()) {
                statusLabel.setText("Report successfully generated & saved!");
                loadReports();
            } else {
                statusLabel.setText("Error generating report.");
                statusLabel.setStyle("-fx-text-fill: #e74c3c;");
            }
        });

        topBox.getChildren().addAll(generateBtn, statusLabel);
        topBox.setAlignment(Pos.CENTER_LEFT);

        Label listTitle = new Label("Saved Reports");
        listTitle.getStyleClass().add("label-header");

        reportsList = new VBox(15);
        ScrollPane scroll = new ScrollPane(reportsList);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        loadReports();

        root.getChildren().addAll(pageTitle, subTitle, topBox, listTitle, scroll);
        return root;
    }

    private boolean generateReport() {
        String queryGetStats = "SELECT gross_co2_kg, offset_co2_kg, net_co2_kg FROM user_net_emissions WHERE user_id = ?";
        String insertReport = "INSERT INTO Reports (user_id, org_id, period_type, period_start, period_end, gross_co2_kg, offset_co2_kg, net_co2_kg, goal_met) " +
                              "VALUES (?, ?, 'monthly', ?, ?, ?, ?, ?, ?)";
                              
        try (Connection conn = DatabaseUtil.getConnection()) {
            double gross = 0, offset = 0, net = 0;
            try (PreparedStatement stmt = conn.prepareStatement(queryGetStats)) {
                stmt.setInt(1, SessionManager.getUserId());
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    gross = rs.getDouble("gross_co2_kg");
                    offset = rs.getDouble("offset_co2_kg");
                    net = rs.getDouble("net_co2_kg");
                }
            }

            // Check if goal met
            boolean goalMet = false;
            String queryGoal = "SELECT target_co2_kg FROM Goals WHERE user_id = ? AND status = 'active' AND period_type = 'monthly' ORDER BY start_date DESC LIMIT 1";
            try (PreparedStatement stmt = conn.prepareStatement(queryGoal)) {
                stmt.setInt(1, SessionManager.getUserId());
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    if (net <= rs.getDouble("target_co2_kg")) {
                        goalMet = true;
                    }
                }
            }

            LocalDate start = LocalDate.now().withDayOfMonth(1);
            LocalDate end = LocalDate.now();

            try (PreparedStatement stmt = conn.prepareStatement(insertReport)) {
                stmt.setInt(1, SessionManager.getUserId());
                if (SessionManager.getOrgId() != null) {
                    stmt.setInt(2, SessionManager.getOrgId());
                } else {
                    stmt.setNull(2, java.sql.Types.INTEGER);
                }
                stmt.setString(3, start.toString());
                stmt.setString(4, end.toString());
                stmt.setDouble(5, gross);
                stmt.setDouble(6, offset);
                stmt.setDouble(7, net);
                stmt.setBoolean(8, goalMet);
                stmt.executeUpdate();
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void loadReports() {
        reportsList.getChildren().clear();
        String query = "SELECT report_id, period_start, period_end, gross_co2_kg, net_co2_kg, generated_at, goal_met " +
                       "FROM Reports WHERE user_id = ? ORDER BY generated_at DESC";
                       
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, SessionManager.getUserId());
            ResultSet rs = stmt.executeQuery();
            
            boolean hasReports = false;
            while (rs.next()) {
                hasReports = true;
                String generated = rs.getString("generated_at");
                String start = rs.getString("period_start");
                String end = rs.getString("period_end");
                double gross = rs.getDouble("gross_co2_kg");
                double net = rs.getDouble("net_co2_kg");
                boolean met = rs.getBoolean("goal_met");

                HBox row = new HBox(15);
                row.setAlignment(Pos.CENTER_LEFT);
                row.setPadding(new Insets(15));
                row.getStyleClass().add("glass-pane");
                
                VBox textInfo = new VBox(5);
                Label titleLabel = new Label(String.format("Report snapshot: %s", generated.split(" ")[0]));
                titleLabel.getStyleClass().add("label-header");
                titleLabel.setStyle("-fx-font-size: 16px;");

                Label detailLabel = new Label(String.format("Period: %s -> %s | Gross: %.1f kg | Net: %.1f kg | Goal Met: %s", start, end, gross, net, met ? "Yes" : "No"));
                detailLabel.getStyleClass().add("label-subheader");

                textInfo.getChildren().addAll(titleLabel, detailLabel);
                row.getChildren().add(textInfo);
                reportsList.getChildren().add(row);
            }
            if (!hasReports) {
                Label emptyLabel = new Label("No reports generated yet.");
                emptyLabel.getStyleClass().add("label-normal");
                reportsList.getChildren().add(emptyLabel);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
