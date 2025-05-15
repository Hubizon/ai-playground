package pl.edu.uj.tcs.aiplayground.core;

import javafx.util.Pair;
import org.jooq.JSONB;
import org.json.JSONArray;
import org.json.JSONObject;
import pl.edu.uj.tcs.aiplayground.core.evalMetric.Accuracy;
import pl.edu.uj.tcs.aiplayground.core.layers.Layer;
import pl.edu.uj.tcs.aiplayground.core.layers.LinearLayer;
import pl.edu.uj.tcs.aiplayground.core.layers.ReluLayer;
import pl.edu.uj.tcs.aiplayground.core.layers.SigmoidLayer;
import pl.edu.uj.tcs.aiplayground.core.loss.BCE;
import pl.edu.uj.tcs.aiplayground.core.loss.LossFunc;
import pl.edu.uj.tcs.aiplayground.core.optim.AdamOptimizer;
import pl.edu.uj.tcs.aiplayground.core.optim.Optimizer;
import pl.edu.uj.tcs.aiplayground.dto.TrainingDto;
import pl.edu.uj.tcs.aiplayground.dto.TrainingMetricDto;
import pl.edu.uj.tcs.aiplayground.dto.architecture.LayerConfig;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class NeuralNet {

    public List<Layer> layers = new ArrayList<>();

    public NeuralNet() {
        // TODO
    }

    public NeuralNet(List<LayerConfig> configs) {
        this.layers = configs.stream()
                .map(LayerConfig::toLayer)
                .toList();
        // TODO
    }

    public NeuralNet(JSONB architecture) {
        // TODO
    }

    public static NeuralNet load(String filePath) {
        NeuralNet neuralNet = new NeuralNet();
        try {
            File file = new File(filePath);
            Scanner scanner = new Scanner(file);
            StringBuilder jsonContent = new StringBuilder();
            while (scanner.hasNextLine()) {
                jsonContent.append(scanner.nextLine());
            }
            scanner.close();

            JSONObject jsonObject = new JSONObject(jsonContent.toString());
            JSONArray layersArray = jsonObject.getJSONArray("layers");

            for (int i = 0; i < layersArray.length(); i++) {
                JSONObject layerWrapper = layersArray.getJSONObject(i);
                String layerJsonString = layerWrapper.getString("layer");
                JSONObject layerJson = new JSONObject(layerJsonString);
                String type = layerJson.getString("type");

                switch (type) {
                    case "LinearLayer":
                        int inputSize = layerJson.getInt("inputSize");
                        int outputSize = layerJson.getInt("outputSize");
                        boolean useBias = layerJson.getBoolean("useBias");
                        LinearLayer linearLayer = new LinearLayer(inputSize, outputSize, useBias);
                        neuralNet.layers.add(linearLayer);
                        break;
                    case "ReluLayer":
                        neuralNet.layers.add(new ReluLayer());
                        break;
                    case "SigmoidLayer":
                        neuralNet.layers.add(new SigmoidLayer());
                        break;
                    default:
                        System.err.println("Unknown layer type: " + type);
                        break;
                }
            }

        } catch (IOException e) {
            System.err.println("Error loading model: " + e.getMessage());
        }
        return neuralNet;
    }

    public ArrayList<Tensor> getParams() {
        ArrayList<Tensor> params = new ArrayList<>();
        for (Layer layer : layers) {
            params.addAll(layer.getParams());
        }
        return params;
    }

    public Tensor forward(Tensor input, ComputationalGraph graph) {
        Tensor forwardTensor = input;
        for (Layer layer : layers) {
            forwardTensor = layer.forward(forwardTensor, graph);
        }
        return forwardTensor;
    }

    public void save(String outputPath) {
        JSONObject json = new JSONObject();
        JSONArray layersArray = new JSONArray();

        for (Layer layer : layers) {
            JSONObject layerJson = new JSONObject();
            layerJson.put("layer", layer.toJson());
            layersArray.put(layerJson);
        }

        json.put("layers", layersArray);

        try (FileWriter file = new FileWriter(outputPath)) {
            file.write(json.toString(4)); // Pretty print with 4 spaces
        } catch (IOException e) {
            System.err.println("Error saving model: " + e.getMessage());
        }
    }

    public List<LayerConfig> toConfigList() {
        return layers.stream()
                .map(Layer::toConfig)
                .toList();
    }

    public void train(TrainingDto dto, AtomicBoolean isCancelled, Consumer<TrainingMetricDto> callback) {
        Dataset dataset = new Dataset(new ArrayList<>(List.of(3)),new ArrayList<>(List.of(3)));
        dataset.load(dto.dataset(), 0.8F);
        LossFunc loss = new BCE();
        Optimizer optimizer = new AdamOptimizer(this.getParams(),0.1);
        double lossValue, accuracy;
        for (int epoch = 0; epoch < dto.maxEpochs(); epoch++) {
            Dataset.DataLoader trainLoader = dataset.getDataLoader("train",32);
            ArrayList<Pair<Tensor,Tensor>> datapionts;
            Tensor output;
            ComputationalGraph graph = new ComputationalGraph();
            lossValue = 0.0;
            while(trainLoader.hasNext()) {
                datapionts = trainLoader.next();
                optimizer.zeroGradient();
                graph.clear();
                for (Pair<Tensor,Tensor> datapoint : datapionts) {
                    output = forward(datapoint.getKey(), graph);
                    lossValue += loss.loss(output,datapoint.getValue());
                }
                graph.propagate();
                optimizer.optimize();
            }
            Accuracy acc = new Accuracy();
            accuracy = acc.eval(dataset,this);
            if (isCancelled.get())
                break;
            TrainingMetricDto metric = new TrainingMetricDto(epoch, lossValue, accuracy);
            callback.accept(metric);
        }
    }


    public JSONB toJson() {
        return null;
    }
}
