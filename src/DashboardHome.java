import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DashboardHome {

    public Pane getView() {
        VBox root = new VBox(0);
        root.getStyleClass().add("root-pane");
        root.setFillWidth(true);

        // ── Top greeting banner ────────────────────────────────────────────
        HBox banner = new HBox();
        banner.setAlignment(Pos.CENTER_LEFT);
        banner.setPadding(new Insets(32, 40, 24, 40));
        banner.setStyle(
            "-fx-background-color: linear-gradient(to right, rgba(0,255,140,0.06), rgba(0,130,230,0.04), transparent);" +
            "-fx-border-color: rgba(0,255,140,0.08);" +
            "-fx-border-width: 0 0 1 0;"
        );

        VBox greetBox = new VBox(5);
        String hour = String.valueOf(java.time.LocalTime.now().getHour());
        String greet = Integer.parseInt(hour) < 12 ? "Good morning" :
                       Integer.parseInt(hour) < 17 ? "Good afternoon" : "Good evening";
        Label greetLabel = new Label(greet + ", " + SessionManager.getName() + " 👋");
        greetLabel.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #e0ffe8;");
        Label dateLabel = new Label(LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d yyyy")));
        dateLabel.getStyleClass().add("label-subheader");
        greetBox.getChildren().addAll(greetLabel, dateLabel);

        Region bannerSpacer = new Region();
        HBox.setHgrow(bannerSpacer, Priority.ALWAYS);

        // Eco score badge (right of banner)
        VBox scoreBadge = new VBox(4);
        scoreBadge.setAlignment(Pos.CENTER);
        scoreBadge.setStyle(
            "-fx-background-color: rgba(0,255,163,0.10);" +
            "-fx-background-radius: 16;" +
            "-fx-border-color: rgba(0,255,163,0.30);" +
            "-fx-border-radius: 16;" +
            "-fx-border-width: 1;" +
            "-fx-padding: 14 24 14 24;"
        );
        Label ecoIcon = new Label("🌱");
        ecoIcon.setStyle("-fx-font-size: 26px;");
        Label ecoTitle = new Label("Eco Score");
        ecoTitle.setStyle("-fx-font-size: 11px; -fx-text-fill: rgba(160,220,185,0.55); -fx-font-weight: bold;");
        Label ecoScore = new Label("A+");
        ecoScore.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #00ffa3;" +
                "-fx-effect: dropshadow(one-pass-box, rgba(0,255,163,0.8), 12, 0, 0, 0);");
        scoreBadge.getChildren().addAll(ecoIcon, ecoTitle, ecoScore);

        banner.getChildren().addAll(greetBox, bannerSpacer, scoreBadge);

        // ── Scrollable content ─────────────────────────────────────────────
        VBox content = new VBox(32);
        content.setPadding(new Insets(32, 40, 40, 40));
        content.setFillWidth(true);

        // ── Stats row ──────────────────────────────────────────────────────
        Label statsHeader = sectionLabel("📈  Overview");

        double gross  = getGrossEmissions();
        double offset = getOffsets();
        double net    = getNetEmissions();
        double goal   = getActiveMonthlyGoal();
        String limitStr = goal > 0 ? String.format("%.1f kg", goal) : "No Plan";

        HBox statsRow = new HBox(20);
        statsRow.setAlignment(Pos.TOP_LEFT);

        VBox c1 = statCard("Gross Emissions",    String.format("%.1f", gross)  + " kg", "🏭", "card-accent-1", "stat-value");
        VBox c2 = statCard("Total Offsets",      String.format("%.1f", offset) + " kg", "🌳", "card-accent-2", "stat-value-blue");
        VBox c3 = statCard("Net Footprint",      String.format("%.1f", net)    + " kg", "⚖️",  "card-accent-3", "stat-value-amber");
        VBox c4 = statCard("Monthly Limit",      limitStr,                               "🎯", "card-accent-4", "stat-value-purple");

        for (VBox card : new VBox[]{c1, c2, c3, c4}) {
            HBox.setHgrow(card, Priority.ALWAYS);
        }
        statsRow.getChildren().addAll(c1, c2, c3, c4);

        // ── Progress toward goal ───────────────────────────────────────────
        VBox progressSection = new VBox(12);
        Label progressHeader = sectionLabel("📊  Monthly Goal Progress");

        VBox progressCard = new VBox(14);
        progressCard.setStyle(
            "-fx-background-color: rgba(12,28,20,0.75);" +
            "-fx-background-radius: 16;" +
            "-fx-border-color: rgba(0,255,140,0.14);" +
            "-fx-border-radius: 16;" +
            "-fx-border-width: 1;" +
            "-fx-padding: 22 26 22 26;"
        );

        double pct = (goal > 0) ? Math.min(gross / goal, 1.0) : 0.0;
        String pctStr = goal > 0 ? String.format("%.0f%%", pct * 100) : "N/A";
        String pctColor = pct < 0.6 ? "#00ffa3" : pct < 0.9 ? "#ffcf40" : "#ff6b6b";

        HBox progressTop = new HBox();
        progressTop.setAlignment(Pos.CENTER_LEFT);
        Label pLabel = new Label("Gross vs Monthly Goal");
        pLabel.getStyleClass().add("label-normal");
        Region ps = new Region(); HBox.setHgrow(ps, Priority.ALWAYS);
        Label pPct = new Label(pctStr);
        pPct.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: " + pctColor + ";");
        progressTop.getChildren().addAll(pLabel, ps, pPct);

        // Manual progress bar via StackPane
        StackPane barBg = new StackPane();
        barBg.setMaxHeight(10); barBg.setMinHeight(10);
        barBg.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 6;");
        barBg.setMaxWidth(Double.MAX_VALUE);

        double fillPct = goal > 0 ? pct : 0.0;
        Region barFill = new Region();
        barFill.setStyle(
            "-fx-background-color: linear-gradient(to right, " + pctColor + "aa, " + pctColor + ");" +
            "-fx-background-radius: 6;"
        );
        barFill.setMaxHeight(10); barFill.setMinHeight(10);
        barFill.setPrefWidth(fillPct * 700);

        HBox progressBarWrap = new HBox(barFill);
        progressBarWrap.setMaxWidth(Double.MAX_VALUE);
        progressBarWrap.setMaxHeight(10);
        progressBarWrap.setMinHeight(10);
        progressBarWrap.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 6;");

        progressCard.getChildren().addAll(progressTop, progressBarWrap);
        progressSection.getChildren().addAll(progressHeader, progressCard);

        // ── Recent Activity ────────────────────────────────────────────────
        VBox activitySection = new VBox(14);
        Label activityHeader = sectionLabel("🕒  Recent Activity");

        VBox activityList = new VBox(8);
        loadHistory(activityList);

        ScrollPane activityScroll = new ScrollPane(activityList);
        activityScroll.setFitToWidth(true);
        activityScroll.setPrefHeight(260);
        activityScroll.getStyleClass().add("scroll-pane");

        activitySection.getChildren().addAll(activityHeader, activityScroll);

        content.getChildren().addAll(statsHeader, statsRow, progressSection, activitySection);

        ScrollPane mainScroll = new ScrollPane(content);
        mainScroll.setFitToWidth(true);
        mainScroll.getStyleClass().add("scroll-pane");
        VBox.setVgrow(mainScroll, Priority.ALWAYS);

        root.getChildren().addAll(banner, mainScroll);
        return root;
    }

    // ── Helpers ─────────────────────────────────────────────────────────────

    private Label sectionLabel(String text) {
        Label lbl = new Label(text);
        lbl.setStyle("-fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: rgba(200,240,215,0.75);");
        return lbl;
    }

    private VBox statCard(String title, String value, String icon, String accentClass, String valueClass) {
        VBox card = new VBox(10);
        card.getStyleClass().addAll("card", accentClass);
        card.setAlignment(Pos.TOP_LEFT);
        card.setMinWidth(180);

        HBox iconRow = new HBox(8);
        iconRow.setAlignment(Pos.CENTER_LEFT);
        Label iconLbl = new Label(icon);
        iconLbl.setStyle("-fx-font-size: 22px;");
        Label titleLbl = new Label(title);
        titleLbl.getStyleClass().add("label-subheader");
        iconRow.getChildren().addAll(iconLbl, titleLbl);

        Label valueLbl = new Label(value);
        valueLbl.getStyleClass().add(valueClass);

        card.getChildren().addAll(iconRow, valueLbl);
        return card;
    }

    private void loadHistory(VBox box) {
        String query =
            "SELECT a.activity_date, t.name, a.quantity, a.unit, ec.co2_result " +
            "FROM Activities a " +
            "JOIN Activity_Types t ON a.activity_type_id = t.activity_type_id " +
            "JOIN Emission_Calculations ec ON a.activity_id = ec.activity_id " +
            "WHERE a.user_id = ? ORDER BY a.activity_date DESC LIMIT 10";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, SessionManager.getUserId());
            ResultSet rs = stmt.executeQuery();
            boolean hasData = false;
            while (rs.next()) {
                hasData = true;
                String date    = rs.getString("activity_date");
                String name    = rs.getString("name");
                double qty     = rs.getDouble("quantity");
                String unit    = rs.getString("unit");
                double co2     = rs.getDouble("co2_result");

                HBox row = new HBox(14);
                row.setAlignment(Pos.CENTER_LEFT);
                row.getStyleClass().add("activity-row");

                Label dateLbl = new Label("📅 " + date);
                dateLbl.setStyle("-fx-text-fill: rgba(160,210,185,0.55); -fx-font-size: 12px; -fx-min-width: 110;");

                Label nameLbl = new Label(name);
                nameLbl.setStyle("-fx-text-fill: #d0ffea; -fx-font-size: 13px; -fx-font-weight: 600; -fx-min-width: 160;");

                Region rowSpacer = new Region();
                HBox.setHgrow(rowSpacer, Priority.ALWAYS);

                Label qtyLbl = new Label(String.format("%.1f %s", qty, unit));
                qtyLbl.setStyle("-fx-text-fill: rgba(200,230,215,0.55); -fx-font-size: 12px;");

                String badgeStyle = co2 > 1.5
                    ? "-fx-text-fill: #ff6b6b;" : co2 > 0.5
                    ? "-fx-text-fill: #ffcf40;" : "-fx-text-fill: #00ffa3;";
                Label co2Lbl = new Label(String.format("+%.2f kg CO₂", co2));
                co2Lbl.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;" + badgeStyle);

                row.getChildren().addAll(dateLbl, nameLbl, rowSpacer, qtyLbl, co2Lbl);
                box.getChildren().add(row);
            }
            if (!hasData) {
                Label empty = new Label("No activity logged yet. Start tracking your carbon footprint!");
                empty.setStyle("-fx-text-fill: rgba(160,200,180,0.40); -fx-font-size: 14px; -fx-padding: 20;");
                box.getChildren().add(empty);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private double getGrossEmissions() { return queryNetView("gross_co2_kg"); }
    private double getOffsets()        { return queryNetView("offset_co2_kg"); }
    private double getNetEmissions()   { return queryNetView("net_co2_kg"); }

    private double queryNetView(String col) {
        String query = "SELECT " + col + " FROM user_net_emissions WHERE user_id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, SessionManager.getUserId());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getDouble(col);
        } catch (Exception e) { e.printStackTrace(); }
        return 0.0;
    }

    private double getActiveMonthlyGoal() {
        String query = "SELECT target_co2_kg FROM Goals WHERE user_id = ? AND status='active' AND period_type='monthly' ORDER BY start_date DESC LIMIT 1";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, SessionManager.getUserId());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getDouble("target_co2_kg");
        } catch (Exception e) { e.printStackTrace(); }
        return 0.0;
    }
}
