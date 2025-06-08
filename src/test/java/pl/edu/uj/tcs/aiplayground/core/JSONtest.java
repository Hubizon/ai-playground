package pl.edu.uj.tcs.aiplayground.core;

import org.junit.jupiter.api.Test;
import pl.edu.uj.tcs.aiplayground.dto.architecture.EmptyParams;
import pl.edu.uj.tcs.aiplayground.dto.architecture.LayerConfig;
import pl.edu.uj.tcs.aiplayground.dto.architecture.LayerType;
import pl.edu.uj.tcs.aiplayground.dto.architecture.LinearParams;
import pl.edu.uj.tcs.aiplayground.exception.InvalidHyperparametersException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JSONtest {
    @Test
    void basicTest() throws InvalidHyperparametersException {
        List<LayerConfig> architecture = List.of(
                new LayerConfig(LayerType.LINEAR, new LinearParams(4, 16, true)),
                new LayerConfig(LayerType.SIGMOID, new EmptyParams()),
                new LayerConfig(LayerType.LINEAR, new LinearParams(16, 16, true)),
                new LayerConfig(LayerType.SIGMOID, new EmptyParams()),
                new LayerConfig(LayerType.LINEAR, new LinearParams(16, 3, true))
        );

        NeuralNet nn = new NeuralNet(architecture);
        NeuralNet nn2 = new NeuralNet(nn.toJson());
        assertEquals(nn.toJson(), nn2.toJson());

        List<LayerConfig> architecture2 = List.of(
                new LayerConfig(LayerType.LINEAR, new LinearParams(4, 162222, false)),
                new LayerConfig(LayerType.RELU, new EmptyParams()),
                new LayerConfig(LayerType.LINEAR, new LinearParams(16222, 16, true)),
                new LayerConfig(LayerType.SOFTMAX, new EmptyParams())
        );

        NeuralNet nn3 = new NeuralNet(architecture2);
        NeuralNet nn4 = new NeuralNet(nn3.toJson());
        assertEquals(nn3.toJson(), nn4.toJson());
    }
}
