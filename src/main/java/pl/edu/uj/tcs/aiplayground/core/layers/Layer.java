package pl.edu.uj.tcs.aiplayground.core.layers;

import pl.edu.uj.tcs.aiplayground.core.ComputationalGraph;
import pl.edu.uj.tcs.aiplayground.core.Tensor;

import java.util.ArrayList;

public interface Layer {
    public Tensor forward(Tensor input, ComputationalGraph graph);

    public ArrayList<Tensor> getParams();

    public String toJson();
}
