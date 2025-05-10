package pl.edu.uj.tcs.aiplayground.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import pl.edu.uj.tcs.aiplayground.dto.UserDto;
import pl.edu.uj.tcs.aiplayground.exception.DatabaseException;
import pl.edu.uj.tcs.aiplayground.exception.UserModificationException;
import pl.edu.uj.tcs.aiplayground.form.LoginForm;
import pl.edu.uj.tcs.aiplayground.form.RegisterForm;
import pl.edu.uj.tcs.aiplayground.repository.IUserRepository;
import pl.edu.uj.tcs.aiplayground.utility.PasswordHasher;
import pl.edu.uj.tcs.jooq.tables.records.UsersRecord;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {
    private IUserRepository userRepo;
    private UserService service;

    @BeforeEach
    void setup() {
        userRepo = mock(IUserRepository.class);
        service = new UserService(userRepo);
    }

    @Test
    void loginShouldReturnUserWhenCredentialsAreCorrect() {
        String username = "testUsername123";
        String rawPassword = "testPassword123!@#";
        String hashedPassword = PasswordHasher.hash(rawPassword);

        UsersRecord userRecord = new UsersRecord();
        String testFirstName = "testFirstName";
        userRecord.setFirstName(testFirstName);
        userRecord.setUsername(username);
        userRecord.setPasswordHash(hashedPassword);

        when(userRepo.findByUsername(username)).thenReturn(userRecord);
        when(userRepo.existUsername(username)).thenReturn(true);

        LoginForm form = new LoginForm(username, rawPassword);

        try {
            UserDto result = service.login(form);
            assertEquals(username, result.username());
            assertEquals(testFirstName, result.firstName());
        } catch (UserModificationException | DatabaseException e) {
            fail(e.getMessage());
        }
    }

    @Test
    void loginShouldThrowWhenUserNotFound() {
        when(userRepo.existUsername("ghost")).thenReturn(false);
        LoginForm form1 = new LoginForm("ghost", "pass");
        assertThrows(UserModificationException.class, () -> service.login(form1));
    }

    @Test
    void loginShouldThrowWhenPasswordIsIncorrect() {
        UsersRecord user = new UsersRecord();
        user.setUsername("user");
        user.setPasswordHash(PasswordHasher.hash("correctPassword"));

        when(userRepo.findByUsername("user")).thenReturn(user);

        LoginForm form = new LoginForm("user", "wrongPassword");

        assertThrows(UserModificationException.class, () -> service.login(form));
    }

    @Test
    void registerShouldInsertUserWhenAllDataValid() {
        RegisterForm form = new RegisterForm(
                "john123", "John", "Smith", "john@smith.org",
                "!1securePassword1!", "Poland", LocalDate.of(2000, 1, 1)
        );

        when(userRepo.getCountryIdByName("Poland")).thenReturn(1);
        when(userRepo.existUsername("john123")).thenReturn(false);
        when(userRepo.existEmail("john@smith.org")).thenReturn(false);

        assertDoesNotThrow(() -> service.register(form));

        verify(userRepo).insertUser(any(RegisterForm.class));

        ArgumentCaptor<RegisterForm> captor = ArgumentCaptor.forClass(RegisterForm.class);
        verify(userRepo).insertUser(captor.capture());
        RegisterForm inserted = captor.getValue();

        assertEquals("john123", inserted.username());
        assertEquals("John", inserted.firstName());
        assertEquals("Smith", inserted.lastName());
        assertEquals("john@smith.org", inserted.email());
        assertEquals("Poland", inserted.country());
    }

    @Test
    void registerShouldThrowWhenCountryNotFound() {
        RegisterForm form = new RegisterForm(
                "john123", "John", "Smith", "john@smith.org",
                "!1securePassword1!", "Nowhere", LocalDate.of(2000, 1, 1)
        );

        when(userRepo.getCountryIdByName("Nowhere")).thenReturn(null);

        assertThrows(UserModificationException.class, () -> service.register(form));
    }

    @Test
    void registerShouldThrowWhenUsernameExists() {
        RegisterForm form = new RegisterForm(
                "john123", "John", "Smith", "john@smith.org",
                "!1securePassword1!", "Poland", LocalDate.of(2000, 1, 1)
        );

        when(userRepo.getCountryIdByName("Poland")).thenReturn(1);
        when(userRepo.existUsername("john123")).thenReturn(true);
        when(userRepo.existEmail("john@smith.org")).thenReturn(false);

        assertThrows(UserModificationException.class, () -> service.register(form));
    }

    @Test
    void registerShouldThrowWhenEmailExists() {
        RegisterForm form = new RegisterForm(
                "john123", "John", "Smith", "john@smith.org",
                "!1securePassword1!", "Poland", LocalDate.of(2000, 1, 1)
        );

        when(userRepo.getCountryIdByName("Poland")).thenReturn(1);
        when(userRepo.existUsername("john123")).thenReturn(false);
        when(userRepo.existEmail("john@smith.org")).thenReturn(true);

        assertThrows(UserModificationException.class, () -> service.register(form));
    }

    @Test
    void registerShouldThrowOnInsertFailure() {
        RegisterForm form = new RegisterForm(
                "john123", "John", "Smith", "john@smith.org",
                "!1securePassword1!", "Poland", LocalDate.of(2000, 1, 1)
        );

        when(userRepo.getCountryIdByName("Poland")).thenReturn(1);
        when(userRepo.existUsername("john123")).thenReturn(false);
        when(userRepo.existEmail("john@smith.org")).thenReturn(false);

        doThrow(new RuntimeException("DB failure")).when(userRepo).insertUser(any());

        assertThrows(DatabaseException.class, () -> service.register(form));
    }
}
