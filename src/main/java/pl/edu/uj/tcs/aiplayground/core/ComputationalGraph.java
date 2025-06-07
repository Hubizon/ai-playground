package pl.edu.uj.tcs.aiplayground.core;

import java.util.ArrayList;

public class ComputationalGraph {

    ArrayList<CompGraphNode> nodes = new ArrayList<>();

    public void addNode(Tensor result, ArrayList<Tensor> components, TensorOperator operation, ArrayList<Object> params) {
        nodes.add(new CompGraphNode(result, components, operation,params));
    }

    public void propagate() {
        for (int i = nodes.size() - 1; i >= 0; i--) {
            nodes.get(i).propagateGradient();
        }
    }

    public void clear() {
        nodes.clear();
    }

    private static class CompGraphNode {
        Tensor result;
        ArrayList<Tensor> components;
        TensorOperator operation;
        ArrayList<Object> params;

        public CompGraphNode(Tensor result, ArrayList<Tensor> components, TensorOperator operation, ArrayList<Object> params) {
            this.result = result;
            this.components = components;
            this.operation = operation;
            this.params = params;
        }

        public void propagateGradient() {
            if (operation.equals(TensorOperator.ADD)) {
                for (Tensor component : components) {
                    for (int i = 0; i < component.rows; i++) {
                        for (int j = 0; j < component.cols; j++) {
                            component.gradient[i][j] += result.gradient[i][j];
                        }
                    }
                }
            } else if (operation.equals(TensorOperator.MULTIPLY)) {
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
            } else if (operation.equals(TensorOperator.SUMROWS)) {
                Tensor base = components.getFirst();
                for (int i = 0; i < base.rows; i++) {
                    for (int j = 0; j < base.cols; j++) {
                        base.gradient[i][j] += result.gradient[0][j];
                    }
                }
            } else if (operation.equals(TensorOperator.SUMCOLS)) {
                Tensor base = components.getFirst();
                for (int i = 0; i < base.rows; i++) {
                    for (int j = 0; j < base.cols; j++) {
                        base.gradient[i][j] += result.gradient[i][0];
                    }
                }
            } else if (operation.equals(TensorOperator.RELU)) {
                Tensor input = components.getFirst();
                for (int i = 0; i < input.rows; i++) {
                    for (int j = 0; j < input.cols; j++) {
                        if (input.data[i][j] > 0) {
                            input.gradient[i][j] += result.gradient[i][j];
                        } else {
                            input.gradient[i][j] = 0;
                        }
                    }
                }
            } else if (operation.equals(TensorOperator.MATMUL)) {
                Tensor a = components.get(0);
                Tensor b = components.get(1);

                Tensor gradA = Tensor.zerosLike(a);
                for (int i = 0; i < a.rows; i++) {
                    for (int k = 0; k < a.cols; k++) {
                        for (int j = 0; j < b.cols; j++) {
                            gradA.gradient[i][k] += result.gradient[i][j] * b.data[k][j];
                        }
                    }
                }

                Tensor gradB = Tensor.zerosLike(b);
                for (int k = 0; k < a.cols; k++) {
                    for (int j = 0; j < b.cols; j++) {
                        for (int i = 0; i < a.rows; i++) {
                            gradB.gradient[k][j] += a.data[i][k] * result.gradient[i][j];
                        }
                    }
                }

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
            } else if (operation.equals(TensorOperator.SIGMOID)) {
                Tensor input = components.getFirst();
                for (int i = 0; i < input.rows; i++) {
                    for (int j = 0; j < input.cols; j++) {
                        double sigmoidValue = result.data[i][j];
                        sigmoidValue = Math.min(1 - 1e-5, Math.max(1e-5, sigmoidValue));
                        double derivative = sigmoidValue * (1 - sigmoidValue);
                        input.gradient[i][j] += result.gradient[i][j] * derivative;
                    }
                }
            } else if (operation.equals(TensorOperator.SOFTMAX)) {
                Tensor input = components.getFirst();
                for (int i = 0; i < input.cols; i++) {
                    for (int j = 0; j < input.rows; j++) {
                        double grad = 0;
                        for (int k = 0; k < input.rows; k++) {
                            double s_j = result.data[j][i];
                            double s_k = result.data[k][i];
                            double delta = (j == k) ? 1.0 : 0.0;
                            grad += result.gradient[k][i] * s_j * (delta - s_k);
                        }
                        input.gradient[j][i] += grad;
                    }
                }
            } else if (operation.equals(TensorOperator.LEAKYRELU)) {
                Tensor input = components.getFirst();
                double alpha;
                alpha = (double) params.getFirst();
                for (int i = 0; i < input.rows; i++) {
                    for (int j = 0; j < input.cols; j++) {
                        if (input.data[i][j] > 0) {
                            input.gradient[i][j] += result.gradient[i][j];
                        } else {
                            input.gradient[i][j] += result.gradient[i][j] * alpha;
                        }
                    }
                }
            } else if (operation.equals(TensorOperator.TANH)) {
                Tensor input = components.getFirst();
                for (int i = 0; i < input.rows; i++) {
                    for (int j = 0; j < input.cols; j++) {
                        double tanhValue = result.data[i][j];
                        double derivative = 1.0 - (tanhValue * tanhValue);
                        input.gradient[i][j] += result.gradient[i][j] * derivative;
                    }
                }
            } else if (operation.equals(TensorOperator.GELU)) {
                Tensor input = components.getFirst();
                final double SQRT_2_OVER_PI = Math.sqrt(2.0 / Math.PI);
                final double CONST_0_044715 = 0.044715;

                for (int i = 0; i < input.rows; i++) {
                    for (int j = 0; j < input.cols; j++) {
                        double x = input.data[i][j];
                        double x_cubed = x * x * x;
                        double inner_term = x + CONST_0_044715 * x_cubed;
                        double tanh_arg = SQRT_2_OVER_PI * inner_term;
                        double tanh_val = Math.tanh(tanh_arg);
                        double derivative_inner_term = 1.0 + 3.0 * CONST_0_044715 * (x * x);
                        double sech2_val = 1.0 - (tanh_val * tanh_val);
                        double derivative_tanh_component = sech2_val * SQRT_2_OVER_PI * derivative_inner_term;
                        double derivative_gelu = 0.5 * (1.0 + tanh_val) + 0.5 * x * derivative_tanh_component;
                        input.gradient[i][j] += result.gradient[i][j] * derivative_gelu;
                    }
                }
            } else if (operation.equals(TensorOperator.DROPOUT)) {
                Tensor input = components.getFirst();
                for (int i = 0; i < input.rows; i++) {
                    for (int j = 0; j < input.cols; j++) {
                        if (input.data[i][j] != 0) {
                            input.gradient[i][j] += result.gradient[i][j];
                        }
                    }
                }
            }
        }
    }
}
