package pl.edu.uj.tcs.aiplayground.validation;

import pl.edu.uj.tcs.aiplayground.dto.LoginForm;
import pl.edu.uj.tcs.aiplayground.dto.RegisterForm;
import pl.edu.uj.tcs.aiplayground.exception.UserLoginException;
import pl.edu.uj.tcs.aiplayground.exception.UserRegisterException;

public class UserValidation {
    private static final String USERNAME_REGEX = "^[A-Za-z0-9_.]{5,50}$";
    private static final String FIRST_NAME_REGEX = "^[A-Za-zÀ-ÿ'\\- ]{3,100}$";
    private static final String LAST_NAME_REGEX = "^[A-Za-zÀ-ÿ'\\- ]{3,100}$";
    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    private static final String PASSWORD_REGEX = "^.{5,50}$";

    public static void validateLoginForm(LoginForm loginForm) throws UserLoginException {
        if (!loginForm.username().matches(USERNAME_REGEX))
            throw new UserLoginException("Invalid username");
        if (!loginForm.password().matches(PASSWORD_REGEX))
            throw new UserLoginException("Invalid password");
    }

    public static void validateRegisterForm(RegisterForm registerForm) throws UserRegisterException {
        if (!registerForm.username().matches(USERNAME_REGEX))
            throw new UserRegisterException("Invalid username");

        if (!registerForm.firstName().matches(FIRST_NAME_REGEX))
            throw new UserRegisterException("Invalid first name");

        if (!registerForm.lastName().matches(LAST_NAME_REGEX))
            throw new UserRegisterException("Invalid last name");

        if (!registerForm.email().matches(EMAIL_REGEX))
            throw new UserRegisterException("Invalid email address");

        if (!registerForm.password().matches(PASSWORD_REGEX))
            throw new UserRegisterException("Invalid password");
    }
}
