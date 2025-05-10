package pl.edu.uj.tcs.aiplayground.viewmodel;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import pl.edu.uj.tcs.aiplayground.form.LoginForm;
import pl.edu.uj.tcs.aiplayground.form.RegisterForm;
import pl.edu.uj.tcs.aiplayground.dto.UserDto;
import pl.edu.uj.tcs.aiplayground.exception.DatabaseException;
import pl.edu.uj.tcs.aiplayground.exception.UserModificationException;
import pl.edu.uj.tcs.aiplayground.service.UserService;

import java.util.List;

public class UserViewModel {
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
        } catch(Exception e) {
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
            statusMessage.set("Internal Error");
            user.set(null);
        }
    }

    public void register(RegisterForm registerForm) {
        try {
            userService.register(registerForm);
            statusMessage.set("Registration Successful");
        }
        catch (UserModificationException e) {
            statusMessage.set(e.getMessage());
        } catch (DatabaseException e) {
            statusMessage.set("Internal Error");
            user.set(null);
        }
    }
}
