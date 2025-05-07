package org.example.aiplayground.core;

import java.util.ArrayList;
import java.util.Random;

public class FirstNN {
    public static void main(String[] args) {
        ArrayList<Tensor> X = new ArrayList<>();
        ArrayList<Tensor> Y = new ArrayList<>();
        Random rand = new Random();

        int numPoints = 100000;
        double r1 = 0.5;


        for (int i = 0; i < numPoints; i++) {

            double centerX = 0.5;
            double centerY = 0.5;
            double radiusBlob = 0.2;
            double radiusInnerRing = 0.3;
            double radiusOuterRing = 0.4;

            boolean isBlob = rand.nextBoolean();
            int label_;
            double x, y;

            if (isBlob) {
                double angle = 2 * Math.PI * rand.nextDouble();
                double radius = radiusBlob * Math.sqrt(rand.nextDouble());
                x = centerX + radius * Math.cos(angle);
                y = centerY + radius * Math.sin(angle);
                label_ = 1;
            } else {
                double angle = 2 * Math.PI * rand.nextDouble();
                double ringRadius = Math.sqrt(radiusInnerRing * radiusInnerRing + (radiusOuterRing * radiusOuterRing - radiusInnerRing * radiusInnerRing) * rand.nextDouble());
                x = centerX + ringRadius * Math.cos(angle);
                y = centerY + ringRadius * Math.sin(angle);
                label_ = -1;
            }


            // Create input tensor
            Tensor input = new Tensor(new double[][]{{x}, {y}}, 2, 1);
            // Create output tensor (label)
            Tensor output = new Tensor(new double[][]{{label_}}, 1, 1);

            X.add(input);
            Y.add(output);
        }
        Tensor L1,L2, B1, B2,c = new Tensor(0),d,e,f,g,h;

        ComputationalGraph graph = new ComputationalGraph();
        L1 = Tensor.randomMatrix(4,2,-1,1);
        B1 = Tensor.randomMatrix(4,1,-1,1);
        L2 = Tensor.randomMatrix(1,4,-1,1);
        B2 = Tensor.randomMatrix(1,1,-1,1);
        ArrayList<Tensor> params = new ArrayList<>();
        params.add(L1);
        params.add(L2);
        params.add(B1);
        params.add(B2);
        Optimizers.AdamOptimizer optimizer = new Optimizers.AdamOptimizer(params,0.1);
        for (int epoch=0;epoch<10000;epoch++)
        {   optimizer.zeroGradient();


            if(epoch%1==0)
            {
                int sum=0;
                System.out.println("EVAL MODE: "+epoch);
                for(int i=0;i<numPoints;i++)
                {
                    d = Tensor.matMul(L1,X.get(i), graph);
                    f = Tensor.add(d,B1, graph);
                    e= Tensor.Sigmoid(f, graph);
                    g= Tensor.matMul(L2, e, graph);
                    c = Tensor.add(g,B2, graph);

                    //System.out.println(c.data[0][0] + " "+ Y.get(i).data[0][0]);
                    if(c.data[0][0]*Y.get(i).data[0][0]>0)
                    {
                        sum++;
                    }
                }
                System.out.println(((double) sum)/numPoints);
            }

            optimizer.zeroGradient();
            for(int i=0;i<numPoints;i++)
            {
                d = Tensor.matMul(L1,X.get(i), graph);
                f = Tensor.add(d,B1, graph);
                e= Tensor.Sigmoid(f, graph);
                g= Tensor.matMul(L2, e, graph);
                f = Tensor.add(g,B2, graph);
                c = Tensor.Sigmoid(f, graph);
                LossFunctions.BCE(c,Y.get(i));
                if(i%32==0)
                {
                    graph.propagate();
                    optimizer.optimize();
                    graph.clear();
                    optimizer.zeroGradient();
                }
            }


        }

    }
}
