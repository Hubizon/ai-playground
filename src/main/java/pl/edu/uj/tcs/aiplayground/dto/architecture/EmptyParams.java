package pl.edu.uj.tcs.aiplayground.dto.architecture;

import org.json.JSONObject;

import java.util.List;

public record EmptyParams() implements LayerParams {
    @Override
    public List<String> getParamNames() {
        return List.of();
    }

    public List<Class<?>> getParamTypes() {
        return List.of();
    }

    @Override
    public JSONObject toJson() {
        return new JSONObject();
    }

    @Override
    public LayerParams loadFromJson(JSONObject json) {
        return new EmptyParams();
    }
}