package org.example.aiplayground.viewmodels;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.example.aiplayground.services.UserService;

public class LoginViewModel {

    private final StringProperty username = new SimpleStringProperty("");
    private final StringProperty password = new SimpleStringProperty("");
    private final UserService userService;

    public LoginViewModel() {
        this(new UserService());
    }

    public LoginViewModel(UserService userService) {
        this.userService = userService;
    }

    public boolean login() {
        boolean success = userService.login(username.get(), password.get());
        System.out.println(success ? "Login successful" : "Login error");
        return success;
    }

    public StringProperty usernameProperty() {
        return username;
    }

    public StringProperty passwordProperty() {
        return password;
    }
}
