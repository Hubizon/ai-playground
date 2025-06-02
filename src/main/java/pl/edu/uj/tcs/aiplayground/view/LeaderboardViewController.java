package pl.edu.uj.tcs.aiplayground.view;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import pl.edu.uj.tcs.aiplayground.dto.LeaderboardDto;
import pl.edu.uj.tcs.aiplayground.viewmodel.LeaderboardViewModel;
import pl.edu.uj.tcs.aiplayground.viewmodel.ViewModelFactory;

import java.util.List;

public class LeaderboardViewController {
    @FXML
    private HBox root; // This now matches the fx:id in FXML

    private LeaderboardViewModel leaderboardViewModel;
    private TableView<LeaderboardDto> leaderboardTable;

    public void initialize(ViewModelFactory factory) {
        this.leaderboardViewModel = factory.getLeaderboardViewModel();
        createLeaderboardView();
    }

    private void createLeaderboardView() {
        // Clear existing children if any
        root.getChildren().clear();

        VBox container = new VBox();
        container.setSpacing(10);
        container.setStyle("-fx-padding: 20;");

        Label titleLabel = new Label("Leaderboard");
        titleLabel.setStyle("-fx-font-size: 20; -fx-font-weight: bold; -fx-text-fill: white;");

        // Initialize table
        leaderboardTable = new TableView<>();
        leaderboardTable.setStyle("-fx-background-color: #3C3C3C;");
        leaderboardTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Create columns
        TableColumn<LeaderboardDto, Integer> rankColumn = new TableColumn<>("Rank");
        rankColumn.setCellValueFactory(new PropertyValueFactory<>("rank"));

        TableColumn<LeaderboardDto, String> usernameColumn = new TableColumn<>("Username");
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));

        TableColumn<LeaderboardDto, Double> scoreColumn = new TableColumn<>("Score");
        scoreColumn.setCellValueFactory(new PropertyValueFactory<>("score"));

        TableColumn<LeaderboardDto, String> countryColumn = new TableColumn<>("Country");
        countryColumn.setCellValueFactory(new PropertyValueFactory<>("country"));

        leaderboardTable.getColumns().addAll(rankColumn, usernameColumn, scoreColumn, countryColumn);
        leaderboardTable.setPlaceholder(new Label("No data available"));

        // Style columns
        String columnStyle = "-fx-text-fill: white; -fx-alignment: CENTER;";
        rankColumn.setStyle(columnStyle);
        usernameColumn.setStyle(columnStyle);
        scoreColumn.setStyle(columnStyle);
        countryColumn.setStyle(columnStyle);

        container.getChildren().addAll(titleLabel, leaderboardTable);
        root.getChildren().add(container);

        // Make the table expand to fill available space
        VBox.setVgrow(leaderboardTable, Priority.ALWAYS);
        HBox.setHgrow(container, Priority.ALWAYS);
    }

    public void loadData(List<LeaderboardDto> data) {
        if (leaderboardTable != null) {
            leaderboardTable.setItems(FXCollections.observableArrayList(data));
        }
    }
}