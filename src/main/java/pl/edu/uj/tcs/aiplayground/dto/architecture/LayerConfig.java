package pl.edu.uj.tcs.aiplayground.dto.architecture;

import pl.edu.uj.tcs.aiplayground.core.layers.Layer;

public record LayerConfig(LayerType type, LayerParams params) {
    public Layer toLayer() {
        return type.createLayer(params);
    }
}
