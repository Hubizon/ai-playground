package pl.edu.uj.tcs.aiplayground.viewmodel;

import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.uj.tcs.aiplayground.core.NeuralNet;
import pl.edu.uj.tcs.aiplayground.core.TrainingHandler;
import pl.edu.uj.tcs.aiplayground.dto.*;
import pl.edu.uj.tcs.aiplayground.dto.architecture.*;
import pl.edu.uj.tcs.aiplayground.dto.form.ModelForm;
import pl.edu.uj.tcs.aiplayground.dto.TrainingDto;
import pl.edu.uj.tcs.aiplayground.dto.form.TrainingForm;
import pl.edu.uj.tcs.aiplayground.dto.validation.TrainingValidation;
import pl.edu.uj.tcs.aiplayground.exception.DatabaseException;
import pl.edu.uj.tcs.aiplayground.exception.InvalidHyperparametersException;
import pl.edu.uj.tcs.aiplayground.exception.ModelModificationException;
import pl.edu.uj.tcs.aiplayground.exception.TrainingException;
import pl.edu.uj.tcs.aiplayground.service.ModelService;
import pl.edu.uj.tcs.aiplayground.service.TrainingService;

import java.util.concurrent.atomic.AtomicBoolean;

public class MainViewModel {
    private static final Logger logger = LoggerFactory.getLogger(MainViewModel.class);
    private final ModelService modelService;
    private final TrainingService trainingService;
    private final AtomicBoolean isCancelled = new AtomicBoolean(false);
    private final StringProperty modelName = new SimpleStringProperty();
    private final IntegerProperty modelVersion = new SimpleIntegerProperty();
    private final ObservableList<String> userModelNames = FXCollections.observableArrayList();
    private final ObservableList<TrainingMetricDto> liveMetrics = FXCollections.observableArrayList();
    private final ObservableList<LayerConfig> layers = FXCollections.observableArrayList();
    private final BooleanProperty isTrainingInProgress = new SimpleBooleanProperty(false);
    private final BooleanProperty isPreviousVersion = new SimpleBooleanProperty(false);
    private final BooleanProperty isNextVersion = new SimpleBooleanProperty(false);
    private final BooleanProperty isModelLoaded = new SimpleBooleanProperty(false);
    private final ObjectProperty<AlertEvent> alertEvent = new SimpleObjectProperty<>();
    private final ObjectProperty<StatusName> trainingStatus = new SimpleObjectProperty<>();
    private UserDto user = null;
    private ModelDto model = null;
    private TrainingHandler trainingHandler = null;

    public MainViewModel(ModelService modelService, TrainingService trainingService) {
        this.modelService = modelService;
        this.trainingService = trainingService;
    }

    private void setupModel() {
        layers.clear();
        if (model != null) {
            NeuralNet neuralNet = new NeuralNet(model.architecture());
            layers.addAll(neuralNet.toConfigList());
            updateIsPreviousVersion();
            updateIsNextVersion();
            modelName.set(model.modelName());
            modelVersion.set(model.versionNumber());
        }
        isModelLoaded.set(model != null);
    }

