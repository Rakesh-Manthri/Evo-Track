import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class AnalyticsView {

    public Pane getView() {
        VBox root = new VBox(0);
        root.getStyleClass().add("root-pane");
        root.setFillWidth(true);

        // ── Page Header ────────────────────────────────────────────────────
        HBox pageHeader = new HBox();
        pageHeader.setAlignment(Pos.CENTER_LEFT);
        pageHeader.setPadding(new Insets(32, 40, 24, 40));
        pageHeader.setStyle(
            "-fx-background-color: linear-gradient(to right, rgba(0,207,255,0.05), transparent);" +
            "-fx-border-color: rgba(0,255,140,0.08);" +
            "-fx-border-width: 0 0 1 0;"
        );

        VBox titleBox = new VBox(4);
        Label pageTitle = new Label("📊  Analytics");
        pageTitle.getStyleClass().add("label-title");
        Label pageSub = new Label("Visualise your carbon footprint breakdown over time");
        pageSub.getStyleClass().add("label-subheader");
        titleBox.getChildren().addAll(pageTitle, pageSub);
        pageHeader.getChildren().add(titleBox);

        // ── Scrollable content ─────────────────────────────────────────────
        VBox content = new VBox(28);
        content.setPadding(new Insets(32, 40, 40, 40));
        content.setFillWidth(true);

        // ── Summary mini-cards ─────────────────────────────────────────────
        HBox miniCards = new HBox(16);
        miniCards.setAlignment(Pos.CENTER_LEFT);
        miniCards.getChildren().addAll(
            miniCard("🏭", "Gross Emissions",  getGrossEmissions() + " kg",  "#00ffa3"),
            miniCard("🌳", "Total Offsets",    getOffsets()        + " kg",  "#00cfff"),
            miniCard("⚖️",  "Net Footprint",   getNetEmissions()   + " kg",  "#ffcf40")
        );

        // ── Charts row ─────────────────────────────────────────────────────
        HBox chartsRow = new HBox(24);
        chartsRow.setAlignment(Pos.TOP_LEFT);
        chartsRow.setFillHeight(true);

        // Pie chart card
        VBox pieCard = chartCard("🍩  Emissions by Category");
        PieChart pieChart = new PieChart();
        pieChart.setLegendVisible(true);
        pieChart.setLabelsVisible(true);
        pieChart.setPrefHeight(300);
        pieChart.setStyle("-fx-background-color: transparent;");
        loadCategoryData(pieChart);
        pieCard.getChildren().add(pieChart);
        HBox.setHgrow(pieCard, Priority.ALWAYS);

        // Bar chart card
        VBox barCard = chartCard("📅  Monthly CO₂ Trend");
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Month");
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("kg CO₂");
        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setLegendVisible(false);
        barChart.setPrefHeight(300);
        barChart.setStyle("-fx-background-color: transparent;");
        loadMonthlyData(barChart);
        barCard.getChildren().add(barChart);
        HBox.setHgrow(barCard, Priority.ALWAYS);

        chartsRow.getChildren().addAll(pieCard, barCard);

        // ── Tips section ──────────────────────────────────────────────────
        VBox tipsSection = new VBox(14);
        Label tipsHeader = new Label("💡  Personalised Eco Tips");
        tipsHeader.setStyle("-fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: rgba(200,240,215,0.75);");

        HBox tipsRow = new HBox(16);
        tipsRow.getChildren().addAll(
            tipCard("🚗", "Transport",  "Consider carpooling or using\npublic transport this week.",  "#00ffa3"),
            tipCard("⚡", "Energy",     "Switch to LED bulbs and unplug\nchargers when not in use.",   "#00cfff"),
            tipCard("🥦", "Diet",       "Try one meat-free day per week\nto cut 2 kg CO₂ monthly.",   "#ffcf40")
        );
        tipsSection.getChildren().addAll(tipsHeader, tipsRow);

        content.getChildren().addAll(miniCards, chartsRow, tipsSection);

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("scroll-pane");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        root.getChildren().addAll(pageHeader, scroll);
        return root;
    }

    // ── Helpers ─────────────────────────────────────────────────────────────

    private VBox chartCard(String title) {
        VBox card = new VBox(12);
        card.setStyle(
            "-fx-background-color: rgba(10,24,18,0.80);" +
            "-fx-background-radius: 18;" +
            "-fx-border-color: rgba(0,255,140,0.12);" +
            "-fx-border-radius: 18;" +
            "-fx-border-width: 1;" +
            "-fx-padding: 24 22 20 22;"
        );
        Label lbl = new Label(title);
        lbl.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: rgba(200,240,215,0.80);");
        card.getChildren().add(lbl);
        return card;
    }

    private HBox miniCard(String icon, String label, String value, String color) {
        HBox card = new HBox(14);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle(
            "-fx-background-color: rgba(10,24,18,0.75);" +
            "-fx-background-radius: 14;" +
            "-fx-border-color: " + color + "28;" +
            "-fx-border-radius: 14;" +
            "-fx-border-width: 1;" +
            "-fx-padding: 16 22 16 18;"
        );
        HBox.setHgrow(card, Priority.ALWAYS);

        Label ico = new Label(icon);
        ico.setStyle("-fx-font-size: 24px;");
        VBox texts = new VBox(3);
        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-size: 11px; -fx-text-fill: rgba(160,200,180,0.50); -fx-font-weight: bold;");
        Label val = new Label(value);
        val.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: " + color + ";" +
                "-fx-effect: dropshadow(one-pass-box, " + color + "88, 10, 0, 0, 0);");
        texts.getChildren().addAll(lbl, val);
        card.getChildren().addAll(ico, texts);
        return card;
    }

    private VBox tipCard(String icon, String category, String tip, String color) {
        VBox card = new VBox(10);
        card.setStyle(
            "-fx-background-color: rgba(10,24,18,0.70);" +
            "-fx-background-radius: 14;" +
            "-fx-border-color: " + color + "22;" +
            "-fx-border-radius: 14;" +
            "-fx-border-width: 1;" +
            "-fx-padding: 18 20 18 20;"
        );
        HBox.setHgrow(card, Priority.ALWAYS);

        Label ico = new Label(icon);
        ico.setStyle("-fx-font-size: 28px;");
        Label cat = new Label(category);
        cat.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");
        Label tipLbl = new Label(tip);
        tipLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: rgba(190,225,205,0.65); -fx-wrap-text: true;");
        tipLbl.setWrapText(true);
        card.getChildren().addAll(ico, cat, tipLbl);
        return card;
    }

    // ── DB queries ───────────────────────────────────────────────────────────

    private String getGrossEmissions() { return String.format("%.1f", queryVal("gross_co2_kg")); }
    private String getOffsets()        { return String.format("%.1f", queryVal("offset_co2_kg")); }
    private String getNetEmissions()   { return String.format("%.1f", queryVal("net_co2_kg")); }

    private double queryVal(String col) {
        String q = "SELECT " + col + " FROM user_net_emissions WHERE user_id = ?";
        try (Connection c = DatabaseUtil.getConnection(); PreparedStatement s = c.prepareStatement(q)) {
            s.setInt(1, SessionManager.getUserId());
            ResultSet rs = s.executeQuery();
            if (rs.next()) return rs.getDouble(col);
        } catch (Exception e) { e.printStackTrace(); }
        return 0.0;
    }

    private void loadCategoryData(PieChart chart) {
        String q =
            "SELECT ac.category_name, SUM(ec.co2_result) as total " +
            "FROM Activities a " +
            "JOIN Activity_Types t ON a.activity_type_id = t.activity_type_id " +
            "JOIN Activity_Categories ac ON t.category_id = ac.category_id " +
            "JOIN Emission_Calculations ec ON a.activity_id = ec.activity_id " +
            "WHERE a.user_id = ? GROUP BY ac.category_name";
        try (Connection c = DatabaseUtil.getConnection(); PreparedStatement s = c.prepareStatement(q)) {
            s.setInt(1, SessionManager.getUserId());
            ResultSet rs = s.executeQuery();
            while (rs.next()) {
                chart.getData().add(new PieChart.Data(
                    rs.getString("category_name") + "  (" + String.format("%.1f kg", rs.getDouble("total")) + ")",
                    rs.getDouble("total")
                ));
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void loadMonthlyData(BarChart<String, Number> chart) {
        String[] months = {"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
        String q =
            "SELECT month, year, gross_co2_kg FROM monthly_emission_summary " +
            "WHERE user_id = ? ORDER BY year, month LIMIT 12";
        try (Connection c = DatabaseUtil.getConnection(); PreparedStatement s = c.prepareStatement(q)) {
            s.setInt(1, SessionManager.getUserId());
            ResultSet rs = s.executeQuery();
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("CO₂ kg");
            while (rs.next()) {
                int m = rs.getInt("month");
                int y = rs.getInt("year");
                series.getData().add(new XYChart.Data<>(months[m - 1] + " '" + (y % 100), rs.getDouble("gross_co2_kg")));
            }
            chart.getData().add(series);
        } catch (Exception e) { e.printStackTrace(); }
    }
}
