package pl.edu.uj.tcs.aiplayground.dto.architecture;

import java.math.BigDecimal;
import java.util.List;

public record DropoutParams(BigDecimal amount) implements LayerParams {
    public DropoutParams() {
        this(new BigDecimal("0.01"));
    }

    @Override
    public List<String> getParamNames() {
        return List.of("Amount");
    }
}
