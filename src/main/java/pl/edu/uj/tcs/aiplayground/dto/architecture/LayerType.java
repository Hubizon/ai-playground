package pl.edu.uj.tcs.aiplayground.dto.architecture;

import pl.edu.uj.tcs.aiplayground.core.layers.*;

import java.util.function.Function;

public enum LayerType {
    LINEAR("Linear", new LinearParams(), params -> new LinearLayer((LinearParams) params)),
    RELU("ReLU", new EmptyParams(), params -> new ReluLayer()),
    SIGMOID("Sigmoid", new EmptyParams(), params -> new SigmoidLayer()),
    SOFTMAX("Softmax", new EmptyParams(), params -> new SoftMaxLayer()),
    LEAKYRELU("LeakyReLU", new LeakyReLUParams(), params -> new LeakyReluLayer((LeakyReLUParams) params)),
    DROPOUT("Dropout", new DropoutParams(), params -> new DropoutLayer((DropoutParams) params)),
    GELU("GELU", new EmptyParams(), params -> new GeluLayer()),
    TANH("Tanh", new EmptyParams(), params -> new TanhLayer());
    private final String name;
    private final LayerParams params;
    private final Function<LayerParams, Layer> factory;

    LayerType(String name, LayerParams params, Function<LayerParams, Layer> factory) {
        this.name = name;
        this.params = params;
        this.factory = factory;
    }

    @Override
    public String toString() {
        return name;
    }

    public LayerParams getParams() {
        return params;
    }

    public Layer createLayer(LayerParams params) {
        return factory.apply(params);
    }
}
