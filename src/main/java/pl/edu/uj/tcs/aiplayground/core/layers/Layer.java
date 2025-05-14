package pl.edu.uj.tcs.aiplayground.core.layers;

import pl.edu.uj.tcs.aiplayground.core.ComputationalGraph;
import pl.edu.uj.tcs.aiplayground.core.Tensor;
import pl.edu.uj.tcs.aiplayground.dto.architecture.LayerConfig;

import java.util.ArrayList;

public interface Layer {
    Tensor forward(Tensor input, ComputationalGraph graph);

    ArrayList<Tensor> getParams();

    String toJson();

    LayerConfig toConfig();
}
