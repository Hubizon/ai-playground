package pl.edu.uj.tcs.aiplayground.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.uj.tcs.aiplayground.dto.StatusType;
import pl.edu.uj.tcs.aiplayground.dto.TrainingDto;
import pl.edu.uj.tcs.aiplayground.dto.TrainingMetricDto;
import pl.edu.uj.tcs.aiplayground.exception.DatabaseException;
import pl.edu.uj.tcs.aiplayground.exception.InsufficientTokensException;
import pl.edu.uj.tcs.aiplayground.exception.TrainingException;
import pl.edu.uj.tcs.aiplayground.service.TrainingService;
import pl.edu.uj.tcs.aiplayground.service.repository.JooqFactory;
import pl.edu.uj.tcs.aiplayground.service.repository.TrainingRepository;

import java.util.UUID;
import java.util.function.Consumer;

public class TrainingHandler {
    private static final Logger logger = LoggerFactory.getLogger(TrainingHandler.class);
    private final TrainingService trainingService;
    private final Consumer<StatusType> statusListener;
    private UUID trainingId = null;
    private TrainingMetricDto recentMetric;

    public TrainingHandler(TrainingService trainingService,
                           TrainingDto trainingDto,
                           Consumer<StatusType> statusListener) throws InsufficientTokensException {
        this.trainingService = trainingService;
        this.statusListener = statusListener;

        try {
            trainingId = trainingService.addNewTraining(trainingDto);
        } catch (InsufficientTokensException e) {
            throw e;
        } catch (DatabaseException e) {
            logger.error("Failed to create training for core={}, error={}", trainingDto, e.getMessage(), e);
        }
    }

    public TrainingHandler(TrainingDto trainingDto) throws InsufficientTokensException {
        this(new TrainingService(new TrainingRepository(
                JooqFactory.getConnection(), JooqFactory.getDSLContext())), trainingDto, null);
    }

    public TrainingHandler(TrainingDto trainingDto, Consumer<StatusType> statusListener) throws InsufficientTokensException {
        this(new TrainingService(new TrainingRepository(
                JooqFactory.getConnection(), JooqFactory.getDSLContext())), trainingDto, statusListener);
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

    public void updateTrainingStatus(StatusType status) {
        try {
            trainingService.updateTrainingStatus(trainingId, status);
            if (statusListener != null)
                statusListener.accept(status);
        } catch (DatabaseException e) {
            logger.error("Failed to update the status for for trainingId={}, status={}, error={}",
                    trainingId, status, e.getMessage(), e);
        }
    }

    public String shareTraining() {
        try {
            return trainingService.shareTraining(trainingId, recentMetric.accuracy(), recentMetric.loss());
        } catch (DatabaseException e) {
            logger.error("Failed to share training for trainingId={}, error={}",
                    trainingId, e.getMessage(), e);
        }
        return null;
    }
}
