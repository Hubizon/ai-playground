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
import pl.edu.uj.tcs.aiplayground.core.loss.MSE;
import pl.edu.uj.tcs.aiplayground.core.optim.AdamOptimizer;
import pl.edu.uj.tcs.aiplayground.core.optim.Optimizer;
import pl.edu.uj.tcs.aiplayground.dto.TrainingDto;
import pl.edu.uj.tcs.aiplayground.dto.TrainingMetricDto;
import pl.edu.uj.tcs.aiplayground.dto.architecture.LayerConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class NeuralNet {

    public List<Layer> layers = new ArrayList<>();

    public NeuralNet() {
        layers = new ArrayList<>();
    }

    public NeuralNet(List<LayerConfig> configs) {
        this.layers = configs.stream()
                .map(LayerConfig::toLayer)
                .toList();
    }

    public NeuralNet(JSONB architecture) {
        String jsonString = architecture.data(); // Get the JSON string from JSONB
        JSONObject jsonObject = new JSONObject(jsonString);
        JSONArray layersArray = jsonObject.getJSONArray("layers");

        this.layers = new ArrayList<>();
        for (int i = 0; i < layersArray.length(); i++) {
            JSONObject layerJson = layersArray.getJSONObject(i);
            String type = layerJson.getString("type");
            Layer layer;
            switch (type) {
                case "linear":
                    layer = new LinearLayer();
                    layer.loadJson(layerJson);
                    break;
                case "relu":
                    layer = new ReluLayer();
                    layer.loadJson(layerJson);
                    break;
                case "sigmoid":
                    layer = new SigmoidLayer();
                    layer.loadJson(layerJson);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown layer type: " + type);
            }
            this.layers.add(layer);
        }
    }

    public JSONB toJson() {
        JSONObject jsonObject = new JSONObject();
        JSONArray layersArray = new JSONArray();

        for (Layer layer : layers) {
            layersArray.put(new JSONObject(layer.toJson()));
        }
        jsonObject.put("layers", layersArray);

        return JSONB.jsonb(jsonObject.toString());
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

    public List<LayerConfig> toConfigList() {
        return layers.stream()
                .map(Layer::toConfig)
                .toList();
    }

    public void train(TrainingDto dto, AtomicBoolean isCancelled, Consumer<TrainingMetricDto> callback) {
        Dataset dataset = new Dataset(new ArrayList<>(List.of(4)),new ArrayList<>(List.of(3)));
        dataset.load(dto.dataset(), 0.8F);
        LossFunc loss = new MSE();
        Optimizer optimizer = new AdamOptimizer(this.getParams(),0.1);
        double lossValue, accuracy;
        for (int epoch = 0; epoch < dto.maxEpochs(); epoch++) {
            Dataset.DataLoader trainLoader = dataset.getDataLoader("train",16);
            ArrayList<Pair<Tensor,Tensor>> datapionts;
            Tensor output;
            ComputationalGraph graph = new ComputationalGraph();
            lossValue = 0.0;
            while(trainLoader.hasNext()) {
                datapionts = trainLoader.next();
                optimizer.zeroGradient();
                graph.clear();
                for (Pair<Tensor,Tensor> datapoint : datapionts) {
                    output = forward(datapoint.getKey().transpose(), graph);
                    lossValue += loss.loss(output,datapoint.getValue().transpose());
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
}
