package pl.edu.uj.tcs.aiplayground.core.layers;

import org.json.JSONObject;
import pl.edu.uj.tcs.aiplayground.core.ComputationalGraph;
import pl.edu.uj.tcs.aiplayground.core.Tensor;
import pl.edu.uj.tcs.aiplayground.dto.architecture.EmptyParams;
import pl.edu.uj.tcs.aiplayground.dto.architecture.LayerConfig;
import pl.edu.uj.tcs.aiplayground.dto.architecture.LayerType;

import java.util.ArrayList;

public class SigmoidLayer implements Layer {
    public SigmoidLayer() {
    }

    public Tensor forward(Tensor input, ComputationalGraph graph) {
        return Tensor.Sigmoid(input, graph);
    }

    public ArrayList<Tensor> getParams() {
        return new ArrayList<>();
    }

    @Override
    public LayerConfig toConfig() {
        return new LayerConfig(LayerType.SIGMOID, new EmptyParams());
    }
}