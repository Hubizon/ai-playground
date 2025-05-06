package pl.edu.uj.tcs.aiplayground.service;

import pl.edu.uj.tcs.jooq.tables.records.UsersRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import pl.edu.uj.tcs.aiplayground.dto.LoginForm;
import pl.edu.uj.tcs.aiplayground.dto.RegisterForm;
import pl.edu.uj.tcs.aiplayground.exception.UserLoginException;
import pl.edu.uj.tcs.aiplayground.exception.UserRegisterException;
import pl.edu.uj.tcs.aiplayground.repository.ICountryRepository;
import pl.edu.uj.tcs.aiplayground.repository.IUserRepository;
import pl.edu.uj.tcs.aiplayground.utility.PasswordHasher;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {
    private IUserRepository userRepo;
    private ICountryRepository countryRepo;
    private UserService service;

    @BeforeEach
    void setup() {
        userRepo = mock(IUserRepository.class);
        countryRepo = mock(ICountryRepository.class);
        service = new UserService(userRepo, countryRepo);
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

        LoginForm form = new LoginForm(username, rawPassword);

        try {
            UsersRecord result = service.login(form);
            assertEquals(username, result.getUsername());
            assertEquals(hashedPassword, result.getPasswordHash());
            assertEquals(testFirstName, result.getFirstName());
        } catch (UserLoginException e) {
            fail(e.getMessage());
        }
    }

    @Test
    void loginShouldThrowWhenUserNotFound() {
        when(userRepo.existUsername("ghost")).thenReturn(false);
        LoginForm form1 = new LoginForm("ghost", "pass");
        assertThrows(UserLoginException.class, () -> service.login(form1));
    }

    @Test
    void loginShouldThrowWhenPasswordIsIncorrect() {
        UsersRecord user = new UsersRecord();
        user.setUsername("user");
        user.setPasswordHash(PasswordHasher.hash("correctPassword"));

        when(userRepo.findByUsername("user")).thenReturn(user);

        LoginForm form = new LoginForm("user", "wrongPassword");

        assertThrows(UserLoginException.class, () -> service.login(form));
    }

    @Test
    void registerShouldInsertUserWhenAllDataValid() {
        RegisterForm form = new RegisterForm(
                "john123", "John", "Smith", "john@smith.org",
                "!1securePassword1!", "Poland", LocalDate.of(2000, 1, 1)
        );

        when(countryRepo.getCountryIdByName("Poland")).thenReturn(1);
        when(userRepo.existUsername("john123")).thenReturn(false);
        when(userRepo.existEmail("john@smith.org")).thenReturn(false);

        assertDoesNotThrow(() -> service.register(form));

        verify(userRepo).insertUser(any(UsersRecord.class));

        ArgumentCaptor<UsersRecord> captor = ArgumentCaptor.forClass(UsersRecord.class);
        verify(userRepo).insertUser(captor.capture());
        UsersRecord inserted = captor.getValue();

        assertEquals("john123", inserted.getUsername());
        assertEquals("John", inserted.getFirstName());
        assertEquals("Smith", inserted.getLastName());
        assertEquals("john@smith.org", inserted.getEmail());
        assertEquals(1, inserted.getCountryId());
        assertTrue(PasswordHasher.verify("!1securePassword1!", inserted.getPasswordHash()));
    }

    @Test
    void registerShouldThrowWhenCountryNotFound() {
        RegisterForm form = new RegisterForm(
                "john123", "John", "Smith", "john@smith.org",
                "!1securePassword1!", "Nowhere", LocalDate.of(2000, 1, 1)
        );

        when(countryRepo.getCountryIdByName("Nowhere")).thenReturn(null);

        assertThrows(UserRegisterException.class, () -> service.register(form));
    }

    @Test
    void registerShouldThrowWhenUsernameExists() {
        RegisterForm form = new RegisterForm(
                "john123", "John", "Smith", "john@smith.org",
                "!1securePassword1!", "Poland", LocalDate.of(2000, 1, 1)
        );

        when(countryRepo.getCountryIdByName("Poland")).thenReturn(1);
        when(userRepo.existUsername("john123")).thenReturn(true);
        when(userRepo.existEmail("john@smith.org")).thenReturn(false);

        assertThrows(UserRegisterException.class, () -> service.register(form));
    }

    @Test
    void registerShouldThrowWhenEmailExists() {
        RegisterForm form = new RegisterForm(
                "john123", "John", "Smith", "john@smith.org",
                "!1securePassword1!", "Poland", LocalDate.of(2000, 1, 1)
        );

        when(countryRepo.getCountryIdByName("Poland")).thenReturn(1);
        when(userRepo.existUsername("john123")).thenReturn(false);
        when(userRepo.existEmail("john@smith.org")).thenReturn(true);

        assertThrows(UserRegisterException.class, () -> service.register(form));
    }

    @Test
    void registerShouldThrowOnInsertFailure() {
        RegisterForm form = new RegisterForm(
                "john123", "John", "Smith", "john@smith.org",
                "!1securePassword1!", "Poland", LocalDate.of(2000, 1, 1)
        );

        when(countryRepo.getCountryIdByName("Poland")).thenReturn(1);
        when(userRepo.existUsername("john123")).thenReturn(false);
        when(userRepo.existEmail("john@smith.org")).thenReturn(false);

        doThrow(new RuntimeException("DB failure")).when(userRepo).insertUser(any());

        assertThrows(UserRegisterException.class, () -> service.register(form));
    }
}
