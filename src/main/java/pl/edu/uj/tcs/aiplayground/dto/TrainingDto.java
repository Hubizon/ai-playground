package pl.edu.uj.tcs.aiplayground.dto;

import pl.edu.uj.tcs.aiplayground.dto.architecture.DatasetType;
import pl.edu.uj.tcs.aiplayground.dto.architecture.LossFunctionType;
import pl.edu.uj.tcs.aiplayground.dto.architecture.OptimizerType;

import java.util.UUID;

public record TrainingDto(
        UUID modelVersionId,
        Integer maxEpochs,
        Integer batchSize,
        Double learningRate,
        DatasetType dataset,
        OptimizerType optimizer,
        LossFunctionType lossFunction
) {
}
