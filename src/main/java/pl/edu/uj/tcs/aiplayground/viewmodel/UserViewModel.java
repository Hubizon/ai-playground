package pl.edu.uj.tcs.aiplayground.viewmodel;

import pl.edu.uj.tcs.aiplayground.dto.LoginForm;
import pl.edu.uj.tcs.aiplayground.dto.RegisterForm;
import pl.edu.uj.tcs.aiplayground.exception.UserLoginException;
import pl.edu.uj.tcs.aiplayground.exception.UserRegisterException;
import pl.edu.uj.tcs.aiplayground.service.UserService;
import org.example.jooq.tables.records.UsersRecord;

public class UserViewModel {
    private final UserService userService;

    public UserViewModel(UserService userService) {
        this.userService = userService;
    }

    public UsersRecord login(LoginForm loginForm) throws UserLoginException {
        return userService.login(loginForm);
    }

    public void register(RegisterForm registerForm) throws UserRegisterException {
        userService.register(registerForm);
    }
}
