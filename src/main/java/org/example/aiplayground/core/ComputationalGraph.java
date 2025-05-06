package org.example.aiplayground.core;

import java.util.ArrayList;

public class ComputationalGraph {
    public static double[] matmul(double[] a, ArrayList<Integer> shapeA, double[] b, ArrayList<Integer> shapeB) {
        int aRows = shapeA.get(0);
        int aCols = shapeA.get(1);
        int bRows = shapeB.get(0);
        int bCols = shapeB.get(1);

        if (aCols != bRows) {
            throw new IllegalArgumentException("Matrix dimensions do not align: " +
                    aCols + " (A columns) vs " + bRows + " (B rows).");
        }

        double[] result = new double[aRows * bCols];

        for (int i = 0; i < aRows; i++) {
            for (int j = 0; j < bCols; j++) {
                double sum = 0.0;
                for (int k = 0; k < aCols; k++) {
                    sum += a[i * aCols + k] * b[k * bCols + j];
                }
                result[i * bCols + j] = sum;
            }
        }

        return result;
    }
    private static class CompGraphNode{
        Tensor result;
        ArrayList<Tensor> components;
        String operation;
        public CompGraphNode(Tensor result, ArrayList<Tensor> components, String operation) {
            this.result = result;
            this.components = components;
            this.operation = operation;
        }
        public void propagateGradient() {
            if(operation.equals("+")) {
                for(Tensor component : components) {
                    for(int i=0;i<component.gradient.length;i++) {
                        component.gradient[i] += result.gradient[i]/components.size();
                    }
                }
            }
            else if (operation.equals("*")) {
                int length = result.data.length;
                for (int j = 0; j < components.size(); j++) {
                    Tensor target = components.get(j);
                    for (int i = 0; i < length; i++) {
                        double partial = 1.0;
                        for (int k = 0; k < components.size(); k++) {
                            if (k != j) {
                                partial *= components.get(k).data[i];
                            }
                        }
                        target.gradient[i] += result.gradient[i] * partial;
                    }
                }
            }
            else if (operation.equals("Sum0")) {
                Tensor base = components.get(0);
                int howManyTimes = base.data.length/result.data.length;
                for (int i = 0; i < result.data.length; i++) {
                    for (int j = 0; j < howManyTimes; j++) {
                        base.gradient[i+j*result.data.length] += result.gradient[i]/howManyTimes;
                    }
                }
            }
            else if(operation.equals("matMul")) {
                Tensor a = components.get(0);
                Tensor b = components.get(1);
                Tensor c = result;
                a.gradient = matmul(c.gradient, c.shape, b.transpose().data, b.shape);
                b.gradient = matmul(a.transpose().data, a.transpose().shape, c.gradient, c.shape);
            }
        }
    }

    ArrayList<CompGraphNode> nodes = new ArrayList<CompGraphNode>();

    public void addNode(Tensor result, ArrayList<Tensor> components, String operation)
    {
        nodes.add(new CompGraphNode(result, components, operation));
    }
    public void propagate() {
        for(int i = nodes.size() - 1; i >= 0; i--) {
            nodes.get(i).propagateGradient();
        }
    }
    public void clear()
    {
        nodes.clear();
    }
}
