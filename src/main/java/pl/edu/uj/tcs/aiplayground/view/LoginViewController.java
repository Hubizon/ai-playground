package pl.edu.uj.tcs.aiplayground.view;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.uj.tcs.aiplayground.dto.UserDto;
import pl.edu.uj.tcs.aiplayground.form.LoginForm;
import pl.edu.uj.tcs.aiplayground.viewmodel.LoginViewModel;
import pl.edu.uj.tcs.aiplayground.viewmodel.ViewModelFactory;

import java.io.IOException;

public class LoginViewController {
    private static final Logger logger = LoggerFactory.getLogger(LoginViewController.class);

    private final LoginViewModel loginViewModel;
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label statusLabel;
    private Stage stage;

    public LoginViewController() {
        this.loginViewModel = ViewModelFactory.createLoginViewModel();
    }

    public void setStage(Stage stage) { // Add setter for stage
        this.stage = stage;
    }

    @FXML
    private void initialize() {
        statusLabel.textProperty().bind(loginViewModel.statusMessageProperty());
    }

    @FXML
    private void onLoginClicked() {
        LoginForm form = new LoginForm(usernameField.getText(), passwordField.getText());
        loginViewModel.login(form);

        if (loginViewModel.isLoggedIn()) {
            UserDto user = loginViewModel.userProperty().get();
            System.out.println("Login successful: " + user.username());

            // closing current window
            if (stage != null) {
                stage.close();
            }

            openMainWindow();
        } else {
            System.out.println("Login failed: " + loginViewModel.statusMessageProperty().get());
        }
    }

    private void openMainWindow() {
        try {
            Stage mainStage = new Stage();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/pl/edu/uj/tcs/aiplayground/view/MainView.fxml"));
            Scene scene = new Scene(loader.load());

            mainStage.setTitle("AI Playground");
            mainStage.setScene(scene);
            mainStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onRegisterClicked() {
        try {
            Stage mainStage = new Stage();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/pl/edu/uj/tcs/aiplayground/view/Register.fxml"));
            Scene scene = new Scene(loader.load());

            mainStage.setTitle("AI Playground - Register");
            mainStage.setScene(scene);
            mainStage.show();
        } catch (IOException e) {
            logger.error("Problem with loading the scene, error={}", e.getMessage(), e);
        }
    }
}
