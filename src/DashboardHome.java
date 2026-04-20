import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Pane;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class DashboardHome {

    public Pane getView() {
        VBox root = new VBox(30);
        root.setPadding(new Insets(40));
        root.setAlignment(Pos.TOP_LEFT);

        Label pageTitle = new Label("My Dashboard");
        pageTitle.getStyleClass().add("label-title");

        GridPane statsGrid = new GridPane();
        statsGrid.setHgap(20);
        statsGrid.setVgap(20);

        VBox grossCard = createStatCard("Gross Emissions", getGrossEmissions() + " kg");
        VBox offsetCard = createStatCard("Total Offsets", getOffsets() + " kg");
        VBox netCard = createStatCard("Net Carbon Footprint", getNetEmissions() + " kg");
        
        double monthlyGoal = getActiveMonthlyGoal();
        String limitStr = monthlyGoal > 0 ? monthlyGoal + " kg" : "No Plan";
        VBox goalCard = createStatCard("Monthly Limit", limitStr);

        statsGrid.add(grossCard, 0, 0);
        statsGrid.add(offsetCard, 1, 0);
        statsGrid.add(netCard, 2, 0);
        statsGrid.add(goalCard, 3, 0);

        Label historyTitle = new Label("Recent Activity");
        historyTitle.getStyleClass().add("label-header");

        VBox historyBox = new VBox(10);
        loadHistory(historyBox);

        ScrollPane scroll = new ScrollPane(historyBox);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        root.getChildren().addAll(pageTitle, statsGrid, historyTitle, scroll);
        return root;
    }

    private VBox createStatCard(String title, String value) {
        VBox card = new VBox(10);
        card.getStyleClass().add("card");
        card.setAlignment(Pos.CENTER);
        card.setMinWidth(220);
        
        Label titleLbl = new Label(title);
        titleLbl.getStyleClass().add("label-subheader");
        
        Label valueLbl = new Label(value);
        valueLbl.getStyleClass().add("stat-value");
        
        card.getChildren().addAll(titleLbl, valueLbl);
        return card;
    }

    private double getGrossEmissions() {
        return queryNetView("gross_co2_kg");
    }

    private double getOffsets() {
        return queryNetView("offset_co2_kg");
    }

    private double getNetEmissions() {
        return queryNetView("net_co2_kg");
    }

    private double queryNetView(String col) {
        String query = "SELECT " + col + " FROM user_net_emissions WHERE user_id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, SessionManager.getUserId());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getDouble(col);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    private double getActiveMonthlyGoal() {
        String query = "SELECT target_co2_kg FROM Goals WHERE user_id = ? AND status = 'active' AND period_type = 'monthly' ORDER BY start_date DESC LIMIT 1";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, SessionManager.getUserId());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getDouble("target_co2_kg");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    private void loadHistory(VBox box) {
        String query = "SELECT a.activity_date, t.name, a.quantity, a.unit, ec.co2_result " +
                       "FROM Activities a " +
                       "JOIN Activity_Types t ON a.activity_type_id = t.activity_type_id " +
                       "JOIN Emission_Calculations ec ON a.activity_id = ec.activity_id " +
                       "WHERE a.user_id = ? ORDER BY a.activity_date DESC LIMIT 10";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, SessionManager.getUserId());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String row = String.format("%s | %s: %.1f %s => +%.2f kg CO2", 
                    rs.getString("activity_date"), rs.getString("name"),
                    rs.getDouble("quantity"), rs.getString("unit"), rs.getDouble("co2_result"));
                Label lbl = new Label(row);
                lbl.getStyleClass().add("label-normal");
                box.getChildren().add(lbl);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
