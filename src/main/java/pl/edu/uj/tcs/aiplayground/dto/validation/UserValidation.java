package pl.edu.uj.tcs.aiplayground.dto.validation;

import pl.edu.uj.tcs.aiplayground.dto.form.LoginForm;
import pl.edu.uj.tcs.aiplayground.dto.form.RegisterForm;
import pl.edu.uj.tcs.aiplayground.dto.form.UpdateUserForm;
import pl.edu.uj.tcs.aiplayground.exception.UserModificationException;

public class UserValidation {
    private static final String USERNAME_REGEX = "^[A-Za-z0-9_.]{3,50}$";
    private static final String FIRST_NAME_REGEX = "^[A-Za-zÀ-ÿ'\\- ]{1,100}$";
    private static final String LAST_NAME_REGEX = "^[A-Za-zÀ-ÿ'\\- ]{1,100}$";
    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    private static final String PASSWORD_REGEX = "^.{5,50}$";

    public static void validateLoginForm(LoginForm loginForm) throws UserModificationException {
        if (!loginForm.username().matches(USERNAME_REGEX))
            throw new UserModificationException("Invalid username");
        if (!loginForm.password().matches(PASSWORD_REGEX))
            throw new UserModificationException("Invalid password");
    }

    public static void validateRegisterForm(RegisterForm registerForm) throws UserModificationException {
        if (!registerForm.username().matches(USERNAME_REGEX))
            throw new UserModificationException("Invalid username");

        if (!registerForm.firstName().matches(FIRST_NAME_REGEX))
            throw new UserModificationException("Invalid first name");

        if (!registerForm.lastName().matches(LAST_NAME_REGEX))
            throw new UserModificationException("Invalid last name");

        if (!registerForm.email().matches(EMAIL_REGEX))
            throw new UserModificationException("Invalid email address");

        if (!registerForm.password().matches(PASSWORD_REGEX))
            throw new UserModificationException("Invalid password");
    }

    public static void validateUpdateUserForm(UpdateUserForm updateUserForm) throws UserModificationException {
        if (!updateUserForm.email().matches(EMAIL_REGEX))
            throw new UserModificationException("Invalid email address");

        if (!updateUserForm.password().matches(PASSWORD_REGEX))
            throw new UserModificationException("Invalid password");
    }
}
