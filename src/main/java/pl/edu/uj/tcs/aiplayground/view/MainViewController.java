package pl.edu.uj.tcs.aiplayground.view;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.scene.control.TextInputDialog;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.Optional;

import pl.edu.uj.tcs.aiplayground.dto.architecture.*;
import pl.edu.uj.tcs.aiplayground.viewmodel.MainViewModel;
import pl.edu.uj.tcs.aiplayground.viewmodel.UserViewModel;
import pl.edu.uj.tcs.aiplayground.viewmodel.ViewModelFactory;

import java.util.ArrayList;
import java.util.List;

public class MainViewController {

    private final double SPACER = 200;
    @FXML
    public LineChart<?, ?> lossChart;
    @FXML
    public LineChart<?, ?> accuracyChart;
    @FXML
    private TabPane leftTabPane;

    private ViewModelFactory factory;
    private UserViewModel userViewModel;
    private MainViewModel mainViewModel;
    @FXML
    private VBox barsContainer;
    @FXML
    private Label accuracyField;
    @FXML
    private Label lossField;
    @FXML
    private Label epochField;
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
    private VBox layerButtonsContainer;

    public void initialize(ViewModelFactory factory) {
        this.factory = factory;
        this.userViewModel = factory.getUserViewModel();
        this.mainViewModel = factory.getMainViewModel();

        // Initially select "My models" tab
        leftTabPane.getSelectionModel().select(1); // "My models" tab

        for (Tab tab : leftTabPane.getTabs()) {
            if (!"My models".equals(tab.getText())) {
                tab.disableProperty().bind(mainViewModel.isModelLoadedProperty().not());
            }
        }

        // Check if user is logged in and load models
        if (userViewModel.isLoggedIn()) {
            List<String> modelNames = mainViewModel.getUserModelNames(userViewModel.getUser());
            if (modelNames != null && !modelNames.isEmpty()) {
                // Here you would populate the models list in the UI
            }
        }

        optimizerComboBox.setItems(FXCollections.observableArrayList(OptimizerType.values()));
        lossComboBox.setItems(FXCollections.observableArrayList(LossFunctionType.values()));
        datasetComboBox.setItems(FXCollections.observableArrayList(DatasetType.values()));
        leaderboards_select_dataset_combobox.setItems(FXCollections.observableArrayList(DatasetType.values()));

        accuracyField.setText("0");

        leftTabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            System.out.println("Tab changed to: " + newTab.getText());
        });

        datasetComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                System.out.println("Selected dataset: " + newVal);
            }
        });
        optimizerComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                System.out.println("Selected optimizer: " + newVal);
            }
        });
        lossComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                System.out.println("Selected loss function: " + newVal);
            }
        });
        leaderboards_select_dataset_combobox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                System.out.println("Selected dataset for leaderboards sorting: " + newVal);
            }
        });
        maxEpochField.setTextFormatter(new TextFormatter<>(change -> {
            if (change.getControlNewText().matches("\\d*")) {
                return change;
            }
            return null;
        }));
        learningRateField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*(\\.\\d*)?")) { // Allows digits and optional decimal
                learningRateField.setText(oldValue);
            }
        });
        batchField.setTextFormatter(new TextFormatter<>(change -> {
            if (change.getControlNewText().matches("\\d*")) {
                return change;
            }
            return null;
        }));
        createLayerButtons();
    }

    private int getMaxEpoch() {
        try {
            int value = Integer.parseInt(maxEpochField.getText());
            if (value < 0) {
                System.err.println("Max epoch rate must be between 0");
                return 10;
            }
            return value;
        } catch (NumberFormatException e) {
            // Handle the case when the input is not a valid integer
            System.err.println("Invalid number format in maxEpochField");
            return 10; // or some default value
        }
    }

    private int getBatch() {
        try {
            int value = Integer.parseInt(batchField.getText());
            if (value < 0) {
                System.err.println("Batch must be between 0");
                return 8;
            }
            return value;
        } catch (NumberFormatException e) {
            System.err.println("Invalid number format in maxEpochField");
            return 8; // or some default value
        }
    }

    private double getLearningRate() {
        try {
            double value = Double.parseDouble(learningRateField.getText());
            if (value < 0.0 || value > 1.0) {
                System.err.println("Learning rate must be between 0.0 and 1.0");
                return 0.01; // Default fallback
            }
            return value;
        } catch (NumberFormatException e) {
            System.err.println("Invalid learning rate format");
            return 0.01; // Default fallback
        }
    }

    @FXML
    private void pop_up_warning_missing_comboBox_selection(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Missing Selection");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.getDialogPane().getStylesheets().add(
                getClass().getResource("/pl/edu/uj/tcs/aiplayground/view/style/styles.css").toExternalForm()
        );
        alert.getDialogPane().getStyleClass().add("dialog-pane");
        ButtonType okButton = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        alert.getButtonTypes().setAll(okButton);
        alert.showAndWait();
    }

    @FXML
    private void onRunBarClicked() {
        if (datasetComboBox.getValue() == null) {
            pop_up_warning_missing_comboBox_selection("You must select a dataset");
            return;
        }
        if (optimizerComboBox.getValue() == null) {
            pop_up_warning_missing_comboBox_selection("You must select a optimizer");
            return;
        }
        if (lossComboBox.getValue() == null) {
            pop_up_warning_missing_comboBox_selection("You must select a loss");
            return;
        }
        if (mainViewModel.layersProperty().isEmpty()) {
            pop_up_warning_missing_comboBox_selection("You must add at least one layer");
            return;
        }
        System.out.println("Run button clicked - training started");
        mainViewModel.train(getMaxEpoch(),
                getLearningRate(),
                datasetComboBox.getValue(),
                optimizerComboBox.getValue(),
                lossComboBox.getValue());
    }

    @FXML
    private void onPauseBarClicked() {
        System.out.println("Pause button clicked - training stopped");
        mainViewModel.stopTraining();
    }

    @FXML
    private void onClearBarClicked() {
        clearBars();
    }

    @FXML
    private void onLeaderboardsClicked() {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Leaderboards");
        alert.setHeaderText(null);
        alert.setContentText("There will be leaderboards here in the future");
        alert.showAndWait();
    }

    @FXML
    private void onUserInfoClicked() {
        try {
            Stage mainStage = new Stage();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/pl/edu/uj/tcs/aiplayground/view/UserInfoView.fxml"));
            Scene scene = new Scene(loader.load());

            mainStage.setTitle("AI Playground - User info");
            mainStage.setScene(scene);
            mainStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addLayerBar(LayerType layerType) {
        HBox barContainer = new HBox();
        barContainer.setStyle("-fx-background-color: #444; -fx-padding: 15; -fx-spacing: 10;");
        barContainer.setPrefHeight(40);

        Label nameLabel = new Label(layerType.toString());
        nameLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16;");

        Region leftSpacer = new Region();
        HBox.setHgrow(leftSpacer, Priority.ALWAYS);
        barContainer.getChildren().addAll(leftSpacer, nameLabel);
        try {
            // Get params using the new layerType.getParams() method
            LayerParams params = layerType.getParams();
            List<String> paramNames = params.getParamNames();
            List<Class<?>> paramTypes = params.getParamTypes();

            // Create UI controls for each parameter
            for (int i = 0; i < paramNames.size(); i++) {
                String paramName = paramNames.get(i);
                Class<?> paramType = paramTypes.get(i);

                Label paramLabel = new Label(paramName + ":");
                paramLabel.setStyle("-fx-text-fill: white;");

                if (paramType == Integer.class || paramType == int.class) {
                    TextField intField = new TextField();
                    intField.setPrefWidth(50);
                    intField.setStyle("-fx-control-inner-background: #444; -fx-text-fill: white;");

                    // Set initial value using reflection
                    try {
                        Field field = params.getClass().getDeclaredField(paramName.replace(" ", "").toLowerCase());
                        field.setAccessible(true);
                        intField.setText(String.valueOf(field.get(params)));
                    } catch (Exception e) {
                        intField.setText("0"); // Fallback default
                    }

                    intField.textProperty().addListener((obs, oldVal, newVal) -> {
                        if (newVal.matches("\\d*")) {
                            updateLayerParams(barContainer, params, paramName,
                                    newVal.isEmpty() ? 0 : Integer.parseInt(newVal));
                        }
                    });

                    barContainer.getChildren().addAll(paramLabel, intField);
                } else if (paramType == Boolean.class || paramType == boolean.class) {
                    CheckBox checkBox = new CheckBox();
                    checkBox.setStyle("-fx-text-fill: white;");

                    // Set initial value using reflection
                    try {
                        Field field = params.getClass().getDeclaredField(paramName.replace(" ", "").toLowerCase());
                        field.setAccessible(true);
                        checkBox.setSelected((Boolean) field.get(params));
                    } catch (Exception e) {
                        checkBox.setSelected(false); // Fallback default
                    }

                    checkBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
                        updateLayerParams(barContainer, params, paramName, newVal);
                    });

                    barContainer.getChildren().addAll(paramLabel, checkBox);
                } else if (paramType == Double.class || paramType == double.class) {
                    TextField doubleField = new TextField();
                    doubleField.setPrefWidth(50);
                    doubleField.setStyle("-fx-control-inner-background: #444; -fx-text-fill: white;");

                    // Set initial value using reflection
                    try {
                        Field field = params.getClass().getDeclaredField(paramName.replace(" ", "").toLowerCase());
                        field.setAccessible(true);
                        doubleField.setText(String.valueOf(field.get(params)));
                    } catch (Exception e) {
                        doubleField.setText("0.0"); // Fallback default
                    }

                    doubleField.textProperty().addListener((obs, oldVal, newVal) -> {
                        if (newVal.matches("[\\d\\.]*")) {
                            try {
                                updateLayerParams(barContainer, params, paramName,
                                        newVal.isEmpty() ? 0.0 : Double.parseDouble(newVal));
                            } catch (NumberFormatException e) {
                                // Ignore invalid input
                            }
                        }
                    });

                    barContainer.getChildren().addAll(paramLabel, doubleField);
                }
            }



            Region rightSpacer = new Region();
            HBox.setHgrow(rightSpacer, Priority.ALWAYS);

            Button removeButton = new Button("remove");
            removeButton.setStyle("-fx-background-color: #FF5555; -fx-text-fill: white;");
            removeButton.setOnAction(e -> {
                // Get the index BEFORE removing
                int index = barsContainer.getChildren().indexOf(barContainer);
                barsContainer.getChildren().remove(barContainer);
                mainViewModel.removeLayer(index); // Use the pre-removal index
            });


            barContainer.getChildren().addAll(rightSpacer, removeButton);

            barsContainer.getChildren().add(barContainer);

            // Add the layer to the view model
            mainViewModel.addLayer(layerType);

        } catch (Exception ex) {
            throw new RuntimeException("Cannot create params for " + layerType, ex);
        }
    }

    private void updateLayerParams(HBox barContainer, LayerParams oldParams,
                                   String paramName, Object newValue) {

        //oldParams.updated(paramName, newValue)
        try {
            Class<?> paramsClass = oldParams.getClass();

            // For records, use getRecordComponents() instead of getDeclaredFields()
            RecordComponent[] components = paramsClass.getRecordComponents();
            Object[] newValues = new Object[components.length];

            // Get current values using accessor methods
            for (int i = 0; i < components.length; i++) {
                Method accessor = components[i].getAccessor();
                newValues[i] = accessor.invoke(oldParams);
            }

            // Update the changed parameter
            String targetName = paramName.replace(" ", "").toLowerCase();
            for (int i = 0; i < components.length; i++) {
                if (components[i].getName().equals(targetName)) {
                    newValues[i] = newValue;
                    break;
                }
            }

            // Get the canonical constructor with proper parameter types
            Class<?>[] paramTypes = Arrays.stream(components)
                    .map(RecordComponent::getType)
                    .toArray(Class<?>[]::new);

            Constructor<?> constructor = paramsClass.getDeclaredConstructor(paramTypes);
            LayerParams newParams = (LayerParams) constructor.newInstance(newValues);

            // Update in view model
            int index = barsContainer.getChildren().indexOf(barContainer);
            if (index >= 0) {
                mainViewModel.updateLayer(index, newParams);
            }
        } catch (Exception e) {
            System.err.println("Failed to update layer params: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void createLayerButtons() {
        for (LayerType type : LayerType.values()) {
            Button layerButton = new Button("Add " + type.toString().toLowerCase() + " layer");
            layerButton.setOnAction(e -> addLayerBar(type));
            layerButton.setStyle("-fx-background-color: #3C3C3C; -fx-text-fill: white;");
            layerButtonsContainer.getChildren().add(layerButton);
        }
    }

    private void clearBars() {
        barsContainer.getChildren().clear();
        mainViewModel.layersProperty().clear();
    }

    public int getAccuracy() {
        return Integer.parseInt(accuracyField.getText());
    }

    public void setAccuracy(int accuracy) {
        accuracyField.setText(String.valueOf(accuracy));
    }

    public int getLossPercentage() {
        return Integer.parseInt(lossField.getText());
    }

    public void setLossPercentage(int lossPercentage) {
        lossField.setText(String.valueOf(lossPercentage));
    }

    public void setStage(Stage stage) {
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
                getClass().getResource("/pl/edu/uj/tcs/aiplayground/view/style/styles.css").toExternalForm()
        );
        dialog.getDialogPane().getStyleClass().add("dialog-pane");

        TextField textField = dialog.getEditor();
        textField.getStyleClass().add("text-field");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(modelName -> {
            mainViewModel.createNewModel(userViewModel.getUser(), modelName);
        });
    }
}