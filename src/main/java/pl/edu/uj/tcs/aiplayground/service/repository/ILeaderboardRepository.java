package pl.edu.uj.tcs.aiplayground.service.repository;

import pl.edu.uj.tcs.aiplayground.dto.LeaderboardDto;
import pl.edu.uj.tcs.aiplayground.dto.architecture.DatasetType;

import java.util.List;

public interface ILeaderboardRepository {
    List<LeaderboardDto> getLeaderboardGlobal(DatasetType datasetType);

    List<LeaderboardDto> getLeaderboardLocal(DatasetType datasetType, String countryName);
}
