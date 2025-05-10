package pl.edu.uj.tcs.aiplayground.service;

import pl.edu.uj.tcs.aiplayground.exception.ModelModificationException;
import pl.edu.uj.tcs.aiplayground.validation.ModelValidation;
import pl.edu.uj.tcs.aiplayground.dto.ModelDto;
import pl.edu.uj.tcs.aiplayground.form.ModelForm;
import pl.edu.uj.tcs.aiplayground.exception.DatabaseException;
import pl.edu.uj.tcs.aiplayground.repository.IModelRepository;

import java.util.List;
import java.util.UUID;

public class ModelService {
    private final IModelRepository modelRepository;

    public static Integer FIRST_VERSION_NUMBER = 1;

    public ModelService(IModelRepository modelRepository) {
        this.modelRepository = modelRepository;
    }

    public List<String> getUserModelNames(UUID userId) throws DatabaseException {
        try {
            return modelRepository.getUserModelNames(userId);
        } catch(Exception e) {
            throw new DatabaseException(e);
        }
    }

    public ModelDto getModel(UUID userId, String modelName) throws DatabaseException {
        try {
            return modelRepository.getRecentModel(userId, modelName);
        } catch(Exception e) {
            throw new DatabaseException(e);
        }
    }

    public ModelDto getModel(UUID userId, String modelName, Integer modelVersion) throws DatabaseException {
        try {
            return modelRepository.getModel(userId, modelName, modelVersion);
        } catch(Exception e) {
            throw new DatabaseException(e);
        }
    }

    public ModelDto addModel(ModelForm modelForm) throws DatabaseException, ModelModificationException {
        ModelValidation.validateModelForm(modelForm);

        /*UUID modelsId = UUID.randomUUID();
        ModelsRecord modelsRecord = new ModelsRecord(
                modelsId,
                modelForm.userId(),
                modelForm.name()
        );

        UUID modelVersionId = UUID.randomUUID();
        OffsetDateTime createdAt = OffsetDateTime.now();
        ModelVersionsRecord modelVersionsRecord = new ModelVersionsRecord(
                modelVersionId,
                modelsId,
                FIRST_VERSION_NUMBER,
                modelForm.jsonArchitecture(),
                createdAt
        );*/

        try {
            return modelRepository.insertModel(modelForm);
            //modelRepository.insertModel(modelsRecord);
            //modelRepository.insertModelVersion(modelVersionsRecord);
        } catch(Exception e) {
            throw new DatabaseException(e);
        }

        /*return new ModelDto(
                modelsRecord.getUserId(),
                modelForm.userId(),
                modelVersionsRecord.getId(),
                modelsRecord.getName(),
                modelVersionsRecord.getVersionNumber(),
                modelVersionsRecord.getArchitecture()
        );*/
    }

    public ModelDto updateModel(ModelForm modelForm) throws DatabaseException, ModelModificationException {
        ModelValidation.validateModelForm(modelForm);

        //ModelVersionsRecord modelVersionsRecord;
        try {
            /*UUID modelId = modelRepository.getModelId(modelForm.userId(), modelForm.name());
            Integer recentVersion = modelRepository.getRecentModelVersion(modelId);

            UUID modelVersionId = UUID.randomUUID();
            OffsetDateTime createdAt = OffsetDateTime.now();
            modelVersionsRecord = new ModelVersionsRecord(
                    modelVersionId,
                    modelId,
                    getNextVersionNumber(recentVersion),
                    modelForm.jsonArchitecture(),
                    createdAt
            );

            modelRepository.insertModelVersion(modelVersionsRecord);*/

            return modelRepository.insertModelVersion(modelForm);
        } catch(Exception e) {
            throw new DatabaseException(e);
        }

        /*return new ModelDto(
                modelForm.userId(),
                modelForm.userId(),
                modelVersionsRecord.getId(),
                modelForm.name(),
                modelVersionsRecord.getVersionNumber(),
                modelVersionsRecord.getArchitecture()
        );*/
    }
}
