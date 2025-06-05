package pl.edu.uj.tcs.aiplayground.core.layers;

import pl.edu.uj.tcs.aiplayground.core.ComputationalGraph;
import pl.edu.uj.tcs.aiplayground.core.Tensor;
import pl.edu.uj.tcs.aiplayground.dto.architecture.DropoutParams;
import pl.edu.uj.tcs.aiplayground.dto.architecture.LayerConfig;
import pl.edu.uj.tcs.aiplayground.dto.architecture.LayerType;
import pl.edu.uj.tcs.aiplayground.dto.architecture.LeakyReLUParams;

import java.math.BigDecimal;
import java.util.ArrayList;

public class DropoutLayer implements  Layer{

    private final BigDecimal amount;

    public DropoutLayer(BigDecimal amount) {
        this.amount = amount;
    }

    public DropoutLayer(DropoutParams params) {
        amount = params.amount();
    }

    @Override
    public Tensor forward(Tensor input, ComputationalGraph graph) {
        return Tensor.dropout(input,amount.doubleValue(), graph);
    }

    @Override
    public ArrayList<Tensor> getParams() {
        return new ArrayList<>();
    }

    @Override
    public LayerConfig toConfig() {
        return new LayerConfig(LayerType.DROPOUT, new DropoutParams(amount));
    }

}
