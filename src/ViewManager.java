import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;

public class ViewManager {
    private static Stage primaryStage;

    public static void setStage(Stage stage) {
        primaryStage = stage;
    }

    private static void setScene(Pane rootPane) {
        rootPane.getStyleClass().add("root-pane");
        Scene scene = new Scene(rootPane, 1000, 700);
        scene.getStylesheets().add(ViewManager.class.getResource("style.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.centerOnScreen();
    }

    public static void showLogin() {
        setScene(new LoginView().getView());
    }

    public static void showRegister() {
        setScene(new RegisterView().getView());
    }

    public static void showMainLayout() {
        setScene(new MainLayout().getView());
    }
}
