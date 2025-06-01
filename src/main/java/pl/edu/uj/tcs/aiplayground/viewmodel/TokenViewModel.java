package pl.edu.uj.tcs.aiplayground.viewmodel;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.edu.uj.tcs.aiplayground.exception.DatabaseException;
import pl.edu.uj.tcs.aiplayground.service.TokenService;
import pl.edu.uj.tcs.aiplayground.dto.UserDto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TokenViewModel {
    private static final Logger logger = LoggerFactory.getLogger(TokenViewModel.class);

    private final TokenService tokenService;
    private final UserViewModel userViewModel;

    private final IntegerProperty currentTokens = new SimpleIntegerProperty(0);
    private final StringProperty statusMessage = new SimpleStringProperty("");
    private final ObjectProperty<String> selectedCurrency = new SimpleObjectProperty<>("USD");

    private final Map<Integer, Double> baseTokenPrices = new HashMap<>();
    private final Map<String, Double> exchangeRates = new HashMap<>();

    public TokenViewModel(TokenService tokenService, UserViewModel userViewModel) {
        this.tokenService = tokenService;
        this.userViewModel = userViewModel;

        setupBaseTokenPrices();
        setupExchangeRates();

        this.userViewModel.userProperty().addListener((obs, oldUser, newUser) -> {
            if (newUser != null) {

                try {
                    currentTokens.set(tokenService.getUserTokens(newUser));
                } catch (DatabaseException e) {
                    System.out.println("Failed to fetch user tokens for user " + newUser.username() + ": " + e.getMessage());
                    currentTokens.set(0);
                    statusMessage.set("Failed to fetch user tokens.");
                }
            } else {
                currentTokens.set(0);
            }
        });

        if (this.userViewModel.isLoggedIn()) {
            try {
            currentTokens.set(tokenService.getUserTokens(this.userViewModel.getUser()));
            } catch (DatabaseException e) {
                System.out.println("Failed to fetch user tokens for user " + this.userViewModel.getUser().username() + ": " + e.getMessage());
                currentTokens.set(0);
                statusMessage.set("Failed to fetch user tokens.");
            }
        }
    }

    public IntegerProperty currentTokensProperty() {
        return currentTokens;
    }

    public StringProperty statusMessageProperty() {
        return statusMessage;
    }

    public ObjectProperty<String> selectedCurrencyProperty() {
        return selectedCurrency;
    }

    private void setupBaseTokenPrices() {// TODO: Może trzeba to trzymać w bazie?
        // All prices are in USD (our base currency)
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

    private void setupExchangeRates() { //TODO: Fetchowanie z bazy
        exchangeRates.put("USD", 1.0);
        exchangeRates.put("EUR", 0.92);
        exchangeRates.put("PLN", 3.98);
    }

    public List<Integer> getTokenAmounts() {
        return baseTokenPrices.keySet().stream().sorted().collect(Collectors.toList());
    }

    public List<String> getAvailableCurrencies() {
        return exchangeRates.keySet().stream().sorted().collect(Collectors.toList());
    }

    public Double getConvertedPrice(int amount, String targetCurrency) {
        Double usdPrice = baseTokenPrices.get(amount);
        if (usdPrice == null) {
            logger.warn("No base price found for {} tokens.", amount);
            return null;
        }

        Double rate = exchangeRates.get(targetCurrency);
        if (rate == null) {
            logger.error("Exchange rate for {} not found. Cannot convert price.", targetCurrency);
            statusMessage.set("Error: Currency not supported.");
            return null;
        }
        return usdPrice * rate;
    }

    public boolean purchaseTokens(int amount) {
        if (!userViewModel.isLoggedIn()) {
            statusMessage.set("You must be logged in to purchase tokens.");
            return false;
        }

        try {
            UserDto currentUser = userViewModel.getUser();
            if (currentUser != null) {
                tokenService.insertNewTokens(currentUser, amount);
                UserDto updatedUser = userViewModel.getUser();
                if (updatedUser != null) {
                    currentTokens.set(tokenService.getUserTokens(updatedUser));
                    statusMessage.set(String.format("Successfully purchased %d tokens!", amount));
                    return true;
                } else {
                    statusMessage.set("Purchase successful, but failed to update token display.");
                    return false;
                }
            } else {
                statusMessage.set("User data not available for purchase.");
                return false;
            }
        } catch (Exception e) {
            logger.error("An unexpected error occurred during token purchase: {}", e.getMessage(), e);
            statusMessage.set("An unexpected error occurred during purchase.");
            return false;
        }
    }
}