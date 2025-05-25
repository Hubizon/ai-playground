package pl.edu.uj.tcs.aiplayground.core;
import org.junit.jupiter.api.Test;
import pl.edu.uj.tcs.aiplayground.dto.TrainingDto;
import pl.edu.uj.tcs.aiplayground.dto.TrainingMetricDto;
import pl.edu.uj.tcs.aiplayground.dto.architecture.*;
import pl.edu.uj.tcs.aiplayground.exception.TrainingException;
import pl.edu.uj.tcs.aiplayground.service.TrainingService;
import pl.edu.uj.tcs.aiplayground.service.repository.JooqFactory;
import pl.edu.uj.tcs.aiplayground.service.repository.TrainingRepository;
import java.util.concurrent.atomic.AtomicReference;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class NeuralNetTest {

    @Test
    void trainIrisTest() {
        List<LayerConfig> architecture = List.of(
                new LayerConfig(LayerType.LINEAR, new LinearParams(4,12, true)),
                new LayerConfig(LayerType.RELU, new EmptyParams()),
                new LayerConfig(LayerType.LINEAR, new LinearParams(12,12,true)),
                new LayerConfig(LayerType.RELU, new EmptyParams()),
                new LayerConfig(LayerType.LINEAR, new LinearParams(12,3,true))
        );

        NeuralNet nn = new NeuralNet(architecture);

        DatasetType datasetType = DatasetType.IRIS;
        datasetType.setTrainingService(new TrainingService(new TrainingRepository(JooqFactory.getDSLContext())));
        TrainingDto dto = new TrainingDto(
                UUID.randomUUID(),
                10,
                8,
                0.001,
                datasetType,
                OptimizerType.ADADELTA,
                LossFunctionType.CrossEntropy
        );

        AtomicBoolean isCancelled = new AtomicBoolean(false);

        AtomicReference<Double> lastAccuracy = new AtomicReference<>(0.0);

        Consumer<TrainingMetricDto> callback = metric -> {
            lastAccuracy.set(metric.accuracy());
        };

        try {
            nn.train(dto, isCancelled, callback);
        } catch (TrainingException e) {
            e.printStackTrace();
        }
        assertTrue(lastAccuracy.get() >0.8, "Accuracy should be greater than " + 0.8);
    }
}