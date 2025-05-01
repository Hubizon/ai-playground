package org.example.aiplayground.view;

import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import jdk.jshell.spi.ExecutionControl;
import org.example.aiplayground.exception.EmailAlreadyUsedException;
import org.example.aiplayground.exception.InvalidLoginOrPasswordException;
import org.example.aiplayground.exception.UsernameTakenException;
import org.example.aiplayground.viewmodel.UserViewModel;
import org.example.jooq.tables.records.UsersRecord;

import java.time.LocalDate;

public class LoginViewController {

    private final UserViewModel userViewModel;
    @FXML
    private TextField usernameField, emailField;
    @FXML
    private PasswordField passwordField;

    public LoginViewController() {
        this.userViewModel = new UserViewModel();
    }

    @FXML
    private void onLoginClicked() {
        try {
            UsersRecord user =  userViewModel.login(usernameField.getText(), passwordField.getText());
            System.out.println(user.toString());
        } catch (InvalidLoginOrPasswordException e) {
            System.out.println("InvalidLoginOrPasswordException: " + e.getMessage());
        }
        catch (IllegalArgumentException e) {
            System.out.println("IllegalArgumentException: " + e.getMessage());
        }
    }

    @FXML
    private void onRegisterClicked() {
        String firstName = "test";
        String lastName = "test";
        String country = "Polska";
        LocalDate birthday = LocalDate.of(2000, 1, 1);
        try {
            userViewModel.register(usernameField.getText(), firstName, lastName, emailField.getText(),
                    passwordField.getText(), country, birthday);
            System.out.println("Registered successfully");
        } catch (UsernameTakenException e) {
            System.out.println("UsernameTakenException: " + e.getMessage());
        } catch (EmailAlreadyUsedException e) {
            System.out.println("EmailAlreadyUsedException: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("IllegalArgumentException: " + e.getMessage());
        }
    }
}
