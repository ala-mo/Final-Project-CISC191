package budgettracker.ui;

import budgettracker.persistence.TransactionRepository;
import budgettracker.services.FinanceService;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

// sets up the service layer and passes it to the controller via FXML.
public class BudgetTrackerApp extends Application {

    private static final String DATA_FILE = "transactions.csv";

    @Override
    public void start(Stage stage) throws IOException {
        // sets up the data layer and service
        TransactionRepository repo = new TransactionRepository(DATA_FILE);
        try { repo.loadFromFile(); } 
        catch (Exception e) { /* first run, no file yet */ }

        FinanceService service = new FinanceService(repo);

        // loads FXML and gives the controller access to the service
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/budgettracker/view/main-view.fxml"));
        Scene scene = new Scene(loader.load(), 860, 600);

        BudgetController controller = loader.getController();
        controller.init(service, repo);

        stage.setTitle("Budget Tracker");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}