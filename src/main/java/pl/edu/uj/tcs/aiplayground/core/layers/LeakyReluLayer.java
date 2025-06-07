package pl.edu.uj.tcs.aiplayground.core.layers;

import pl.edu.uj.tcs.aiplayground.core.ComputationalGraph;
import pl.edu.uj.tcs.aiplayground.core.Tensor;
import pl.edu.uj.tcs.aiplayground.dto.architecture.LayerConfig;
import pl.edu.uj.tcs.aiplayground.dto.architecture.LayerType;
import pl.edu.uj.tcs.aiplayground.dto.architecture.LeakyReLUParams;

import java.math.BigDecimal;
import java.util.ArrayList;

public class LeakyReluLayer implements Layer {

    private final BigDecimal alpha;

    public LeakyReluLayer(BigDecimal alpha) {
        this.alpha = alpha;
    }

    public LeakyReluLayer(LeakyReLUParams params) {
        alpha = params.alpha();
    }

    @Override
    public Tensor forward(Tensor input, ComputationalGraph graph) {
        return Tensor.leakyRelu(input, graph, alpha.doubleValue());
    }

    @Override
    public ArrayList<Tensor> getParams() {
        return new ArrayList<>();
    }

    @Override
    public LayerConfig toConfig() {
        return new LayerConfig(LayerType.LEAKYRELU, new LeakyReLUParams(alpha));
    }
}