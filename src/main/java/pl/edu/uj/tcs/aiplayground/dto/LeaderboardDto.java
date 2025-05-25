package pl.edu.uj.tcs.aiplayground.dto;

public record LeaderboardDto(
        String userName,
        Double accuracy,
        Double loss
) {
}
