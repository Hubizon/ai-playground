package pl.edu.uj.tcs.aiplayground.view;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.converter.NumberStringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.uj.tcs.aiplayground.dto.DataLoaderType;
import pl.edu.uj.tcs.aiplayground.dto.LeaderboardDto;
import pl.edu.uj.tcs.aiplayground.dto.StatusType;
import pl.edu.uj.tcs.aiplayground.dto.TrainingMetricDto;
import pl.edu.uj.tcs.aiplayground.dto.architecture.*;
import pl.edu.uj.tcs.aiplayground.dto.form.TrainingForm;
import pl.edu.uj.tcs.aiplayground.viewmodel.LeaderboardViewModel;
import pl.edu.uj.tcs.aiplayground.viewmodel.MainViewModel;
import pl.edu.uj.tcs.aiplayground.viewmodel.UserViewModel;
import pl.edu.uj.tcs.aiplayground.viewmodel.ViewModelFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class MainViewController {
    private final int maxEpochValue = 100000;
    private final double maxLearningRateValue = 1.1;
    private final int maxBatchSizeValue = 100000;
    private static final Logger logger = LoggerFactory.getLogger(MainViewController.class);
    private final XYChart.Series<Number, Number> lossSeriesTest = new XYChart.Series<>();
    private final XYChart.Series<Number, Number> accuracySeriesTest = new XYChart.Series<>();
    private final XYChart.Series<Number, Number> lossSeriesTrain = new XYChart.Series<>();
    private final XYChart.Series<Number, Number> accuracySeriesTrain = new XYChart.Series<>();
    @FXML
    public LineChart<Number, Number> lossChart;
    @FXML
    public LineChart<Number, Number> accuracyChart;
    @FXML
    public Label statusField;
    @FXML
    private NumberAxis accY;
    @FXML
    private TabPane leftTabPane;
    private Stage stage;
    private ViewModelFactory factory;
    private UserViewModel userViewModel;
    private MainViewModel mainViewModel;
    private String lastSelectedModel = null;
    @FXML
    private VBox barsContainer;
    @FXML
    private Label tokenField;
    @FXML
    private Label trainAccuracyField;
    @FXML
    private Label trainLossField;
    @FXML
    private Label testAccuracyField;
    @FXML
    private Label testLossField;
    @FXML
    private Label epochField;
    @FXML
    private Label modelNameField;
    @FXML
    private Label modelVersionField;
    @FXML
    private TextField maxEpochField;
    @FXML
    private TextField batchField;
    @FXML
    private TextField learningRateField;
    @FXML
    private ComboBox<OptimizerType> optimizerComboBox;
    @FXML
    private ComboBox<LossFunctionType> lossComboBox;
    @FXML
    private ComboBox<DatasetType> datasetComboBox;
    @FXML
    private ComboBox<DatasetType> leaderboards_select_dataset_combobox;
    @FXML
    private ComboBox<LeaderboardRegion> leaderbors_select_region_combobox;
    @FXML
    private VBox layerButtonsContainer;
    @FXML
    private Button runButton;
    @FXML
    private Button cancelButton;
    @FXML
    private Button prevVersionButton;
    @FXML
    private Button nextVersionButton;
    @FXML
    private Button shareButton;
    @FXML
    private ListView<String> modelsListView;
    @FXML
    private Tab adminTab;
    @FXML
    private TableView<String> usersTableView;
    @FXML
    private ComboBox<String> rolesComboBox;
    @FXML
    private Button assignRoleButton;
    @FXML
    private Button deleteUserButton;
    @FXML
    private Label currentRoleLabel;

    public void initialize(ViewModelFactory factory) {
        this.factory = factory;
        this.userViewModel = factory.getUserViewModel();
        this.mainViewModel = factory.getMainViewModel();
        this.mainViewModel.setUser(userViewModel.getUser());


        if (!userViewModel.isAdminProperty().get()) {
            leftTabPane.getTabs().remove(adminTab);
        } else {
            initializeAdminTab();
        }
        userViewModel.isAdminProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal && !leftTabPane.getTabs().contains(adminTab)) {
                leftTabPane.getTabs().add(leftTabPane.getTabs().size(), adminTab);
            } else if (!newVal) {
                leftTabPane.getTabs().remove(adminTab);
            }
        });


        leftTabPane.getSelectionModel().select(2); // "My models" tab

        initializeModelsList();

        for (Tab tab : leftTabPane.getTabs()) {
            if (!"My models".equals(tab.getText()) && !"Leaderboards".equals(tab.getText()) && !"Tokens".equals(tab.getText()) && !"Admin Tab".equals(tab.getText())) {
                tab.disableProperty().bind(mainViewModel.isModelLoadedProperty().not());
            }
        }

        lossChart.setCreateSymbols(false);
        lossChart.setAnimated(false);
        lossChart.setLegendVisible(false);
        accuracyChart.setCreateSymbols(false);
        accuracyChart.setAnimated(false);
        accuracyChart.setLegendVisible(false);
        accY.setAutoRanging(false);

        styleSeries(lossSeriesTrain, "#1E90FF");
        styleSeries(accuracySeriesTrain, "#1E90FF");
        styleSeries(lossSeriesTest, "#FF8C00");
        styleSeries(accuracySeriesTest, "#FF8C00");

        lossChart.getData().add(lossSeriesTrain);
        accuracyChart.getData().add(accuracySeriesTrain);
        lossChart.getData().add(lossSeriesTest);
        accuracyChart.getData().add(accuracySeriesTest);

        resetTrainingFields();

        mainViewModel.liveMetricsProperty().addListener((ListChangeListener<TrainingMetricDto>) change -> {
            List<XYChart.Data<Number, Number>> newLossDataTest = new ArrayList<>();
            List<XYChart.Data<Number, Number>> newAccuracyDataTest = new ArrayList<>();
            List<XYChart.Data<Number, Number>> newLossDataTrain = new ArrayList<>();
            List<XYChart.Data<Number, Number>> newAccuracyDataTrain = new ArrayList<>();
            boolean shouldClear = false;

            while (change.next()) {
                if (mainViewModel.liveMetricsProperty().isEmpty()) {
                    shouldClear = true;
                } else if (change.wasAdded()) {
                    if (shouldClear) {
                        newLossDataTest.clear();
                        newAccuracyDataTest.clear();
                        newLossDataTrain.clear();
                        newAccuracyDataTrain.clear();
                        shouldClear = false;
                    }
                    for (TrainingMetricDto m : change.getAddedSubList()) {
                        if (m.type() == DataLoaderType.TEST) {
                            newLossDataTest.add(new XYChart.Data<>(m.iter(), m.loss()));
                            newAccuracyDataTest.add(new XYChart.Data<>(m.iter(), m.accuracy()));
                        } else if (m.type() == DataLoaderType.TRAIN) {
                            newLossDataTrain.add(new XYChart.Data<>(m.iter(), m.loss()));
                            newAccuracyDataTrain.add(new XYChart.Data<>(m.iter(), m.accuracy()));
                        }
                    }
                }
            }

            TrainingMetricDto lastTestMetric = null, lastTrainMetric = null;
            if (!mainViewModel.liveMetricsProperty().isEmpty()) {
                lastTestMetric = TrainingMetricDto.lastMetric(mainViewModel.liveMetricsProperty(), DataLoaderType.TEST);
                lastTrainMetric = TrainingMetricDto.lastMetric(mainViewModel.liveMetricsProperty(), DataLoaderType.TRAIN);
            }

            boolean finalShouldClear = shouldClear;
            TrainingMetricDto finalLastTestMetric = lastTestMetric;
            TrainingMetricDto finalLastTrainMetric = lastTrainMetric;

            Platform.runLater(() -> {
                if (finalShouldClear) {
                    // The charts refuse to update when cleared unless reset
                    lossSeriesTest.getData().clear();
                    accuracySeriesTest.getData().clear();
                    lossSeriesTrain.getData().clear();
                    accuracySeriesTrain.getData().clear();

                    lossChart.getData().remove(lossSeriesTest);
                    accuracyChart.getData().remove(accuracySeriesTest);
                    lossChart.getData().remove(lossSeriesTrain);
                    accuracyChart.getData().remove(accuracySeriesTrain);

                    lossChart.layout();
                    accuracyChart.layout();

                    lossChart.getData().add(lossSeriesTrain);
                    accuracyChart.getData().add(accuracySeriesTrain);
                    lossChart.getData().add(lossSeriesTest);
                    accuracyChart.getData().add(accuracySeriesTest);
                } else {
                    lossSeriesTrain.getData().addAll(newLossDataTrain);
                    accuracySeriesTrain.getData().addAll(newAccuracyDataTrain);
                    lossSeriesTest.getData().addAll(newLossDataTest);
                    accuracySeriesTest.getData().addAll(newAccuracyDataTest);
                }

                if (finalLastTestMetric != null && finalLastTrainMetric != null) {
                    epochField.setText(String.valueOf(finalLastTestMetric.epoch()));
                    testAccuracyField.setText(String.format("%.2f", finalLastTestMetric.accuracy()) + "%");
                    testLossField.setText(String.format("%.3f", finalLastTestMetric.loss()));
                    trainAccuracyField.setText(String.format("%.2f", finalLastTrainMetric.accuracy()) + "%");
                    trainLossField.setText(String.format("%.3f", finalLastTrainMetric.loss()));

                } else {
                    resetTrainingFields();
                }
            });
        });

        statusField.textProperty().bind(Bindings.createStringBinding(
                () -> {
                    StatusType status = mainViewModel.trainingStatusProperty().get();
                    return status != null ? status.toString() : "-";
                },
                mainViewModel.trainingStatusProperty()
        ));

        optimizerComboBox.setItems(FXCollections.observableArrayList(OptimizerType.values()));
        lossComboBox.setItems(FXCollections.observableArrayList(LossFunctionType.values()));
        datasetComboBox.setItems(FXCollections.observableArrayList(DatasetType.values()));
        leaderboards_select_dataset_combobox.setItems(FXCollections.observableArrayList(DatasetType.values()));
        leaderbors_select_region_combobox.setItems(FXCollections.observableArrayList(LeaderboardRegion.values()));

        optimizerComboBox.valueProperty().bindBidirectional(mainViewModel.optimizerTypeProperty());
        lossComboBox.valueProperty().bindBidirectional(mainViewModel.lossFunctionTypeProperty());
        datasetComboBox.valueProperty().bindBidirectional(mainViewModel.datasetTypeProperty());

        Bindings.bindBidirectional(learningRateField.textProperty(), mainViewModel.learningRateProperty(), new NumberStringConverter());
        Bindings.bindBidirectional(batchField.textProperty(), mainViewModel.batchSizeProperty(), new NumberStringConverter("#"));
        Bindings.bindBidirectional(maxEpochField.textProperty(), mainViewModel.maxEpochsProperty(), new NumberStringConverter("#"));

        maxEpochField.setTextFormatter(new TextFormatter<>(change -> {
            if (change.getControlNewText().isEmpty()) {
                return change;
            }
            if (change.getControlNewText().matches("0|[1-9]\\d*")) {
                try {
                    int value = Integer.parseInt(change.getControlNewText());
                    if (value <= maxEpochValue && value > 0) {
                        return change;
                    }
                } catch (NumberFormatException e) {
                    return null;
                }
            }
            return null;
        }));

        learningRateField.setTextFormatter(new TextFormatter<>(change -> {
            if (change.getControlNewText().isEmpty()) {
                return change;
            }
            if (change.getControlNewText().matches("\\d*(\\.\\d*)?")) {
                try {
                    double value = Double.parseDouble(change.getControlNewText());
                    if (value <= maxEpochValue) {
                        return change;
                    }
                } catch (NumberFormatException e) {
                    return null;
                }
            }
            return null;
        }));

        batchField.setTextFormatter(new TextFormatter<>(change -> {
            if (change.getControlNewText().isEmpty()) {
                return change;
            }
            if (change.getControlNewText().matches("0|[1-9]\\d*")) {
                try {
                    int value = Integer.parseInt(change.getControlNewText());
                    if (value <= maxBatchSizeValue && value > 0) {
                        return change;
                    }
                } catch (NumberFormatException e) {
                    return null;
                }
            }
            return null;
        }));

        modelNameField.textProperty().bind(
                Bindings.when(mainViewModel.isModelLoadedProperty())
                        .then(mainViewModel.modelNameProperty())
                        .otherwise("Model not loaded")
        );

        modelVersionField.textProperty().bind(
                Bindings.when(mainViewModel.isModelLoadedProperty())
                        .then(Bindings.concat(" v", mainViewModel.modelVersionProperty()))
                        .otherwise("")
        );

        createLayerButtons();
        runButton.disableProperty().bind(mainViewModel.isTrainingInProgressProperty());
        cancelButton.disableProperty().bind(mainViewModel.isTrainingInProgressProperty().not());
        shareButton.disableProperty().bind(
                mainViewModel.isRecentTrainingAvailableProperty().not().or(mainViewModel.isTrainingInProgressProperty())
        );

        mainViewModel.layersProperty().addListener((ListChangeListener<LayerConfig>) change -> {
            while (change.next()) {
                if (!change.wasReplaced() && change.wasAdded()) {
                    for (LayerConfig config : change.getAddedSubList())
                        addLayerBar(config);
                } else if (!change.wasReplaced() && change.wasRemoved()) {
                    int from = change.getFrom();
                    int to = from + change.getRemovedSize();
                    barsContainer.getChildren().subList(from, to).clear();
                }
            }
        });

        mainViewModel.alertEventProperty().addListener((observable, oldValue, newValue) -> {
            newValue.display();
        });

        prevVersionButton.disableProperty().bind(
                mainViewModel.isModelLoadedProperty().not()
                        .or(mainViewModel.isPreviousVersionProperty().not())
        );

        nextVersionButton.disableProperty().bind(
                mainViewModel.isModelLoadedProperty().not()
                        .or(mainViewModel.isNextVersionProperty().not())
        );

        prevVersionButton.setOnAction(e -> mainViewModel.setPreviousVersion());
        nextVersionButton.setOnAction(e -> mainViewModel.setNextVersion());

        tokenField.textProperty().bind(mainViewModel.userTokensProperty().asString());
    }

    private void addLayerBar(LayerConfig layerConfig) {
        HBox barContainer = new HBox();
        barContainer.setStyle("-fx-background-color: #444; -fx-padding: 15; -fx-spacing: 10;");
        barContainer.setPrefHeight(40);

        Label nameLabel = new Label(layerConfig.type().toString());
        nameLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16;");

        LayerParams params = layerConfig.params();
        List<String> paramNames = params.getParamNames();
        List<Class<?>> paramTypes = params.getParamTypes();
        List<Object> paramValues = params.getParamValues();

        for (int i = 0; i < paramNames.size(); i++) {
            String paramName = paramNames.get(i);
            Class<?> paramType = paramTypes.get(i);
            Object paramValue = paramValues.get(i);

            Label paramLabel = new Label(paramName + ":");
            paramLabel.setStyle("-fx-text-fill: white;");

            if (paramType == Integer.class || paramType == int.class) {
                TextField intField = new TextField();
                intField.setPrefWidth(50);
                intField.setStyle("-fx-control-inner-background: #444; -fx-text-fill: white;");

                intField.setText(String.valueOf(paramValue));

                intField.textProperty().addListener((obs, oldVal, newVal) -> {
                    if (newVal.matches("\\d*")) {
                        try {
                            int value = newVal.isEmpty() ? 0 : Integer.parseInt(newVal);
                            if (value >= 0 && value <= 100000) {
                                updateLayerParams(barContainer, paramName, value);
                            } else {
                                intField.setText(oldVal);
                            }
                        } catch (NumberFormatException e) {
                            intField.setText(oldVal);
                        }
                    } else {
                        intField.setText(oldVal);
                    }
                });

                barContainer.getChildren().addAll(paramLabel, intField);
            } else if (paramType == Boolean.class || paramType == boolean.class) {
                CheckBox checkBox = new CheckBox();
                checkBox.setStyle("-fx-text-fill: white;");

                checkBox.setSelected((Boolean) paramValue);

                checkBox.selectedProperty().addListener((obs, oldVal, newVal) -> updateLayerParams(barContainer, paramName, newVal));

                barContainer.getChildren().addAll(paramLabel, checkBox);
            } else if (paramType == BigDecimal.class) {
                TextField doubleField = new TextField();
                doubleField.setPrefWidth(50);
                doubleField.setStyle("-fx-control-inner-background: #444; -fx-text-fill: white;");

                doubleField.setText(String.valueOf(paramValue));

                doubleField.textProperty().addListener((obs, oldVal, newVal) -> {
                    if (newVal.matches("-?\\d*\\.?\\d{0,4}")) {
                        try {
                            if (newVal.equals("0.") || newVal.equals(".")) {
                                doubleField.setText(newVal);
                                updateLayerParams(barContainer, paramName, BigDecimal.ZERO);
                                return;
                            }

                            BigDecimal value = new BigDecimal(newVal.isEmpty() ? "0" : newVal);
                            if (value.compareTo(BigDecimal.ZERO) >= 0 &&
                                    value.compareTo(BigDecimal.ONE) <= 0) {
                                updateLayerParams(barContainer, paramName, value);
                            } else {
                                doubleField.setText(oldVal);
                            }
                        } catch (NumberFormatException e) {
                            doubleField.setText(oldVal);
                        }
                    } else {
                        doubleField.setText(oldVal);
                    }
                });

                barContainer.getChildren().addAll(paramLabel, doubleField);
            }
        }

        Region leftSpacer = new Region();
        Region rightSpacer = new Region();
        HBox.setHgrow(leftSpacer, Priority.ALWAYS);
        HBox.setHgrow(rightSpacer, Priority.ALWAYS);

        Button removeButton = new Button("remove");
        removeButton.setStyle("-fx-background-color: #FF5555; -fx-text-fill: white;");
        removeButton.setOnAction(e -> {
            int index = barsContainer.getChildren().indexOf(barContainer);
            mainViewModel.removeLayer(index);
        });

        barContainer.getChildren().addAll(leftSpacer, nameLabel);
        barContainer.getChildren().addAll(rightSpacer, removeButton);

        barsContainer.getChildren().add(barContainer);

        Platform.runLater(() -> new Timeline(new KeyFrame(Duration.millis(100), e -> {
            Parent parent = barsContainer.getParent();
            while (parent != null && !(parent instanceof ScrollPane)) {
                parent = parent.getParent();
            }
            if (parent != null) {
                ScrollPane scrollPane = (ScrollPane) parent;
                scrollPane.setVvalue(scrollPane.getVmax());
            }
        })).play());
    }

    private void styleSeries(XYChart.Series<Number, Number> series, String color) {
        series.nodeProperty().addListener((obs, oldNode, newNode) -> {
            if (newNode != null) {
                newNode.setStyle(String.format("-fx-stroke: %s;", color));
            }
        });
    }

    private void resetTrainingFields() {
        epochField.setText("-");
        testAccuracyField.setText("-");
        testLossField.setText("-");
        trainAccuracyField.setText("-");
        trainLossField.setText("-");
    }

    private void alertMessage(String message, Boolean isInfo) {
        Alert alert;
        if (isInfo)
            alert = new Alert(Alert.AlertType.INFORMATION);
        else
            alert = new Alert(AlertType.WARNING);
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.getDialogPane().getStylesheets().add(
                Objects.requireNonNull(getClass().getResource("/pl/edu/uj/tcs/aiplayground/view/style/styles.css")).toExternalForm()
        );
        alert.getDialogPane().getStyleClass().add("dialog-pane");
        ButtonType okButton = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        alert.getButtonTypes().setAll(okButton);
        alert.showAndWait();
    }

    @FXML
    private void onRunBarClicked() {
        System.out.println("Run button clicked - training started");
        try {
            double learningRateV = Double.parseDouble(learningRateField.getText());
            if (learningRateV > maxLearningRateValue) {
                alertMessage("Maximum learning rate is " + maxLearningRateValue, false);
                return;
            }
            mainViewModel.train(new TrainingForm(
                    Integer.parseInt(maxEpochField.getText()),
                    Integer.parseInt(batchField.getText()),
                    Double.parseDouble(learningRateField.getText()),
                    datasetComboBox.getValue(),
                    optimizerComboBox.getValue(),
                    lossComboBox.getValue())
            );
        } catch (Exception e) {
            alertMessage("Invalid hyperparameters", false);
        }
    }

    @FXML
    private void onCancelBarClicked() {
        System.out.println("Cancel button clicked - training stopped");
        mainViewModel.stopTraining();
    }

    @FXML
    private void onClearBarClicked() {
        clearBars();
    }

    @FXML
    private void onShowLeaderboardsClicked() {
        try {
            LeaderboardRegion region = leaderbors_select_region_combobox.getValue();
            DatasetType datasetType = leaderboards_select_dataset_combobox.getValue();

            if (datasetType == null) {
                alertMessage("Please select a dataset type", false);
                return;
            }

            if (region == null) {
                alertMessage("Please select a region", false);
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/pl/edu/uj/tcs/aiplayground/view/LeaderboardView.fxml"));
            Parent root = loader.load();
            LeaderboardViewController controller = loader.getController();
            controller.initialize(factory);

            LeaderboardViewModel leaderboardViewModel = factory.getLeaderboardViewModel();
            List<LeaderboardDto> leaderboardData;

            if (LeaderboardRegion.GLOBAL.equals(region)) {
                leaderboardData = leaderboardViewModel.getLeaderboardGlobal(datasetType);
            } else if (LeaderboardRegion.COUNTRY.equals(region)) {
                String country = userViewModel.getUser().countryName();
                leaderboardData = leaderboardViewModel.getLeaderboardLocal(datasetType, country);
            } else
                return;

            controller.loadData(leaderboardData);

            Stage stage = new Stage();
            stage.setScene(new Scene(root, 600, 400));
            if (region == LeaderboardRegion.COUNTRY)
                stage.setTitle("Leaderboard - " + userViewModel.getUser().countryName() + " - " + datasetType);
            else
                stage.setTitle("Leaderboard - " + region + " - " + datasetType);
            stage.show();
        } catch (Exception e) {
            logger.error("Failed to load leaderboard, error={}", e.getMessage(), e);
            alertMessage("Failed to load leaderboard: " + e.getMessage(), false);
        }
    }

    @FXML
    private void onUserInfoClicked() {
        try {
            Stage stage = new Stage();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/pl/edu/uj/tcs/aiplayground/view/UserInfoView.fxml"));
            Scene scene = new Scene(loader.load());

            UserInfoController controller = loader.getController();
            controller.initialize(factory);
            controller.setStage(stage);

            stage.setTitle("AI Playground - User info");
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            logger.error("Failed to load scene, error={}", e.getMessage(), e);
        }
    }

    private void updateLayerParams(HBox barContainer, String paramName, Object newValue) {
        int index = barsContainer.getChildren().indexOf(barContainer);
        mainViewModel.updateLayer(index, paramName, newValue);
    }

    private void createLayerButtons() {
        for (LayerType type : LayerType.values()) {
            Button layerButton = new Button("Add " + type.toString().toLowerCase() + " layer");
            layerButton.setOnAction(e -> mainViewModel.addLayer(type));
            layerButton.setStyle("-fx-background-color: #3C3C3C; -fx-text-fill: white;");
            layerButtonsContainer.getChildren().add(layerButton);
        }
    }

    private void clearBars() {
        mainViewModel.layersProperty().clear();
    }

    public void setStage(Stage stage) {
        this.stage = stage;
        stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icon.png"))));
    }

    @FXML
    private void onShareButtonClicked() {
        mainViewModel.shareTraining();
    }

    @FXML
    private void onCreateNewModelClicked() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Create New Model");
        dialog.setHeaderText("Enter model name:");
        dialog.setContentText("Name:");

        dialog.setGraphic(null);
        dialog.setHeaderText(null);

        dialog.getDialogPane().getStylesheets().add(
                Objects.requireNonNull(getClass().getResource("/pl/edu/uj/tcs/aiplayground/view/style/styles.css")).toExternalForm()
        );
        dialog.getDialogPane().getStyleClass().add("dialog-pane");

        TextField textField = dialog.getEditor();
        textField.getStyleClass().add("text-field");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(modelName -> {
            mainViewModel.createNewModel(userViewModel.getUser(), modelName);
            modelsListView.getSelectionModel().select(modelName);
        });
    }

    @FXML
    private void onBuyTokensClicked() {
        try {
            Stage stage = new Stage();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/pl/edu/uj/tcs/aiplayground/view/TokenShopView.fxml"));
            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/pl/edu/uj/tcs/aiplayground/view/style/styles.css")).toExternalForm());
            TokenShopController controller = loader.getController();
            controller.initialize(factory);
            controller.setStage(stage);

            stage.setTitle("AI Playground - Buy Tokens");
            stage.setScene(scene);
            stage.show();

            stage.setOnCloseRequest(event -> mainViewModel.updateUserTokens());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initializeModelsList() {
        modelsListView.setItems(mainViewModel.userModelNamesProperty());
        modelsListView.getStyleClass().add("custom-list-view");

        modelsListView.setOnMouseClicked(event -> {
            String clickedModel = modelsListView.getSelectionModel().getSelectedItem();

            if (event.getClickCount() >= 2) {
                if (clickedModel != null) {
                    mainViewModel.setModel(userViewModel.getUser(), clickedModel);
                    lastSelectedModel = clickedModel;
                }
            } else {
                modelsListView.getSelectionModel().clearSelection();
                if (lastSelectedModel != null) {
                    modelsListView.getSelectionModel().select(lastSelectedModel);
                }
            }

        });
    }

    private void initializeAdminTab() {
        if (!userViewModel.isAdminProperty().get()) {
            return;
        }

        TableColumn<String, String> userColumn = new TableColumn<>("Users");
        userColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue()));
        userColumn.setPrefWidth(210);
        userColumn.setStyle("-fx-alignment: CENTER_LEFT;");

        usersTableView.getColumns().setAll(userColumn);
        usersTableView.setItems(FXCollections.observableArrayList(userViewModel.getUsernames()));

        usersTableView.setRowFactory(tv -> {
            TableRow<String> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getClickCount() == 1) {
                    String selectedUser = row.getItem();
                    userViewModel.chosenUserProperty().set(selectedUser);
                }
            });
            return row;
        });

        rolesComboBox.setItems(FXCollections.observableArrayList(userViewModel.getRoles()));
        rolesComboBox.valueProperty().bindBidirectional(userViewModel.chosenRoleProperty());

        currentRoleLabel.textProperty().bind(
                Bindings.when(userViewModel.chosenUserRoleProperty().isNotNull())
                        .then(Bindings.concat("Current role: ").concat(userViewModel.chosenUserRoleProperty()))
                        .otherwise("Select user to assign new role")
        );

        assignRoleButton.setOnAction(event -> {
            userViewModel.setRoleForUser();
        });

        deleteUserButton.setOnAction(event -> {
            userViewModel.deleteUser();
            usersTableView.setItems(FXCollections.observableArrayList(userViewModel.getUsernames()));
        });

        userViewModel.isAdminProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                usersTableView.setItems(FXCollections.observableArrayList(userViewModel.getUsernames()));
                rolesComboBox.setItems(FXCollections.observableArrayList(userViewModel.getRoles()));
            }
        });

        userViewModel.adminAlertEventProperty().addListener((observable, oldValue, newValue) -> {
            newValue.display();
        });
    }

    public enum LeaderboardRegion {
        COUNTRY("Country"),
        GLOBAL("Global");

        private final String displayName;

        LeaderboardRegion(String displayName) {
            this.displayName = displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }
}