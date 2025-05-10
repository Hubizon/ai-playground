package pl.edu.uj.tcs.aiplayground.view;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import pl.edu.uj.tcs.jooq.tables.records.UsersRecord;
import pl.edu.uj.tcs.aiplayground.form.LoginForm;
import pl.edu.uj.tcs.aiplayground.form.RegisterForm;
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

    public LoginViewController() {
        this.userViewModel = ViewModelFactory.createUserViewModel();
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
        } else {
            System.out.println("Login failed: " + userViewModel.statusMessageProperty().get());
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
