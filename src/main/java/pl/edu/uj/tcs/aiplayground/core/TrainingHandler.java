package pl.edu.uj.tcs.aiplayground.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.uj.tcs.aiplayground.dto.StatusName;
import pl.edu.uj.tcs.aiplayground.dto.TrainingDto;
import pl.edu.uj.tcs.aiplayground.dto.TrainingMetricDto;
import pl.edu.uj.tcs.aiplayground.exception.DatabaseException;
import pl.edu.uj.tcs.aiplayground.service.TrainingService;
import pl.edu.uj.tcs.aiplayground.service.repository.JooqFactory;
import pl.edu.uj.tcs.aiplayground.service.repository.TrainingRepository;

import java.util.UUID;

public class TrainingHandler {
    private static final Logger logger = LoggerFactory.getLogger(TrainingHandler.class);
    private final TrainingService trainingService;
    private UUID trainingId = null;
    private TrainingMetricDto recentMetric;

    public TrainingHandler(TrainingService trainingService, TrainingDto trainingDto) {
        this.trainingService = trainingService;

        try {
            trainingId = trainingService.addNewTraining(trainingDto);
        } catch (DatabaseException e) {
            logger.error("Failed to create training for core={}, error={}", trainingDto, e.getMessage(), e);
        }
    }

    public TrainingHandler(TrainingDto trainingDto) {
        this(new TrainingService(new TrainingRepository(JooqFactory.getDSLContext())), trainingDto);
    }

    public void addNewTrainingMetric(TrainingMetricDto trainingMetricDto) {
        recentMetric = trainingMetricDto;

        try {
            trainingService.addNewTrainingMetric(trainingId, trainingMetricDto);
        } catch (DatabaseException e) {
            logger.error("Failed to add training metric for trainingId={}, trainingMetric={}, error={}",
                    trainingId, trainingMetricDto, e.getMessage(), e);
        }
    }

    public void updateTrainingStatus(StatusName status) {
        try {
            trainingService.updateTrainingStatus(trainingId, status);
        } catch (DatabaseException e) {
            logger.error("Failed to update the status for for trainingId={}, status={}, error={}",
                    trainingId, status, e.getMessage(), e);
        }
    }

    public void shareTraining() {
        try {
            trainingService.shareTraining(trainingId, recentMetric.accuracy(), recentMetric.loss());
        } catch (DatabaseException e) {
            logger.error("Failed to share training for trainingId={}, error={}",
                    trainingId, e.getMessage(), e);
        }
    }
}
