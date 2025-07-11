package pl.edu.uj.tcs.aiplayground.viewmodel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.uj.tcs.aiplayground.dto.LeaderboardDto;
import pl.edu.uj.tcs.aiplayground.dto.architecture.DatasetType;
import pl.edu.uj.tcs.aiplayground.exception.DatabaseException;
import pl.edu.uj.tcs.aiplayground.service.LeaderboardService;

import java.util.List;

public class LeaderboardViewModel {
    private static final Logger logger = LoggerFactory.getLogger(LeaderboardViewModel.class);
    private final LeaderboardService leaderboardService;

    public LeaderboardViewModel(LeaderboardService leaderboardService) {
        this.leaderboardService = leaderboardService;
    }

    public List<LeaderboardDto> getLeaderboardGlobal(DatasetType datasetType) {
        try {
            return leaderboardService.getLeaderboardGlobal(datasetType);
        } catch (DatabaseException e) {
            logger.error("Failed to load the leaderboard for datasetType={}, error={}",
                    datasetType, e.getMessage(), e);
        }
        return List.of();
    }

    public List<LeaderboardDto> getLeaderboardLocal(DatasetType datasetType, String countryName) {
        try {
            return leaderboardService.getLeaderboardLocal(datasetType, countryName);
        } catch (DatabaseException e) {
            logger.error("Failed to load the leaderboard for datasetType={}, countryName={}, error={}",
                    datasetType, countryName, e.getMessage(), e);
        }
        return List.of();
    }
}
