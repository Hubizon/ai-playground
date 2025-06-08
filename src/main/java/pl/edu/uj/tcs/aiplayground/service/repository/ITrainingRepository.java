package pl.edu.uj.tcs.aiplayground.service.repository;

import pl.edu.uj.tcs.aiplayground.dto.StatusType;
import pl.edu.uj.tcs.aiplayground.dto.TrainingDto;
import pl.edu.uj.tcs.aiplayground.dto.TrainingMetricDto;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public interface ITrainingRepository {
    List<TrainingMetricDto> getTrainingMetrics(UUID trainingId);

    void insertTrainingMetric(UUID trainingId, TrainingMetricDto record);

    UUID insertTraining(TrainingDto trainingsRecord);

    void updateTrainingStatus(UUID trainingId, StatusType status);

    void finishTraining(UUID trainingId);

    String getDatasetPathByName(String dbName);

    String shareTraining(UUID trainingId) throws SQLException;
}
