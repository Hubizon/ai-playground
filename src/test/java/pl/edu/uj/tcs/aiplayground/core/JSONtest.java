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
        NeuralNet nn2 = new NeuralNet(nn.toJson());
        assertEquals(nn.toJson(),nn2.toJson());

        List<LayerConfig> architecture2 = List.of(
                new LayerConfig(LayerType.LINEAR, new LinearParams(4,162222, false)),
                new LayerConfig(LayerType.RELU, new EmptyParams()),
                new LayerConfig(LayerType.LINEAR, new LinearParams(16222,16,true)),
                new LayerConfig(LayerType.SOFTMAX, new EmptyParams())
        );

        NeuralNet nn3 = new NeuralNet(architecture);
        NeuralNet nn4 = new NeuralNet(nn3.toJson());
        assertEquals(nn3.toJson(),nn4.toJson());
    }
}
