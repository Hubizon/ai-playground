package pl.edu.uj.tcs.aiplayground.view;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import pl.edu.uj.tcs.aiplayground.viewmodel.TokenViewModel;
import pl.edu.uj.tcs.aiplayground.viewmodel.UserViewModel;
import pl.edu.uj.tcs.aiplayground.viewmodel.ViewModelFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TokenShopController {

    @FXML
    private Label currentTokensLabel;
    @FXML
    private ComboBox<String> currencyComboBox;
    @FXML
    private TabPane tokenTabPane;
    @FXML
    private HBox currencySelectorBox;
    @FXML
    private Label tokenShopTitle;

    private ViewModelFactory factory;
    private TokenViewModel tokenViewModel;
    private Stage stage;

    private final Map<Integer, Double> baseTokenPrices = new HashMap<>();

    private final Map<String, Double> exchangeRates = new HashMap<>();

    private final List<Integer> tokenAmounts = Arrays.asList(
            100, 250, 500, 1000, 2500, 5000, 10000, 25000, 50000
    );

    public void initialize(ViewModelFactory factory) {
        this.factory = factory;
        this.tokenViewModel = factory.getTokenViewModel();

        currencyComboBox.setItems(FXCollections.observableArrayList("USD", "EUR", "PLN"));
        currencyComboBox.setValue("USD");

        setupBaseTokenPrices();

        setupExchangeRates();

        setupTokenTabs();

        updateCurrentTokensDisplay();

        currencyComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                updateAllTokenPrices(newVal);
            }
        });

        updateAllTokenPrices(currencyComboBox.getValue());

        tokenShopTitle.getStyleClass().add("title-label");
        currentTokensLabel.getStyleClass().add("value-label");
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    private void setupBaseTokenPrices() {
        baseTokenPrices.put(100, 1.99);
        baseTokenPrices.put(250, 4.49);
        baseTokenPrices.put(500, 7.99);
        baseTokenPrices.put(1000, 14.99);
        baseTokenPrices.put(2500, 34.99);
        baseTokenPrices.put(5000, 59.99);
        baseTokenPrices.put(10000, 99.99);
        baseTokenPrices.put(25000, 199.99);
        baseTokenPrices.put(50000, 349.99);
    }

    private void setupExchangeRates() {
        exchangeRates.put("USD", 1.0);
        exchangeRates.put("EUR", 0.92);
        exchangeRates.put("PLN", 3.98);
    }

    private double convertPrice(double usdPrice, String targetCurrency) {
        Double rate = exchangeRates.get(targetCurrency);
        if (rate == null) {
            System.err.println("Exchange rate for " + targetCurrency + " not found. Using USD price.");
            return usdPrice;
        }
        return usdPrice * rate;
    }

    private void setupTokenTabs() {
        tokenTabPane.getTabs().clear();
        for (Integer amount : tokenAmounts) {
            Tab tab = new Tab(amount + " Tokens");
            tab.setId("tab" + amount + "Tokens");

            VBox contentBox = new VBox(20);
            contentBox.setAlignment(Pos.CENTER);
            contentBox.getStyleClass().add("token-tab-content");

            Label amountLabel = new Label("Buy " + amount + " Tokens");
            amountLabel.getStyleClass().add("amount-label");

            Label priceLabel = new Label("Loading price...");
            priceLabel.setId("price" + amount + "TokensLabel");
            priceLabel.getStyleClass().add("price-label");
            priceLabel.setFont(new Font(36.0));

            Button buyButton = new Button("Buy Now!");
            buyButton.setId("buy" + amount + "TokensButton");
            buyButton.getStyleClass().add("buy-button");
            buyButton.setFont(new Font(24.0));
            buyButton.setOnAction(event -> confirmPurchase(amount));

            contentBox.getChildren().addAll(amountLabel, priceLabel, buyButton);
            tab.setContent(contentBox);
            tokenTabPane.getTabs().add(tab);
        }
    }

    private void updateCurrentTokensDisplay() {
        currentTokensLabel.setText(String.valueOf(tokenViewModel.currentTokensProperty().getValue()));
    }

    private void updateAllTokenPrices(String currency) {
        for (Tab tab : tokenTabPane.getTabs()) {
            String tabId = tab.getId();
            if (tabId != null && tabId.startsWith("tab") && tabId.endsWith("Tokens")) {
                try {
                    int amount = Integer.parseInt(tabId.replace("tab", "").replace("Tokens", ""));
                    Double usdPrice = baseTokenPrices.get(amount);

                    if (usdPrice != null) {
                        double convertedPrice = convertPrice(usdPrice, currency);
                        Label priceLabel = (Label) tab.getContent().lookup("#price" + amount + "TokensLabel");
                        if (priceLabel != null) {
                            priceLabel.setText(String.format("%.2f %s", convertedPrice, currency));
                        }
                    }
                } catch (NumberFormatException e) {
                    System.err.println("Could not parse amount from tab ID: " + tabId + " - " + e.getMessage());
                }
            }
        }
    }

    private void confirmPurchase(int amount) {
        String currency = currencyComboBox.getValue();
        Double usdPrice = baseTokenPrices.get(amount);
        Double finalPrice;

        if (usdPrice != null) {
            finalPrice = convertPrice(usdPrice, currency);
        } else {
            finalPrice = null;
        }

        if (finalPrice == null) {
            alertMessage("Error: Price for " + amount + " tokens in " + currency + " is not available.", false);
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Purchase");
        alert.setHeaderText("Confirm your token purchase");
        alert.setContentText(String.format("Are you sure you want to buy %d tokens for %.2f %s?", amount, finalPrice, currency));
        alert.getDialogPane().getStylesheets().add(
                getClass().getResource("/pl/edu/uj/tcs/aiplayground/view/style/styles.css").toExternalForm()
        );
        alert.getDialogPane().getStyleClass().add("dialog-pane");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                System.out.println("Purchasing " + amount + " tokens for " + String.format("%.2f", finalPrice) + " " + currency);
                boolean success =  tokenViewModel.purchaseTokens(amount);
                if (success) {
                    alertMessage("Purchase successful! You received " + amount + " tokens.", true);
                    updateCurrentTokensDisplay();
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