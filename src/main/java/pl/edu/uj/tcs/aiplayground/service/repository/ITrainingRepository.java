package pl.edu.uj.tcs.aiplayground.service.repository;

import pl.edu.uj.tcs.aiplayground.dto.StatusType;
import pl.edu.uj.tcs.aiplayground.dto.TrainingDto;
import pl.edu.uj.tcs.aiplayground.dto.TrainingMetricDto;

import java.util.List;
import java.util.UUID;

public interface ITrainingRepository {
    List<TrainingMetricDto> getTrainingMetrics(UUID trainingId);

    void insertTrainingMetric(UUID trainingId, TrainingMetricDto record);

    UUID insertTraining(TrainingDto trainingsRecord);

    void updateTrainingStatus(UUID trainingId, StatusType status);

    String getDatasetPathByName(String dbName);

    void shareTraining(UUID trainingId, Double accuracy, Double loss);
}
