package pl.edu.uj.tcs.aiplayground.dto.architecture;

import pl.edu.uj.tcs.aiplayground.core.loss.BCE;
import pl.edu.uj.tcs.aiplayground.core.loss.CrossEntropy;
import pl.edu.uj.tcs.aiplayground.core.loss.LossFunc;
import pl.edu.uj.tcs.aiplayground.core.loss.MSE;

public enum LossFunctionType {
    MSE("Mean Squared Error") {
        @Override
        public LossFunc create() {
            return new MSE();
        }
    },
    BCE("Binary Cross Entropy") {
        @Override
        public LossFunc create() {
            return new BCE();
        }
    },
    CrossEntropy("Cross Entropy") {
        @Override
        public LossFunc create() {
            return new CrossEntropy();
        }
    };

    private final String displayName;

    LossFunctionType(String displayName) {
        this.displayName = displayName;
    }

    public abstract LossFunc create();

    @Override
    public String toString() {
        return displayName;
    }
}
