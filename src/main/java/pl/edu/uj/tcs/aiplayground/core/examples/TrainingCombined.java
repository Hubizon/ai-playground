package pl.edu.uj.tcs.aiplayground.core.examples;

import pl.edu.uj.tcs.aiplayground.core.NeuralNet;
import pl.edu.uj.tcs.aiplayground.dto.TrainingDto;
import pl.edu.uj.tcs.aiplayground.dto.TrainingMetricDto;
import pl.edu.uj.tcs.aiplayground.dto.architecture.*;
import pl.edu.uj.tcs.aiplayground.exception.TrainingException;
import pl.edu.uj.tcs.aiplayground.service.TrainingService;
import pl.edu.uj.tcs.aiplayground.service.repository.JooqFactory;
import pl.edu.uj.tcs.aiplayground.service.repository.TrainingRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class TrainingCombined {
    public static void main(String[] args) {
        List<LayerConfig> architecture = List.of(
                new LayerConfig(LayerType.LINEAR, new LinearParams(4, 12, true)),
                new LayerConfig(LayerType.SIGMOID, new EmptyParams()),
                new LayerConfig(LayerType.LINEAR, new LinearParams(12, 12, true)),
                new LayerConfig(LayerType.DROPOUT, new EmptyParams()),
                new LayerConfig(LayerType.LEAKYRELU, new LeakyReLUParams(new BigDecimal("0.3"))),
                new LayerConfig(LayerType.LINEAR, new LinearParams(12, 3, true))
        );

        NeuralNet nn = new NeuralNet(architecture);

        DatasetType datasetType = DatasetType.IRIS;
        datasetType.setTrainingService(new TrainingService(
                new TrainingRepository(JooqFactory.getConnection(), JooqFactory.getDSLContext())));
        TrainingDto dto = new TrainingDto(
                UUID.randomUUID(),
                100,
                32,
                0.001,
                datasetType,
                OptimizerType.ADADELTA,
                LossFunctionType.CrossEntropy
        );

        AtomicBoolean isCancelled = new AtomicBoolean(false);
        Consumer<TrainingMetricDto> callback = metric -> {
            System.out.println("Epoch " + metric.epoch() +
                    ": Loss = " + metric.loss() +
                    ", Accuracy = " + metric.accuracy());
        };

        try {
            nn.train(dto, isCancelled, callback);
        } catch (TrainingException e) {
            e.printStackTrace();
        }
    }
}