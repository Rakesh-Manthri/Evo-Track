import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

public class MainLayout {

    private BorderPane mainPane;
    private VBox sidebar;
    private Button currentActiveBtn;

    public Pane getView() {
        mainPane = new BorderPane();
        mainPane.getStyleClass().add("root-pane");

        // ── TOP HEADER BAR ─────────────────────────────────────────────────
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.getStyleClass().add("header-bar");
        header.setPrefHeight(60);

        // Left: breadcrumb area
        HBox leftGroup = new HBox(10);
        leftGroup.setAlignment(Pos.CENTER_LEFT);
        Label section = new Label("🌿");
        section.setStyle("-fx-font-size: 18px;");
        Label appName = new Label("EvoTrack");
        appName.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #00ffa3;" +
                "-fx-effect: dropshadow(one-pass-box, rgba(0,255,163,0.6), 10, 0, 0, 0);");
        leftGroup.getChildren().addAll(section, appName);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Right: user greeting + logout
        HBox rightGroup = new HBox(16);
        rightGroup.setAlignment(Pos.CENTER_RIGHT);

        Label userBadge = new Label("👤  " + SessionManager.getName());
        userBadge.setStyle(
            "-fx-background-color: rgba(0,255,163,0.10);" +
            "-fx-background-radius: 20;" +
            "-fx-border-color: rgba(0,255,163,0.25);" +
            "-fx-border-radius: 20;" +
            "-fx-border-width: 1;" +
            "-fx-padding: 6 16 6 12;" +
            "-fx-text-fill: rgba(200,240,215,0.80);" +
            "-fx-font-size: 13px;"
        );

        Button logoutBtn = new Button("Sign Out");
        logoutBtn.setId("logout-btn");
        logoutBtn.getStyleClass().add("button-secondary");
        logoutBtn.setStyle(logoutBtn.getStyle() + " -fx-font-size: 13px; -fx-padding: 7 18 7 18;");
        logoutBtn.setOnAction(e -> {
            SessionManager.clearSession();
            ViewManager.showLogin();
        });

        rightGroup.getChildren().addAll(userBadge, logoutBtn);
        header.getChildren().addAll(leftGroup, spacer, rightGroup);
        mainPane.setTop(header);

        // ── SIDEBAR ─────────────────────────────────────────────────────────
        sidebar = new VBox(4);
        sidebar.setPrefWidth(230);
        sidebar.getStyleClass().add("sidebar");

        // Logo area inside sidebar (top)
        Label sideTitle = new Label("🌿  EvoTrack");
        sideTitle.getStyleClass().add("sidebar-logo-label");
        Label sideSub = new Label("Sustainability Dashboard");
        sideSub.getStyleClass().add("sidebar-logo-sub");

        Region sideDivider = new Region();
        sideDivider.setPrefHeight(1);
        sideDivider.setMaxWidth(Double.MAX_VALUE);
        sideDivider.setStyle("-fx-background-color: rgba(0,255,163,0.10);");
        VBox.setMargin(sideDivider, new Insets(4, 0, 8, 0));

        Label mainNavLabel = new Label("MAIN MENU");
        mainNavLabel.getStyleClass().add("sidebar-section-label");

        Button dashboardBtn  = createNavButton("🏠  Dashboard",      () -> new DashboardHome().getView());
        Button logActivityBtn= createNavButton("📝  Log Activity",   () -> new ActivityLogger().getView());
        Button historyBtn    = createNavButton("📅  History",         () -> new HistoryView().getView());
        Button goalsBtn      = createNavButton("🎯  My Goals",        () -> new GoalsView().getView());
        Button offsetsBtn    = createNavButton("🌳  Carbon Offsets",  () -> new OffsetsView().getView());

        Label insightsLabel = new Label("INSIGHTS");
        insightsLabel.getStyleClass().add("sidebar-section-label");

        Button rewardsBtn    = createNavButton("🏅  Achievements",   () -> new RewardsView().getView());
        Button analyticsBtn  = createNavButton("📊  Analytics",       () -> new AnalyticsView().getView());
        Button reportsBtn    = createNavButton("📄  Reports",         () -> new ReportsView().getView());

        sidebar.getChildren().addAll(
            sideTitle, sideSub, sideDivider,
            mainNavLabel,
            dashboardBtn, logActivityBtn, historyBtn, goalsBtn, offsetsBtn,
            insightsLabel,
            rewardsBtn, analyticsBtn, reportsBtn
        );

        // Office-only section
        if ("office".equals(SessionManager.getAccountType())) {
            Label orgLabel = new Label("ORGANISATION");
            orgLabel.getStyleClass().add("sidebar-section-label");
            Button orgBtn = createNavButton("🏢  My Organisation", () -> new OrganizationView().getView());
            sidebar.getChildren().addAll(orgLabel, orgBtn);
        }

        // Spacer + version at bottom
        Region bottomSpacer = new Region();
        VBox.setVgrow(bottomSpacer, Priority.ALWAYS);
        Label versionLabel = new Label("v1.0.0  •  EvoTrack");
        versionLabel.getStyleClass().add("label-muted");
        versionLabel.setPadding(new Insets(10, 0, 0, 12));
        sidebar.getChildren().addAll(bottomSpacer, versionLabel);

        mainPane.setLeft(sidebar);

        // Default dashboard view
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
