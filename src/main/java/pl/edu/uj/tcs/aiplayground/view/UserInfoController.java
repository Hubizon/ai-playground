package pl.edu.uj.tcs.aiplayground.view;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;

public class UserInfoController {
    @FXML
    private PasswordField passwordField;
    @FXML
    private Button showPasswordButton;

    private boolean passwordVisible = false;

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
}