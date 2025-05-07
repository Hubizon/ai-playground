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
                    for(int i=0;i<component.rows;i++) {
                        for(int j=0;j<component.cols;j++) {
                            component.gradient[i][j] += result.gradient[i][j];
                        }
                    }
                }
            }

            else if (operation.equals("*")) {
                for (Tensor component : components) {
                    for (int i = 0; i < component.rows; i++) {
                        for (int j = 0; j < component.cols; j++) {
                            double partial = 1.0;
                            for (Tensor other : components) {
                                if (other != component) {
                                    partial *= other.data[i][j];
                                }
                            }
                            component.gradient[i][j] += result.gradient[i][j] * partial;
                        }
                    }
                }
            }

            else if (operation.equals("sumRows")) {
                Tensor base = components.get(0);
                for (int i = 0; i < base.rows; i++) {
                    for (int j = 0; j < base.cols; j++) {
                        base.gradient[i][j] += result.gradient[0][j];
                    }
                }
            }

            else if (operation.equals("sumCols")) {
                Tensor base = components.get(0);
                for (int i = 0; i < base.rows; i++) {
                    for (int j = 0; j < base.cols; j++) {
                        base.gradient[i][j] += result.gradient[i][0];
                    }
                }
            }

            else if(operation.equals("relu")) {
                Tensor input = components.get(0);
                for (int i = 0; i < input.rows; i++) {
                    for (int j = 0; j < input.cols; j++) {
                        if (input.data[i][j] > 0) {
                            input.gradient[i][j] += result.gradient[i][j];
                        }
                        else {input.gradient[i][j] = 0;}

                    }

                }
            }

            else if (operation.equals("matMul")) {
                Tensor a = components.get(0);
                Tensor b = components.get(1);

                // Gradient w.r.t. A: grad(C) * B^T
                Tensor gradA = Tensor.zerosLike(a);
                for (int i = 0; i < a.rows; i++) {
                    for (int k = 0; k < a.cols; k++) {
                        for (int j = 0; j < b.cols; j++) {
                            gradA.gradient[i][k] += result.gradient[i][j] * b.data[k][j];
                        }
                    }
                }

                // Gradient w.r.t. B: A^T * grad(C)
                Tensor gradB = Tensor.zerosLike(b);
                for (int k = 0; k < a.cols; k++) {
                    for (int j = 0; j < b.cols; j++) {
                        for (int i = 0; i < a.rows; i++) {
                            gradB.gradient[k][j] += a.data[i][k] * result.gradient[i][j];
                        }
                    }
                }

                // Accumulate gradients to the original tensors
                for (int i = 0; i < a.rows; i++) {
                    for (int k = 0; k < a.cols; k++) {
                        a.gradient[i][k] += gradA.gradient[i][k];
                    }
                }
                for (int k = 0; k < b.rows; k++) {
                    for (int j = 0; j < b.cols; j++) {
                        b.gradient[k][j] += gradB.gradient[k][j];
                    }
                }
            }
            else if (operation.equals("sigmoid")) {
                Tensor input = components.get(0);
                for (int i = 0; i < input.rows; i++) {
                    for (int j = 0; j < input.cols; j++) {
                        double sigmoidValue = result.data[i][j];
                        double derivative = sigmoidValue * (1 - sigmoidValue);
                        input.gradient[i][j] += result.gradient[i][j] * derivative;
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
