package budget.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Entry point for the JavaFX application.
 * Module 7: extends Application, loads FXML, shows the window.
 */
public class BudgetTrackerApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/budget/ui/main.fxml")
        );
        Scene scene = new Scene(loader.load(), 900, 600);
        stage.setTitle("Budget Tracker");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
