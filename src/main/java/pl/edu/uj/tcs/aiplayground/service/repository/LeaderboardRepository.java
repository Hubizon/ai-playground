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
                    SELECT
                        leaderboards.username AS "userName",
                        leaderboards.accuracy AS "accuracy",
                        leaderboards.loss AS "loss"
                    FROM leaderboards
                    WHERE leaderboards.dataset = ?
                    ORDER BY position, username
                """, datasetType.name()
        ).into(LeaderboardDto.class);
    }

    @Override
    public List<LeaderboardDto> getLeaderboardLocal(DatasetType datasetType, String countryName) {
        return dsl.fetch("""
                    SELECT
                        leaderboards.username AS "userName",
                        leaderboards.accuracy AS "accuracy",
                        leaderboards.loss AS "loss"
                    FROM leaderboards
                    WHERE leaderboards.dataset = ?
                      AND leaderboards.country = ?
                    ORDER BY position, username
                """, datasetType.name(), countryName
        ).into(LeaderboardDto.class);
    }
}