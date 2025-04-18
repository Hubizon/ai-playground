package org.example.aiplayground.views;

import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.example.aiplayground.viewmodels.LoginViewModel;

public class LoginViewController {

    private final LoginViewModel viewModel;
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;

    public LoginViewController() {
        this.viewModel = new LoginViewModel();
    }

    @FXML
    private void onLoginClicked() {
        viewModel.usernameProperty().set(usernameField.getText());
        viewModel.passwordProperty().set(passwordField.getText());

        viewModel.login();
    }
}
