package pl.edu.uj.tcs.aiplayground;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import pl.edu.uj.tcs.aiplayground.view.LoginViewController;

import java.util.Objects;

public class App extends Application {
    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/pl/edu/uj/tcs/aiplayground/views/LoginView.fxml"));
        Scene scene = new Scene(loader.load());
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/styles.css")).toExternalForm());

        // Get the controller and set the stage
        LoginViewController controller = loader.getController();
        controller.setStage(stage);

        stage.setTitle("AI Playground - Login");
        stage.setScene(scene);
        stage.show();
    }
}