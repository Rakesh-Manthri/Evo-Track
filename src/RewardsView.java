import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class RewardsView {

    public Pane getView() {
        VBox root = new VBox(30);
        root.setPadding(new Insets(40));
        root.setAlignment(Pos.TOP_LEFT);

        Label pageTitle = new Label("My Achievements");
        pageTitle.getStyleClass().add("label-title");
        
        Label subTitle = new Label("Earn badges by hitting your sustainability targets!");
        subTitle.getStyleClass().add("label-subheader");

        FlowPane badgesPane = new FlowPane(20, 20);
        badgesPane.setAlignment(Pos.TOP_LEFT);

        ScrollPane scroll = new ScrollPane(badgesPane);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        loadBadges(badgesPane);

        root.getChildren().addAll(pageTitle, subTitle, scroll);
        return root;
    }

    private void loadBadges(FlowPane badgesPane) {
        // Query to get all rewards and check if the current user has earned them
        String query = "SELECT r.reward_id, r.reward_name, r.description, r.badge_icon_url, ur.earned_at " +
                       "FROM Rewards r " +
                       "LEFT JOIN User_Rewards ur ON r.reward_id = ur.reward_id AND ur.user_id = ? " +
                       "ORDER BY r.condition_value DESC";
                       
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, SessionManager.getUserId());
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                String name = rs.getString("reward_name");
                String desc = rs.getString("description");
                String icon = rs.getString("badge_icon_url");
                String earnedAt = rs.getString("earned_at");
                boolean isEarned = earnedAt != null;

                VBox badgeCard = new VBox(10);
                badgeCard.setAlignment(Pos.CENTER);
                badgeCard.getStyleClass().add("glass-pane");
                badgeCard.setPrefWidth(220);
                badgeCard.setPrefHeight(220);

                // Badge Icon Circle
                StackPane iconStack = new StackPane();
                Circle circle = new Circle(40);
                circle.setFill(isEarned ? Color.web("#f1c40f") : Color.web("#bdc3c7"));
                
                Label iconLabel = new Label(icon);
                iconLabel.setStyle("-fx-font-size: 40px;");
                if (!isEarned) iconLabel.setOpacity(0.3);

                iconStack.getChildren().addAll(circle, iconLabel);

                Label nameLabel = new Label(name);
                nameLabel.getStyleClass().add("label-header");
                nameLabel.setStyle("-fx-font-size: 18px;");
                if (!isEarned) nameLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #95a5a6;");

                Label descLabel = new Label(desc);
                descLabel.getStyleClass().add("label-normal");
                descLabel.setWrapText(true);
                descLabel.setAlignment(Pos.CENTER);
                if (!isEarned) descLabel.setStyle("-fx-text-fill: #95a5a6;");

                Label statusLabel = new Label(isEarned ? "Earned: " + earnedAt.split(" ")[0] : "Locked");
                statusLabel.setStyle(isEarned ? "-fx-text-fill: #27ae60; -fx-font-weight: bold;" : "-fx-text-fill: #7f8c8d;");

                badgeCard.getChildren().addAll(iconStack, nameLabel, descLabel, statusLabel);
                badgesPane.getChildren().add(badgeCard);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
