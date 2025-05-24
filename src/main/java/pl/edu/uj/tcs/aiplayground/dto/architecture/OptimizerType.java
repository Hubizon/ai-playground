package pl.edu.uj.tcs.aiplayground.dto.architecture;

import pl.edu.uj.tcs.aiplayground.core.Tensor;
import pl.edu.uj.tcs.aiplayground.core.optim.AdamOptimizer;
import pl.edu.uj.tcs.aiplayground.core.optim.Optimizer;
import pl.edu.uj.tcs.aiplayground.core.optim.SGDOptimizer;

import java.util.ArrayList;

public enum OptimizerType {
    SGD("SGD", "Stochastic Gradient Descent") {
        @Override
        public Optimizer create(ArrayList<Tensor> params, double learningRate) {
            return new SGDOptimizer(params, learningRate);
        }
    },
    ADAM("Adam", "Adam Optimizer") {
        @Override
        public Optimizer create(ArrayList<Tensor> params, double learningRate) {
            return new AdamOptimizer(params, learningRate);
        }
    };

    private final String dbKey;
    private final String displayName;

    OptimizerType(String dbKey, String displayName) {
        this.dbKey = dbKey;
        this.displayName = displayName;
    }

    public static OptimizerType fromKey(String key) {
        for (OptimizerType type : values())
            if (type.dbKey.equalsIgnoreCase(key))
                return type;
        throw new IllegalArgumentException("Unknown optimizer key: " + key);
    }

    public String getDbKey() {
        return dbKey;
    }

    public abstract Optimizer create(ArrayList<Tensor> params, double learningRate);

    @Override
    public String toString() {
        return displayName;
    }
}
