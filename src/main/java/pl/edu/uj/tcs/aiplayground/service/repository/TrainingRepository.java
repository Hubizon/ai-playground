package pl.edu.uj.tcs.aiplayground.service.repository;

import org.jooq.DSLContext;
import org.postgresql.PGConnection;
import org.postgresql.PGNotification;
import pl.edu.uj.tcs.aiplayground.dto.StatusType;
import pl.edu.uj.tcs.aiplayground.dto.TrainingDto;
import pl.edu.uj.tcs.aiplayground.dto.TrainingMetricDto;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

@SuppressWarnings("ConstantConditions")
public class TrainingRepository implements ITrainingRepository {
    private static final StatusType DEFAULT_STATUS = StatusType.QUEUE;
    private final Connection dbConnection;
    private final DSLContext dsl;

    public TrainingRepository(Connection dbConnection, DSLContext dslContext) {
        this.dbConnection = dbConnection;
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
                        INSERT INTO trainings (model_version_id, learning_rate, dataset_id, optimizer, loss_function, max_epochs, batch_size, status)
                        VALUES (
                            ?, ?,
                            (SELECT id FROM datasets WHERE name = ?),
                            (SELECT id FROM optimizers WHERE name = ?),
                            (SELECT id FROM loss_functions WHERE name = ?),
                            ?, ?,
                            (SELECT id FROM statuses WHERE name = ?)
                        )
                        RETURNING id;
                        """,
                trainingDto.modelVersionId(),
                trainingDto.learningRate(),
                trainingDto.dataset().name(),
                trainingDto.optimizer().name(),
                trainingDto.lossFunction().name(),
                trainingDto.maxEpochs(),
                trainingDto.batchSize(),
                DEFAULT_STATUS.getName()
        ).into(UUID.class);
    }

    @Override
    public void updateTrainingStatus(UUID trainingId, StatusType status) {
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
    public void finishTraining(UUID trainingId) {
        dsl.query("""
                        UPDATE trainings
                            SET finished_at = NOW()
                                WHERE id = ?;
                        """,
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

    @Override
    public String shareTraining(UUID trainingId, Double accuracy, Double loss) throws SQLException {
        PGConnection pgConnection = dbConnection.unwrap(PGConnection.class);
        try (var statement = dbConnection.createStatement()) {
            statement.execute("LISTEN reward_channel");
        }

        dsl.query("""
                INSERT INTO public_results (training_id, accuracy, loss)
                VALUES (?, ?, ?)
                ON CONFLICT (training_id) DO NOTHING
                """, trainingId, accuracy, loss).execute();

        PGNotification[] notifications = pgConnection.getNotifications();
        if (notifications != null && notifications.length > 0)
            return notifications[0].getParameter().replace("\\n", "\n");

        return null;
    }
}
