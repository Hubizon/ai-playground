package pl.edu.uj.tcs.aiplayground.viewmodel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.edu.uj.tcs.aiplayground.dto.UserDto;
import pl.edu.uj.tcs.aiplayground.dto.form.LoginForm;
import pl.edu.uj.tcs.aiplayground.dto.form.RegisterForm;
import pl.edu.uj.tcs.aiplayground.exception.UserModificationException;
import pl.edu.uj.tcs.aiplayground.service.UserService;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserViewModelTest {

    private UserService userService;
    private UserViewModel viewModel;

    @BeforeEach
    void setUp() {
        userService = mock(UserService.class);
        viewModel = new UserViewModel(userService);
    }

    @Test
    void loginSuccessfulShouldSetUserAndMessage() {
        LoginForm form = new LoginForm("john", "secret");
        UserDto record = new UserDto(
                UUID.randomUUID(),
                "john",
                "John",
                "Wick",
                "john@wick.com",
                "Poland",
                LocalDate.of(2020, 1, 1)
        );

        try {
            when(userService.login(form)).thenReturn(record);

            viewModel.login(form);

            assertEquals(record, viewModel.userProperty().get());
            assertEquals("Login Successful: john", viewModel.statusMessageProperty().get());
            assertTrue(viewModel.isLoggedIn());
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    void loginFailureShouldClearUserAndSetErrorMessage() {
        LoginForm form = new LoginForm("john", "wrong");

        try {
            when(userService.login(form)).thenThrow(new UserModificationException("Invalid credentials"));

            viewModel.login(form);

            assertNull(viewModel.userProperty().get());
            assertEquals("Invalid credentials", viewModel.statusMessageProperty().get());
            assertFalse(viewModel.isLoggedIn());
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    void registerSuccessShouldSetSuccessMessage() {
        RegisterForm form = new RegisterForm(
                "alice", "Alice", "Smith", "alice@example.com",
                "strongPass", "Poland", LocalDate.of(1990, 1, 1)
        );

        try {
            doNothing().when(userService).register(form);

            viewModel.register(form);

            assertEquals("Registration Successful", viewModel.statusMessageProperty().get());
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    void getCountryNamesShouldReturnFromService() {
        List<String> countries = List.of("Poland", "Germany", "France");
        try {
            when(userService.getCountryNames()).thenReturn(countries);

            List<String> result = viewModel.getCountryNames();

            assertEquals(countries, result);
            verify(userService).getCountryNames();
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    void isLoggedInShouldReturnFalseWhenUserIsNull() {
        assertFalse(viewModel.isLoggedIn());
    }
}
