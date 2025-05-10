package pl.edu.uj.tcs.aiplayground.service;

import pl.edu.uj.tcs.jooq.tables.records.UsersRecord;
import pl.edu.uj.tcs.aiplayground.form.LoginForm;
import pl.edu.uj.tcs.aiplayground.form.RegisterForm;
import pl.edu.uj.tcs.aiplayground.dto.UserDto;
import pl.edu.uj.tcs.aiplayground.exception.DatabaseException;
import pl.edu.uj.tcs.aiplayground.exception.UserModificationException;
import pl.edu.uj.tcs.aiplayground.repository.IUserRepository;
import pl.edu.uj.tcs.aiplayground.utility.PasswordHasher;
import pl.edu.uj.tcs.aiplayground.validation.UserValidation;

import java.util.List;

public class UserService {
    private final IUserRepository userRepository;

    public UserService(IUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<String> getCountryNames() throws DatabaseException {
        try {
            return userRepository.getCountries();
        } catch(Exception e) {
            throw new DatabaseException(e);
        }
    }

    public UserDto login(LoginForm loginForm) throws UserModificationException, DatabaseException {
        UserValidation.validateLoginForm(loginForm);

        if (!userRepository.existUsername(loginForm.username()))
            throw new UserModificationException("User doesn't exist");

        UsersRecord user;
        try {
            user = userRepository.findByUsername(loginForm.username());
        } catch(Exception e) {
            throw new DatabaseException(e);
        }

        if (!PasswordHasher.verify(loginForm.password(), user.getPasswordHash()))
            throw new UserModificationException("Invalid username or password");

        String countryName;
        try {
            countryName = userRepository.getCountryNameById(user.getCountryId());
        } catch(Exception e) {
            throw new DatabaseException(e);
        }

        return new UserDto(
                user.getId(),
                user.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                countryName,
                user.getBirthDate()
        );
    }

    public void register(RegisterForm registerForm) throws UserModificationException, DatabaseException {
        UserValidation.validateRegisterForm(registerForm);

        Integer country_id = userRepository.getCountryIdByName(registerForm.country());
        if (country_id == null)
            throw new UserModificationException("Country not found");

        if (userRepository.existUsername(registerForm.username()))
            throw new UserModificationException("Username taken");
        if (userRepository.existEmail(registerForm.email()))
            throw new UserModificationException("User with this email already exists");

        String hashedPassword = PasswordHasher.hash(registerForm.password());
        RegisterForm registerFormHashed = registerForm.withHashedPassword(hashedPassword);

        try {
            userRepository.insertUser(registerFormHashed);
        } catch (Exception e) {
            throw new DatabaseException(e);
        }
    }
}
