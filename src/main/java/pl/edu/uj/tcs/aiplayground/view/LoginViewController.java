package pl.edu.uj.tcs.aiplayground.view;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import pl.edu.uj.tcs.jooq.tables.records.UsersRecord;
import pl.edu.uj.tcs.aiplayground.dto.LoginForm;
import pl.edu.uj.tcs.aiplayground.dto.RegisterForm;
import pl.edu.uj.tcs.aiplayground.viewmodel.UserViewModel;
import pl.edu.uj.tcs.aiplayground.viewmodel.ViewModelFactory;

import java.time.LocalDate;

public class LoginViewController {

    private final UserViewModel userViewModel;
    @FXML
    private TextField usernameField, emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label statusLabel;
    private Stage stage; // Add reference to the stage

    public LoginViewController() {
        this.userViewModel = ViewModelFactory.createUserViewModel();
    }

    public void setStage(Stage stage) { // Add setter for stage
        this.stage = stage;
    }

    @FXML
    private void initialize() {
        statusLabel.textProperty().bind(userViewModel.statusMessageProperty());
    }

    @FXML
    private void onLoginClicked() {
        LoginForm form = new LoginForm(usernameField.getText(), passwordField.getText());
        userViewModel.login(form);

        if (userViewModel.isLoggedIn()) {
            UsersRecord user = userViewModel.userProperty().get();
            System.out.println("Login successful: " + user.getUsername());

            // Close current window
            if (stage != null) {
                stage.close();
            }

            // Open new window
            openMainWindow();
        } else {
            System.out.println("Login failed: " + userViewModel.statusMessageProperty().get());
        }
    }

    private void openMainWindow() {
        try {
            Stage mainStage = new Stage();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/pl/edu/uj/tcs/aiplayground/views/MainView.fxml"));
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
        RegisterForm form = new RegisterForm(
                usernameField.getText(),
                "Test",
                "User",
                emailField.getText(),
                passwordField.getText(),
                "Polska",
                LocalDate.of(2000, 1, 1)
        );

        userViewModel.register(form);
    }
}