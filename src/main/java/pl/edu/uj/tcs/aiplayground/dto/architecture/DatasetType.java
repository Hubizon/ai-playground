package pl.edu.uj.tcs.aiplayground.dto.architecture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.uj.tcs.aiplayground.core.Dataset;
import pl.edu.uj.tcs.aiplayground.exception.DatabaseException;
import pl.edu.uj.tcs.aiplayground.service.TrainingService;

public enum DatasetType {
    IRIS("Iris", "Iris dataset"),
    MOONS("Moons", "Moons dataset"),
    BLOBS("Blobs", "Blobs dataset"),
    CIRCLES("Circles", "Circles dataset"),
    MNIST("MNIST", "MNIST dataset");

    private static final Logger logger = LoggerFactory.getLogger(DatasetType.class);
    private final String dbKey;
    private final String displayName;
    private TrainingService service;


    DatasetType(String dbKey, String displayName) {
        this.dbKey = dbKey;
        this.displayName = displayName;
    }

    public static DatasetType fromKey(String key) {
        for (DatasetType type : values())
            if (type.dbKey.equalsIgnoreCase(key))
                return type;
        throw new IllegalArgumentException("Unknown dataset key: " + key);
    }

    public String getDbKey() {
        return dbKey;
    }

    @Override
    public String toString() {
        return displayName;
    }

    public void setTrainingService(TrainingService service) {
        this.service = service;
    }

    public Dataset create() {
        if (service == null) {
            logger.error("Service not set up");
            return null;
        }

        try {
            String path = service.getDatasetPathByName(dbKey);
            return new Dataset(path);
        } catch (DatabaseException e) {
            logger.error("Failed to load dataset, error={}", e.getMessage(), e);
            return null;
        }
    }
}
