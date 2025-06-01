package pl.edu.uj.tcs.aiplayground.view;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import pl.edu.uj.tcs.aiplayground.viewmodel.UserViewModel;
import pl.edu.uj.tcs.aiplayground.viewmodel.ViewModelFactory;

import java.util.HashMap;
import java.util.Map;

public class TokenShopController {

    @FXML
    private Label currentTokensLabel;
    @FXML
    private ComboBox<String> currencyComboBox;
    @FXML
    private TabPane tokenTabPane;

    // Labels for displaying prices in each tab
    @FXML private Label price100TokensLabel;
    @FXML private Label price250TokensLabel;
    @FXML private Label price500TokensLabel;
    @FXML private Label price1000TokensLabel;
    @FXML private Label price2500TokensLabel;
    @FXML private Label price5000TokensLabel;
    @FXML private Label price10000TokensLabel;
    @FXML private Label price25000TokensLabel;
    @FXML private Label price50000TokensLabel;


    private ViewModelFactory factory;
    private UserViewModel userViewModel;
    private Stage stage;

    // Simulate token prices in different currencies
    private final Map<String, Map<Integer, Double>> tokenPrices = new HashMap<>();

    public void initialize(ViewModelFactory factory) {
        this.factory = factory;
        this.userViewModel = factory.getUserViewModel();

        // Initialize currency options
        currencyComboBox.setItems(FXCollections.observableArrayList("USD", "EUR", "PLN"));
        currencyComboBox.setValue("USD"); // Default currency

        // Populate mock token prices
        setupMockTokenPrices();

        // Update current tokens label
        updateCurrentTokensDisplay();

        // Listener for currency changes
        currencyComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                updateAllTokenPrices(newVal);
            }
        });

        // Initial price update
        updateAllTokenPrices(currencyComboBox.getValue());
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    private void setupMockTokenPrices() {
        // USD Prices
        Map<Integer, Double> usdPrices = new HashMap<>();
        usdPrices.put(100, 1.99);
        usdPrices.put(250, 4.49);
        usdPrices.put(500, 7.99);
        usdPrices.put(1000, 14.99);
        usdPrices.put(2500, 34.99);
        usdPrices.put(5000, 59.99);
        usdPrices.put(10000, 99.99);
        usdPrices.put(25000, 199.99);
        usdPrices.put(50000, 349.99);
        tokenPrices.put("USD", usdPrices);

        // EUR Prices
        Map<Integer, Double> eurPrices = new HashMap<>();
        eurPrices.put(100, 1.79);
        eurPrices.put(250, 3.99);
        eurPrices.put(500, 6.99);
        eurPrices.put(1000, 12.99);
        eurPrices.put(2500, 29.99);
        eurPrices.put(5000, 52.99);
        eurPrices.put(10000, 87.99);
        eurPrices.put(25000, 175.99);
        eurPrices.put(50000, 300.99);
        tokenPrices.put("EUR", eurPrices);

        // PLN Prices
        Map<Integer, Double> plnPrices = new HashMap<>();
        plnPrices.put(100, 8.99);
        plnPrices.put(250, 19.99);
        plnPrices.put(500, 34.99);
        plnPrices.put(1000, 64.99);
        plnPrices.put(2500, 149.99);
        plnPrices.put(5000, 269.99);
        plnPrices.put(10000, 449.99);
        plnPrices.put(25000, 899.99);
        plnPrices.put(50000, 1500.99);
        tokenPrices.put("PLN", plnPrices);
    }

    private void updateCurrentTokensDisplay() {
        // Assume UserDto has a getTokens() method, if not, adjust accordingly
        // For now, setting a dummy value or getting from a userViewModel if available
        currentTokensLabel.setText(String.valueOf(2137)); // Assuming `tokens()` method exists in `UserDto`
    }

    private void updateAllTokenPrices(String currency) {
        Map<Integer, Double> prices = tokenPrices.get(currency);
        if (prices != null) {
            price100TokensLabel.setText(String.format("Price: %.2f %s", prices.get(100), currency));
            price250TokensLabel.setText(String.format("Price: %.2f %s", prices.get(250), currency));
            price500TokensLabel.setText(String.format("Price: %.2f %s", prices.get(500), currency));
            price1000TokensLabel.setText(String.format("Price: %.2f %s", prices.get(1000), currency));
            price2500TokensLabel.setText(String.format("Price: %.2f %s", prices.get(2500), currency));
            price5000TokensLabel.setText(String.format("Price: %.2f %s", prices.get(5000), currency));
            price10000TokensLabel.setText(String.format("Price: %.2f %s", prices.get(10000), currency));
            price25000TokensLabel.setText(String.format("Price: %.2f %s", prices.get(25000), currency));
            price50000TokensLabel.setText(String.format("Price: %.2f %s", prices.get(50000), currency));
        } else {
            // Handle case where currency prices are not defined
            System.err.println("Prices for currency " + currency + " not found.");
        }
    }

    @FXML
    private void handleBuy100Tokens() {
        confirmPurchase(100);
    }

    @FXML
    private void handleBuy250Tokens() {
        confirmPurchase(250);
    }

    @FXML
    private void handleBuy500Tokens() {
        confirmPurchase(500);
    }

    @FXML
    private void handleBuy1000Tokens() {
        confirmPurchase(1000);
    }

    @FXML
    private void handleBuy2500Tokens() {
        confirmPurchase(2500);
    }

    @FXML
    private void handleBuy5000Tokens() {
        confirmPurchase(5000);
    }

    @FXML
    private void handleBuy10000Tokens() {
        confirmPurchase(10000);
    }

    @FXML
    private void handleBuy25000Tokens() {
        confirmPurchase(25000);
    }

    @FXML
    private void handleBuy50000Tokens() {
        confirmPurchase(50000);
    }

    private void confirmPurchase(int amount) {
        String currency = currencyComboBox.getValue();
        Double price = tokenPrices.get(currency).get(amount);

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Purchase");
        alert.setHeaderText("Confirm your token purchase");
        alert.setContentText(String.format("Are you sure you want to buy %d tokens for %.2f %s?", amount, price, currency));
        alert.getDialogPane().getStylesheets().add(
                getClass().getResource("/pl/edu/uj/tcs/aiplayground/view/style/styles.css").toExternalForm()
        );
        alert.getDialogPane().getStyleClass().add("dialog-pane");


        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Simulate purchase
                System.out.println("Purchasing " + amount + " tokens for " + price + " " + currency);
                // Call a method in the ViewModel to handle the actual purchase logic
                boolean success = true;//userViewModel.purchaseTokens(amount); // Assuming a purchaseTokens method
                if (success) {
                    alertMessage("Purchase successful! You received " + amount + " tokens.", true);
                    updateCurrentTokensDisplay(); // Refresh token count
                } else {
                    alertMessage("Purchase failed. Please try again.", false);
                }
            }
        });
    }

    @FXML
    private void handleInfo() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Token Information");
        alert.setHeaderText("About AI Tokens");
        alert.setContentText("AI Tokens are used to access premium features and run more complex models within the AI Playground. Each token allows for a certain amount of computational time or access to specific functionalities. The more tokens you have, the more you can explore!");
        alert.getDialogPane().getStylesheets().add(
                getClass().getResource("/pl/edu/uj/tcs/aiplayground/view/style/styles.css").toExternalForm()
        );
        alert.getDialogPane().getStyleClass().add("dialog-pane");
        alert.showAndWait();
    }

    private void alertMessage(String message, Boolean isInfo) {
        Alert alert;
        if (isInfo)
            alert = new Alert(Alert.AlertType.INFORMATION);
        else
            alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(isInfo ? "Success" : "Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.getDialogPane().getStylesheets().add(
                getClass().getResource("/pl/edu/uj/tcs/aiplayground/view/style/styles.css").toExternalForm()
        );
        alert.getDialogPane().getStyleClass().add("dialog-pane");
        ButtonType okButton = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        alert.getButtonTypes().setAll(okButton);
        alert.showAndWait();
    }
}