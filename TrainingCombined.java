package pl.edu.uj.tcs.aiplayground.core.examples;

import pl.edu.uj.tcs.aiplayground.core.NeuralNet;
import pl.edu.uj.tcs.aiplayground.core.NeuralNet.*;
import pl.edu.uj.tcs.aiplayground.dto.TrainingDto;
import pl.edu.uj.tcs.aiplayground.dto.TrainingMetricDto;
import pl.edu.uj.tcs.aiplayground.dto.architecture.EmptyParams;
import pl.edu.uj.tcs.aiplayground.dto.architecture.LayerConfig;
import pl.edu.uj.tcs.aiplayground.dto.architecture.LayerType;
import pl.edu.uj.tcs.aiplayground.dto.architecture.LinearParams;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class TrainingCombined {
    public static void main(String[] args) {
        List<LayerConfig> architecture = List.of(
                new LayerConfig(LayerType.LINEAR, new LinearParams(3,10,true)),
                new LayerConfig(LayerType.RELU, new EmptyParams()),
                new LayerConfig(LayerType.LINEAR, new LinearParams(10,3,true)),
                new LayerConfig(LayerType.SIGMOID,new EmptyParams())
        );

        NeuralNet nn = new NeuralNet(architecture);

        String mockDataset = """
        {
            "data": [
                {"input": [0, 0], "label": [0]},
                {"input": [0, 1], "label": [1]},
                {"input": [1, 0], "label": [1]},
                {"input": [1, 1], "label": [0]}
            ]
        }
        """;

        TrainingDto dto = new TrainingDto(
                UUID.randomUUID(),
                100,
                0.1F,
                "iris.csv",
                "SGD",
                "MSE"
        );

        AtomicBoolean isCancelled = new AtomicBoolean(false);
        Consumer<TrainingMetricDto> callback = metric -> {
            System.out.println("Epoch " + metric.epoch() +
                    ": Loss = " + metric.loss() +
                    ", Accuracy = " + metric.accuracy());
        };

        // 6. Run training
        nn.train(dto, isCancelled, callback);
    }
}