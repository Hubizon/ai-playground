package pl.edu.uj.tcs.aiplayground.dto.architecture;

import java.util.List;

public record EmptyParams() implements LayerParams {
    @Override
    public List<String> getParamNames() {
        return List.of();
    }
}