package pl.edu.uj.tcs.aiplayground.view;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.uj.tcs.aiplayground.dto.UserDto;
import pl.edu.uj.tcs.aiplayground.dto.form.LoginForm;
import pl.edu.uj.tcs.aiplayground.viewmodel.UserViewModel;
import pl.edu.uj.tcs.aiplayground.viewmodel.ViewModelFactory;

import java.io.IOException;

public class LoginViewController {
    private static final Logger logger = LoggerFactory.getLogger(LoginViewController.class);
    private ViewModelFactory factory;
    private UserViewModel userViewModel;

    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label statusLabel;
    private Stage stage;

    public LoginViewController() {
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void initialize(ViewModelFactory factory) {
        this.factory = factory;
        this.userViewModel = factory.getUserViewModel();
        statusLabel.textProperty().bind(userViewModel.statusMessageProperty());

        passwordField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                onLoginClicked();
            }
        });
    }

    @FXML
    private void onLoginClicked() {
        LoginForm form = new LoginForm(usernameField.getText(), passwordField.getText());
        userViewModel.login(form);

        if (userViewModel.isLoggedIn()) {
            UserDto user = userViewModel.userProperty().get();
            System.out.println("Login successful: " + user.username());

            // closing current window
            if (stage != null) {
                stage.close();
            }

            openMainWindow();
        } else {
            System.out.println("Login failed: " + userViewModel.statusMessageProperty().get());
        }
    }

    private void openMainWindow() {
        try {
            Stage mainStage = new Stage();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/pl/edu/uj/tcs/aiplayground/view/MainView.fxml"));
            Scene scene = new Scene(loader.load());

            MainViewController controller = loader.getController();
            controller.initialize(factory);
            controller.setStage(stage);

            mainStage.setTitle("AI Playground");
            mainStage.setScene(scene);
            mainStage.show();
        } catch (IOException e) {
            logger.error("Problem with loading the scene, error={}", e.getMessage(), e);
        }
    }

    @FXML
    private void onRegisterClicked() {
        try {
            Stage mainStage = new Stage();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/pl/edu/uj/tcs/aiplayground/view/RegisterView.fxml"));
            Scene scene = new Scene(loader.load());

            RegisterController controller = loader.getController();
            controller.initialize(factory);
            controller.setStage(mainStage);

            mainStage.setTitle("AI Playground - Register");
            mainStage.setScene(scene);
            mainStage.show();
        } catch (IOException e) {
            logger.error("Problem with loading the scene, error={}", e.getMessage(), e);
        }
    }
}
