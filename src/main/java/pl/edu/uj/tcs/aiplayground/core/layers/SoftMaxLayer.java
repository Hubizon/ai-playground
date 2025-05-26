package pl.edu.uj.tcs.aiplayground.core.layers;

import org.json.JSONObject;
import pl.edu.uj.tcs.aiplayground.core.ComputationalGraph;
import pl.edu.uj.tcs.aiplayground.core.Tensor;
import pl.edu.uj.tcs.aiplayground.dto.architecture.LayerConfig;
import pl.edu.uj.tcs.aiplayground.dto.architecture.LayerType;
import pl.edu.uj.tcs.aiplayground.dto.architecture.EmptyParams;

import java.util.ArrayList;

public class SoftMaxLayer implements Layer {

    public SoftMaxLayer() {}

    @Override
    public Tensor forward(Tensor input, ComputationalGraph graph) {
        Tensor result = Tensor.Softmax(input,graph);
        return result;
    }

    @Override
    public ArrayList<Tensor> getParams() {
        return new ArrayList<>();
    }

    @Override
    public LayerConfig toConfig() {
        return new LayerConfig(LayerType.SOFTMAX, new EmptyParams());
    }
}
