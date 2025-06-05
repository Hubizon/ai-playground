package pl.edu.uj.tcs.aiplayground.core;

import javafx.util.Pair;
import org.jooq.JSONB;
import org.json.JSONArray;
import org.json.JSONObject;
import pl.edu.uj.tcs.aiplayground.core.evalMetric.Accuracy;
import pl.edu.uj.tcs.aiplayground.core.layers.Layer;
import pl.edu.uj.tcs.aiplayground.core.layers.LinearLayer;
import pl.edu.uj.tcs.aiplayground.core.loss.LossFunc;
import pl.edu.uj.tcs.aiplayground.core.optim.Optimizer;
import pl.edu.uj.tcs.aiplayground.dto.DataLoaderType;
import pl.edu.uj.tcs.aiplayground.dto.TrainingDto;
import pl.edu.uj.tcs.aiplayground.dto.TrainingMetricDto;
import pl.edu.uj.tcs.aiplayground.dto.architecture.LayerConfig;
import pl.edu.uj.tcs.aiplayground.dto.architecture.LayerParams;
import pl.edu.uj.tcs.aiplayground.dto.architecture.LayerType;
import pl.edu.uj.tcs.aiplayground.exception.InvalidHyperparametersException;
import pl.edu.uj.tcs.aiplayground.exception.TrainingException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class NeuralNet {

    public List<Layer> layers = new ArrayList<>();

    public NeuralNet() {
    }

    public NeuralNet(List<LayerConfig> configs) {
        this.layers = configs.stream()
                .map(LayerConfig::toLayer)
                .toList();
    }

    public NeuralNet(JSONB architecture) throws InvalidHyperparametersException  {
        String jsonString = architecture.data();
        JSONObject jsonObject = new JSONObject(jsonString);
        JSONArray layersArray = jsonObject.getJSONArray("layers");

        this.layers = new ArrayList<>();

        for (int i = 0; i < layersArray.length(); i++) {
            JSONObject layerJson = layersArray.getJSONObject(i);

            String typeStr = layerJson.getString("type");
            JSONObject paramsJson = layerJson.optJSONObject("params");

            LayerType type = Arrays.stream(LayerType.values())
                    .filter(t -> t.toString().equalsIgnoreCase(typeStr))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Unknown layer type: " + typeStr));

            LayerParams params = type.getParams().loadFromJson(paramsJson);
            Layer layer = new LayerConfig(type, params).toLayer();
            layers.add(layer);
        }
    }

    public static Optional<LayerType> getLayerTypeFor(Layer layer) {
        return Arrays.stream(LayerType.values())
                .filter(t -> t.createLayer(t.getParams()).getClass().equals(layer.getClass()))
                .findFirst();
    }

    public JSONB toJson() {
        JSONArray layersArray = new JSONArray();

        for (Layer layer : layers) {
            LayerConfig config = layer.toConfig();

            JSONObject layerJson = new JSONObject();
            layerJson.put("type", config.type().toString());

            JSONObject paramsJson = config.params().toJson();
            layerJson.put("params", paramsJson);


            layersArray.put(layerJson);
        }

        JSONObject jsonObject = new JSONObject();
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

    public static Pair<Double, Integer> SingleExampleTraining(Pair<Tensor, Tensor> dp,NeuralNet model, LossFunc lossFn) {
        ComputationalGraph g = new ComputationalGraph();
        Tensor x = dp.getKey().transpose();
        Tensor y = dp.getValue().transpose();
        Tensor pred = model.forward(x, g);
        double l = lossFn.loss(pred, y);
        double max = Double.NEGATIVE_INFINITY;
        int maxIndex1 = -1, maxIndex2 = -1;
        for (int i = 0; i < pred.rows; i++) {
            for (int j = 0; j < pred.cols; j++) {
                if (pred.data[i][j] > max) {
                    max = pred.data[i][j];
                    maxIndex1 = i;
                    maxIndex2 = j;
                }
            }
        }
        int correct = 0;
        if (dp.getValue().transpose().data[maxIndex1][maxIndex2] == 1) {
            correct = 1;
        }
        g.propagate();
        return new Pair<Double, Integer>(l, correct);
    }

    public void train(TrainingDto dto, AtomicBoolean isCancelled, Consumer<TrainingMetricDto> callback) throws TrainingException {

        final int batchSize = dto.batchSize();
        final int numThreads = Runtime.getRuntime().availableProcessors();
        System.out.println("Using " + numThreads + " threads");

        Dataset dataset = dto.dataset().create();
        LinearLayer lastLayer = null;
        for (Layer layer : layers) {
            if (layer.getClass() == LinearLayer.class) {
                LinearLayer linearLayer = (LinearLayer) layer;
                if (dataset.inputShape.getFirst() != linearLayer.inputSize && lastLayer == null) {
                    throw new TrainingException("Wrong input size: " + linearLayer.inputSize + " for dataset with input size: " + dataset.inputShape.getFirst());
                }
                if (lastLayer != null) {
                    if (linearLayer.inputSize != lastLayer.outputSize) {
                        throw new TrainingException("Wrong input size: " + linearLayer.inputSize + " for layer with output size: " + lastLayer.outputSize);
                    }
                }
                lastLayer = linearLayer;
            }
        }
        if (lastLayer == null) {
            throw new TrainingException("No LinearLayer found in architecture");
        }
        if (lastLayer.outputSize != dataset.outputShape.getFirst()) {
            throw new TrainingException("Wrong output size: " + lastLayer.outputSize + " for dataset with output size: " + dataset.outputShape.getFirst());
        }
        LossFunc lossFn = dto.lossFunction().create();
        Optimizer optimizer = dto.optimizer().create(getParams(), dto.learningRate());

        ExecutorService exec = Executors.newFixedThreadPool(numThreads);
        try {
            int iter = 0;
            for (int epoch = 0; epoch < dto.maxEpochs(); epoch++) {
                if (isCancelled.get()) break;
                int processed = 0;
                double lastLoss = 0;
                Dataset.DataLoader loader = dataset.getDataLoader(DataLoaderType.TRAIN, batchSize);
                int num_correct = 0;
                while (loader.hasNext()) {
                    List<Pair<Tensor, Tensor>> batch = loader.next();
                    optimizer.zeroGradient();
                    List<Future<Pair<Double,Integer>>> futures = new ArrayList<>(batch.size());
                    for (Pair<Tensor, Tensor> dp : batch) {
                        futures.add(exec.submit(() -> {
                            return SingleExampleTraining(dp, this, lossFn);
                        }));
                    }
                    double batchLoss = 0;
                    for (Future<Pair<Double,Integer>> f : futures) {
                        batchLoss += f.get().getKey();
                        num_correct += f.get().getValue();
                    }
                    for (Tensor p : getParams()) {
                        for (int i = 0; i < p.rows; i++)
                            for (int j = 0; j < p.cols; j++)
                                p.gradient[i][j] /= batchSize;
                    }
                    lastLoss += batchLoss;
                    optimizer.optimize();
                    processed += batchSize;

                    if (processed >= dataset.trainData.size()*dto.maxEpochs()/100) {
                        Accuracy.AccAndLoss acc_test = Accuracy.eval(this, dataset.getDataLoader(DataLoaderType.TEST, 1), lossFn);
                        callback.accept(new TrainingMetricDto(epoch, iter, acc_test.loss(), acc_test.accuracy(), DataLoaderType.TEST));
                        callback.accept(new TrainingMetricDto(epoch, iter, lastLoss / processed, (double) num_correct *100/processed, DataLoaderType.TRAIN));
                        processed = 0;
                        lastLoss = 0;
                        num_correct = 0;
                        iter++;
                        if (isCancelled.get())
                            break;
                    }
                }

                Accuracy.AccAndLoss acc_test = Accuracy.eval(this, dataset.getDataLoader(DataLoaderType.TEST, 1), lossFn);
                Accuracy.AccAndLoss acc_train = Accuracy.eval(this, dataset.getDataLoader(DataLoaderType.TRAIN, 1), lossFn);

                callback.accept(new TrainingMetricDto(epoch, iter, acc_test.loss(), acc_test.accuracy(), DataLoaderType.TEST));
                callback.accept(new TrainingMetricDto(epoch, iter, acc_train.loss(), acc_train.accuracy(), DataLoaderType.TRAIN));
                iter++;

                if (isCancelled.get())
                    break;
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new TrainingException(e);
        } finally {
            exec.shutdown();
        }
    }
}
