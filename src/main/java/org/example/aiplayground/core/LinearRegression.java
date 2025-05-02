package org.example.aiplayground.core;

import java.util.ArrayList;

public class LinearRegression {
    public static void main(String[] args) {
        ArrayList<Tensor> X = new ArrayList<>();
        ArrayList<Tensor> Y = new ArrayList<>();
        for(int i=0;i<1000;i++)
        {
            X.add(new Tensor(i));
            Y.add(new Tensor(i*100+17));
        }
        Tensor a,b,c;
        ComputationalGraph graph = new ComputationalGraph();
        a = new Tensor( Math.random());
        b = new Tensor( Math.random());
        ArrayList<Tensor> params = new ArrayList<>();
        params.add(a);
        params.add(b);
        Optimizers.SGDOptimizer optimizer = new Optimizers.SGDOptimizer(params,0.1);
        for (int epoch=0;epoch<1000;epoch++)
        {   optimizer.zeroGradient();
            if(epoch%10==0)
            {
                System.out.println("Epoch: "+epoch+" a: "+a.data+" b: "+b.data);

            }
            for(int i=1;i<3;i++)
            {
                c= Tensor.add(Tensor.multiply(a,X.get(i), graph), b, graph);
                c.gradient+= c.data - Y.get(i).data;
            }
            graph.propagate();
            optimizer.optimize();
            graph.clear();
        }

    }
}
