package pl.edu.uj.tcs.aiplayground.service;

import pl.edu.uj.tcs.aiplayground.dto.TrainingDto;
import pl.edu.uj.tcs.aiplayground.dto.TrainingMetricDto;
import pl.edu.uj.tcs.aiplayground.exception.DatabaseException;
import pl.edu.uj.tcs.aiplayground.repository.ITrainingRepository;

import java.util.List;
import java.util.UUID;

public class TrainingService {
    private final ITrainingRepository trainingRepository;

    public TrainingService(ITrainingRepository trainingRepository) {
        this.trainingRepository = trainingRepository;
    }

    public UUID addNewTraining(TrainingDto trainingDto) throws DatabaseException {
        try {
            return trainingRepository.insertTraining(trainingDto);
        } catch (Exception e) {
            throw new DatabaseException(e);
        }
    }

    public void updateTrainingStatus(UUID trainingId, String status) throws DatabaseException {
        try {
            trainingRepository.updateTrainingStatus(trainingId, status);
        } catch (Exception e) {
            throw new DatabaseException(e);
        }
    }

    public void addNewTrainingMetric(UUID trainingId, TrainingMetricDto trainingMetricDto) throws DatabaseException {
        try {
            trainingRepository.insertTrainingMetric(trainingId, trainingMetricDto);
        } catch (Exception e) {
            throw new DatabaseException(e);
        }
    }

    public List<TrainingMetricDto> getTrainingMetrics(UUID trainingId) throws DatabaseException {
        try {
            return trainingRepository.getTrainingMetrics(trainingId);
        } catch (Exception e) {
            throw new DatabaseException(e);
        }
    }
}
