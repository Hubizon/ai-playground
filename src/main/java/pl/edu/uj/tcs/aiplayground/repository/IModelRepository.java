package pl.edu.uj.tcs.aiplayground.repository;

import pl.edu.uj.tcs.aiplayground.dto.ModelDto;
import pl.edu.uj.tcs.aiplayground.form.ModelForm;

import java.util.List;
import java.util.UUID;

public interface IModelRepository {
    List<String> getUserModelNames(UUID userId);

    ModelDto insertModel(ModelForm record);

    ModelDto insertModelVersion(ModelForm modelVersionsRecord);

    ModelDto getModel(UUID userId, String modelName, Integer modelVersion);

    ModelDto getRecentModel(UUID userId, String modelName);
}
