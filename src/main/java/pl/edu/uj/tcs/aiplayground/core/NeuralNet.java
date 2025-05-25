package pl.edu.uj.tcs.aiplayground.core;
import javafx.util.Pair;
import org.jooq.JSONB;
import org.json.JSONArray;
import org.json.JSONObject;
import pl.edu.uj.tcs.aiplayground.core.evalMetric.Accuracy;
import pl.edu.uj.tcs.aiplayground.core.layers.*;
import pl.edu.uj.tcs.aiplayground.core.loss.LossFunc;
import pl.edu.uj.tcs.aiplayground.core.optim.Optimizer;
import pl.edu.uj.tcs.aiplayground.dto.TrainingDto;
import pl.edu.uj.tcs.aiplayground.dto.TrainingMetricDto;
import pl.edu.uj.tcs.aiplayground.dto.architecture.LayerConfig;
import pl.edu.uj.tcs.aiplayground.dto.architecture.LayerType;
import pl.edu.uj.tcs.aiplayground.exception.TrainingException;

import java.time.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
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
                case "LinearLayer":
                    layer = new LinearLayer();
                    layer.loadJson(layerJson);
                    break;
                case "ReluLayer":
                    layer = new ReluLayer();
                    layer.loadJson(layerJson);
                    break;
                case "SigmoidLayer":
                    layer = new SigmoidLayer();
                    layer.loadJson(layerJson);
                    break;
                case "SoftMax":
                    layer = new SoftMaxLayer();
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
            layersArray.put(layer.toJson());
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

    public void train(TrainingDto dto, AtomicBoolean isCancelled, Consumer<TrainingMetricDto> callback) throws TrainingException {

        final int batchSize=8;
        final int numThreads  = Runtime.getRuntime().availableProcessors();;
        System.out.println("Using "+numThreads+" threads");

        Dataset dataset = dto.dataset().create();
        LossFunc lossFn = dto.lossFunction().create();
        Optimizer optimizer = dto.optimizer().create(getParams(), dto.learningRate());

        ExecutorService exec = Executors.newFixedThreadPool(numThreads);
        Instant t0 = Instant.now();
        double lossValue, lossItem, accuracy;
        try {
            for (int epoch = 0; epoch < dto.maxEpochs(); epoch++) {
                if (isCancelled.get()) break;

                double epochLoss = 0;
                int processed   = 0;
                Dataset.DataLoader loader = dataset.getDataLoader("train", batchSize);

                while (loader.hasNext()) {
                    List<Pair<Tensor,Tensor>> batch = loader.next();
                    optimizer.zeroGradient();
                    List<Future<Double>> futures = new ArrayList<>(batch.size());
                    for (Pair<Tensor,Tensor> dp : batch) {
                        futures.add(exec.submit(() -> {
                            ComputationalGraph g = new ComputationalGraph();
                            Tensor x    = dp.getKey().transpose();
                            Tensor y    = dp.getValue().transpose();
                            Tensor pred = forward(x, g);
                            double l    = lossFn.loss(pred, y);
                            g.propagate();
                            return l;
                        }));
                    }
                    double batchLoss = 0;
                    for (Future<Double> f : futures) {
                        batchLoss += f.get();
                    }
                    epochLoss += batchLoss;
                    for (Tensor p : getParams()) {
                        for (int i = 0; i < p.rows; i++)
                            for (int j = 0; j < p.cols; j++)
                                p.gradient[i][j] /= batchSize;
                    }
                    if(processed % 4096 == 0) System.out.println(processed+" "+batchLoss);
                    epochLoss += batchLoss;
                    optimizer.optimize();
                    processed+=batchSize;
                }

                Accuracy acc = new Accuracy();
                accuracy = acc.eval(dataset,this);
                if (isCancelled.get())
                    break;
                TrainingMetricDto metric = new TrainingMetricDto(epoch, epochLoss, accuracy);
                callback.accept(metric);
                Instant t1 = Instant.now();
                System.out.println("Epoch "+epoch+" took "+Duration.between(t0, t1).toMillis()+" ms");
                t0 = t1;
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new TrainingException(e);
        } finally {
            exec.shutdown();
        }
    }

    static class BatchResult {
        final double loss;
        final List<double[][]> grads;
        BatchResult(double loss, List<double[][]> grads) {
            this.loss = loss;
            this.grads = grads;
        }
    }
}
