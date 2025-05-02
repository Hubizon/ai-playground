package pl.edu.uj.tcs.aiplayground.viewmodel;

import org.example.jooq.tables.records.UsersRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.edu.uj.tcs.aiplayground.dto.LoginForm;
import pl.edu.uj.tcs.aiplayground.dto.RegisterForm;
import pl.edu.uj.tcs.aiplayground.exception.UserLoginException;
import pl.edu.uj.tcs.aiplayground.exception.UserRegisterException;
import pl.edu.uj.tcs.aiplayground.service.UserService;

import java.time.LocalDate;
import java.util.List;

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
        UsersRecord record = new UsersRecord();
        record.setUsername("john");

        try {
            when(userService.login(form)).thenReturn(record);

            viewModel.login(form);

            assertEquals(record, viewModel.userProperty().get());
            assertEquals("Login Successful: john", viewModel.statusMessageProperty().get());
            assertTrue(viewModel.isLoggedIn());
        } catch (UserLoginException e) {
            fail(e.getMessage());
        }
    }

    @Test
    void loginFailureShouldClearUserAndSetErrorMessage() {
        LoginForm form = new LoginForm("john", "wrong");

        try {
            when(userService.login(form)).thenThrow(new UserLoginException("Invalid credentials"));

            viewModel.login(form);

            assertNull(viewModel.userProperty().get());
            assertEquals("Invalid credentials", viewModel.statusMessageProperty().get());
            assertFalse(viewModel.isLoggedIn());
        } catch (UserLoginException e) {
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
        } catch (UserRegisterException e) {
            fail(e.getMessage());
        }
    }

    @Test
    void registerFailureShouldSetErrorMessage() {
        RegisterForm form = new RegisterForm(
                "bob", "Bob", "Smith", "bob@example.com",
                "weak", "Nowhere", LocalDate.of(1990, 1, 1)
        );

        try {
            doThrow(new UserRegisterException("Username taken")).when(userService).register(form);

            viewModel.register(form);

            assertEquals("Username taken", viewModel.statusMessageProperty().get());
        } catch (UserRegisterException e) {
            fail(e.getMessage());
        }
    }

    @Test
    void getCountryNamesShouldReturnFromService() {
        List<String> countries = List.of("Poland", "Germany", "France");
        when(userService.getCountryNames()).thenReturn(countries);

        List<String> result = viewModel.getCountryNames();

        assertEquals(countries, result);
        verify(userService).getCountryNames();
    }

    @Test
    void isLoggedInShouldReturnFalseWhenUserIsNull() {
        assertFalse(viewModel.isLoggedIn());
    }
}
