package org.example.aiplayground.core;

import java.util.ArrayList;

public class Optimizers {
    public static class SGDOptimizer {
        ArrayList<Tensor> params;
        double learningRate;
        public SGDOptimizer(ArrayList<Tensor> params,double learningRate) {
            this.params = params;
            this.learningRate = learningRate;
        }
        public void optimize() {
            for (Tensor param : params) {
                param.data-=param.gradient*learningRate;
            }
        }
        public void zeroGradient() {
            for (Tensor param : params) {
                param.gradient=0;
            }
        }
    }
}
