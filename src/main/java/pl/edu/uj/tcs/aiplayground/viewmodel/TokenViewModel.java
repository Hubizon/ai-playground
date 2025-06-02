package pl.edu.uj.tcs.aiplayground.viewmodel;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.edu.uj.tcs.aiplayground.dto.CurrencyDto;
import pl.edu.uj.tcs.aiplayground.exception.DatabaseException;
import pl.edu.uj.tcs.aiplayground.service.TokenService;
import pl.edu.uj.tcs.aiplayground.dto.UserDto;

import java.util.*;
import java.util.stream.Collectors;

public class TokenViewModel {
    private static final Logger logger = LoggerFactory.getLogger(TokenViewModel.class);

    private final TokenService tokenService;
    private final UserViewModel userViewModel;

    private final IntegerProperty currentTokens = new SimpleIntegerProperty(0);
    private final StringProperty statusMessage = new SimpleStringProperty("");
    private final ObjectProperty<String> selectedCurrency = new SimpleObjectProperty<>("USD");

    private List<CurrencyDto> currencyList = new ArrayList<>();
    private final List<Integer> tokenPackets = new ArrayList<>();

    public TokenViewModel(TokenService tokenService, UserViewModel userViewModel) {
        this.tokenService = tokenService;
        this.userViewModel = userViewModel;

        setupCurrencyTokenPrices();
        setupPackets();

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

    private void setupCurrencyTokenPrices() {
        try {
            currencyList = tokenService.getCurrencyList();
        } catch (DatabaseException e) {

        }
    }

    private void setupPackets() {
        tokenPackets.add(100);
        tokenPackets.add(200);
        tokenPackets.add(500);
        tokenPackets.add(1000);
        tokenPackets.add(2000);
        tokenPackets.add(5000);
        tokenPackets.add(10000);
        tokenPackets.add(20000);
        tokenPackets.add(50000);
    }

    public List<Integer> getTokenAmounts() {
        return tokenPackets.stream().sorted().collect(Collectors.toList());
    }

    public ObservableList<String> getAvailableCurrencies() {
        return FXCollections.observableArrayList(
                currencyList.stream()
                        .map(CurrencyDto::name)
                        .sorted()
                        .collect(Collectors.toList())
        );
    }

    public Double getConvertedPrice(int amount, String targetCurrency) {

        Optional<CurrencyDto> currencyDtoOpt = currencyList.stream()
                .filter(c -> c.name().equals(targetCurrency))
                .findFirst();

        if (currencyDtoOpt.isEmpty()) {
            logger.error("Exchange rate for {} not found. Cannot convert price.", targetCurrency);
            statusMessage.set("Error: Currency not supported.");
            return null;
        }

        Double rate = currencyDtoOpt.get().conversionRate();
        return amount * rate;
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