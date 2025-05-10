package pl.edu.uj.tcs.aiplayground.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.uj.tcs.aiplayground.dto.TrainingDto;
import pl.edu.uj.tcs.aiplayground.dto.TrainingMetricDto;
import pl.edu.uj.tcs.aiplayground.exception.DatabaseException;
import pl.edu.uj.tcs.aiplayground.repository.JooqFactory;
import pl.edu.uj.tcs.aiplayground.repository.TrainingRepository;
import pl.edu.uj.tcs.aiplayground.service.TrainingService;

import java.util.UUID;

public class Training {
    private static final Logger logger = LoggerFactory.getLogger(Training.class);
    private final TrainingService trainingService;
    private UUID trainingId = null;

    public Training(TrainingService trainingService, TrainingDto trainingDto) {
        this.trainingService = trainingService;

        try {
            trainingId = trainingService.addNewTraining(trainingDto);
        } catch (DatabaseException e) {
            logger.error("Failed to create training for core={}, error={}", trainingDto, e.getMessage(), e);
        }
    }

    public Training(TrainingDto trainingDto) {
        this(new TrainingService(new TrainingRepository(JooqFactory.getDSLContext())), trainingDto);
    }

    public void addNewTrainingMetric(TrainingMetricDto trainingMetricDto) {
        try {
            trainingService.addNewTrainingMetric(trainingId, trainingMetricDto);
        } catch (DatabaseException e) {
            logger.error("Failed to add training metric for trainingId={}, trainingMetric={}, error={}",
                    trainingId, trainingMetricDto, e.getMessage(), e);
        }
    }

    public void updateTrainingStatus(String status) {
        try {
            trainingService.updateTrainingStatus(trainingId, status);
        } catch (DatabaseException e) {
            logger.error("Failed to update the status for for trainingId={}, status={}, error={}",
                    trainingId, status, e.getMessage(), e);
        }
    }
}
