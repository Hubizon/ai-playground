package pl.edu.uj.tcs.aiplayground.core.examples;

import pl.edu.uj.tcs.aiplayground.core.ComputationalGraph;
import pl.edu.uj.tcs.aiplayground.core.Tensor;
import pl.edu.uj.tcs.aiplayground.core.loss.MSE;
import pl.edu.uj.tcs.aiplayground.core.optim.AdamOptimizer;

import java.util.ArrayList;

public class LinearRegression {
    public static void main(String[] args) {
        ArrayList<Tensor> X = new ArrayList<>();
        ArrayList<Tensor> Y = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            for (int j = 0; j < 1000; j++) {

                X.add(new Tensor(new double[][]{{(double) i / 1000, (double) j / 1000}}, 1, 2));
                Y.add(new Tensor((double) ((-20) * i + 11 * j + 2137) / 1000));
            }
        }
        Tensor a, b, c = new Tensor(0), d = new Tensor(0);

        ComputationalGraph graph = new ComputationalGraph();
        a = Tensor.randomMatrix(1, 2, -1, 1);
        b = Tensor.randomMatrix(1, 1, -1, 1);
        ArrayList<Tensor> params = new ArrayList<>();
        params.add(a);
        params.add(b);
        AdamOptimizer optimizer = new AdamOptimizer(params, 5);
        MSE mse = new MSE();
        for (int epoch = 0; epoch < 1000; epoch++) {
            optimizer.zeroGradient();
            for (int i = 1; i < 1000000; i++) {
                d = Tensor.multiply(a, X.get(i), graph).sumCols(graph);
                c = Tensor.add(b, d, graph);
                mse.loss(c, Y.get(i));
            }
            graph.propagate();
            optimizer.optimize();
            if (epoch % 10 == 0) {
                System.out.println(epoch);
                System.out.println(c.data[0][0]);
                for (int i = 0; i < 2; i++) {
                    System.out.println(i + " a: " + a.data[0][i] + " " + a.gradient[0][i]);
                }
                for (int i = 0; i < b.data.length; i++) {
                    System.out.println(i + " b: " + b.data[0][i] + " " + b.gradient[0][i]);
                }
            }
            graph.clear();
        }

    }
}
