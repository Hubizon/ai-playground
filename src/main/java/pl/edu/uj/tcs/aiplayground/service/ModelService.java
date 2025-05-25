package pl.edu.uj.tcs.aiplayground.service;

import pl.edu.uj.tcs.aiplayground.dto.ModelDto;
import pl.edu.uj.tcs.aiplayground.dto.form.ModelForm;
import pl.edu.uj.tcs.aiplayground.exception.DatabaseException;
import pl.edu.uj.tcs.aiplayground.exception.ModelModificationException;
import pl.edu.uj.tcs.aiplayground.service.repository.IModelRepository;
import pl.edu.uj.tcs.aiplayground.dto.validation.ModelValidation;

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

    public ModelDto addModel(ModelForm modelForm) throws DatabaseException, ModelModificationException {
        ModelValidation.validateModelForm(modelForm);

        try {
            return modelRepository.insertModel(modelForm);
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
}
