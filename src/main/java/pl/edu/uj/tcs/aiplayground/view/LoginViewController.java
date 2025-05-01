package pl.edu.uj.tcs.aiplayground.view;

import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import pl.edu.uj.tcs.aiplayground.dto.LoginForm;
import pl.edu.uj.tcs.aiplayground.dto.RegisterForm;
import pl.edu.uj.tcs.aiplayground.exception.UserLoginException;
import pl.edu.uj.tcs.aiplayground.exception.UserRegisterException;
import pl.edu.uj.tcs.aiplayground.viewmodel.UserViewModel;
import org.example.jooq.tables.records.UsersRecord;
import pl.edu.uj.tcs.aiplayground.viewmodel.ViewModelFactory;

import java.time.LocalDate;

public class LoginViewController {

    private final UserViewModel userViewModel;
    @FXML
    private TextField usernameField, emailField;
    @FXML
    private PasswordField passwordField;

    public LoginViewController() {
        this.userViewModel = ViewModelFactory.createUserViewModel();
    }

    @FXML
    private void onLoginClicked() {
        try {
            UsersRecord user =  userViewModel.login(new LoginForm(usernameField.getText(), passwordField.getText()));
            System.out.println("Login successful, user:");
            System.out.println(user.toString());
        } catch (UserLoginException e) {
            System.out.println("UserLoginException: " + e.getMessage());
        }
    }

    @FXML
    private void onRegisterClicked() {
        String firstName = "test";
        String lastName = "test";
        String country = "Polska";
        LocalDate birthday = LocalDate.of(2000, 1, 1);
        try {
            userViewModel.register(new RegisterForm(usernameField.getText(), firstName, lastName, emailField.getText(),
                    passwordField.getText(), country, birthday));
            System.out.println("Registered successfully.");
        } catch (UserRegisterException e) {
            System.out.println("UserRegisterException: " + e.getMessage());
        }
    }
}
