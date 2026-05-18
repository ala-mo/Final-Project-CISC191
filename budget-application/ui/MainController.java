package budget.ui;

import budget.model.*;
import budget.persistence.CsvStorage;
import budget.service.TransactionService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDate;
import java.util.Map;

/**
 * The Controller in MVC.
 * It connects the FXML view to the TransactionService (the Model).
 *
 * Module 7: JavaFX controls, event handlers, lambdas, threading (Task),
 *           MVC separation (controller knows nothing about CSV or math).
 */
public class MainController {

    // --- Service (Model) ---
    private final TransactionService service = new TransactionService();

    // --- FXML fields (injected from main.fxml) ---

    // Stat labels at the top
    @FXML private Label labelIncome;
    @FXML private Label labelExpenses;
    @FXML private Label labelBalance;
    @FXML private Label labelStatus;

    // Add-transaction form
    // ToggleGroup wired in initialize() to avoid FXML $ reference issues
    private final ToggleGroup typeToggle = new ToggleGroup();
    @FXML private RadioButton radioExpense;
    @FXML private RadioButton radioIncome;
    @FXML private TextField fieldAmount;
    @FXML private TextField fieldDescription;
    @FXML private ComboBox<Category> comboCategory;
    @FXML private DatePicker datePicker;

    // Transaction table
    @FXML private TableView<Transaction> tableView;
    @FXML private TableColumn<Transaction, String>   colType;
    @FXML private TableColumn<Transaction, String>   colDesc;
    @FXML private TableColumn<Transaction, Category> colCategory;
    @FXML private TableColumn<Transaction, Double>   colAmount;
    @FXML private TableColumn<Transaction, LocalDate> colDate;

    // Pie chart
    @FXML private PieChart pieChart;

    // The live list that the TableView watches
    private final ObservableList<Transaction> tableData = FXCollections.observableArrayList();

    // ----------------------------------------------------------------
    // Called automatically by JavaFX after all @FXML fields are set
    // ----------------------------------------------------------------
    @FXML
    public void initialize() {
        // Wire the ToggleGroup to radio buttons (done here instead of FXML)
        radioExpense.setToggleGroup(typeToggle);
        radioIncome.setToggleGroup(typeToggle);
        radioExpense.setSelected(true);

        // Set up table columns — each column knows which getter to call
        // Module 7: lambda-style PropertyValueFactory wires columns to model fields
        colType.setCellValueFactory(    new PropertyValueFactory<>("label"));
        colDesc.setCellValueFactory(    new PropertyValueFactory<>("description"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colAmount.setCellValueFactory(  new PropertyValueFactory<>("amount"));
        colDate.setCellValueFactory(    new PropertyValueFactory<>("date"));

        // Color expense rows red, income rows green
        // Module 7: lambda as event handler (row factory)
        tableView.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(Transaction t, boolean empty) {
                super.updateItem(t, empty);
                if (t == null || empty) {
                    setStyle("");
                } else if (t.getLabel().equals("Expense")) {
                    setStyle("-fx-background-color: #fff0f0;");
                } else {
                    setStyle("-fx-background-color: #f0fff0;");
                }
            }
        });

        tableView.setItems(tableData);

        // Populate the category dropdown
        comboCategory.setItems(FXCollections.observableArrayList(Category.values()));
        comboCategory.getSelectionModel().selectFirst();

        // Default date = today
        datePicker.setValue(LocalDate.now());

        // Load saved data from CSV in a background thread
        loadFromFile();
    }

    // ----------------------------------------------------------------
    // Button: Add transaction
    // Module 7: @FXML event handler with lambda for background save
    // ----------------------------------------------------------------
    @FXML
    private void handleAdd() {
        // Validate inputs
        double amount;
        try {
            amount = Double.parseDouble(fieldAmount.getText().trim());
            if (amount <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            showAlert("Please enter a positive number for amount.");
            return;
        }

        String desc = fieldDescription.getText().trim();
        if (desc.isBlank()) {
            showAlert("Please enter a description.");
            return;
        }

        Category cat  = comboCategory.getValue();
        LocalDate date = datePicker.getValue();
        int id        = service.getNextId();

        // Module 3: polymorphism — create the right subclass based on toggle
        Transaction t;
        if (radioIncome.isSelected()) {
            t = new Income(id, amount, desc, cat, date);
        } else {
            t = new Expense(id, amount, desc, cat, date);
        }

        service.addTransaction(t);
        refreshUI();
        saveInBackground();

        // Clear form
        fieldAmount.clear();
        fieldDescription.clear();
        datePicker.setValue(LocalDate.now());
    }

    // ----------------------------------------------------------------
    // Button: Delete selected transaction
    // ----------------------------------------------------------------
    @FXML
    private void handleDelete() {
        Transaction selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Select a transaction to delete.");
            return;
        }
        service.removeTransaction(selected.getId());
        refreshUI();
        saveInBackground();
    }

    // ----------------------------------------------------------------
    // Refresh stat labels, table, and pie chart
    // ----------------------------------------------------------------
    private void refreshUI() {
        labelIncome.setText(  String.format("$%.2f", service.getTotalIncome()));
        labelExpenses.setText(String.format("$%.2f", service.getTotalExpenses()));
        labelBalance.setText( String.format("$%.2f", service.getBalance()));

        tableData.setAll(service.getSorted()); // uses Transaction.compareTo()
        refreshPieChart();
    }

    private void refreshPieChart() {
        ObservableList<PieChart.Data> slices = FXCollections.observableArrayList();

        // Module 6: groupByCategory() uses Collectors.groupingBy stream
        Map<Category, java.util.List<Transaction>> grouped = service.groupByCategory();

        // Module 2: forEach with lambda (Consumer)
        grouped.forEach((cat, list) -> {
            double total = list.stream().mapToDouble(Transaction::getAmount).sum();
            slices.add(new PieChart.Data(cat.toString(), total));
        });

        pieChart.setData(slices);
    }

    // ----------------------------------------------------------------
    // Module 7: Background thread — saves CSV without freezing the UI
    // ----------------------------------------------------------------
    private void saveInBackground() {
        Task<Void> saveTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                CsvStorage.save(service);
                return null;
            }
        };

        // Update the status label on success/failure (must happen on FX thread)
        saveTask.setOnSucceeded(e -> labelStatus.setText("Saved"));
        saveTask.setOnFailed(   e -> labelStatus.setText("Save failed"));

        Thread thread = new Thread(saveTask);
        thread.setDaemon(true); // don't prevent the app from closing
        thread.start();
    }

    // ----------------------------------------------------------------
    // Load CSV in a background thread on startup
    // ----------------------------------------------------------------
    private void loadFromFile() {
        Task<Void> loadTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                CsvStorage.load(service);
                return null;
            }
        };

        loadTask.setOnSucceeded(e -> {
            refreshUI();
            labelStatus.setText("Loaded");
        });

        loadTask.setOnFailed(e -> labelStatus.setText("Load failed"));

        Thread thread = new Thread(loadTask);
        thread.setDaemon(true);
        thread.start();
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING, message, ButtonType.OK);
        alert.showAndWait();
    }
}
