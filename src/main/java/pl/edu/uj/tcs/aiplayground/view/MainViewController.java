package pl.edu.uj.tcs.aiplayground.view;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
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
import java.util.Optional;
import pl.edu.uj.tcs.aiplayground.viewmodel.MainViewModel;
import pl.edu.uj.tcs.aiplayground.viewmodel.UserViewModel;
import pl.edu.uj.tcs.aiplayground.viewmodel.ViewModelFactory;

import java.util.ArrayList;
import java.util.List;

public class MainViewController {

    private final double SPACER = 200;
    private final List<Integer[]> barValues = new ArrayList<>();

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
    private ComboBox<String> datasetComboBox;
    @FXML
    private ComboBox<String> optimizerComboBox;
    @FXML
    private ComboBox<String> lossComboBox;

    public void initialize(ViewModelFactory factory) {
        this.factory = factory;
        this.userViewModel = factory.getUserViewModel();
        this.mainViewModel = factory.getMainViewModel();

        // Initially select "My models" tab and disable other tabs
        leftTabPane.getSelectionModel().select(1); // "My models" tab
        disableTabsExceptMyModels();

        // Check if user is logged in and load models
        if (userViewModel.isLoggedIn()) {
            List<String> modelNames = mainViewModel.getUserModelNames(userViewModel.getUser());
            if (modelNames != null && !modelNames.isEmpty()) {
                // Here you would populate the models list in the UI
                // For now, we'll just enable all tabs if there are models
                enableAllTabs();
            }
        }

        // Listen to model loaded property
        mainViewModel.isModelLoadedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                enableAllTabs();
            } else {
                disableTabsExceptMyModels();
                leftTabPane.getSelectionModel().select(1); // "My models" tab
            }
        });

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
        maxEpochField.setTextFormatter(new TextFormatter<>(change -> {
            if (change.getControlNewText().matches("\\d*")) {
                return change;
            }
            return null;
        }));
    }
    private void disableTabsExceptMyModels() {
        for (Tab tab : leftTabPane.getTabs()) {
            if (!"My models".equals(tab.getText())) {
                tab.setDisable(true);
            }
        }
    }

    private void enableAllTabs() {
        for (Tab tab : leftTabPane.getTabs()) {
            tab.setDisable(false);
        }
    }



    @FXML
    private void onRunBarClicked() {
    }

    @FXML
    private void onPauseBarClicked() {
    }

    @FXML
    private void onResetBarClicked() {
    }

    @FXML
    private void onAddLinearBarClicked() {
        addLinearBar();
        mainViewModel.isModelLoadedProperty().set(true); // This will trigger the listener
    }

    @FXML
    private void onAddSigmoidBarClicked() {
        addSigmoidBar();
    }

    @FXML
    private void onAddReluBarClicked() {
        addReluBar();
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

    private void addLinearBar() {
        HBox barContainer = new HBox();
        barContainer.setStyle("-fx-background-color: #444; -fx-padding: 15; -fx-spacing: 10;");
        barContainer.setPrefHeight(40);

        Label nameLabel = new Label("Linear");
        nameLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16;");

        TextField param1Field = new TextField("0");
        param1Field.setPrefWidth(50);
        param1Field.setStyle("-fx-control-inner-background: #444; -fx-text-fill: white;");

        TextField param2Field = new TextField("0");
        param2Field.setPrefWidth(50);
        param2Field.setStyle("-fx-control-inner-background: #444; -fx-text-fill: white;");

        CheckBox param3CheckBox = new CheckBox("");
        param3CheckBox.setStyle("-fx-text-fill: white;");

        Text text = new Text("Linear");
        text.setFont(nameLabel.getFont());
        double textWidth = text.getLayoutBounds().getWidth();
        nameLabel.setMinWidth(textWidth);

        HBox.setHgrow(nameLabel, Priority.NEVER);

        Region leftSpacer = new Region();
        Region rightSpacer = new Region();
        HBox.setHgrow(leftSpacer, Priority.ALWAYS);
        HBox.setHgrow(rightSpacer, Priority.ALWAYS);

        Button removeButton = new Button("remove");
        removeButton.setStyle("-fx-background-color: #FF5555; -fx-text-fill: white;");
        removeButton.setOnAction(e -> {
            barsContainer.getChildren().remove(barContainer);
            barValues.remove(new Integer[]{0, Integer.parseInt(param1Field.getText()), Integer.parseInt(param2Field.getText()), param3CheckBox.isSelected() ? 1 : 0});
        });

        barContainer.getChildren().addAll(leftSpacer, nameLabel, param1Field, param2Field, param3CheckBox, rightSpacer, removeButton);
        barValues.add(new Integer[]{0, Integer.parseInt(param1Field.getText()), Integer.parseInt(param2Field.getText()), param3CheckBox.isSelected() ? 1 : 0});

        barsContainer.getChildren().add(barContainer);
    }

    private void addSigmoidBar() {
        HBox barContainer = new HBox();
        barContainer.setStyle("-fx-background-color: #444; -fx-padding: 15; -fx-spacing: 10;");
        barContainer.setPrefHeight(40);

        Label nameLabel = new Label("Sigmoid");
        nameLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16;");

        Text text = new Text("Sigmoid");
        text.setFont(nameLabel.getFont());
        double textWidth = text.getLayoutBounds().getWidth();
        nameLabel.setMinWidth(textWidth);

        HBox.setHgrow(nameLabel, Priority.NEVER);

        Region leftSpacer = new Region();
        Region rightSpacer = new Region();
        HBox.setHgrow(leftSpacer, Priority.ALWAYS);
        HBox.setHgrow(rightSpacer, Priority.ALWAYS);

        Button removeButton = new Button("remove");
        removeButton.setStyle("-fx-background-color: #FF5555; -fx-text-fill: white;");
        removeButton.setOnAction(e -> {
            barsContainer.getChildren().remove(barContainer);
            barValues.remove(new Integer[]{1});
        });

        barContainer.getChildren().addAll(leftSpacer, nameLabel, rightSpacer, removeButton);
        barValues.add(new Integer[]{1});

        barsContainer.getChildren().add(barContainer);
    }

    private void addReluBar() {
        HBox barContainer = new HBox();
        barContainer.setStyle("-fx-background-color: #444; -fx-padding: 15; -fx-spacing: 10;");
        barContainer.setPrefHeight(40);

        Label nameLabel = new Label("Relu");
        nameLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16;");

        Text text = new Text("Relu");
        text.setFont(nameLabel.getFont());
        double textWidth = text.getLayoutBounds().getWidth();
        nameLabel.setMinWidth(textWidth);

        HBox.setHgrow(nameLabel, Priority.NEVER);

        Region leftSpacer = new Region();
        Region rightSpacer = new Region();
        HBox.setHgrow(leftSpacer, Priority.ALWAYS);
        HBox.setHgrow(rightSpacer, Priority.ALWAYS);

        Button removeButton = new Button("remove");
        removeButton.setStyle("-fx-background-color: #FF5555; -fx-text-fill: white;");
        removeButton.setOnAction(e -> {
            barsContainer.getChildren().remove(barContainer);
            barValues.remove(new Integer[]{2});
        });

        barContainer.getChildren().addAll(leftSpacer, nameLabel, rightSpacer, removeButton);
        barValues.add(new Integer[]{2});

        barsContainer.getChildren().add(barContainer);
    }

    private  void clearBars(){
        barValues.clear();
        barsContainer.getChildren().clear();
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

    public List<Integer[]> getBarValues() {
        return barValues;
    }

    public void setStage(Stage stage) {
    }

    @FXML
    private void onCreateNewModelClicked() {
        // Show dialog to get model name
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Create New Model");
        dialog.setHeaderText("Enter model name:");
        dialog.setContentText("Name:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(modelName -> {
            mainViewModel.createNewModel(userViewModel.getUser(), modelName);
            // The listener on isModelLoadedProperty will handle the tab enabling
        });
        if (mainViewModel.isModelLoadedProperty().get()) {
            enableAllTabs();
        }
        enableAllTabs(); //just for testing
    }
}