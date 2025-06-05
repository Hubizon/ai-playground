package pl.edu.uj.tcs.aiplayground.service.repository;

import org.jooq.DSLContext;
import org.jooq.Record;
import pl.edu.uj.tcs.aiplayground.dto.DataLoaderType;
import pl.edu.uj.tcs.aiplayground.dto.ModelDto;
import pl.edu.uj.tcs.aiplayground.dto.TrainingDto;
import pl.edu.uj.tcs.aiplayground.dto.TrainingMetricDto;
import pl.edu.uj.tcs.aiplayground.dto.architecture.DatasetType;
import pl.edu.uj.tcs.aiplayground.dto.architecture.LossFunctionType;
import pl.edu.uj.tcs.aiplayground.dto.architecture.OptimizerType;
import pl.edu.uj.tcs.aiplayground.dto.form.ModelForm;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@SuppressWarnings("ConstantConditions")
public class ModelRepository implements IModelRepository {
    private final DSLContext dsl;

    public ModelRepository(DSLContext dslContext) {
        this.dsl = dslContext;
    }

    @Override
    public List<String> getUserModelNames(UUID userId) {
        return dsl.fetch("""
                SELECT name FROM models
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
                                SELECT id, ?::jsonb, now()
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
                                       ?::jsonb,
                                       now()
                                FROM model_id_fetch
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

    @Override
    public boolean existUserModelName(UUID userId, String name) {
        return !dsl.fetch("""
                        SELECT 1
                        FROM models
                        WHERE user_id = ? AND name = ?;
                        """,
                userId, name
        ).isEmpty();
    }

    @Override
    public TrainingDto getTrainingForModel(UUID modelVersionId) {
        Record record = dsl.fetchOne("""
                    SELECT t.model_version_id,
                           t.max_epochs,
                           t.batch_size,
                           t.learning_rate,
                           d.name AS dataset,
                           o.name AS optimizer,
                           l.name AS loss_function
                    FROM trainings t
                    JOIN datasets d ON t.dataset_id = d.id
                    JOIN optimizers o ON t.optimizer = o.id
                    JOIN loss_functions l ON t.loss_function = l.id
                    WHERE t.model_version_id = ?
                    ORDER BY t.started_at DESC
                    LIMIT 1
                """, modelVersionId);

        if (record == null)
            return null;

        return new TrainingDto(
                record.get("model_version_id", UUID.class),
                record.get("max_epochs", Integer.class),
                record.get("batch_size", Integer.class),
                record.get("learning_rate", Double.class),
                DatasetType.valueOf(record.get("dataset", String.class)),
                OptimizerType.valueOf(record.get("optimizer", String.class)),
                LossFunctionType.valueOf(record.get("loss_function", String.class))
        );
    }

    @Override
    public List<TrainingMetricDto> getMetricsForModel(UUID modelVersionId) {
        List<Record> records = dsl.fetch("""
                            SELECT m.epoch, m.loss, m.accuracy, m.type
                            FROM training_metrics m
                            JOIN trainings t ON m.training_id = t.id
                            WHERE t.model_version_id = ?
                            ORDER BY m.epoch
                        """,
                modelVersionId
        );

        if (records == null || records.isEmpty())
            return null;

        return records.stream()
                .map(r -> new TrainingMetricDto(
                        r.get("epoch", Integer.class),
                        r.get("loss", Double.class),
                        r.get("accuracy", Double.class),
                        DataLoaderType.valueOf(r.get("type", String.class))
                ))
                .collect(Collectors.toList());
    }

    @Override
    public UUID getTrainingIdForModel(UUID modelVersionId) {
        Record record = dsl.fetchOne("""
                        SELECT t.id
                        FROM trainings t
                        WHERE t.model_version_id = ?
                        ORDER BY t.started_at DESC
                        LIMIT 1;
                        """,
                modelVersionId
        );

        if (record == null)
            return null;
        return record.into(UUID.class);
    }

    @Override
    public boolean hasUserAlreadySharedTraining(UUID trainingId) {
        return !dsl.fetch("""
                        SELECT 1
                        FROM public_results pt
                        WHERE pt.training_id = ?;
                        """,
                trainingId
        ).isEmpty();
    }

    @Override
    public String getTrainingStatusName(UUID trainingId) {
        Record record = dsl.fetchOne("""
                        SELECT s.name
                        FROM trainings t
                        JOIN statuses s ON s.id = t.status
                        WHERE t.id = ?
                        LIMIT 1;
                        """,
                trainingId
        );

        if (record == null)
            return null;
        return record.into(String.class);
    }
}
