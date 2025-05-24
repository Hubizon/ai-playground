package pl.edu.uj.tcs.aiplayground.service.repository;

import org.jooq.DSLContext;
import pl.edu.uj.tcs.aiplayground.dto.StatusName;
import pl.edu.uj.tcs.aiplayground.dto.TrainingDto;
import pl.edu.uj.tcs.aiplayground.dto.TrainingMetricDto;

import java.util.List;
import java.util.UUID;

@SuppressWarnings("ConstantConditions")
public class TrainingRepository implements ITrainingRepository {
    private static final StatusName DEFAULT_STATUS = StatusName.QUEUE;
    private final DSLContext dsl;

    public TrainingRepository(DSLContext dslContext) {
        this.dsl = dslContext;
    }

    @Override
    public List<TrainingMetricDto> getTrainingMetrics(UUID trainingId) {
        return dsl.fetch("""
                SELECT epoch, loss, accuracy
                FROM training_metrics
                WHERE training_id = ?
                ORDER BY epoch;
                """, trainingId
        ).into(TrainingMetricDto.class);
    }

    @Override
    public void insertTrainingMetric(UUID trainingId, TrainingMetricDto trainingMetricDto) {
        dsl.query("""
                        INSERT INTO training_metrics(id, training_id, epoch, loss, accuracy, timestamp)
                        VALUES (DEFAULT, ?, ?, ?, ?, now());
                        """,
                trainingId,
                trainingMetricDto.epoch(),
                trainingMetricDto.loss(),
                trainingMetricDto.accuracy()
        ).execute();
    }

    @Override
    public UUID insertTraining(TrainingDto trainingDto) {
        return dsl.fetchOne("""
        INSERT INTO trainings (model_version_id, learning_rate, dataset_id, optimizer, loss_function, status)
        VALUES (
            ?,
            ?,
            (SELECT id FROM datasets WHERE name = ?),
            (SELECT id FROM optimizers WHERE name = ?),
            (SELECT id FROM loss_functions WHERE name = ?),
            (SELECT id FROM statuses WHERE name = ?)
        )
        RETURNING id;
        """,
                trainingDto.modelVersionId(),
                trainingDto.learningRate(),
                trainingDto.dataset().getDbKey(),
                trainingDto.optimizer().getDbKey(),
                trainingDto.lossFunction().getDbKey(),
                DEFAULT_STATUS.getName()
        ).into(UUID.class);
    }

    @Override
    public void updateTrainingStatus(UUID trainingId, StatusName status) {
        dsl.query("""
                        UPDATE trainings
                            SET status = (SELECT id FROM statuses WHERE name = ?)
                                WHERE id = ?;
                        """,
                status.getName(),
                trainingId
        ).execute();
    }

    @Override
    public String getDatasetPathByName(String dbName) {
        return dsl.fetchOne("""
                        SELECT path FROM datasets
                            WHERE name = ?;
                        """,
                dbName
        ).into(String.class);
    }
}
