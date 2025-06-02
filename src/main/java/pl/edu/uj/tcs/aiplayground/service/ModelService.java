package pl.edu.uj.tcs.aiplayground.service;

import org.jooq.exception.DataAccessException;
import org.postgresql.util.PSQLException;
import pl.edu.uj.tcs.aiplayground.dto.ModelDto;
import pl.edu.uj.tcs.aiplayground.dto.StatusType;
import pl.edu.uj.tcs.aiplayground.dto.TrainingDto;
import pl.edu.uj.tcs.aiplayground.dto.TrainingMetricDto;
import pl.edu.uj.tcs.aiplayground.dto.form.ModelForm;
import pl.edu.uj.tcs.aiplayground.dto.validation.ModelValidation;
import pl.edu.uj.tcs.aiplayground.exception.DatabaseException;
import pl.edu.uj.tcs.aiplayground.exception.InsufficientTokensException;
import pl.edu.uj.tcs.aiplayground.exception.ModelModificationException;
import pl.edu.uj.tcs.aiplayground.service.repository.IModelRepository;

import java.util.List;
import java.util.UUID;

public class ModelService {
    private final IModelRepository modelRepository;

    public ModelService(IModelRepository modelRepository) {
        this.modelRepository = modelRepository;
    }

    public List<String> getUserModelNames(UUID userId) throws DatabaseException {
        try {
            return modelRepository.getUserModelNames(userId);
        } catch (Exception e) {
            throw new DatabaseException(e);
        }
    }

    public ModelDto getModel(UUID userId, String modelName) throws DatabaseException {
        try {
            return modelRepository.getRecentModel(userId, modelName);
        } catch (Exception e) {
            throw new DatabaseException(e);
        }
    }

    public ModelDto getModel(UUID userId, String modelName, Integer modelVersion) throws DatabaseException {
        try {
            return modelRepository.getModel(userId, modelName, modelVersion);
        } catch (Exception e) {
            throw new DatabaseException(e);
        }
    }

    public ModelDto addModel(ModelForm modelForm) throws DatabaseException, ModelModificationException, InsufficientTokensException {
        ModelValidation.validateModelForm(modelForm);

        if (modelRepository.existUserModelName(modelForm.userId(), modelForm.name()))
            throw new ModelModificationException("Model name must be unique");

        try {
            return modelRepository.insertModel(modelForm);
        } catch (DataAccessException e) {
            if (e.getCause() instanceof PSQLException ex) {
                if ("P0001".equals(ex.getSQLState())) {
                    throw new InsufficientTokensException(ex.getServerErrorMessage().getMessage());
                }
            }
            throw e;
        } catch (Exception e) {
            throw new DatabaseException(e);
        }
    }

    public ModelDto updateModel(ModelForm modelForm) throws DatabaseException, ModelModificationException {
        ModelValidation.validateModelForm(modelForm);

        try {
            return modelRepository.insertModelVersion(modelForm);
        } catch (Exception e) {
            throw new DatabaseException(e);
        }
    }

    public Integer getPreviousVersion(UUID userId, String modelName, Integer modelVersion) throws DatabaseException {
        try {
            List<Integer> versions = modelRepository.getModelVersions(userId, modelName);
            return versions.stream()
                    .filter(i -> i < modelVersion)
                    .max(Integer::compareTo)
                    .orElse(null);
        } catch (Exception e) {
            throw new DatabaseException(e);
        }
    }

    public Integer getNextVersion(UUID userId, String modelName, Integer modelVersion) throws DatabaseException {
        try {
            List<Integer> versions = modelRepository.getModelVersions(userId, modelName);
            return versions.stream()
                    .filter(i -> i > modelVersion)
                    .min(Integer::compareTo)
                    .orElse(null);
        } catch (Exception e) {
            throw new DatabaseException(e);
        }
    }

    public UUID getTrainingIdForModel(UUID modelVersionId) throws DatabaseException {
        try {
            return modelRepository.getTrainingIdForModel(modelVersionId);
        } catch (Exception e) {
            throw new DatabaseException(e);
        }
    }

    public TrainingDto getTrainingForModel(UUID modelVersionId) throws DatabaseException {
        try {
            return modelRepository.getTrainingForModel(modelVersionId);
        } catch (Exception e) {
            throw new DatabaseException(e);
        }
    }

    public List<TrainingMetricDto> getMetricsForModel(UUID modelVersionId) throws DatabaseException {
        try {
            return modelRepository.getMetricsForModel(modelVersionId);
        } catch (Exception e) {
            throw new DatabaseException(e);
        }
    }

    public boolean hasUserAlreadySharedTraining(UUID trainingId) throws DatabaseException {
        try {
            return modelRepository.hasUserAlreadySharedTraining(trainingId);
        } catch (Exception e) {
            throw new DatabaseException(e);
        }
    }

    public StatusType getTrainingStatus(UUID trainingId) throws DatabaseException {
        try {
            return StatusType.valueOf(modelRepository.getTrainingStatusName(trainingId));
        } catch (Exception e) {
            throw new DatabaseException(e);
        }
    }
}
