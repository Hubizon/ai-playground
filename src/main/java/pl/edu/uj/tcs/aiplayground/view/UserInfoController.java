package pl.edu.uj.tcs.aiplayground.view;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import pl.edu.uj.tcs.aiplayground.dto.UserDto;
import pl.edu.uj.tcs.aiplayground.dto.form.UpdateUserForm;
import pl.edu.uj.tcs.aiplayground.viewmodel.UserViewModel;
import pl.edu.uj.tcs.aiplayground.viewmodel.ViewModelFactory;

public class UserInfoController {
    @FXML
    private PasswordField passwordField;
    @FXML
    private Button showPasswordButton;
    @FXML
    private TextField visiblePasswordField;
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
    @FXML
    private Button editInfoButton;
    @FXML
    private Button saveButton;
    @FXML
    private Button cancelButton;

    private boolean passwordVisible = false;
    private boolean editMode = false;
    private Stage stage;

    private ViewModelFactory factory;
    private UserViewModel userViewModel;

    public void initialize(ViewModelFactory factory) {
        this.factory = factory;
        this.userViewModel = factory.getUserViewModel();

        countryInfoComboBox.setItems(FXCollections.observableArrayList(userViewModel.getCountryNames()));

        UserDto user = userViewModel.getUser();
        if (user != null) {
            usernameInfoField.setText(user.username());
            firstNameInfoField.setText(user.firstName());
            lastNameInfoField.setText(user.lastName());
            emailInfoField.setText(user.email());
            countryInfoComboBox.setValue(user.countryName());
            birthDateInfoDatePicker.setValue(user.birthDate());
        }
        countryInfoComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                System.out.println("Selected new country in user info: " + newVal);
            }
        });
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private void handleShowPassword() {
        if (!editMode) return;

        passwordVisible = !passwordVisible;

        if (passwordVisible) {
            String password = passwordField.getText();
            visiblePasswordField.setText(password);
            visiblePasswordField.setManaged(true);
            visiblePasswordField.setVisible(true);
            passwordField.setManaged(false);
            passwordField.setVisible(false);
            showPasswordButton.setText("Hide Password");
        } else {
            // Hide password
            String password = visiblePasswordField.getText();
            passwordField.setText(password);
            passwordField.setManaged(true);
            passwordField.setVisible(true);
            visiblePasswordField.setManaged(false);
            visiblePasswordField.setVisible(false);
            showPasswordButton.setText("Show Password");
        }
    }

    @FXML
    private void onEditInfoClick() {
        editMode = true;
        updateEditability();
    }

    @FXML
    public void onSaveClick() {
        String password = passwordVisible ? visiblePasswordField.getText() : passwordField.getText();

        UpdateUserForm updateUserForm = new UpdateUserForm(
                firstNameInfoField.getText(),
                lastNameInfoField.getText(),
                password,
                countryInfoComboBox.getValue(),
                birthDateInfoDatePicker.getValue()
        );
        boolean isSuccess = userViewModel.updateUser(updateUserForm);
        if (isSuccess) {
            editMode = false;
            updateEditability();
            if (stage != null) {
                stage.close();
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Invalid data");
            alert.setHeaderText(null);
            alert.setContentText(userViewModel.statusMessageProperty().getValue());
            alert.getDialogPane().getStylesheets().add(
                    getClass().getResource("/pl/edu/uj/tcs/aiplayground/view/style/styles.css").toExternalForm()
            );
            alert.getDialogPane().getStyleClass().add("dialog-pane");
            ButtonType okButton = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
            alert.getButtonTypes().setAll(okButton);
            alert.showAndWait();
        }
    }

    @FXML
    public void onCancelClick() {
        UserDto user = userViewModel.getUser();
        if (user != null) {
            emailInfoField.setText(user.email());
            passwordField.setText("");
            visiblePasswordField.setText("");
            countryInfoComboBox.setValue(user.countryName());
            birthDateInfoDatePicker.setValue(user.birthDate());
        }

        editMode = false;
        updateEditability();

        if (passwordVisible) {
            handleShowPassword();
        }
    }

    private void updateEditability() {
        usernameInfoField.setEditable(false);
        firstNameInfoField.setEditable(editMode);
        lastNameInfoField.setEditable(editMode);

        emailInfoField.setEditable(false);
        passwordField.setEditable(editMode);
        visiblePasswordField.setEditable(editMode);
        countryInfoComboBox.setDisable(!editMode);
        birthDateInfoDatePicker.setDisable(!editMode);
        showPasswordButton.setDisable(!editMode);

        editInfoButton.setDisable(editMode);
        saveButton.setDisable(!editMode);
        cancelButton.setDisable(!editMode);
    }
}