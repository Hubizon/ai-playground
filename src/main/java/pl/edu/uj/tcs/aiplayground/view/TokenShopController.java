package pl.edu.uj.tcs.aiplayground.view;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import pl.edu.uj.tcs.aiplayground.viewmodel.TokenViewModel;
import pl.edu.uj.tcs.aiplayground.viewmodel.ViewModelFactory;

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


    public void initialize(ViewModelFactory factory) {
        this.factory = factory;
        this.tokenViewModel = factory.getTokenViewModel();

        currencyComboBox.setItems(tokenViewModel.getAvailableCurrencies());
        currencyComboBox.setValue("USD");

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
        this.stage.setMinWidth(tokenViewModel.getTokenAmounts().size() * 100 + 100);
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/icon.png")));
    }


    private void setupTokenTabs() {
        tokenTabPane.getTabs().clear();
        for (Integer amount : tokenViewModel.getTokenAmounts()) {
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
                    double convertedPrice = tokenViewModel.getConvertedPrice(amount, currency);
                    Label priceLabel = (Label) tab.getContent().lookup("#price" + amount + "TokensLabel");
                    if (priceLabel != null) {
                        priceLabel.setText(String.format("%.2f %s", convertedPrice, currency));
                    }

                } catch (NumberFormatException e) {
                    System.err.println("Could not parse amount from tab ID: " + tabId + " - " + e.getMessage());
                }
            }
        }
    }

    private void confirmPurchase(int amount) {
        String currency = currencyComboBox.getValue();

        double Price = tokenViewModel.getConvertedPrice(amount, currency);

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Purchase");
        alert.setHeaderText("Confirm your token purchase");

        alert.setContentText(String.format("Are you sure you want to buy %d tokens for %.2f %s?", amount, Price, currency));
        alert.getDialogPane().getStylesheets().add(
                getClass().getResource("/pl/edu/uj/tcs/aiplayground/view/style/styles.css").toExternalForm()
        );
        alert.getDialogPane().getStyleClass().add("dialog-pane");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                System.out.println("Purchasing " + amount + " tokens for " + String.format("%.2f", Price) + " " + currency);
                boolean success = tokenViewModel.purchaseTokens(amount);
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