package pl.edu.uj.tcs.aiplayground.service;

import pl.edu.uj.tcs.aiplayground.dto.LeaderboardDto;
import pl.edu.uj.tcs.aiplayground.dto.architecture.DatasetType;
import pl.edu.uj.tcs.aiplayground.exception.DatabaseException;
import pl.edu.uj.tcs.aiplayground.service.repository.ILeaderboardRepository;

import java.util.List;

public class LeaderboardService {
    private final ILeaderboardRepository leaderboardService;

    public LeaderboardService(ILeaderboardRepository leaderboardService) {
        this.leaderboardService = leaderboardService;
    }

    public List<LeaderboardDto> getLeaderboardGlobal(DatasetType datasetType) throws DatabaseException {
        try {
            return leaderboardService.getLeaderboardGlobal(datasetType);
        } catch (Exception e) {
            throw new DatabaseException(e);
        }
    }

    public List<LeaderboardDto> getLeaderboardLocal(DatasetType datasetType, String countryName) throws DatabaseException {
        try {
            return leaderboardService.getLeaderboardLocal(datasetType, countryName);
        } catch (Exception e) {
            throw new DatabaseException(e);
        }
    }
}
