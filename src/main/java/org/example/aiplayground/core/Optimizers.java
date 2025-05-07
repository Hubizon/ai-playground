package org.example.aiplayground.core;

import java.util.ArrayList;
import java.util.Arrays;

public class Optimizers {
    public static class SGDOptimizer {
        ArrayList<Tensor> params;
        double learningRate;

        public SGDOptimizer(ArrayList<Tensor> params, double learningRate) {
            this.params = params;
            this.learningRate = learningRate;
        }

        public void optimize() {
            for (Tensor param : params) {
                for (int i = 0; i < param.rows; i++) {
                    for(int j=0;j<param.cols;j++)
                    {
                        param.data[i][j] -= param.gradient[i][j] * learningRate;
                    }
                }

            }
        }

        public void zeroGradient() {
            for (Tensor param : params) {
                for(int i = 0; i < param.rows; i++) {
                    Arrays.fill(param.gradient[i], 0.0);
                }
            }
        }
    }

    public static class AdamOptimizer {
        ArrayList<Tensor> params;
        double learningRate;
        double beta1 = 0.9;
        double beta2 = 0.999;
        double epsilon = 1e-8;
        int timestep = 0;

        ArrayList<double[][]> m;
        ArrayList<double[][]> v;

        public AdamOptimizer(ArrayList<Tensor> params, double learningRate) {
            this.params = params;
            this.learningRate = learningRate;
            this.m = new ArrayList<>();
            this.v = new ArrayList<>();

            // Initialize moment vectors to match param shapes
            for (Tensor param : params) {
                m.add(new double[param.rows][param.cols]);
                v.add(new double[param.rows][param.cols]);
            }
        }

        public void optimize() {
            timestep++;
            for (int i = 0; i < params.size(); i++) {
                Tensor param = params.get(i);
                double[][] grad = param.gradient;
                double[][] m_t = m.get(i);
                double[][] v_t = v.get(i);

                for (int r = 0; r < param.rows; r++) {
                    for (int c = 0; c < param.cols; c++) {
                        // Update biased first moment estimate
                        m_t[r][c] = beta1 * m_t[r][c] + (1 - beta1) * grad[r][c];

                        // Update biased second raw moment estimate
                        v_t[r][c] = beta2 * v_t[r][c] + (1 - beta2) * grad[r][c] * grad[r][c];

                        // Compute bias-corrected first and second moment estimates
                        double mHat = m_t[r][c] / (1 - Math.pow(beta1, timestep));
                        double vHat = v_t[r][c] / (1 - Math.pow(beta2, timestep));

                        // Update parameter
                        param.data[r][c] -= learningRate * mHat / (Math.sqrt(vHat) + epsilon);
                    }
                }
            }
        }

        public void zeroGradient() {
            for (Tensor param : params) {
                for (int r = 0; r < param.rows; r++) {
                    Arrays.fill(param.gradient[r], 0.0);
                }
            }
        }
    }
}