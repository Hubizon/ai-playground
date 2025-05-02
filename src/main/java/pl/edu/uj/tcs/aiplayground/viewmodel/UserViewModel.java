package pl.edu.uj.tcs.aiplayground.viewmodel;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.example.jooq.tables.records.UsersRecord;
import pl.edu.uj.tcs.aiplayground.dto.LoginForm;
import pl.edu.uj.tcs.aiplayground.dto.RegisterForm;
import pl.edu.uj.tcs.aiplayground.exception.UserLoginException;
import pl.edu.uj.tcs.aiplayground.exception.UserRegisterException;
import pl.edu.uj.tcs.aiplayground.service.UserService;

import java.util.List;

public class UserViewModel {
    private final UserService userService;

    private final StringProperty statusMessage = new SimpleStringProperty();
    private final ObjectProperty<UsersRecord> user = new SimpleObjectProperty<>(null);

    public UserViewModel(UserService userService) {
        this.userService = userService;
    }

    public StringProperty statusMessageProperty() {
        return statusMessage;
    }

    public ObjectProperty<UsersRecord> userProperty() {
        return user;
    }

    public boolean isLoggedIn() {
        return user.get() != null;
    }

    public List<String> getCountryNames() {
        return userService.getCountryNames();
    }

    public void login(LoginForm loginForm) {
        try {
            UsersRecord loggedInUser = userService.login(loginForm);
            user.set(loggedInUser);
            statusMessage.set("Login Successful: " + loggedInUser.getUsername());
        } catch (UserLoginException e) {
            statusMessage.set(e.getMessage());
            user.set(null);
        }
    }

    public void register(RegisterForm registerForm) {
        try {
            userService.register(registerForm);
            statusMessage.set("Registration Successful");
        }
        catch (UserRegisterException e) {
            statusMessage.set(e.getMessage());
        }
    }
}
