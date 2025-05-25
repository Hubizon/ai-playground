package pl.edu.uj.tcs.aiplayground.dto.validation;

import pl.edu.uj.tcs.aiplayground.dto.form.TrainingForm;
import pl.edu.uj.tcs.aiplayground.exception.InvalidHyperparametersException;

public class TrainingValidation {
    public static void validateTrainingForm(TrainingForm trainingForm) throws InvalidHyperparametersException {
        if (trainingForm.maxEpochs() == null || trainingForm.maxEpochs() <= 0)
            throw new InvalidHyperparametersException("Max epochs must be a positive integer.\n");

        if (trainingForm.batchSize() == null || trainingForm.batchSize() <= 0)
            throw new InvalidHyperparametersException("Batch size must be a positive integer.\n");

        if (trainingForm.learningRate() == null || trainingForm.learningRate() <= 0 || trainingForm.learningRate() > 10.0)
            throw new InvalidHyperparametersException("Learning rate must be in (0, 10].\n");

        if (trainingForm.dataset() == null)
            throw new InvalidHyperparametersException("Dataset type must be specified.\n");

        if (trainingForm.optimizer() == null)
            throw new InvalidHyperparametersException("Optimizer type must be specified.\n");

        if (trainingForm.lossFunction() == null)
            throw new InvalidHyperparametersException("Loss function type must be specified.\n");
    }
}
