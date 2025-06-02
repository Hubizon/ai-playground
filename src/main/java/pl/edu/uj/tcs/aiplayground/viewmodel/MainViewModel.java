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
import pl.edu.uj.tcs.aiplayground.dto.form.TrainingForm;
import pl.edu.uj.tcs.aiplayground.dto.validation.TrainingValidation;
import pl.edu.uj.tcs.aiplayground.exception.*;
import pl.edu.uj.tcs.aiplayground.service.ModelService;
import pl.edu.uj.tcs.aiplayground.service.TrainingService;
import pl.edu.uj.tcs.aiplayground.service.UserService;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public class MainViewModel {
    private static final Logger logger = LoggerFactory.getLogger(MainViewModel.class);
    private final ModelService modelService;
    private final UserService userService;
    private final TrainingService trainingService;
    private final AtomicBoolean isCancelled = new AtomicBoolean(false);
    private final StringProperty modelName = new SimpleStringProperty();
    private final IntegerProperty userTokens = new SimpleIntegerProperty();
    private final IntegerProperty modelVersion = new SimpleIntegerProperty();
    private final ObservableList<String> userModelNames = FXCollections.observableArrayList();
    private final ObservableList<TrainingMetricDto> liveMetrics = FXCollections.observableArrayList();
    private final ObservableList<LayerConfig> layers = FXCollections.observableArrayList();
    private final BooleanProperty isRecentTrainingAvailable = new SimpleBooleanProperty(false);
    private final BooleanProperty isTrainingInProgress = new SimpleBooleanProperty(false);
    private final BooleanProperty isPreviousVersion = new SimpleBooleanProperty(false);
    private final BooleanProperty isNextVersion = new SimpleBooleanProperty(false);
    private final BooleanProperty isModelLoaded = new SimpleBooleanProperty(false);
    private final DoubleProperty learningRate = new SimpleDoubleProperty(0.01);
    private final IntegerProperty batchSize = new SimpleIntegerProperty(16);
    private final IntegerProperty maxEpochs = new SimpleIntegerProperty(100);
    private final ObjectProperty<DatasetType> datasetType = new SimpleObjectProperty<>();
    private final ObjectProperty<OptimizerType> optimizerType = new SimpleObjectProperty<>();
    private final ObjectProperty<LossFunctionType> lossFunctionType = new SimpleObjectProperty<>();
    private final ObjectProperty<AlertEvent> alertEvent = new SimpleObjectProperty<>();
    private final ObjectProperty<StatusType> trainingStatus = new SimpleObjectProperty<>();
    private UserDto user = null;
    private ModelDto model = null;
    private TrainingHandler trainingHandler = null;

    public MainViewModel(ModelService modelService, UserService userService, TrainingService trainingService) {
        this.modelService = modelService;
        this.userService = userService;
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

            try {
                UUID trainingId = modelService.getTrainingIdForModel(model.modelVersionId());
                TrainingDto trainingDto = modelService.getTrainingForModel(model.modelVersionId());
                List<TrainingMetricDto> metrics = modelService.getMetricsForModel(model.modelVersionId());
                trainingStatus.set(modelService.getTrainingStatus(trainingId));
                if (trainingDto != null) {
                    learningRate.set(trainingDto.learningRate());
                    batchSize.set(trainingDto.batchSize());
                    maxEpochs.set(trainingDto.maxEpochs());
                    datasetType.set(trainingDto.dataset());
                    optimizerType.set(trainingDto.optimizer());
                    lossFunctionType.set(trainingDto.lossFunction());
                }
                if (metrics != null) {
                    liveMetrics.clear();
                    liveMetrics.addAll(metrics);
                    if (trainingId != null) {
                        trainingHandler = new TrainingHandler(trainingId, liveMetrics.getLast());
                    }
                }
            } catch (DatabaseException e) {
                logger.error("Failed to get model training data for model={}, error={}", model, e.getMessage(), e);
                alertEvent.set(AlertEvent.createAlertEvent("Internal Error", false));
            }
        }
        else
            trainingHandler = null;
        isModelLoaded.set(model != null);
        updateUserTokens();
        updateIsRecentTrainingAvailable();
    }

    private void updateUserModelNames() {
        userModelNames.clear();
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

    private void updateIsRecentTrainingAvailable() {
        try {
            isRecentTrainingAvailable.set(trainingHandler != null
                    && !modelService.hasUserAlreadySharedTraining(trainingHandler.getTrainingId()));
        } catch (DatabaseException e) {
            logger.error("Failed to get information about shared training, error={}", e.getMessage(), e);
            alertEvent.set(AlertEvent.createAlertEvent("Internal Error", false));
            isRecentTrainingAvailable.set(false);
        }
    }

    public DoubleProperty learningRateProperty() {
        return learningRate;
    }

    public IntegerProperty batchSizeProperty() {
        return batchSize;
    }

    public IntegerProperty maxEpochsProperty() {
        return maxEpochs;
    }

    public IntegerProperty userTokensProperty() {
        return userTokens;
    }

    public ObjectProperty<DatasetType> datasetTypeProperty() {
        return datasetType;
    }

    public ObjectProperty<OptimizerType> optimizerTypeProperty() {
        return optimizerType;
    }

    public ObjectProperty<LossFunctionType> lossFunctionTypeProperty() {
        return lossFunctionType;
    }

    public ObjectProperty<AlertEvent> alertEventProperty() {
        return alertEvent;
    }

    public ObjectProperty<StatusType> trainingStatusProperty() {
        return trainingStatus;
    }

    public StringProperty modelNameProperty() {
        return modelName;
    }

    public IntegerProperty modelVersionProperty() {
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

    public BooleanProperty isRecentTrainingAvailableProperty() {
        return isRecentTrainingAvailable;
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

    public void updateUserTokens() {
        try {
            userTokens.set(userService.userTokenCount(user.userId()));
        } catch (DatabaseException e) {
            logger.error("Failed to get user tokens for user={}, error={}", user, e.getMessage(), e);
            alertEvent.set(AlertEvent.createAlertEvent("Internal Error", false));
        }
    }

    public boolean isLoggedIn() {
        return user != null;
    }

    public String getModelName() {
        if (isModelLoaded.get())
            return model.modelName();
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
        updateUserTokens();
    }

    public void setModel(UserDto user, String modelName) {
        try {
            this.model = modelService.getModel(user.userId(), modelName);
        } catch (DatabaseException e) {
            logger.error("Failed to get the model for user={}, modelName={}, error={}",
                    user, modelName, e.getMessage(), e);
            alertEvent.set(AlertEvent.createAlertEvent("Internal Error", false));
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
        } catch (ModelModificationException e) {
            logger.error("Failed to create the model for user={}, modelName={}, error={}",
                    user, modelName, e.getMessage(), e);
            alertEvent.set(AlertEvent.createAlertEvent("Illegal model name: " + e.getMessage(), false));
        } catch (InsufficientTokensException e) {
            logger.error("Failed to create the model for user={}, modelName={}, error={}",
                    user, modelName, e.getMessage(), e);
            alertEvent.set(AlertEvent.createAlertEvent("Insufficient tokens: " + e.getMessage(), false));
        } catch (DatabaseException e) {
            logger.error("Failed to create the model for user={}, modelName={}, error={}",
                    user, modelName, e.getMessage(), e);
            alertEvent.set(AlertEvent.createAlertEvent("Internal Error", false));
        }
        setupModel();
        updateUserModelNames();
    }

    public void shareTraining() {
        if (trainingHandler != null) {
            String mess = trainingHandler.shareTraining();
            if (mess != null) {
                alertEvent.set(AlertEvent.createAlertEvent(mess, true));
            }
        }
        updateIsRecentTrainingAvailable();
    }

    public void stopTraining() {
        isCancelled.set(true);
    }

    private void runTraining(TrainingDto dto, NeuralNet net, TrainingHandler handler) throws TrainingException {
        net.train(dto, isCancelled, metric -> {
            Platform.runLater(() -> {
                if (handler != null) {
                    handler.addNewTrainingMetric(metric);
                }
                liveMetrics.add(metric);
            });
        });
    }

    public void train(TrainingForm trainingForm) {
        try {
            TrainingValidation.validateTrainingForm(trainingForm);
        } catch (InvalidHyperparametersException e) {
            alertEvent.set(AlertEvent.createAlertEvent("Invalid hyperparameters: " + e.getMessage(), false));
            return;
        }

        isCancelled.set(false);
        isTrainingInProgress.set(true);
        liveMetrics.clear();

        new Thread(() -> {
            try {
                NeuralNet net = new NeuralNet(layers);
                model = modelService.updateModel(new ModelForm(user.userId(), model.modelName(), net.toJson()));
                Platform.runLater(this::setupModel);

                TrainingDto trainingDto = trainingForm.toDto(model.modelVersionId());
                trainingDto.dataset().setTrainingService(trainingService);
                trainingHandler = new TrainingHandler(trainingDto,
                        (StatusType statusType) -> Platform.runLater(() -> trainingStatus.set(statusType)));
                trainingHandler.updateTrainingStatus(StatusType.IN_PROGRESS);

                runTraining(trainingDto, net, trainingHandler);

                if (trainingHandler != null) {
                    if (isCancelled.get()) {
                        trainingHandler.updateTrainingStatus(StatusType.CANCELLED);
                    } else {
                        trainingHandler.updateTrainingStatus(StatusType.FINISHED);
                    }
                }
            } catch (TrainingException e) {
                logger.error("Training error", e);
                if (trainingHandler != null)
                    trainingHandler.updateTrainingStatus(StatusType.ERROR);
                Platform.runLater(() -> alertEvent.set(AlertEvent.createAlertEvent(
                        "Training failed: " + e.getMessage(), false))
                );
            } catch (InsufficientTokensException e) {
                logger.error("Insufficient Tokens", e);
                if (trainingHandler != null)
                    trainingHandler.updateTrainingStatus(StatusType.ERROR);
                Platform.runLater(() -> alertEvent.set(AlertEvent.createAlertEvent(
                        "Insufficient Tokens: " + e.getMessage(),
                        false))
                );
            } catch (DatabaseException e) {
                logger.error("Internal error", e);
                if (trainingHandler != null)
                    trainingHandler.updateTrainingStatus(StatusType.ERROR);
                Platform.runLater(() -> alertEvent.set(AlertEvent.createAlertEvent(
                        "Internal error", false))
                );
            } catch (ModelModificationException e) {
                logger.error("Illegal hyperparameters", e);
                if (trainingHandler != null)
                    trainingHandler.updateTrainingStatus(StatusType.ERROR);
                Platform.runLater(() -> alertEvent.set(AlertEvent.createAlertEvent(
                        "Illegal hyperparameters: " + e.getMessage(), false))
                );
            } finally {
                Platform.runLater(() -> {
                    updateIsRecentTrainingAvailable();
                    isTrainingInProgress.set(false);
                });
            }
        }).start();
    }
}
