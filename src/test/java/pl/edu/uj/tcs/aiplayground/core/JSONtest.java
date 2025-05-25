package pl.edu.uj.tcs.aiplayground.core;

import org.jooq.JSONB;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.edu.uj.tcs.aiplayground.dto.TrainingDto;
import pl.edu.uj.tcs.aiplayground.dto.TrainingMetricDto;
import pl.edu.uj.tcs.aiplayground.dto.architecture.*;
import pl.edu.uj.tcs.aiplayground.exception.DatabaseException;
import pl.edu.uj.tcs.aiplayground.exception.TrainingException;
import pl.edu.uj.tcs.aiplayground.service.TrainingService;
import pl.edu.uj.tcs.aiplayground.service.repository.JooqFactory;
import pl.edu.uj.tcs.aiplayground.service.repository.TrainingRepository;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class JSONtest {
    @Test
    void basicTest() throws DatabaseException {
        List<LayerConfig> architecture = List.of(
                new LayerConfig(LayerType.LINEAR, new LinearParams(4,16, true)),
                new LayerConfig(LayerType.SIGMOID, new EmptyParams()),
                new LayerConfig(LayerType.LINEAR, new LinearParams(16,16,true)),
                new LayerConfig(LayerType.SIGMOID, new EmptyParams()),
                new LayerConfig(LayerType.LINEAR, new LinearParams(16,3,true))
        );

        NeuralNet nn = new NeuralNet(architecture);
        System.out.println(nn.toJson());
        NeuralNet nn2 = new NeuralNet(nn.toJson());
        System.out.println(nn2.toJson());
        assertEquals(nn.toJson(),nn2.toJson());

        DatasetType datasetType = DatasetType.IRIS;
        datasetType.setTrainingService(new TrainingService(new TrainingRepository(JooqFactory.getDSLContext())));
        TrainingDto dto = new TrainingDto(
                UUID.randomUUID(),
                10,
                32,
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

        try {
            nn2.train(dto, isCancelled, callback);
        } catch (TrainingException e) {
            e.printStackTrace();
        }
    }
}
