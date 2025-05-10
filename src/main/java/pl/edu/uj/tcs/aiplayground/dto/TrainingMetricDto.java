package pl.edu.uj.tcs.aiplayground.dto;

public record TrainingMetricDto(
        Integer epoch,
        Double loss,
        Double accuracy
) {
}
