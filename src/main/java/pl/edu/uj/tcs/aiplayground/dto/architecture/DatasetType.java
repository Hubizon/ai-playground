package pl.edu.uj.tcs.aiplayground.dto.architecture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.uj.tcs.aiplayground.core.Dataset;
import pl.edu.uj.tcs.aiplayground.exception.DatabaseException;
import pl.edu.uj.tcs.aiplayground.service.TrainingService;

public enum DatasetType {
    IRIS("Iris dataset"),
    MOONS("Moons dataset"),
    BLOBS("Blobs dataset"),
    CIRCLES("Circles dataset"),
    MNIST("MNIST dataset");

    private static final Logger logger = LoggerFactory.getLogger(DatasetType.class);
    private final String displayName;
    private TrainingService service;

    DatasetType(String displayName) {
        this.displayName = displayName;
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
            String path = service.getDatasetPathByName(name());
            return new Dataset(path);
        } catch (DatabaseException e) {
            logger.error("Failed to load dataset, error={}", e.getMessage(), e);
            return null;
        }
    }
}
