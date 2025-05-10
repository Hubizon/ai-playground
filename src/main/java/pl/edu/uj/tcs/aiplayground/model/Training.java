package pl.edu.uj.tcs.aiplayground.model;

import pl.edu.uj.tcs.aiplayground.dto.TrainingMetricDto;
import pl.edu.uj.tcs.aiplayground.exception.DatabaseException;
import pl.edu.uj.tcs.aiplayground.dto.TrainingDto;
import pl.edu.uj.tcs.aiplayground.service.TrainingService;

import java.util.UUID;

public class Training {
    private final TrainingService trainingService;
    private UUID trainingId = null;

    public Training(TrainingService trainingService, TrainingDto trainingDto) {
        this.trainingService = trainingService;

        try {
            trainingId = trainingService.addNewTraining(trainingDto);
        } catch (DatabaseException e) {
            // TODO
        }
    }

    public Training(TrainingDto trainingDto) {
        this(ServiceFactory.createTrainingService(), trainingDto);
    }

    public void addNewTrainingMetric(TrainingMetricDto trainingMetricDto) {
        try {
            trainingService.addNewTrainingMetric(trainingMetricDto);
        } catch(DatabaseException e) {
            // TODO
        }
    }

    public void endTraining(String status) {
        try {
            trainingService.updateTrainingStatus(trainingId, status);
        } catch(DatabaseException e) {
            // TODO
        }
    }
}
