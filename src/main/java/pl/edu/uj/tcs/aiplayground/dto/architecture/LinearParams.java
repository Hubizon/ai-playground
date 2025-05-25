package pl.edu.uj.tcs.aiplayground.dto.architecture;

import org.json.JSONObject;

import java.util.List;

public record LinearParams(int inputSize, int outputSize, boolean isBias) implements LayerParams {
    public LinearParams() {
        this(1, 1, true);
    }

    @Override
    public List<String> getParamNames() {
        return List.of("Input Size", "Output Size", "Is Bias");
    }

    public List<Class<?>> getParamTypes() {
        return List.of(Integer.class, Integer.class, Boolean.class);
    }

    @Override
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("inputSize", inputSize);
        json.put("outputSize", outputSize);
        json.put("isBias", isBias);
        return json;
    }

    @Override
    public LayerParams loadFromJson(JSONObject json) {
        int inputSize = json.getInt("inputSize");
        int outputSize = json.getInt("outputSize");
        boolean isBias = json.optBoolean("isBias", true);
        return new LinearParams(inputSize, outputSize, isBias);
    }
}