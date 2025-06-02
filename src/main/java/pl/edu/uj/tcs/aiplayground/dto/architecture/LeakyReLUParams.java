package pl.edu.uj.tcs.aiplayground.dto.architecture;

import org.json.JSONObject;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

public record LeakyReLUParams(double alpha) implements LayerParams {

    public LeakyReLUParams() {
        this(0.01);
    }

    @Override
    public List<String> getParamNames() {
        return List.of("Alpha");
    }
}