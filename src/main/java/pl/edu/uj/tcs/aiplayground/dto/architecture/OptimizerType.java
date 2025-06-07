package pl.edu.uj.tcs.aiplayground.dto.architecture;

import pl.edu.uj.tcs.aiplayground.core.Tensor;
import pl.edu.uj.tcs.aiplayground.core.optim.*;

import java.util.ArrayList;

public enum OptimizerType {
    SGD("Stochastic Gradient Descent") {
        @Override
        public Optimizer create(ArrayList<Tensor> params, double learningRate) {
            return new SGDOptimizer(params, learningRate);
        }
    },
    ADAM("Adam Optimizer") {
        @Override
        public Optimizer create(ArrayList<Tensor> params, double learningRate) {
            return new AdamOptimizer(params, learningRate);
        }
    },

    ADAGRAD("AdaGrad Optimizer") {
        @Override
        public Optimizer create(ArrayList<Tensor> params, double learningRate) {
            return new AdaGradOptimizer(params, learningRate);
        }
    },

    ADADELTA("AdaDelta Optimizer") {
        @Override
        public Optimizer create(ArrayList<Tensor> params, double learningRate) {
            return new AdaDeltaOptimizer(params, learningRate);
        }
    },

    RMSPROP("RMSProp Optimizer") {
        @Override
        public Optimizer create(ArrayList<Tensor> params, double learningRate) {
            return new RMSPropOptimizer(params, learningRate);
        }
    };

    private final String displayName;

    OptimizerType(String displayName) {
        this.displayName = displayName;
    }

    public abstract Optimizer create(ArrayList<Tensor> params, double learningRate);

    @Override
    public String toString() {
        return displayName;
    }
}
