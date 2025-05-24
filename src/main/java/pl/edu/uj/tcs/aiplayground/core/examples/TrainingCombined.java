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
                new LayerConfig(LayerType.LINEAR, new LinearParams(4,16,true)),
                new LayerConfig(LayerType.RELU, new EmptyParams()),
                new LayerConfig(LayerType.LINEAR, new LinearParams(16,3,true)),
                new LayerConfig(LayerType.SOFTMAX,new EmptyParams())
        );

        NeuralNet nn = new NeuralNet(architecture);


        TrainingDto dto = new TrainingDto(
                UUID.randomUUID(),
                100000,
                0.1F,
                "iris.csv",
                "Adam",
                "MSE"
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