    private void updateUserModelNames() {
        try {
            userModelNames.addAll(modelService.getUserModelNames(user.userId()));
        } catch (DatabaseException e) {
            logger.error("Failed to get model names for user={}, error={}", user, e.getMessage(), e);
            alertEvent.set(AlertEvent.createAlertEvent("Internal Error", false));
        }
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
            alertEvent.set(AlertEvent.createAlertEvent("Internal Error", false));
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
            alertEvent.set(AlertEvent.createAlertEvent("Internal Error", false));
            isNextVersion.set(false);
        }
    }

    public ObjectProperty<AlertEvent> alertEventProperty() {
        return alertEvent;
    }

    public ObjectProperty<StatusName> trainingStatusProperty() {
        return trainingStatus;
    }

    private StringProperty modelNameProperty() {
        return modelName;
    }

    private IntegerProperty modelVersionProperty() {
        return modelVersion;
    }

    public ObservableList<String> userModelNamesProperty() {
        return userModelNames;
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
        LayerParams defaultParams = layer.getParams();
        LayerConfig config = new LayerConfig(layer, defaultParams);
        layers.add(config);
    }

    public void removeLayer(int idx) {
        layers.remove(idx);
    }

    public void updateLayer(int idx, String paramName, Object newValue) {
        LayerType type = layers.get(idx).type();
        LayerParams oldParams = layers.get(idx).params();
        LayerParams newParams = oldParams.updated(paramName, newValue);
        layers.set(idx, new LayerConfig(type, newParams));
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
            alertEvent.set(AlertEvent.createAlertEvent("Internal Error", false));
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
            alertEvent.set(AlertEvent.createAlertEvent("Internal Error", false));
        }
        setupModel();
    }

    public void setUser(UserDto user) {
        this.user = user;
        updateUserModelNames();
    }

    public void setModel(UserDto user, String modelName) {
        try {
            this.model = modelService.getModel(user.userId(), modelName);
        } catch (DatabaseException e) {
            logger.error("Failed to get the model for user={}, modelName={}, error={}",
                    user, modelName, e.getMessage(), e);
            alertEvent.set(AlertEvent.createAlertEvent("Internal Error", false));
            this.model = null;
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
            isModelLoaded.set(true);
        } catch (ModelModificationException e) {
            logger.error("Failed to create the model for user={}, modelName={}, error={}",
                    user, modelName, e.getMessage(), e);
            alertEvent.set(AlertEvent.createAlertEvent("Illegal model name", false));
            this.model = null;
            isModelLoaded.set(false);
        } catch (DatabaseException e) {
            logger.error("Failed to create the model for user={}, modelName={}, error={}",
                    user, modelName, e.getMessage(), e);
            alertEvent.set(AlertEvent.createAlertEvent("Internal Error", false));
            this.model = null;
            isModelLoaded.set(false);
        }
        setupModel();
        updateUserModelNames();
    }

    public void shareTraining() {
        trainingHandler.shareTraining();
    }

    public void stopTraining() {
        isCancelled.set(true);
    }

    private void runTraining(TrainingDto dto, NeuralNet net, TrainingHandler handler) throws TrainingException {
        net.train(dto, isCancelled, metric -> {
            Platform.runLater(() -> {
                handler.addNewTrainingMetric(metric);
                liveMetrics.add(metric);
            });
        });
    }

    public void train(TrainingForm trainingForm) {
        try {
            TrainingValidation.validateTrainingForm(trainingForm);
        } catch(InvalidHyperparametersException e) {
            alertEvent.set(AlertEvent.createAlertEvent("Invalid hyperparameters", false));
            return;
        }

        isCancelled.set(false);
        isTrainingInProgress.set(true);
        liveMetrics.clear();

        new Thread(() -> {
            try {
                NeuralNet net = new NeuralNet(layers);
                model = modelService.addModel(new ModelForm(user.userId(), model.modelName(), net.toJson()));

                TrainingDto trainingDto = trainingForm.toDto(model.modelVersionId());
                trainingDto.dataset().setTrainingService(trainingService);
                trainingHandler = new TrainingHandler(trainingDto, trainingStatus::set);
                trainingHandler.updateTrainingStatus(StatusName.IN_PROGRESS);

                runTraining(trainingDto, net, trainingHandler);

                if (isCancelled.get()) {
                    trainingHandler.updateTrainingStatus(StatusName.CANCELLED);
                } else {
                    trainingHandler.updateTrainingStatus(StatusName.FINISHED);
                }
            } catch (Exception e) {
                if (trainingHandler != null)
                    trainingHandler.updateTrainingStatus(StatusName.ERROR);
                logger.error("Training error", e);
                Platform.runLater(() ->
                    alertEvent.set(AlertEvent.createAlertEvent("Training failed", false))
                );
            } finally {
                Platform.runLater(() -> isTrainingInProgress.set(false));
            }
        }).start();
    }
}
