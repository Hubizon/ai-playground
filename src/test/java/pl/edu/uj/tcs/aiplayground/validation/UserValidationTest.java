package pl.edu.uj.tcs.aiplayground.validation;

import org.junit.jupiter.api.Test;
import pl.edu.uj.tcs.aiplayground.exception.UserModificationException;
import pl.edu.uj.tcs.aiplayground.form.LoginForm;
import pl.edu.uj.tcs.aiplayground.form.RegisterForm;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserValidationTest {

    @Test
    void validLoginFormShouldPass() {
        LoginForm form = new LoginForm("validUser123", "passworD!");
        assertDoesNotThrow(() -> UserValidation.validateLoginForm(form));
    }

    @Test
    void emptyUsernameInLoginFormShouldThrow() {
        LoginForm form = new LoginForm("", "passworD!");
        assertThrows(UserModificationException.class, () -> UserValidation.validateLoginForm(form));
    }

    @Test
    void tooShortUsernameInLoginFormShouldThrow() {
        LoginForm form = new LoginForm("no", "passworD!");
        assertThrows(UserModificationException.class, () -> UserValidation.validateLoginForm(form));
    }

    @Test
    void tooLongUsernameInLoginFormShouldThrow() {
        LoginForm form = new LoginForm("x".repeat(101), "passworD!");
        assertThrows(UserModificationException.class, () -> UserValidation.validateLoginForm(form));
    }

    @Test
    void invalidCharactersUsernameInLoginFormShouldThrow() {
        LoginForm form2 = new LoginForm("abc/abc", "passworD!");
        assertThrows(UserModificationException.class, () -> UserValidation.validateLoginForm(form2));

        LoginForm form3 = new LoginForm("abc!abc", "passworD!");
        assertThrows(UserModificationException.class, () -> UserValidation.validateLoginForm(form3));

        LoginForm form1 = new LoginForm("abcąabc", "passworD!");
        assertThrows(UserModificationException.class, () -> UserValidation.validateLoginForm(form1));
    }

    @Test
    void emptyPasswordInLoginFormShouldThrow() {
        LoginForm form = new LoginForm("validLogin", "");
        assertThrows(UserModificationException.class, () -> UserValidation.validateLoginForm(form));
    }

    @Test
    void tooShortPasswordInLoginFormShouldThrow() {
        LoginForm form = new LoginForm("validLogin", "no");
        assertThrows(UserModificationException.class, () -> UserValidation.validateLoginForm(form));
    }

    @Test
    void tooLongPasswordInLoginFormShouldThrow() {
        LoginForm form = new LoginForm("validLogin", "x".repeat(51));
        assertThrows(UserModificationException.class, () -> UserValidation.validateLoginForm(form));
    }

    @Test
    void validRegisterFormShouldPass() {
        RegisterForm form = new RegisterForm(
                "iamjohnwick",
                "John",
                "Wick",
                "john.wick@example.com",
                "Password:)",
                "Poland",
                LocalDate.of(2000, 1, 1)
        );
        assertDoesNotThrow(() -> UserValidation.validateRegisterForm(form));
    }

    @Test
    void invalidUsernameShouldThrow() {
        RegisterForm form = new RegisterForm(
                "a",
                "John",
                "Wick",
                "john.wick@example.com",
                "Password:)",
                "Poland",
                LocalDate.of(2000, 1, 1)
        );
        assertThrows(UserModificationException.class, () -> UserValidation.validateRegisterForm(form));
    }

    @Test
    void invalidFirstNameShouldThrow() {
        RegisterForm form1 = new RegisterForm(
                "iamjohnwick",
                "",
                "Wick",
                "john.wick@example.com",
                "Password:)",
                "Poland",
                LocalDate.of(2000, 1, 1)
        );
        assertThrows(UserModificationException.class, () -> UserValidation.validateRegisterForm(form1));

        RegisterForm form2 = new RegisterForm(
                "iamjohnwick",
                "John!",
                "Wick",
                "john.wick@example.com",
                "Password:)",
                "Poland",
                LocalDate.of(2000, 1, 1)
        );
        assertThrows(UserModificationException.class, () -> UserValidation.validateRegisterForm(form2));
    }

    @Test
    void invalidLastNameShouldThrow() {
        RegisterForm form1 = new RegisterForm(
                "iamjohnwick",
                "John",
                "",
                "john.wick@example.com",
                "Password:)",
                "Poland",
                LocalDate.of(2000, 1, 1)
        );
        assertThrows(UserModificationException.class, () -> UserValidation.validateRegisterForm(form1));

        RegisterForm form2 = new RegisterForm(
                "iamjohnwick",
                "John",
                "Wick!",
                "john.wick@example.com",
                "Password:)",
                "Poland",
                LocalDate.of(2000, 1, 1)
        );
        assertThrows(UserModificationException.class, () -> UserValidation.validateRegisterForm(form2));
    }

    @Test
    void invalidEmailShouldThrow() {
        RegisterForm form1 = new RegisterForm(
                "iamjohnwick",
                "John",
                "Wick",
                "john.wick_example.com",
                "Password:)",
                "Poland",
                LocalDate.of(2000, 1, 1)
        );
        assertThrows(UserModificationException.class, () -> UserValidation.validateRegisterForm(form1));

        RegisterForm form2 = new RegisterForm(
                "iamjohnwick",
                "John",
                "Wick",
                "john.wick@example_com",
                "Password:)",
                "Poland",
                LocalDate.of(2000, 1, 1)
        );
        assertThrows(UserModificationException.class, () -> UserValidation.validateRegisterForm(form2));

        RegisterForm form3 = new RegisterForm(
                "iamjohnwick",
                "John",
                "Wick",
                "john.wi/ck@example.com",
                "Password:)",
                "Poland",
                LocalDate.of(2000, 1, 1)
        );
        assertThrows(UserModificationException.class, () -> UserValidation.validateRegisterForm(form3));
    }

    @Test
    void invalidPasswordShouldThrow() {
        RegisterForm form = new RegisterForm(
                "iamjohnwick",
                "John",
                "Wick",
                "john.wi/ck@example.com",
                "P",
                "Poland",
                LocalDate.of(2000, 1, 1)
        );
        assertThrows(UserModificationException.class, () -> UserValidation.validateRegisterForm(form));
    }
}
