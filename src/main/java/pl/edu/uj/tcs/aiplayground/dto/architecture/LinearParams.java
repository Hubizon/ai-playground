package pl.edu.uj.tcs.aiplayground.dto.architecture;

import java.util.List;

public record LinearParams(int inputSize, int outputSize, boolean isBias) implements LayerParams {
    @Override
    public List<String> getParamNames() {
        return List.of("Input Size", "Output Size", "Is Bias");
    }
}