import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Pane;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class AnalyticsView {

    public Pane getView() {
        VBox root = new VBox(30);
        root.setPadding(new Insets(40));
        root.setAlignment(Pos.TOP_LEFT);

        Label pageTitle = new Label("Analytics & Reports");
        pageTitle.getStyleClass().add("label-title");
        
        Label subTitle = new Label("Visualize your carbon footprint breakdown");
        subTitle.getStyleClass().add("label-subheader");

        GridPane chartsGrid = new GridPane();
        chartsGrid.setHgap(30);
        chartsGrid.setVgap(30);
        chartsGrid.setAlignment(Pos.CENTER);

        // 1. Pie Chart - Breakdown by Category
        VBox pieBox = new VBox(10);
        pieBox.getStyleClass().add("glass-pane");
        pieBox.setPrefWidth(400);
        pieBox.setPrefHeight(350);
        
        Label pieTitle = new Label("Emissions by Category");
        pieTitle.getStyleClass().add("label-header");
        
        PieChart categoryChart = new PieChart();
        loadCategoryData(categoryChart);
        categoryChart.setPrefHeight(280);
        categoryChart.setLegendVisible(true);
        
        pieBox.getChildren().addAll(pieTitle, categoryChart);

        // 2. Bar Chart - Monthly Trends
        VBox barBox = new VBox(10);
        barBox.getStyleClass().add("glass-pane");
        barBox.setPrefWidth(450);
        barBox.setPrefHeight(350);

        Label barTitle = new Label("Monthly Trend (Gross kg CO2)");
        barTitle.getStyleClass().add("label-header");

        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Month");
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("kg CO2");

        BarChart<String, Number> monthlyBarChart = new BarChart<>(xAxis, yAxis);
        monthlyBarChart.setLegendVisible(false);
        loadMonthlyData(monthlyBarChart);
        
        barBox.getChildren().addAll(barTitle, monthlyBarChart);

        chartsGrid.add(pieBox, 0, 0);
        chartsGrid.add(barBox, 1, 0);

        ScrollPane scroll = new ScrollPane(chartsGrid);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        root.getChildren().addAll(pageTitle, subTitle, scroll);
        return root;
    }

    private void loadCategoryData(PieChart chart) {
        String query = "SELECT ac.category_name, SUM(ec.co2_result) as total_co2 " +
                       "FROM Activities a " +
                       "JOIN Activity_Types t ON a.activity_type_id = t.activity_type_id " +
                       "JOIN Activity_Categories ac ON t.category_id = ac.category_id " +
                       "JOIN Emission_Calculations ec ON a.activity_id = ec.activity_id " +
                       "WHERE a.user_id = ? " +
                       "GROUP BY ac.category_name";
                       
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, SessionManager.getUserId());
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                String category = rs.getString("category_name");
                double amount = rs.getDouble("total_co2");
                chart.getData().add(new PieChart.Data(category + " (" + String.format("%.1f", amount) + "kg)", amount));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadMonthlyData(BarChart<String, Number> chart) {
        String query = "SELECT month, year, gross_co2_kg " +
                       "FROM monthly_emission_summary WHERE user_id = ? " +
                       "ORDER BY year ASC, month ASC LIMIT 12";

        String[] monthNames = {"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
                       
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, SessionManager.getUserId());
            ResultSet rs = stmt.executeQuery();
            
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            
            while (rs.next()) {
                int month = rs.getInt("month");
                int year = rs.getInt("year");
                double grossCo2 = rs.getDouble("gross_co2_kg");
                
                String label = monthNames[month - 1] + " " + year;
                XYChart.Data<String, Number> data = new XYChart.Data<>(label, grossCo2);
                series.getData().add(data);
            }
            chart.getData().add(series);
            
            // Adjust bar colors to theme manually by searching nodes once rendered
            for (XYChart.Data<String, Number> data : series.getData()) {
                if (data.getNode() != null) {
                    data.getNode().setStyle("-fx-bar-fill: #3498db;");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
