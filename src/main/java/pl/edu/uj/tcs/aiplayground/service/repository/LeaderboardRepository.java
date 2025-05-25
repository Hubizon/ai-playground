package pl.edu.uj.tcs.aiplayground.service.repository;


import org.jooq.DSLContext;
import pl.edu.uj.tcs.aiplayground.dto.LeaderboardDto;
import pl.edu.uj.tcs.aiplayground.dto.architecture.DatasetType;

import java.util.List;

public class LeaderboardRepository implements ILeaderboardRepository {
    private final DSLContext dsl;

    public LeaderboardRepository(DSLContext dslContext) {
        this.dsl = dslContext;
    }

    @Override
    public List<LeaderboardDto> getLeaderboardGlobal(DatasetType datasetType) {
        return dsl.fetch("""
                    SELECT DISTINCT ON (u.username)
                        u.username AS "userName",
                        pr.accuracy AS "accuracy",
                        pr.loss AS "loss"
                    FROM public_results pr
                    JOIN trainings t ON pr.training_id = t.id
                    JOIN model_versions mv ON t.model_version_id = mv.id
                    JOIN models m ON mv.model_id = m.id
                    JOIN users u ON m.user_id = u.id
                    JOIN datasets d ON t.dataset_id = d.id
                    WHERE d.name = ?
                    ORDER BY u.username, pr.accuracy DESC, pr.loss ASC
                """, datasetType.getDbKey()).into(LeaderboardDto.class);
    }

    @Override
    public List<LeaderboardDto> getLeaderboardLocal(DatasetType datasetType, String countryName) {
        return dsl.fetch("""
                    SELECT DISTINCT ON (u.username)
                        u.username AS "userName",
                        pr.accuracy AS "accuracy",
                        pr.loss AS "loss"
                    FROM public_results pr
                    JOIN trainings t ON pr.training_id = t.id
                    JOIN model_versions mv ON t.model_version_id = mv.id
                    JOIN models m ON mv.model_id = m.id
                    JOIN users u ON m.user_id = u.id
                    JOIN countries c ON u.country_id = c.id
                    JOIN datasets d ON t.dataset_id = d.id
                    WHERE d.name = ?
                      AND c.name = ?
                    ORDER BY u.username, pr.accuracy DESC, pr.loss ASC
                """, datasetType.getDbKey(), countryName).into(LeaderboardDto.class);
    }
}