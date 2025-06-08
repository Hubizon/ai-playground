package pl.edu.uj.tcs.aiplayground.service;

import org.jooq.exception.DataAccessException;
import org.postgresql.util.PSQLException;
import pl.edu.uj.tcs.aiplayground.dto.StatusType;
import pl.edu.uj.tcs.aiplayground.dto.TrainingDto;
import pl.edu.uj.tcs.aiplayground.dto.TrainingMetricDto;
import pl.edu.uj.tcs.aiplayground.exception.DatabaseException;
import pl.edu.uj.tcs.aiplayground.exception.InsufficientTokensException;
import pl.edu.uj.tcs.aiplayground.service.repository.ITrainingRepository;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class TrainingService {
    private final ITrainingRepository trainingRepository;

    public TrainingService(ITrainingRepository trainingRepository) {
        this.trainingRepository = trainingRepository;
    }

    public UUID addNewTraining(TrainingDto trainingDto) throws DatabaseException, InsufficientTokensException {
        try {
            return trainingRepository.insertTraining(trainingDto);
        } catch (DataAccessException e) {
            if (e.getCause() instanceof PSQLException ex) {
                if ("P0002".equals(ex.getSQLState())) {
                    throw new InsufficientTokensException(Objects.requireNonNull(ex.getServerErrorMessage()).getMessage());
                }
            }
            throw e;
        } catch (Exception e) {
            throw new DatabaseException(e);
        }
    }

    public void updateTrainingStatus(UUID trainingId, StatusType status) throws DatabaseException {
        try {
            trainingRepository.updateTrainingStatus(trainingId, status);
            if (status.getIsFinished())
                trainingRepository.finishTraining(trainingId);
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

    public String getDatasetPathByName(String dbName) throws DatabaseException {
        try {
            return trainingRepository.getDatasetPathByName(dbName);
        } catch (Exception e) {
            throw new DatabaseException(e);
        }
    }

    public String shareTraining(UUID trainingId) throws DatabaseException {
        try {
            return trainingRepository.shareTraining(trainingId);
        } catch (Exception e) {
            throw new DatabaseException(e);
        }
    }
}
