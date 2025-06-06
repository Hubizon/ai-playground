package pl.edu.uj.tcs.aiplayground.viewmodel;

import javafx.application.Platform;
import javafx.beans.property.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.uj.tcs.aiplayground.dto.AlertEvent;
import pl.edu.uj.tcs.aiplayground.dto.UserDto;
import pl.edu.uj.tcs.aiplayground.dto.form.LoginForm;
import pl.edu.uj.tcs.aiplayground.dto.form.RegisterForm;
import pl.edu.uj.tcs.aiplayground.dto.form.UpdateUserForm;
import pl.edu.uj.tcs.aiplayground.exception.DatabaseException;
import pl.edu.uj.tcs.aiplayground.exception.UserModificationException;
import pl.edu.uj.tcs.aiplayground.service.UserService;

import java.util.List;

public class UserViewModel {
    private static final Logger logger = LoggerFactory.getLogger(UserViewModel.class);
    private final UserService userService;

    private final BooleanProperty isAdmin = new SimpleBooleanProperty(false);
    private final StringProperty statusMessage = new SimpleStringProperty();
    private final ObjectProperty<AlertEvent> registerAlertEvent = new SimpleObjectProperty<>();

    private final ObjectProperty<UserDto> user = new SimpleObjectProperty<>(null);
    private final StringProperty chosenUser = new SimpleStringProperty();
    private final StringProperty chosenRole = new SimpleStringProperty();
    private final StringProperty chosenUserRole = new SimpleStringProperty();

    public UserViewModel(UserService userService) {
        this.userService = userService;
    }

    private void setupUser(UserDto user) {
        this.user.set(user);
        if (user != null) {
            try {
                isAdmin.set(userService.isUserAdmin(user.userId()));
                if (isAdmin.get()) {
                    chosenUser.addListener((observable, oldValue, newValue) -> Platform.runLater(() -> {
                        try {
                            chosenUserRole.set(userService.getUserRole(chosenUser));
                        } catch (DatabaseException e) {
                            logger.error("Failed to get user role, chosenUser={}, error={}",
                                    chosenUser, e.getMessage(), e);
                            statusMessage.set("Internal Error");
                        }
                    }));
                }
            } catch (DatabaseException e) {
                logger.error("Failed to get information about user, user={}, error={}", user, e.getMessage(), e);
                statusMessage.set("Internal Error");
            }
        }
    }

    public BooleanProperty isAdminProperty() {
        return isAdmin;
    }

    public StringProperty statusMessageProperty() {
        return statusMessage;
    }

    public ObjectProperty<AlertEvent> registerAlertEventProperty() {
        return registerAlertEvent;
    }

    public ObjectProperty<UserDto> userProperty() {
        return user;
    }

    public StringProperty chosenUserProperty() {
        return chosenUser;
    }

    public StringProperty chosenRoleProperty() {
        return chosenRole;
    }

    public StringProperty chosenUserRoleProperty() {
        return chosenUserRole;
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

    public List<String> getUsernames() {
        if (isAdmin.get()) {
            try {
                return userService.getUsernamesWithoutUser(user.get().userId());
            } catch (Exception e) {
                logger.error("Failed to get usernames, error={}", e.getMessage(), e);
                statusMessage.set("Internal Error");
            }
        }
        return List.of();
    }

    public List<String> getRoles() {
        if (isAdmin.get()) {
            try {
                return userService.getRoleNames();
            } catch (Exception e) {
                logger.error("Failed to get role names, error={}", e.getMessage(), e);
                statusMessage.set("Internal Error");
            }
        }
        return List.of();
    }

    public void login(LoginForm loginForm) {
        try {
            UserDto loggedInUser = userService.login(loginForm);
            setupUser(loggedInUser);
            statusMessage.set("Login Successful: " + loggedInUser.username());
        } catch (UserModificationException e) {
            statusMessage.set(e.getMessage());
            setupUser(null);
        } catch (DatabaseException e) {
            logger.error("Failed to login for loginForm={}, error={}", loginForm, e.getMessage(), e);
            statusMessage.set("Internal Error");
            setupUser(null);
        }
    }

    public boolean register(RegisterForm registerForm) {
        try {
            userService.register(registerForm);
            statusMessage.set("Registration Successful");
            return true;
        } catch (UserModificationException e) {
            registerAlertEvent.set(AlertEvent.createAlertEvent(e.getMessage(), false));
            setupUser(null);
        } catch (DatabaseException e) {
            logger.error("Failed to register for registerForm={}, error={}", registerForm, e.getMessage(), e);
            registerAlertEvent.set(AlertEvent.createAlertEvent("Internal Error", false));
            setupUser(null);
        }
        return false;
    }

    public UserDto getUser() {
        return user.get();
    }

    public boolean updateUser(UpdateUserForm updateUserForm) {
        try {
            userService.updateUser(user.get().userId(), updateUserForm);
        } catch (UserModificationException e) {
            statusMessage.set(e.getMessage());
            return false;
        } catch (DatabaseException e) {
            logger.error("Failed to update user for updateUserForm={}, error={}", updateUserForm, e.getMessage(), e);
            statusMessage.set("Internal Error");
            return false;
        }
        return true;
    }

    public void setRoleForUser() {
        if (!isAdmin.get())
            return;

        try {
            userService.setRoleForUser(chosenUser.get(), chosenRole.get());
            setupUser(user.get());
        } catch (DatabaseException e) {
            logger.error("Failed to set role for user={}, role={}, error={}",
                    chosenUser, chosenRole, e.getMessage(), e);
            statusMessage.set("Internal Error");
        }
    }
}
