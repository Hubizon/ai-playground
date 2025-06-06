package pl.edu.uj.tcs.aiplayground.view;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.uj.tcs.aiplayground.dto.form.RegisterForm;
import pl.edu.uj.tcs.aiplayground.viewmodel.UserViewModel;
import pl.edu.uj.tcs.aiplayground.viewmodel.ViewModelFactory;

import java.io.IOException;
import java.time.LocalDate;

public class RegisterController {
    private static final Logger logger = LoggerFactory.getLogger(RegisterController.class);
    public TextField usernameField;
    public TextField firstNameField;
    public TextField lastNameField;
    public TextField emailField;
    public TextField visiblePasswordField;
    private ViewModelFactory factory;
    private UserViewModel userViewModel;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Button showPasswordButton;
    @FXML
    private ComboBox<String> countryComboBox;
    @FXML
    private Label statusLabel;
    @FXML
    public DatePicker birthDatePicker;

    private Stage stage;

    private boolean passwordVisible;

    public RegisterController() {
    }

    public void initialize(ViewModelFactory factory) {
        this.factory = factory;
        this.userViewModel = factory.getUserViewModel();
        countryComboBox.setItems(FXCollections.observableArrayList(userViewModel.getCountryNames()));
        countryComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                System.out.println("Selected Country during registration: " + newVal);
            }
        });
        passwordVisible = false;
        visiblePasswordField.setManaged(false);
        visiblePasswordField.setVisible(false);
        showPasswordButton.setText("Show Password");
        birthDatePicker.setValue(LocalDate.now().minusYears(20));
        birthDatePicker.setEditable(false);

        userViewModel.registerAlertEventProperty().addListener((observable, oldValue, newValue) -> {
            newValue.display();
        });
    }

    public void setStage(Stage stage) {
        this.stage = stage;
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/icon.png")));
    }

    @FXML
    private void handleShowPassword() {
        passwordVisible = !passwordVisible;

        if (passwordVisible) {
            // Show password
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
        String password = passwordVisible ? visiblePasswordField.getText() : passwordField.getText();

        LocalDate birthDate = birthDatePicker.getValue();
        if (birthDate == null) {
            birthDate = LocalDate.of(2000, 1, 1);
        }

        RegisterForm form = new RegisterForm(
                usernameField.getText(),
                firstNameField.getText(),
                lastNameField.getText(),
                emailField.getText(),
                password,
                countryComboBox.getValue(),
                birthDate);
        boolean isRegistered = userViewModel.register(form);

        if (isRegistered) {
            if (stage != null) {
                stage.close();
            }
        } else {
            System.out.println("Register failed: " + userViewModel.statusMessageProperty().get());
        }
    }

    @FXML
    public void onCancelClick() {
        if (stage != null) {
            stage.close();
        }
    }
}