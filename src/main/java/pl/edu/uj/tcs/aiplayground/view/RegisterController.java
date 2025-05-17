package pl.edu.uj.tcs.aiplayground.view;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.uj.tcs.aiplayground.dto.form.RegisterForm;
import pl.edu.uj.tcs.aiplayground.viewmodel.UserViewModel;
import pl.edu.uj.tcs.aiplayground.viewmodel.ViewModelFactory;

import java.io.IOException;
import java.time.LocalDate;

public class RegisterController {
    private static final Logger logger = LoggerFactory.getLogger(LoginViewController.class);
    public TextField usernameField;
    public TextField firstNameField;
    public TextField lastNameField;
    public TextField emailField;
    private ViewModelFactory factory;
    private UserViewModel userViewModel;

    @FXML
    private PasswordField passwordField;
    @FXML
    private Button showPasswordButton;

    private Stage stage;

    private boolean passwordVisible = false;

    public RegisterController() {
    }

    public void initialize(ViewModelFactory factory) {
        this.factory = factory;
        this.userViewModel = factory.getUserViewModel();
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private void handleShowPassword() {
        if (!passwordVisible) {
            // Show password
            String password = passwordField.getText();
            passwordField.setPromptText(password);
            passwordField.clear();
            showPasswordButton.setText("Hide Password");
        } else {
            // Hide password
            String password = passwordField.getPromptText();
            passwordField.setText(password);
            passwordField.setPromptText("");
            showPasswordButton.setText("Show Password");
        }
        passwordVisible = !passwordVisible;
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
        RegisterForm form = new RegisterForm(
                usernameField.getText(),
                firstNameField.getText(),
                lastNameField.getText(),
                emailField.getText(),
                passwordField.getText(),
                "Polska",
                LocalDate.of(2000, 1, 1));
        boolean isRegistered = userViewModel.register(form);

        if (isRegistered) {
            if (stage != null) {
                stage.close();
            }
        } else {
            System.out.println("Register failed: " + userViewModel.statusMessageProperty().get());
        }
    }
}