package org.example.aiplayground.core.layers;

import org.example.aiplayground.core.ComputationalGraph;
import org.example.aiplayground.core.Tensor;

import java.util.ArrayList;

public class LinearLayer implements Layer{
    Tensor matrix;
    Tensor bias;
    ArrayList<Tensor> params;
    public LinearLayer(int inputSize, int outputSize, boolean useBias) {
        matrix = Tensor.randomMatrix(outputSize,inputSize,-1,1);
        bias = Tensor.randomMatrix(outputSize,1,-1,1);
        params = new ArrayList<>();
        params.add(matrix);
        params.add(bias);
    }
    public Tensor forward(Tensor input, ComputationalGraph graph) {
        return Tensor.add(Tensor.matMul(matrix,input,graph),bias,graph);
    }
    public ArrayList<Tensor> getParams() {
        return params;
    }
}
