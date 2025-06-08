package pl.edu.uj.tcs.aiplayground.core.layers;

import pl.edu.uj.tcs.aiplayground.core.ComputationalGraph;
import pl.edu.uj.tcs.aiplayground.core.Tensor;
import pl.edu.uj.tcs.aiplayground.dto.architecture.EmptyParams;
import pl.edu.uj.tcs.aiplayground.dto.architecture.LayerConfig;
import pl.edu.uj.tcs.aiplayground.dto.architecture.LayerType;

import java.util.ArrayList;

public class GeluLayer implements Layer {
    public GeluLayer() {
    }

    @Override
    public Tensor forward(Tensor input, ComputationalGraph graph) {
        return Tensor.Gelu(input, graph);
    }

    @Override
    public ArrayList<Tensor> getParams() {
        return new ArrayList<>();
    }

    @Override
    public LayerConfig toConfig() {
        return new LayerConfig(LayerType.GELU, new EmptyParams());
    }
}