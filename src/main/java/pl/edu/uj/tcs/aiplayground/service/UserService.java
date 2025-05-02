package pl.edu.uj.tcs.aiplayground.service;

import org.example.jooq.tables.records.CountriesRecord;
import org.example.jooq.tables.records.UsersRecord;
import pl.edu.uj.tcs.aiplayground.dto.LoginForm;
import pl.edu.uj.tcs.aiplayground.dto.RegisterForm;
import pl.edu.uj.tcs.aiplayground.exception.UserLoginException;
import pl.edu.uj.tcs.aiplayground.exception.UserRegisterException;
import pl.edu.uj.tcs.aiplayground.repository.ICountryRepository;
import pl.edu.uj.tcs.aiplayground.repository.IUserRepository;
import pl.edu.uj.tcs.aiplayground.utility.PasswordHasher;
import pl.edu.uj.tcs.aiplayground.validation.UserValidation;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class UserService {
    private final IUserRepository userRepository;
    private final ICountryRepository countryRepository;

    public UserService(IUserRepository userRepository, ICountryRepository countryRepository) {
        this.userRepository = userRepository;
        this.countryRepository = countryRepository;
    }

    public List<String> getCountryNames() {
        List<CountriesRecord> countriesRecords = countryRepository.getCountries();
        return countriesRecords.stream().map(CountriesRecord::getName).collect(Collectors.toList());
    }

    public UsersRecord login(LoginForm loginForm) throws UserLoginException {
        UserValidation.validateLoginForm(loginForm);

        UsersRecord user = userRepository.findByUsername(loginForm.username());
        if (user == null)
            throw new UserLoginException("User doesn't exist");

        if (!PasswordHasher.verify(loginForm.password(), user.getPasswordHash()))
            throw new UserLoginException("Invalid username or password");

        return user;
    }

    public void register(RegisterForm registerForm) throws UserRegisterException {
        UserValidation.validateRegisterForm(registerForm);

        Integer country_id = countryRepository.getCountryIdByName(registerForm.country());
        if (country_id == null)
            throw new UserRegisterException("Country not found");

        if (userRepository.existUsername(registerForm.username()))
            throw new UserRegisterException("Username taken");
        if (userRepository.existEmail(registerForm.email()))
            throw new UserRegisterException("User with this email already exists");

        String password_hashed = PasswordHasher.hash(registerForm.password());

        UUID userId = UUID.randomUUID();
        OffsetDateTime created_at = OffsetDateTime.now();

        UsersRecord user = new UsersRecord(
                userId,
                registerForm.username(),
                registerForm.firstName(),
                registerForm.lastName(),
                registerForm.email(),
                password_hashed,
                country_id,
                registerForm.birthDate(),
                created_at
        );

        try {
            userRepository.insertUser(user);
        } catch (Exception e) {
            throw new UserRegisterException("Illegal data");
        }
    }
}
