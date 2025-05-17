package pl.edu.uj.tcs.aiplayground.viewmodel;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.uj.tcs.aiplayground.core.NeuralNet;
import pl.edu.uj.tcs.aiplayground.core.TrainingHandler;
import pl.edu.uj.tcs.aiplayground.dto.*;
import pl.edu.uj.tcs.aiplayground.dto.architecture.LayerConfig;
import pl.edu.uj.tcs.aiplayground.dto.architecture.LayerParams;
import pl.edu.uj.tcs.aiplayground.dto.architecture.LayerType;
import pl.edu.uj.tcs.aiplayground.dto.form.ModelForm;
import pl.edu.uj.tcs.aiplayground.exception.DatabaseException;
import pl.edu.uj.tcs.aiplayground.exception.ModelModificationException;
import pl.edu.uj.tcs.aiplayground.service.ModelService;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class MainViewModel {
    private static final Logger logger = LoggerFactory.getLogger(MainViewModel.class);
    private final ModelService modelService;
    private final AtomicBoolean isCancelled = new AtomicBoolean(false);
    private final StringProperty statusMessage = new SimpleStringProperty();
    private final ObservableList<TrainingMetricDto> liveMetrics = FXCollections.observableArrayList();
    private final ObservableList<LayerConfig> layers = FXCollections.observableArrayList();
    private final BooleanProperty isTrainingInProgress = new SimpleBooleanProperty(false);
    private final BooleanProperty isPreviousVersion = new SimpleBooleanProperty(false);
    private final BooleanProperty isNextVersion = new SimpleBooleanProperty(false);
    private final BooleanProperty isModelLoaded = new SimpleBooleanProperty(false);
    private UserDto user = null;
    private ModelDto model = null;

    public MainViewModel(ModelService modelService) {
        this.modelService = modelService;
    }

    private void setupModel() {
        layers.clear();
        if (model != null) {
            NeuralNet neuralNet = new NeuralNet(model.architecture());
            layers.addAll(neuralNet.toConfigList());
            updateIsPreviousVersion();
            updateIsNextVersion();
        }
        isModelLoaded.set(model != null);
    }

    private void setUser(UserDto user) {
        this.user = user;
    }

    private void updateIsPreviousVersion() {
        if (!isModelLoaded.get())
            isPreviousVersion.set(false);
        try {
            Integer previousVersion = modelService.getPreviousVersion(
                    user.userId(),
                    model.modelName(),
                    model.versionNumber());
            isPreviousVersion.set(previousVersion != null);
        } catch (DatabaseException e) {
            logger.error("Failed to get the model version for model={}, error={}",
                    model, e.getMessage(), e);
            statusMessage.set("Internal Error");
            isPreviousVersion.set(false);
        }
    }

    private void updateIsNextVersion() {
        if (!isModelLoaded.get())
            isNextVersion.set(false);
        try {
            Integer nextVersion = modelService.getNextVersion(
                    user.userId(),
                    model.modelName(),
                    model.versionNumber());
            isNextVersion.set(nextVersion != null);
        } catch (DatabaseException e) {
            logger.error("Failed to get the model version for model={}, error={}",
                    model, e.getMessage(), e);
            statusMessage.set("Internal Error");
            isNextVersion.set(false);
        }
    }

    public StringProperty statusMessageProperty() {
        return statusMessage;
    }

    public ObservableList<TrainingMetricDto> liveMetricsProperty() {
        return liveMetrics;
    }

    public BooleanProperty isTrainingInProgressProperty() {
        return isTrainingInProgress;
    }

    public BooleanProperty isPreviousVersionProperty() {
        return isPreviousVersion;
    }

    public BooleanProperty isNextVersionProperty() {
        return isNextVersion;
    }

    public BooleanProperty isModelLoadedProperty() {
        return isModelLoaded;
    }

    public ObservableList<LayerConfig> layersProperty() {
        return layers;
    }

    public void addLayer(LayerType layer) {
        try {
            LayerParams defaultParams = layer.getParamType().getDeclaredConstructor().newInstance();
            LayerConfig config = new LayerConfig(layer, defaultParams);
            layers.add(config);
        } catch(NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            logger.error("Failed to add a layer for layer={}, error={}",
                    layer, e.getMessage(), e);
            statusMessage.set("Internal Error");
        }
    }

    public void removeLayer(int idx) {
        layers.remove(idx);
    }

    public void updateLayer(int idx, LayerParams layerParams) {
        LayerType type = layers.get(idx).type();
        layers.set(idx, new LayerConfig(type, layerParams));
    }

    public boolean isLoggedIn() {
        return user != null;
    }

    public String getModelName() {
        if (isModelLoaded.get())
            return model.modelName();
        return null;
    }

    public Integer getModelVersionNumber() {
        if (isModelLoaded.get())
            return model.versionNumber();
        return null;
    }

    public void setPreviousVersion() {
        if (!isPreviousVersion.get())
            return;
        try {
            Integer previousVersion = modelService.getPreviousVersion(
                    user.userId(),
                    model.modelName(),
                    model.versionNumber());
            model = modelService.getModel(user.userId(), model.modelName(), previousVersion);
        } catch (DatabaseException e) {
            logger.error("Failed to set the model version for model={}, error={}",
                    model, e.getMessage(), e);
            statusMessage.set("Internal Error");
        }
        setupModel();
    }

    public void setNextVersion() {
        if (!isNextVersion.get())
            return;
        try {
            Integer nextVersion = modelService.getNextVersion(
                    user.userId(),
                    model.modelName(),
                    model.versionNumber());
            model = modelService.getModel(user.userId(), model.modelName(), nextVersion);
        } catch (DatabaseException e) {
            logger.error("Failed to set the model version for model={}, error={}",
                    model, e.getMessage(), e);
            statusMessage.set("Internal Error");
        }
        setupModel();
    }

    public List<String> getUserModelNames(UserDto user) {
        try {
            return modelService.getUserModelNames(user.userId());
        } catch (DatabaseException e) {
            logger.error("Failed to get model names for user={}, error={}", user, e.getMessage(), e);
            statusMessage.set("Internal Error");
            return null;
        }
    }

    public void setModel(UserDto user, String modelName) {
        try {
            this.model = modelService.getModel(user.userId(), modelName);
            setUser(user);
        } catch (DatabaseException e) {
            logger.error("Failed to get the model for user={}, modelName={}, error={}",
                    user, modelName, e.getMessage(), e);
            statusMessage.set("Internal Error");
            this.model = null;
            setUser(null);
        }
        setupModel();
    }

    public void createNewModel(UserDto user, String modelName) {
        try {
            NeuralNet neuralNet = new NeuralNet();
            ModelForm modelForm = new ModelForm(
                    user.userId(),
                    modelName,
                    neuralNet.toJson()
            );
            this.model = modelService.addModel(modelForm);
            setUser(user);
        } catch (ModelModificationException e) {
            logger.error("Failed to create the model for user={}, modelName={}, error={}",
                    user, modelName, e.getMessage(), e);
            statusMessage.set("Illegal model name");
            this.model = null;
            setUser(null);
        } catch (DatabaseException e) {
            logger.error("Failed to create the model for user={}, modelName={}, error={}",
                    user, modelName, e.getMessage(), e);
            statusMessage.set("Internal Error");
            this.model = null;
            setUser(null);
        }
        setupModel();
    }

    public void stopTraining() {
        isCancelled.set(true);
    }

    private void runTraining(TrainingDto dto, NeuralNet net, TrainingHandler handler) {
        net.train(dto, isCancelled, metric -> {
            Platform.runLater(() -> {
                handler.addNewTrainingMetric(metric);
                liveMetrics.add(metric);
            });
        });
    }

    public void train(Integer maxEpochs, Float learningRate, String dataset, String optimizer, String lossFunction) {
        isCancelled.set(false);
        isTrainingInProgress.set(true);
        liveMetrics.clear();

        new Thread(() -> {
            TrainingHandler handler = null;

            try {
                NeuralNet net = new NeuralNet(layers);
                model = modelService.addModel(new ModelForm(user.userId(), model.modelName(), net.toJson()));

                TrainingDto trainingDto = new TrainingDto(
                        model.modelVersionId(),
                        maxEpochs,
                        learningRate,
                        dataset,
                        optimizer,
                        lossFunction
                );
                handler = new TrainingHandler(trainingDto);
                handler.updateTrainingStatus(StatusName.IN_PROGRESS);

                runTraining(trainingDto, net, handler);

                if (isCancelled.get()) {
                    handler.updateTrainingStatus(StatusName.CANCELLED);
                    Platform.runLater(() -> statusMessage.set("Training cancelled."));
                } else {
                    handler.updateTrainingStatus(StatusName.FINISHED);
                    Platform.runLater(() -> statusMessage.set("Training finished."));
                }
            } catch (Exception e) {
                if (handler != null)
                    handler.updateTrainingStatus(StatusName.ERROR);
                Platform.runLater(() -> {
                    logger.error("Training error", e);
                    statusMessage.set("Training failed.");
                });
            } finally {
                Platform.runLater(() -> isTrainingInProgress.set(false));
            }
        }).start();
    }
}
