package pl.edu.uj.tcs.aiplayground.view;

import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import pl.edu.uj.tcs.aiplayground.dto.LeaderboardDto;
import pl.edu.uj.tcs.aiplayground.viewmodel.ViewModelFactory;

import java.util.List;
import java.util.Objects;

public class LeaderboardViewController {
    private Stage stage;

    @FXML
    private VBox root;

    @FXML
    private TableView<LeaderboardDto> leaderboardTable;

    @FXML
    private TableColumn<LeaderboardDto, String> userColumn;

    @FXML
    private TableColumn<LeaderboardDto, Double> accuracyColumn;

    @FXML
    private TableColumn<LeaderboardDto, Double> lossColumn;

    public void initialize(ViewModelFactory factory) {
        userColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().userName()));

        accuracyColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().accuracy()).asObject());

        lossColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().loss()).asObject());

        lossColumn.setCellFactory(column -> new javafx.scene.control.TableCell<LeaderboardDto, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f", item));
                }
            }
        });
    }

    public void setStage(Stage stage) {
        this.stage = stage;
        stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icon.png"))));
    }

    public void loadData(List<LeaderboardDto> data) {
        //sort by accuracy
        data.sort((a, b) -> Double.compare(b.accuracy(), a.accuracy()));
        leaderboardTable.getItems().setAll(data);
    }
}