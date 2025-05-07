package org.example.aiplayground.core;

import java.util.ArrayList;
import java.util.List;

public class XORnet {
    public static void main(String[] args) {
        ArrayList<Tensor> X = new ArrayList<>();
        ArrayList<Tensor> Y = new ArrayList<>();

        X.add(new Tensor(new double[] {1, 1}, new ArrayList<>(List.of(2))));
        Y.add(new Tensor(0));
        X.add(new Tensor(new double[] {1, 0}, new ArrayList<>(List.of(2))));
        Y.add(new Tensor(1));
        X.add(new Tensor(new double[] {0, 1}, new ArrayList<>(List.of(2))));
        Y.add(new Tensor(1));
        X.add(new Tensor(new double[] {0, 0}, new ArrayList<>(List.of(2))));
        Y.add(new Tensor(0));

        Tensor a,b,c = new Tensor(0), d;
        ComputationalGraph graph = new ComputationalGraph();
        a = Tensor.randomMatrix(new ArrayList<>(List.of(10,2)),-1,1);
        b = Tensor.randomMatrix(new ArrayList<>(List.of(1,10)),-1,1);

        ArrayList<Tensor> params = new ArrayList<>();
        params.add(a);
        params.add(b);
        Optimizers.AdamOptimizer optimizer = new Optimizers.AdamOptimizer(params,5);

        for (int epoch=0;epoch<1000;epoch++)
        {   optimizer.zeroGradient();
            double total_loss=0;
            for(int i=1;i<4;i++)
            {
                total_loss+= LossFunctions.MSE(Tensor.Relu( Tensor.vecTimesMat(b,
                        Tensor.Relu( Tensor.vecTimesMat(a,X.get(i), graph), graph),graph),graph),Y.get(i));
            }
            System.out.println(total_loss);
            graph.propagate();
            optimizer.optimize();
            graph.clear();
        }

    }
}
