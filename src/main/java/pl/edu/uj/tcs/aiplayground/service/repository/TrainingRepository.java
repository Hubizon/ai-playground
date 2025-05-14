package pl.edu.uj.tcs.aiplayground.service.repository;

import org.jooq.DSLContext;
import pl.edu.uj.tcs.aiplayground.dto.TrainingDto;
import pl.edu.uj.tcs.aiplayground.dto.TrainingMetricDto;

import java.util.List;
import java.util.UUID;

@SuppressWarnings("ConstantConditions")
public class TrainingRepository implements ITrainingRepository {
    private static final String DEFAULT_STATUS = "QUEUE";
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
                ORDER BY epoch, timestamp;
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
                            WITH training_ids AS (
                                SELECT
                                    (SELECT id FROM datasets WHERE name = $1) AS dataset_id,
                                    (SELECT id FROM optimizers WHERE name = $2) AS optimizer_id,
                                    (SELECT id FROM loss_functions WHERE name = $3) AS loss_function_id,
                                    (SELECT id FROM training_statuses WHERE name = $4) AS status_id
                            ),
                            INSERT INTO trainings (model_version_id, dataset_id, optimizer_id, loss_function_id, status_id)
                                SELECT DEFAULT, dataset_id, optimizer_id, loss_function_id, status_id
                                FROM training_ids
                                RETURNING id
                        """,
                trainingDto.dataset(),
                trainingDto.optimizer(),
                trainingDto.lossFunction(),
                DEFAULT_STATUS
        ).into(UUID.class);
    }

    @Override
    public void updateTrainingStatus(UUID trainingId, String status) {
        dsl.query("""
                        UPDATE trainings
                            SET status = (SELECT id FROM statuses WHERE name = ?)
                                WHERE id = ?;
                        """,
                status,
                trainingId
        ).execute();
    }
}
