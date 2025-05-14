package pl.edu.uj.tcs.aiplayground.core.layers;

import org.json.JSONObject;
import pl.edu.uj.tcs.aiplayground.core.ComputationalGraph;
import pl.edu.uj.tcs.aiplayground.core.Tensor;
import pl.edu.uj.tcs.aiplayground.dto.architecture.LayerConfig;
import pl.edu.uj.tcs.aiplayground.dto.architecture.LayerType;
import pl.edu.uj.tcs.aiplayground.dto.architecture.LinearParams;

import java.util.ArrayList;

public class LinearLayer implements Layer {
    int inputSize, outputSize; // TODO
    boolean useBias;
    Tensor matrix;
    Tensor bias;
    ArrayList<Tensor> params;

    public LinearLayer(int inputSize, int outputSize, boolean useBias) {
        matrix = Tensor.randomMatrix(outputSize, inputSize, -1, 1);
        bias = Tensor.randomMatrix(outputSize, 1, -1, 1);
        params = new ArrayList<>();
        params.add(matrix);
        params.add(bias);
    }

    public LinearLayer(LinearParams params) {
        this(params.inputSize(), params.outputSize(), params.isBias());
    }

    public Tensor forward(Tensor input, ComputationalGraph graph) {
        return Tensor.add(Tensor.matMul(matrix, input, graph), bias, graph);
    }

    public ArrayList<Tensor> getParams() {
        return params;
    }

    public String toJson() {
        JSONObject json = new JSONObject();
        json.put("type", "LinearLayer");
        json.put("inputSize", matrix.cols);
        json.put("outputSize", matrix.rows);
        json.put("useBias", params.contains(bias));
        return json.toString();
    }

    @Override
    public LayerConfig toConfig() {
        return new LayerConfig(LayerType.LINEAR, new LinearParams(inputSize, outputSize, useBias));
    }
}
