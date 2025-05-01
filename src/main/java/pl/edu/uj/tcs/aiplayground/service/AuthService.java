package pl.edu.uj.tcs.aiplayground.service;

import pl.edu.uj.tcs.aiplayground.exception.EmailAlreadyUsedException;
import pl.edu.uj.tcs.aiplayground.exception.UsernameTakenException;
import pl.edu.uj.tcs.aiplayground.repository.CountryRepository;
import pl.edu.uj.tcs.aiplayground.repository.UserRepository;
import org.example.jooq.tables.records.UsersRecord;
import org.mindrot.jbcrypt.BCrypt;

import static pl.edu.uj.tcs.aiplayground.validation.UserValidation.*;

public class AuthService {
    private final UserRepository userRepository;
    private final CountryRepository countryRepository;

    public AuthService(UserRepository userRepository, CountryRepository countryRepository) {
        this.userRepository = userRepository;
        this.countryRepository = countryRepository;
    }

    private void checkStringField(String field, int min_chars, int max_chars, String fieldName) throws IllegalArgumentException {
        if (field == null || field.isEmpty())
            throw new IllegalArgumentException(fieldName + " is null or empty");
        if (field.length() < min_chars || field.length() > max_chars)
            throw new IllegalArgumentException(fieldName + " must be between " + min_chars + " and " + max_chars + " characters");
    }

    public UsersRecord authenticate(String username, String password) {
        checkStringField(username, USERNAME_MIN_LENGTH, USERNAME_MAX_LENGTH, "username");
        checkStringField(password, EMAIL_MIN_LENGTH, EMAIL_MAX_LENGTH, "password");

        UsersRecord user = userRepository.findByUsername(username);
        if (user == null)
            return null;

        if (BCrypt.checkpw(password, user.getPasswordHash()))
            return user;

        return null;
    }

    public void register(UsersRecord user)
            throws UsernameTakenException, EmailAlreadyUsedException, IllegalArgumentException {
        checkStringField(user.getUsername(), USERNAME_MIN_LENGTH, USERNAME_MAX_LENGTH, "username");
        checkStringField(user.getEmail(), EMAIL_MIN_LENGTH, EMAIL_MAX_LENGTH, "email");

        if (userRepository.findByUsername(user.getUsername()) != null)
            throw new UsernameTakenException();
        if (userRepository.findByEmail(user.getEmail()) != null)
            throw new EmailAlreadyUsedException();
        if (countryRepository.getCountryById(user.getCountryId()) == null)
            throw new IllegalArgumentException("Illegal country");

        userRepository.insertUser(user);
    }
}
