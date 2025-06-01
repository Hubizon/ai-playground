package pl.edu.uj.tcs.aiplayground.viewmodel;

import java.math.BigDecimal;

public class TokenPackage {
    private final int tokens;
    private final BigDecimal priceUSD; // Using BigDecimal for currency precision

    public TokenPackage(int tokens, BigDecimal priceUSD) {
        this.tokens = tokens;
        this.priceUSD = priceUSD;
    }

    public int getTokens() {
        return tokens;
    }

    public BigDecimal getPriceUSD() {
        return priceUSD;
    }

    // You might add other currencies or logic here later
}