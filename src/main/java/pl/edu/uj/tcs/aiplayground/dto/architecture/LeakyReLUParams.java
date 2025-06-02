package pl.edu.uj.tcs.aiplayground.dto.architecture;

import java.math.BigDecimal;
import java.util.List;

public record LeakyReLUParams(BigDecimal alpha) implements LayerParams {
    public LeakyReLUParams() {
        this(new BigDecimal("0.01"));
    }

    @Override
    public List<String> getParamNames() {
        return List.of("Alpha");
    }
}