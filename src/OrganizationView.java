import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Pane;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class OrganizationView {

    public Pane getView() {
        VBox root = new VBox(30);
        root.setPadding(new Insets(40));
        root.setAlignment(Pos.TOP_LEFT);

        Label pageTitle = new Label("Organization Portal");
        pageTitle.getStyleClass().add("label-title");
        
        Label subTitle = new Label("View your office's global impact.");
        subTitle.getStyleClass().add("label-subheader");

        VBox contentBox = new VBox(20);
        contentBox.getStyleClass().add("glass-pane");

        // If the user has an orgId, show the organization dashboard
        if (SessionManager.getOrgId() != null) {
            renderOrgDashboard(contentBox);
        } else {
            renderJoinCreateOptions(contentBox);
        }

        root.getChildren().addAll(pageTitle, subTitle, contentBox);
        return root;
    }

    private void renderJoinCreateOptions(VBox contentBox) {
        Label prompt = new Label("You do not belong to an Organization yet.");
        prompt.getStyleClass().add("label-header");

        HBox options = new HBox(30);
        
        // Create Org Form
        VBox createBox = new VBox(10);
        Label createTitle = new Label("Register New Organization");
        createTitle.getStyleClass().add("label-normal");
        createTitle.setStyle("-fx-font-weight: bold;");

        TextField orgNameField = new TextField();
        orgNameField.setPromptText("Organization Name");
        orgNameField.getStyleClass().add("text-field-glass");

        TextField industryField = new TextField();
        industryField.setPromptText("Industry (e.g. Tech)");
        industryField.getStyleClass().add("text-field-glass");

        Button createBtn = new Button("Create Org");
        createBtn.getStyleClass().add("button-primary");

        Label createError = new Label("");
        createError.setStyle("-fx-text-fill: #e74c3c;");

        createBtn.setOnAction(e -> {
            String name = orgNameField.getText().trim();
            String industry = industryField.getText().trim();
            if (name.isEmpty()) {
                createError.setText("Empty name!");
                return;
            }
            if (createOrganization(name, industry)) {
                ViewManager.showMainLayout(); // Refresh entire layout to catch org session
            } else {
                createError.setText("Creation failed.");
            }
        });

        createBox.getChildren().addAll(createTitle, orgNameField, industryField, createBtn, createError);

        // Join Org Form
        VBox joinBox = new VBox(10);
        Label joinTitle = new Label("Join Existing Organization");
        joinTitle.getStyleClass().add("label-normal");
        joinTitle.setStyle("-fx-font-weight: bold;");

        TextField orgIdField = new TextField();
        orgIdField.setPromptText("Organization ID Code");
        orgIdField.getStyleClass().add("text-field-glass");

        Button joinBtn = new Button("Join Org");
        joinBtn.getStyleClass().add("button-primary");
        
        Label joinError = new Label("");
        joinError.setStyle("-fx-text-fill: #e74c3c;");

        joinBtn.setOnAction(e -> {
            try {
                int orgId = Integer.parseInt(orgIdField.getText().trim());
                if (joinOrganization(orgId)) {
                    ViewManager.showMainLayout(); // Refresh entire layout to catch org session
                } else {
                    joinError.setText("Invalid Org ID.");
                }
            } catch (NumberFormatException ex) {
                joinError.setText("Must be a number.");
            }
        });

        joinBox.getChildren().addAll(joinTitle, orgIdField, joinBtn, joinError);

        options.getChildren().addAll(createBox, joinBox);
        contentBox.getChildren().addAll(prompt, options);
    }

    private void renderOrgDashboard(VBox contentBox) {
        // Fetch Org data
        String query = "SELECT org_name, industry FROM Organizations WHERE org_id = ?";
        String orgName = "Unknown";
        String industry = "";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, SessionManager.getOrgId());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                orgName = rs.getString("org_name");
                industry = rs.getString("industry");
            }
        } catch (Exception e) { e.printStackTrace(); }

        Label welcome = new Label(orgName + " Dashboard");
        welcome.getStyleClass().add("label-header");
        welcome.setStyle("-fx-font-size: 24px; -fx-text-fill: #27ae60;");
        
        Label detail = new Label("Industry: " + industry + " | Org ID Code: " + SessionManager.getOrgId());
        detail.getStyleClass().add("label-subheader");

        // Fetch Org Summaries
        String viewQuery = "SELECT active_users, total_gross_co2_kg FROM org_monthly_summary WHERE org_id = ? ORDER BY year DESC, month DESC LIMIT 1";
        int activeUsers = 0;
        double totalCo2 = 0;
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(viewQuery)) {
            stmt.setInt(1, SessionManager.getOrgId());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                activeUsers = rs.getInt("active_users");
                totalCo2 = rs.getDouble("total_gross_co2_kg");
            }
        } catch (Exception e) { e.printStackTrace(); }

        HBox stats = new HBox(20);
        stats.getChildren().addAll(
            createStatCard("Active Users this Month", String.valueOf(activeUsers)),
            createStatCard("Org Total Emissions (kg)", String.format("%.2f", totalCo2))
        );

        contentBox.getChildren().addAll(welcome, detail, stats);
    }

    private VBox createStatCard(String title, String value) {
        VBox card = new VBox(5);
        card.getStyleClass().add("card");
        card.setAlignment(Pos.CENTER);
        card.setMinWidth(200);
        
        Label titleLbl = new Label(title);
        titleLbl.getStyleClass().add("label-subheader");
        
        Label valueLbl = new Label(value);
        valueLbl.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #3498db;");
        
        card.getChildren().addAll(titleLbl, valueLbl);
        return card;
    }

    private boolean createOrganization(String name, String industry) {
        String insertOrg = "INSERT INTO Organizations (org_name, industry) VALUES (?, ?)";
        String updateUser = "UPDATE Users SET org_id = ? WHERE user_id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection()) {
            conn.setAutoCommit(false);
            
            try (PreparedStatement stmt1 = conn.prepareStatement(insertOrg, Statement.RETURN_GENERATED_KEYS)) {
                stmt1.setString(1, name);
                stmt1.setString(2, industry);
                stmt1.executeUpdate();
                
                ResultSet rs = stmt1.getGeneratedKeys();
                if (rs.next()) {
                    int orgId = rs.getInt(1);
                    try (PreparedStatement stmt2 = conn.prepareStatement(updateUser)) {
                        stmt2.setInt(1, orgId);
                        stmt2.setInt(2, SessionManager.getUserId());
                        stmt2.executeUpdate();
                    }
                    SessionManager.setSession(SessionManager.getUserId(), SessionManager.getName(), SessionManager.getEmail(), orgId, SessionManager.getAccountType(), SessionManager.getRole());
                    conn.commit();
                    return true;
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    private boolean joinOrganization(int orgId) {
        String query = "UPDATE Users SET org_id = ? WHERE user_id = ? AND EXISTS (SELECT 1 FROM Organizations WHERE org_id = ?)";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, orgId);
            stmt.setInt(2, SessionManager.getUserId());
            stmt.setInt(3, orgId);
            if (stmt.executeUpdate() > 0) {
                SessionManager.setSession(SessionManager.getUserId(), SessionManager.getName(), SessionManager.getEmail(), orgId, SessionManager.getAccountType(), SessionManager.getRole());
                return true;
            }
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }
}
