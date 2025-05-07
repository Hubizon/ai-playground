package org.example.aiplayground.core;

import java.util.ArrayList;

public class ComputationalGraph {

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
                a.gradient = Utils.matmul(c.gradient, c.shape, b.transpose().data, b.shape);
                b.gradient = Utils.matmul(a.transpose().data, a.transpose().shape, c.gradient, c.shape);
            }
            else if(operation.equals("relu")) {
                Tensor input = components.get(0);
                int length = result.data.length;
                for (int i = 0; i < length; i++) {
                    if (input.data[i] > 0) {
                        input.gradient[i] += result.gradient[i];
                    }
                }
            }
            else if(operation.equals("MatVec")) {
                Tensor mat = components.get(0);
                Tensor vector = components.get(1);
                Tensor result = this.result;

                int matRows = mat.shape.get(0);
                int matCols = mat.shape.get(1);
                int vecLen = vector.shape.get(0);
                int resultRows = result.shape.get(0);

                if (matCols != vecLen || matRows != resultRows) {
                    throw new IllegalArgumentException("Dimension mismatch in MatVec gradient propagation");
                }

                for (int i = 0; i < matRows; i++) {
                    for (int j = 0; j < matCols; j++) {
                        mat.gradient[i * matCols + j] += result.gradient[i] * vector.data[j];
                    }
                }

                for (int j = 0; j < vecLen; j++) {
                    for (int i = 0; i < resultRows; i++) {
                        vector.gradient[j] += result.gradient[i] * mat.data[i * matCols + j];
                    }
                }
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
