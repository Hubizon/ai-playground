package pl.edu.uj.tcs.aiplayground.service;

import org.jooq.exception.DataAccessException;
import org.postgresql.util.PSQLException;
import pl.edu.uj.tcs.aiplayground.dto.StatusType;
import pl.edu.uj.tcs.aiplayground.dto.TrainingDto;
import pl.edu.uj.tcs.aiplayground.dto.TrainingMetricDto;
import pl.edu.uj.tcs.aiplayground.exception.DatabaseException;
import pl.edu.uj.tcs.aiplayground.exception.InsufficientTokensException;
import pl.edu.uj.tcs.aiplayground.exception.TrainingException;
import pl.edu.uj.tcs.aiplayground.service.repository.ITrainingRepository;

import java.util.List;
import java.util.UUID;

public class TrainingService {
    private final ITrainingRepository trainingRepository;

    public TrainingService(ITrainingRepository trainingRepository) {
        this.trainingRepository = trainingRepository;
    }

    public UUID addNewTraining(TrainingDto trainingDto) throws DatabaseException, InsufficientTokensException {
        try {
            return trainingRepository.insertTraining(trainingDto);
        } catch(DataAccessException e) {
            if (e.getCause() instanceof org.postgresql.util.PSQLException) {
                PSQLException ex = (PSQLException) e.getCause();
                if ("P0002".equals(ex.getSQLState())) {
                    throw new InsufficientTokensException(ex.getServerErrorMessage().getMessage());
                }
            }
            throw e;
        } catch(Exception e) {
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

    public void shareTraining(UUID trainingId, Double accuracy, Double loss) throws DatabaseException {
        try {
            trainingRepository.shareTraining(trainingId, accuracy, loss);
        } catch (Exception e) {
            throw new DatabaseException(e);
        }
    }
}
