package pl.edu.uj.tcs.aiplayground.core.layers;

import org.jooq.JSONB;
import org.json.JSONObject;
import pl.edu.uj.tcs.aiplayground.core.ComputationalGraph;
import pl.edu.uj.tcs.aiplayground.core.Tensor;
import pl.edu.uj.tcs.aiplayground.dto.architecture.LayerConfig;
import pl.edu.uj.tcs.aiplayground.dto.architecture.LayerType;
import pl.edu.uj.tcs.aiplayground.dto.architecture.LinearParams;

import java.util.ArrayList;

public class LinearLayer implements Layer {
    int inputSize, outputSize;
    boolean useBias;
    Tensor matrix;
    Tensor bias;
    ArrayList<Tensor> params;

    public LinearLayer() {
        matrix = Tensor.randomMatrix(1, 1, -1, 1);
        bias = Tensor.randomMatrix(1, 1, -1, 1);
        params = new ArrayList<>();
        params.add(matrix);
        params.add(bias);
    }

    public LinearLayer(int inputSize, int outputSize, boolean useBias) {
        this.inputSize = inputSize;
        this.outputSize = outputSize;
        this.useBias = useBias;
        double bound = Math.sqrt(6.0 / (inputSize + outputSize));
        matrix = Tensor.randomMatrix(outputSize, inputSize, -bound, bound);
        bias = Tensor.randomMatrix(outputSize, 1, -1, 1);
        params = new ArrayList<>();
        params.add(matrix);
        params.add(bias);
    }

    public LinearLayer(LinearParams params) {
        this(params.inputSize(), params.outputSize(), params.isBias());
    }

    public Tensor forward(Tensor input, ComputationalGraph graph) {
        if( useBias) return Tensor.add(Tensor.matMul(matrix,input, graph), bias, graph);
        else return Tensor.matMul(matrix,input, graph);
    }

    public ArrayList<Tensor> getParams() {
        return params;
    }

    @Override
    public LayerConfig toConfig() {
        return new LayerConfig(LayerType.LINEAR, new LinearParams(inputSize, outputSize, useBias));
    }
}
