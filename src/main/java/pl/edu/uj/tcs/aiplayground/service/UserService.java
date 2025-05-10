package pl.edu.uj.tcs.aiplayground.service;

import pl.edu.uj.tcs.jooq.tables.records.CountriesRecord;
import pl.edu.uj.tcs.jooq.tables.records.UsersRecord;
import pl.edu.uj.tcs.aiplayground.form.LoginForm;
import pl.edu.uj.tcs.aiplayground.form.RegisterForm;
import pl.edu.uj.tcs.aiplayground.dto.UserDto;
import pl.edu.uj.tcs.aiplayground.exception.DatabaseException;
import pl.edu.uj.tcs.aiplayground.exception.UserModificationException;
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

    public List<String> getCountryNames() throws DatabaseException {
        List<CountriesRecord> countriesRecords;
        try {
            countriesRecords = countryRepository.getCountries();
        } catch(Exception e) {
            throw new DatabaseException(e);
        }

        return countriesRecords.stream().map(CountriesRecord::getName).collect(Collectors.toList());
    }

    public UserDto login(LoginForm loginForm) throws UserModificationException, DatabaseException {
        UserValidation.validateLoginForm(loginForm);

        UsersRecord user;
        try {
            user = userRepository.findByUsername(loginForm.username());

        } catch(Exception e) {
            throw new DatabaseException(e);
        }

        if (user == null)
            throw new UserModificationException("User doesn't exist");

        if (!PasswordHasher.verify(loginForm.password(), user.getPasswordHash()))
            throw new UserModificationException("Invalid username or password");

        CountriesRecord countriesRecord;
        try {
            countriesRecord = countryRepository.getCountryById(user.getCountryId());
        } catch(Exception e) {
            throw new DatabaseException(e);
        }

        return new UserDto(
                user.getId(),
                user.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                countriesRecord.getName(),
                user.getBirthDate()
        );
    }

    public void register(RegisterForm registerForm) throws UserModificationException, DatabaseException {
        UserValidation.validateRegisterForm(registerForm);

        Integer country_id = countryRepository.getCountryIdByName(registerForm.country());
        if (country_id == null)
            throw new UserModificationException("Country not found");

        if (userRepository.existUsername(registerForm.username()))
            throw new UserModificationException("Username taken");
        if (userRepository.existEmail(registerForm.email()))
            throw new UserModificationException("User with this email already exists");

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
            throw new DatabaseException(e);
        }
    }
}
