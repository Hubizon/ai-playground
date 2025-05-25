package pl.edu.uj.tcs.aiplayground.view;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import pl.edu.uj.tcs.aiplayground.dto.UserDto;
import pl.edu.uj.tcs.aiplayground.dto.form.UpdateUserForm;
import pl.edu.uj.tcs.aiplayground.viewmodel.UserViewModel;
import pl.edu.uj.tcs.aiplayground.viewmodel.ViewModelFactory;

public class UserInfoController {
    @FXML
    private PasswordField passwordInfoField;
    @FXML
    private Button showPasswordButton;

    @FXML
    private TextField usernameInfoField;
    @FXML
    private TextField firstNameInfoField;
    @FXML
    private TextField lastNameInfoField;
    @FXML
    private TextField emailInfoField;
    @FXML
    private ComboBox<String> countryInfoComboBox;
    @FXML
    private DatePicker birthDateInfoDatePicker;

    private boolean passwordVisible = false;
    private Stage stage;

    private ViewModelFactory factory;
    private UserViewModel userViewModel;

    public void initialize(ViewModelFactory factory) {
        this.factory = factory;
        this.userViewModel = factory.getUserViewModel();

        UserDto user = userViewModel.getUser();
        if (user != null) {
            usernameInfoField.setText(user.username());
            firstNameInfoField.setText(user.firstName());
            lastNameInfoField.setText(user.lastName());
            emailInfoField.setText(user.email());
            countryInfoComboBox.setValue(user.countryName());
            birthDateInfoDatePicker.setValue(user.birthDate());
        }
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private void handleShowPassword() {
        if (!passwordVisible) {
            // Show password
            String password = passwordInfoField.getText();
            passwordInfoField.setPromptText(password);
            passwordInfoField.clear();
            showPasswordButton.setText("Hide Password");
        } else {
            // Hide password
            String password = passwordInfoField.getPromptText();
            passwordInfoField.setText(password);
            passwordInfoField.setPromptText("");
            showPasswordButton.setText("Show Password");
        }
        passwordVisible = !passwordVisible;
    }

    public void onSaveClick() {
        UpdateUserForm updateUserForm = new UpdateUserForm(
                emailInfoField.getText(),
                passwordInfoField.getText(),
                countryInfoComboBox.getValue(),
                birthDateInfoDatePicker.getValue()
        );
        boolean isSuccess = userViewModel.updateUser(updateUserForm);
        if (!isSuccess) {
            // TODO
        } else if (stage != null) {
            stage.close();
        }
    }

    public void onCancelClick() {
        if (stage != null) {
            stage.close();
        }
    }
}