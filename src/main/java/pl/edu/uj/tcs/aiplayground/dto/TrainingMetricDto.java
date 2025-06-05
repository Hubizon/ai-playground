package pl.edu.uj.tcs.aiplayground.dto;

public record TrainingMetricDto(
        Integer epoch,
        Integer iter,
        Double loss,
        Double accuracy,
        DataLoaderType type
) {
}
