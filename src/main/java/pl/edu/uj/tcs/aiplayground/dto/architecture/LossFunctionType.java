package pl.edu.uj.tcs.aiplayground.dto.architecture;

import pl.edu.uj.tcs.aiplayground.core.loss.BCE;
import pl.edu.uj.tcs.aiplayground.core.loss.CrossEntropy;
import pl.edu.uj.tcs.aiplayground.core.loss.LossFunc;
import pl.edu.uj.tcs.aiplayground.core.loss.MSE;

public enum LossFunctionType {
    MSE("Mean Squared Error", "Mean Squared Error") {
        @Override
        public LossFunc create() {
            return new MSE();
        }
    },
    BCE("Binary Cross-Entropy", "Binary Cross Entropy") {
        @Override
        public LossFunc create() {
            return new BCE();
        }
    },
    CrossEntropy("CE", "CrossEntropy")
    {
        @Override
                public LossFunc create() {return new CrossEntropy();}
    };

    private final String dbKey;
    private final String displayName;

    LossFunctionType(String dbKey, String displayName) {
        this.dbKey = dbKey;
        this.displayName = displayName;
    }

    public static LossFunctionType fromKey(String key) {
        for (LossFunctionType type : values())
            if (type.dbKey.equalsIgnoreCase(key))
                return type;
        throw new IllegalArgumentException("Unknown loss function key: " + key);
    }

    public String getDbKey() {
        return dbKey;
    }

    public abstract LossFunc create();

    @Override
    public String toString() {
        return displayName;
    }
}
