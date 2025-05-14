package pl.edu.uj.tcs.aiplayground.dto.architecture;

import pl.edu.uj.tcs.aiplayground.core.layers.Layer;
import pl.edu.uj.tcs.aiplayground.core.layers.LinearLayer;
import pl.edu.uj.tcs.aiplayground.core.layers.ReluLayer;
import pl.edu.uj.tcs.aiplayground.core.layers.SigmoidLayer;

import java.util.function.Function;

public enum LayerType {
    LINEAR(LinearParams.class, params -> new LinearLayer((LinearParams) params)),
    RELU(EmptyParams.class, params -> new ReluLayer()),
    SIGMOID(EmptyParams.class, params -> new SigmoidLayer());

    private final Class<? extends LayerParams> paramType;
    private final Function<LayerParams, Layer> factory;

    LayerType(Class<? extends LayerParams> paramType, Function<LayerParams, Layer> factory) {
        this.paramType = paramType;
        this.factory = factory;
    }

    public Class<? extends LayerParams> getParamType() {
        return paramType;
    }

    public Layer createLayer(LayerParams params) {
        if (!paramType.isInstance(params)) {
            throw new IllegalArgumentException("Expected " + paramType.getSimpleName() + " but got " + params.getClass().getSimpleName());
        }
        return factory.apply(params);
    }
}
