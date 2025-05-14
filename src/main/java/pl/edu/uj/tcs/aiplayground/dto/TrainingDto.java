package pl.edu.uj.tcs.aiplayground.dto;

import java.util.UUID;

public record TrainingDto(
        UUID modelVersionId,
        Integer maxEpochs,
        Float learningRate,
        String dataset,
        String optimizer,
        String lossFunction
) {
}
