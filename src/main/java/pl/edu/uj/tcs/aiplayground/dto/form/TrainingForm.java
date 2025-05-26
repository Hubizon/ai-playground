package pl.edu.uj.tcs.aiplayground.dto.form;

import pl.edu.uj.tcs.aiplayground.dto.TrainingDto;
import pl.edu.uj.tcs.aiplayground.dto.architecture.DatasetType;
import pl.edu.uj.tcs.aiplayground.dto.architecture.LossFunctionType;
import pl.edu.uj.tcs.aiplayground.dto.architecture.OptimizerType;

import java.util.UUID;

public record TrainingForm(
        Integer maxEpochs,
        Integer batchSize,
        Double learningRate,
        DatasetType dataset,
        OptimizerType optimizer,
        LossFunctionType lossFunction
) {
    public TrainingDto toDto(UUID modelVersionID) {
        return new TrainingDto(
                modelVersionID,
                maxEpochs,
                batchSize,
                learningRate,
                dataset,
                optimizer,
                lossFunction
        );
    }
}
