package pl.edu.uj.tcs.aiplayground.dto;

import java.util.List;

public record TrainingMetricDto(
        Integer epoch,
        Integer iter,
        Double loss,
        Double accuracy,
        DataLoaderType type
) {
    public static TrainingMetricDto lastMetric(List<TrainingMetricDto> metrics, DataLoaderType type) {
        for (int i = metrics.size() - 1; i >= 0; i--) {
            if (metrics.get(i).type().equals(type)) {
                return metrics.get(i);
            }
        }
        return null;
    }
}
