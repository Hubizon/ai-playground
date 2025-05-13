package pl.edu.uj.tcs.aiplayground.core.layers;

import org.json.JSONObject;
import pl.edu.uj.tcs.aiplayground.core.ComputationalGraph;
import pl.edu.uj.tcs.aiplayground.core.Tensor;

import java.util.ArrayList;

public class ReluLayer implements Layer {
    public ReluLayer() {
    }

    public Tensor forward(Tensor input, ComputationalGraph graph) {
        return Tensor.Relu(input, graph);
    }

    public ArrayList<Tensor> getParams() {
        return new ArrayList<>();
    }

    public String toJson() {
        JSONObject json = new JSONObject();
        json.put("type", "ReluLayer");
        return json.toString();
    }
}
