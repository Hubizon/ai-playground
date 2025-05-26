package pl.edu.uj.tcs.aiplayground.core.examples;

import pl.edu.uj.tcs.aiplayground.core.ComputationalGraph;
import pl.edu.uj.tcs.aiplayground.core.NeuralNet;
import pl.edu.uj.tcs.aiplayground.core.Tensor;
import pl.edu.uj.tcs.aiplayground.core.layers.LinearLayer;
import pl.edu.uj.tcs.aiplayground.core.layers.SigmoidLayer;
import pl.edu.uj.tcs.aiplayground.core.loss.BCE;
import pl.edu.uj.tcs.aiplayground.core.optim.SGDOptimizer;

import java.util.ArrayList;
import java.util.Random;

public class NNUsingModel {
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
                label_ = 0;
            }
            Tensor input = new Tensor(new double[][]{{x}, {y}}, 2, 1);
            Tensor output = new Tensor(new double[][]{{label_}}, 1, 1);
            X.add(input);
            Y.add(output);
        }
        Tensor pred;
        ComputationalGraph graph = new ComputationalGraph();
        NeuralNet net = new NeuralNet();
        net.layers.add(new LinearLayer(2, 12, true));
        net.layers.add(new SigmoidLayer());
        net.layers.add(new LinearLayer(12, 6, true));
        net.layers.add(new SigmoidLayer());
        net.layers.add(new LinearLayer(6, 1, true));
        SGDOptimizer optimizer = new SGDOptimizer(net.getParams(), 0.1);
        BCE bce = new BCE();
        for (int epoch = 0; epoch < 10; epoch++) {
            optimizer.zeroGradient();
            int sum = 0;
            System.out.println("EVAL MODE: " + epoch);
            for (int i = 0; i < numPoints; i++) {
                pred = net.forward(X.get(i), graph);
                if ((pred.data[0][0] > 0.5) == (Y.get(i).data[0][0] == 1)) {
                    sum++;
                }
                graph.clear();
            }
            System.out.println(((double) sum) / numPoints);
            optimizer.zeroGradient();
            for (int i = 0; i < numPoints; i++) {
                pred = net.forward(X.get(i), graph);
                bce.loss(pred, Y.get(i));
                if (i % 32 == 0) {
                    graph.propagate();
                    optimizer.optimize();
                    graph.clear();
                    optimizer.zeroGradient();
                }
            }
        }

    }
}
