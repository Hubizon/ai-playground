package pl.edu.uj.tcs.aiplayground.viewmodel;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.uj.tcs.aiplayground.dto.UserDto;
import pl.edu.uj.tcs.aiplayground.dto.form.LoginForm;
import pl.edu.uj.tcs.aiplayground.dto.form.RegisterForm;
import pl.edu.uj.tcs.aiplayground.dto.form.UpdateUserForm;
import pl.edu.uj.tcs.aiplayground.exception.DatabaseException;
import pl.edu.uj.tcs.aiplayground.exception.UserModificationException;
import pl.edu.uj.tcs.aiplayground.service.UserService;

import java.time.LocalDate;
import java.util.List;

public class UserViewModel {
    private static final Logger logger = LoggerFactory.getLogger(UserViewModel.class);
    private final UserService userService;

    private final StringProperty statusMessage = new SimpleStringProperty();
    private final ObjectProperty<UserDto> user = new SimpleObjectProperty<>(null);

    public UserViewModel(UserService userService) {
        this.userService = userService;
    }

    public StringProperty statusMessageProperty() {
        return statusMessage;
    }

    public ObjectProperty<UserDto> userProperty() {
        return user;
    }

    public boolean isLoggedIn() {
        return user.get() != null;
    }

    public List<String> getCountryNames() {
        try {
            return userService.getCountryNames();
        } catch (Exception e) {
            logger.error("Failed to get country names, error={}", e.getMessage(), e);
            statusMessage.set("Internal Error");
            return null;
        }
    }

    public void login(LoginForm loginForm) {
        try {
            UserDto loggedInUser = userService.login(loginForm);
            user.set(loggedInUser);
            statusMessage.set("Login Successful: " + loggedInUser.username());
        } catch (UserModificationException e) {
            statusMessage.set(e.getMessage());
            user.set(null);
        } catch (DatabaseException e) {
            logger.error("Failed to login for loginForm={}, error={}", loginForm, e.getMessage(), e);
            statusMessage.set("Internal Error");
            user.set(null);
        }
    }

    public boolean register(RegisterForm registerForm) {
        try {
            userService.register(registerForm);
            statusMessage.set("Registration Successful");
            return true;
        } catch (UserModificationException e) {
            statusMessage.set(e.getMessage());
            user.set(null);
        } catch (DatabaseException e) {
            logger.error("Failed to register for registerForm={}, error={}", registerForm, e.getMessage(), e);
            statusMessage.set("Internal Error");
            user.set(null);
        }
        return false;
    }

    public UserDto getUser() {
        return user.get();
    }

    public void updateUser(UpdateUserForm updateUserForm) {
        try {
            userService.updateUser(user.get().userId(), updateUserForm);
        } catch (UserModificationException e) {
            statusMessage.set(e.getMessage());
            user.set(null);
        }
    }
}
