package pl.edu.uj.tcs.aiplayground;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import pl.edu.uj.tcs.aiplayground.controller.LoginViewController;
import pl.edu.uj.tcs.aiplayground.viewmodel.ViewModelFactory;

import java.util.Objects;

public class App extends Application {
    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) throws Exception {
        ViewModelFactory factory = new ViewModelFactory();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/pl/edu/uj/tcs/aiplayground/view/LoginView.fxml"));
        Scene scene = new Scene(loader.load());
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/pl/edu/uj/tcs/aiplayground/view/style/styles.css")).toExternalForm());

        LoginViewController controller = loader.getController();
        controller.initialize(factory);
        controller.setStage(stage);

        stage.setTitle("AI Playground - Login");
        stage.setScene(scene);
        stage.show();
    }
}