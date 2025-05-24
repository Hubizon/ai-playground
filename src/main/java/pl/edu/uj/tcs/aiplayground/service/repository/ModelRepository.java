package pl.edu.uj.tcs.aiplayground.service.repository;

import org.jooq.DSLContext;
import pl.edu.uj.tcs.aiplayground.dto.ModelDto;
import pl.edu.uj.tcs.aiplayground.dto.form.ModelForm;

import java.util.List;
import java.util.UUID;

@SuppressWarnings("ConstantConditions")
public class ModelRepository implements IModelRepository {
    private final DSLContext dsl;

    public ModelRepository(DSLContext dslContext) {
        this.dsl = dslContext;
    }

    @Override
    public List<String> getUserModelNames(UUID userId) {
        return dsl.fetch("""
                SELECT id FROM models
                    WHERE user_id = ?;
                """, userId
        ).into(String.class);
    }

    @Override
    public ModelDto insertModel(ModelForm modelForm) {
        return dsl.fetchOne("""
                        WITH model_insert AS (
                            INSERT INTO models(id, user_id, name)
                                VALUES (DEFAULT, ?, ?)
                                RETURNING id
                        ),
                        model_version_insert AS (
                            INSERT INTO model_versions(model_id, architecture, created_at)
                                SELECT id, ?, now()
                                FROM model_insert
                                RETURNING id, model_id, version_number, architecture
                        )
                        SELECT model_id AS modelId,
                               ? AS userId,
                               mvi.id AS modelVersionId,
                               ? AS modelName,
                               version_number AS versionNumber,
                               architecture AS architecture
                        FROM model_version_insert mvi;
                        """,
                modelForm.userId(),
                modelForm.name(),
                modelForm.jsonArchitecture(),
                modelForm.userId(),
                modelForm.name()
        ).into(ModelDto.class);
    }

    @Override
    public ModelDto insertModelVersion(ModelForm modelForm) {
        return dsl.fetchOne("""
                        WITH model_id_fetch AS (
                            SELECT id
                            FROM models
                                WHERE models.user_id = ? AND models.name = ?
                        ),
                        model_version_insert AS (
                            INSERT INTO model_versions(model_id, version_number, architecture, created_at)
                                SELECT model_id_fetch.id,
                                       next_model_version((SELECT MAX(version_number)
                                                                FROM model_versions
                                                                WHERE model_id = model_id_fetch.id)),
                                       ?,
                                       now()
                                RETURNING id, model_id, version_number, architecture
                        )
                        SELECT model_id AS modelId,
                               ? AS userId,
                               mvi.id AS modelVersionId,
                               ? AS modelName,
                               version_number AS versionNumber,
                               architecture AS architecture
                        FROM model_version_insert mvi;
                        """,
                modelForm.userId(),
                modelForm.name(),
                modelForm.jsonArchitecture(),
                modelForm.userId(),
                modelForm.name()
        ).into(ModelDto.class);
    }

    @Override
    public ModelDto getModel(UUID userId, String modelName, Integer modelVersion) {
        return dsl.fetchOne("""
                        SELECT model_id AS modelId,
                               models.user_id AS userId,
                               mvi.id AS modelVersionId,
                               models.name AS modelName,
                               mvi.version_number AS versionNumber,
                               mvi.architecture AS architecture
                            FROM model_versions mvi
                            JOIN models ON mvi.model_id = models.id
                            WHERE models.user_id = ?
                              AND models.name = ?
                              AND mvi.version_number = ?;
                        """,
                userId,
                modelName,
                modelVersion
        ).into(ModelDto.class);
    }

    @Override
    public ModelDto getRecentModel(UUID userId, String modelName) {
        return dsl.fetchOne("""
                        SELECT model_id AS modelId,
                               models.user_id AS userId,
                               mvi.id AS modelVersionId,
                               models.name AS modelName,
                               mvi.version_number AS versionNumber,
                               mvi.architecture AS architecture
                            FROM model_versions mvi
                            JOIN models ON mvi.model_id = models.id
                            WHERE models.user_id = ?
                              AND models.name = ?
                              AND mvi.version_number = (SELECT MAX(version_number)
                                                            FROM model_versions mvi2
                                                            WHERE mvi2.model_id = mvi.model_id);
                        """,
                userId,
                modelName
        ).into(ModelDto.class);
    }

    @Override
    public List<Integer> getModelVersions(UUID userId, String modelName) {
        return dsl.fetch("""
            SELECT mvi.version_number
            FROM model_versions mvi
            JOIN models m ON m.id = mvi.model_id
            WHERE m.user_id = ? AND m.name = ?
            ORDER BY mvi.version_number;
            """,
                userId,
                modelName
        ).into(Integer.class);
    }
}
