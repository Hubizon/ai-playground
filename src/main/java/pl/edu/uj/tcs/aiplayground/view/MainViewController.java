package pl.edu.uj.tcs.aiplayground.view;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;
import java.util.List;

public class MainViewController {
    @FXML
    private VBox barsContainer;
    @FXML
    private Label accuracyField;
    @FXML
    private Label lossField;

    private final List<Integer> barValues = new ArrayList<>(); //holds number of neurons in each layer


    private int barCount = 0;
    private static final double BAR_HEIGHT = 25;
    private static final double INITIAL_WIDTH = 120;
    private static final double WIDTH_CHANGE = 25;
    private static final double STRIP_WIDTH = 5;
    private static final double STRIP_SPACING = 25;

    @FXML
    private void initialize() {
        // Initialize metrics fields
        accuracyField.setText("0");
        lossField.setText("0");

        // Add initial bar
        addNewBar();
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
    private void onAddBarClicked() {
        addNewBar();
    }

    @FXML
    private void onRemoveBarClicked() {
        if (barCount > 0) {
            barsContainer.getChildren().removeLast();
            barCount--;

            // Remove the corresponding value from the list
            if (!barValues.isEmpty()) {
                barValues.removeLast();
            }
        }
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
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("User Info");
        alert.setHeaderText(null);
        alert.setContentText("There will be User Info here in the future");
        alert.showAndWait();
    }

    private void addNewBar() {
        Pane barPane = new Pane();
        barPane.setPrefSize(INITIAL_WIDTH, BAR_HEIGHT);

        // Create the background rectangle
        Rectangle background = new Rectangle(INITIAL_WIDTH, BAR_HEIGHT);
        background.setFill(javafx.scene.paint.Color.web("#2E2E2E"));
        barPane.getChildren().add(background);

        // Create the white strips
        createStrips(barPane, INITIAL_WIDTH);

        Button increaseBtn = new Button("+");
        increaseBtn.setOnAction(e -> {
            double newWidth = barPane.getPrefWidth() + (WIDTH_CHANGE + STRIP_WIDTH);
            barPane.setPrefWidth(newWidth);
            background.setWidth(newWidth);
            createStrips(barPane, newWidth);

            // Update the corresponding value in the list
            int index = barsContainer.getChildren().indexOf(barPane.getParent());
            if (index >= 0 && index < barValues.size()) {
                barValues.set(index, barValues.get(index) + 1);
            }
        });

        Button decreaseBtn = new Button("-");
        decreaseBtn.setOnAction(e -> {
            double newWidth = barPane.getPrefWidth() - (WIDTH_CHANGE + STRIP_WIDTH);
            if (newWidth > 0) {
                barPane.setPrefWidth(newWidth);
                background.setWidth(newWidth);
                createStrips(barPane, newWidth);

                // Update the corresponding value in the list
                int index = barsContainer.getChildren().indexOf(barPane.getParent());
                if (index >= 0 && index < barValues.size()) {
                    barValues.set(index, barValues.get(index) - 1);
                }
            }
        });

        // Create a spacer to push the bar to the center
        Region leftSpacer = new Region();
        HBox.setHgrow(leftSpacer, Priority.ALWAYS);

        Region rightSpacer = new Region();
        HBox.setHgrow(rightSpacer, Priority.ALWAYS);

        HBox barContainer = new HBox(5); // 5 is spacing between elements
        barContainer.getChildren().addAll(leftSpacer, barPane, decreaseBtn, increaseBtn, rightSpacer);

        barsContainer.getChildren().add(barContainer);
        barCount++;

        // Add initial value (4) for the new bar
        barValues.add(4);
    }

    // Parts a bar into squares
    private void createStrips(Pane barPane, double width) {
        // Remove existing strips (keep only the background rectangle)
        if (barPane.getChildren().size() > 1) {
            barPane.getChildren().remove(1, barPane.getChildren().size());
        }

        // Add new strips
        double x = STRIP_SPACING;
        while (x < width) {
            Rectangle strip = new Rectangle(STRIP_WIDTH, BAR_HEIGHT);
            strip.setFill(javafx.scene.paint.Color.web("#3C3C3C"));
            strip.setX(x);
            barPane.getChildren().add(strip);
            x += STRIP_SPACING + STRIP_WIDTH;
        }
    }

    // Getters for the metrics fields
    public void setAccuracy(int accuracy) {
        accuracyField.setText(String.valueOf(accuracy));
    }

    public void setLossPercentage(int lossPercentage) {
        lossField.setText(String.valueOf(lossPercentage));
    }

    // Getters for the metrics fields
    public int getAccuracy() {
        return Integer.parseInt(accuracyField.getText());
    }

    public int getLossPercentage() {
        return Integer.parseInt(lossField.getText());
    }

    public List<Integer> getBarValues(){
        return barValues;
    }
}