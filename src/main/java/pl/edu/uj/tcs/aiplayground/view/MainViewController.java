package pl.edu.uj.tcs.aiplayground.view;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class MainViewController {
    @FXML
    private VBox barsContainer;
    @FXML
    private Label accuracyField;
    @FXML
    private Label lossField;
    @FXML
    private Label epochField;
    @FXML
    private ComboBox<String> datasetComboBox;

    private  final double SPACER = 200;
    //bar - one hidden layer
    private final List<Integer[]> barValues = new ArrayList<>(); //holds info about each layer, Integer[0] holds info about type of the layer
    // 0 - linear
    // 1 - sigmoid
    // 2 - relu

    @FXML
    private void initialize() {
        accuracyField.setText("0");
//        lossField.setText("0");

        datasetComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                System.out.println("Selected dataset: " + newVal);
                // dataset selection logic
            }
        });
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/pl/edu/uj/tcs/aiplayground/views/UserInfo.fxml"));
            Scene scene = new Scene(loader.load());

            mainStage.setTitle("AI Playground - User info");
            mainStage.setScene(scene);
            mainStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addLinearBar() {
        // Create the main container for the bar
        HBox barContainer = new HBox();
        barContainer.setStyle("-fx-background-color: #444; -fx-padding: 15; -fx-spacing: 10;");
        barContainer.setPrefHeight(40);

        // Create the label for the bar name
        Label nameLabel = new Label("Linear");
        nameLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16;");

        // Create controls for the 3 parameters
        // First int parameter
        TextField param1Field = new TextField("0");
        param1Field.setPrefWidth(50);
        param1Field.setStyle("-fx-control-inner-background: #444; -fx-text-fill: white;");

        // Second int parameter
        TextField param2Field = new TextField("0");
        param2Field.setPrefWidth(50);
        param2Field.setStyle("-fx-control-inner-background: #444; -fx-text-fill: white;");

        // Bool parameter (as checkbox)
        CheckBox param3CheckBox = new CheckBox("");
        param3CheckBox.setStyle("-fx-text-fill: white;");

        // Calculate the minimum width needed for the label
        Text text = new Text("Linear");
        text.setFont(nameLabel.getFont());
        double textWidth = text.getLayoutBounds().getWidth();
        nameLabel.setMinWidth(textWidth);

        HBox.setHgrow(nameLabel, Priority.NEVER);  // Prevent the label from growing/shrinking

        // Create flexible spacers
        Region leftSpacer = new Region();
        Region rightSpacer = new Region();
        HBox.setHgrow(leftSpacer, Priority.ALWAYS);  // Changed from SOMETIMES to ALWAYS
        HBox.setHgrow(rightSpacer, Priority.ALWAYS);  // Changed from SOMETIMES to ALWAYS

        // Create the remove button
        Button removeButton = new Button("remove");
        removeButton.setStyle("-fx-background-color: #FF5555; -fx-text-fill: white;");
        removeButton.setOnAction(e -> {
            barsContainer.getChildren().remove(barContainer);
            barValues.remove(new Integer[]{0, Integer.parseInt(param1Field.getText()), Integer.parseInt(param2Field.getText()), param3CheckBox.isSelected() ? 1 : 0});
        });

        // Add all elements to the bar container
        barContainer.getChildren().addAll(leftSpacer, nameLabel, param1Field, param2Field, param3CheckBox, rightSpacer, removeButton);
        barValues.add(new Integer[]{0, Integer.parseInt(param1Field.getText()), Integer.parseInt(param2Field.getText()), param3CheckBox.isSelected() ? 1 : 0});

        // Add the bar to the main container
        barsContainer.getChildren().add(barContainer);
    }

    private void addSigmoidBar() {
        // Create the main container for the bar
        HBox barContainer = new HBox();
        barContainer.setStyle("-fx-background-color: #444; -fx-padding: 15; -fx-spacing: 10;");
        barContainer.setPrefHeight(40);

        // Create the label for the bar name
        Label nameLabel = new Label("Sigmoid");
        nameLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16;");

        // Calculate the minimum width needed for the label
        Text text = new Text("Sigmoid");
        text.setFont(nameLabel.getFont());
        double textWidth = text.getLayoutBounds().getWidth();
        nameLabel.setMinWidth(textWidth);

        HBox.setHgrow(nameLabel, Priority.NEVER);  // Prevent the label from growing/shrinking

        // Create flexible spacers
        Region leftSpacer = new Region();
        Region rightSpacer = new Region();
        HBox.setHgrow(leftSpacer, Priority.ALWAYS);  // Changed from SOMETIMES to ALWAYS
        HBox.setHgrow(rightSpacer, Priority.ALWAYS);  // Changed from SOMETIMES to ALWAYS


        // Create the remove button
        Region spacer2 = new Region();
        HBox.setHgrow(spacer2, Priority.ALWAYS);

        Button removeButton = new Button("remove");
        removeButton.setStyle("-fx-background-color: #FF5555; -fx-text-fill: white;");
        removeButton.setOnAction(e -> {
            barsContainer.getChildren().remove(barContainer);
            barValues.remove(new Integer[]{1});
        });

        // Add all elements to the bar container
        barContainer.getChildren().addAll(leftSpacer, nameLabel, rightSpacer, removeButton);
        barValues.add(new Integer[]{1});

        // Add the bar to the main container
        barsContainer.getChildren().add(barContainer);
    }

    private void addReluBar() {
        // Create the main container for the bar
        HBox barContainer = new HBox();
        barContainer.setStyle("-fx-background-color: #444; -fx-padding: 15; -fx-spacing: 10;");
        barContainer.setPrefHeight(40);

        // Create the label for the bar name
        Label nameLabel = new Label("Relu");
        nameLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16;");

        // Calculate the minimum width needed for the label
        Text text = new Text("Relu");
        text.setFont(nameLabel.getFont());
        double textWidth = text.getLayoutBounds().getWidth();
        nameLabel.setMinWidth(textWidth);

        HBox.setHgrow(nameLabel, Priority.NEVER);  // Prevent the label from growing/shrinking

        // Create flexible spacers
        Region leftSpacer = new Region();
        Region rightSpacer = new Region();
        HBox.setHgrow(leftSpacer, Priority.ALWAYS);  // Changed from SOMETIMES to ALWAYS
        HBox.setHgrow(rightSpacer, Priority.ALWAYS);  // Changed from SOMETIMES to ALWAYS

        // Create the remove button
        Button removeButton = new Button("remove");
        removeButton.setStyle("-fx-background-color: #FF5555; -fx-text-fill: white;");
        removeButton.setOnAction(e -> {
            barsContainer.getChildren().remove(barContainer);
            barValues.remove(new Integer[]{2});
        });

        // Add all elements to the bar container
        barContainer.getChildren().addAll(leftSpacer, nameLabel, rightSpacer, removeButton);
        barValues.add(new Integer[]{2});

        // Remove the width listener as it's no longer needed with the new layout approach
        // Add the bar to the main container
        barsContainer.getChildren().add(barContainer);
    }

    public void setAccuracy(int accuracy) {
        accuracyField.setText(String.valueOf(accuracy));
    }

    public void setLossPercentage(int lossPercentage) {
        lossField.setText(String.valueOf(lossPercentage));
    }

    public int getAccuracy() {
        return Integer.parseInt(accuracyField.getText());
    }

    public int getLossPercentage() {
        return Integer.parseInt(lossField.getText());
    }

    public List<Integer[]> getBarValues() {
        return barValues;
    }
}