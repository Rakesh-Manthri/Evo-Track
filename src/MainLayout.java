import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

public class MainLayout {

    private BorderPane mainPane;
    private VBox sidebar;
    private Button currentActiveBtn;

    public Pane getView() {
        mainPane = new BorderPane();
        
        // Header
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(20, 30, 20, 30));
        header.setStyle("-fx-background-color: rgba(255, 255, 255, 0.5); -fx-border-color: rgba(255,255,255,0.4); -fx-border-width: 0 0 1 0;");
        
        Label userLabel = new Label("Welcome back, " + SessionManager.getName());
        userLabel.getStyleClass().add("label-header");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button logoutBtn = new Button("Log Out");
        logoutBtn.getStyleClass().add("button-secondary");
        logoutBtn.setOnAction(e -> {
            SessionManager.clearSession();
            ViewManager.showLogin();
        });
        
        header.getChildren().addAll(userLabel, spacer, logoutBtn);
        mainPane.setTop(header);

        // Sidebar Navigation
        sidebar = new VBox(15);
        sidebar.setPrefWidth(250);
        sidebar.getStyleClass().add("sidebar");
        
        Button dashboardBtn = createNavButton("Dashboard", () -> new DashboardHome().getView());
        Button logActivityBtn = createNavButton("Log Activity", () -> new ActivityLogger().getView());
        Button historyBtn = createNavButton("History", () -> new HistoryView().getView());
        Button goalsBtn = createNavButton("My Goals", () -> new GoalsView().getView());
        Button offsetsBtn = createNavButton("Carbon Offsets", () -> new OffsetsView().getView());
        Button rewardsBtn = createNavButton("Achievements", () -> new RewardsView().getView());
        Button analyticsBtn = createNavButton("Analytics Map", () -> new AnalyticsView().getView());
        
        sidebar.getChildren().addAll(dashboardBtn, logActivityBtn, historyBtn, goalsBtn, offsetsBtn, rewardsBtn, analyticsBtn);

        // Render specific UI for office users
        if ("office".equals(SessionManager.getAccountType())) {
            Button orgBtn = createNavButton("My Organization", () -> new OrganizationView().getView());
            sidebar.getChildren().add(orgBtn);
        }
        
        mainPane.setLeft(sidebar);

        // Set default view
        setActiveButton(dashboardBtn);
        mainPane.setCenter(new DashboardHome().getView());

        return mainPane;
    }

    private Button createNavButton(String text, java.util.function.Supplier<Pane> viewSupplier) {
        Button btn = new Button(text);
        btn.getStyleClass().add("button-sidebar");
        btn.setMaxWidth(Double.MAX_VALUE);
        
        btn.setOnAction(e -> {
            setActiveButton(btn);
            mainPane.setCenter(viewSupplier.get());
        });
        return btn;
    }

    private void setActiveButton(Button btn) {
        if (currentActiveBtn != null) {
            currentActiveBtn.getStyleClass().remove("active");
        }
        btn.getStyleClass().add("active");
        currentActiveBtn = btn;
    }
}
