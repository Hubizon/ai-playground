package pl.edu.uj.tcs.aiplayground.core.examples;

import pl.edu.uj.tcs.aiplayground.core.NeuralNet;
import pl.edu.uj.tcs.aiplayground.core.NeuralNet.*;
import pl.edu.uj.tcs.aiplayground.dto.TrainingDto;
import pl.edu.uj.tcs.aiplayground.dto.TrainingMetricDto;
import pl.edu.uj.tcs.aiplayground.dto.architecture.*;
import pl.edu.uj.tcs.aiplayground.service.TrainingService;
import pl.edu.uj.tcs.aiplayground.service.repository.JooqFactory;
import pl.edu.uj.tcs.aiplayground.service.repository.TrainingRepository;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class TrainingCombined {
    public static void main(String[] args) {
        List<LayerConfig> architecture = List.of(
                new LayerConfig(LayerType.LINEAR, new LinearParams(784,512, true)),
                new LayerConfig(LayerType.SIGMOID, new EmptyParams()),
                new LayerConfig(LayerType.LINEAR, new LinearParams(512,64,true)),
                new LayerConfig(LayerType.SIGMOID, new EmptyParams()),
                new LayerConfig(LayerType.LINEAR, new LinearParams(64,10,true))
        );

        NeuralNet nn = new NeuralNet(architecture);

        DatasetType datasetType = DatasetType.MNIST;
        datasetType.setTrainingService(new TrainingService(new TrainingRepository(JooqFactory.getDSLContext())));
        TrainingDto dto = new TrainingDto(
                UUID.randomUUID(),
                100,
                0.001,
                datasetType,
                OptimizerType.ADAM,
                LossFunctionType.CrossEntropy
        );

        AtomicBoolean isCancelled = new AtomicBoolean(false);
        Consumer<TrainingMetricDto> callback = metric -> {
            System.out.println("Epoch " + metric.epoch() +
                    ": Loss = " + metric.loss() +
                    ", Accuracy = " + metric.accuracy());
        };

        nn.train(dto, isCancelled, callback);
    }
}