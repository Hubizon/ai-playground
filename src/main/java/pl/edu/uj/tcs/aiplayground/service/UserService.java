package pl.edu.uj.tcs.aiplayground.service;

import javafx.beans.property.StringProperty;
import pl.edu.uj.tcs.aiplayground.dto.UserDto;
import pl.edu.uj.tcs.aiplayground.dto.form.LoginForm;
import pl.edu.uj.tcs.aiplayground.dto.form.RegisterForm;
import pl.edu.uj.tcs.aiplayground.dto.form.UpdateUserForm;
import pl.edu.uj.tcs.aiplayground.dto.validation.UserValidation;
import pl.edu.uj.tcs.aiplayground.exception.DatabaseException;
import pl.edu.uj.tcs.aiplayground.exception.UserModificationException;
import pl.edu.uj.tcs.aiplayground.service.repository.IUserRepository;
import pl.edu.uj.tcs.aiplayground.service.utility.PasswordHasher;
import pl.edu.uj.tcs.jooq.tables.records.UsersRecord;

import java.util.List;
import java.util.UUID;

public class UserService {
    private final IUserRepository userRepository;

    public UserService(IUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<String> getCountryNames() throws DatabaseException {
        try {
            return userRepository.getCountries();
        } catch (Exception e) {
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
        } catch (Exception e) {
            throw new DatabaseException(e);
        }

        if (!PasswordHasher.verify(loginForm.password(), user.getPasswordHash()))
            throw new UserModificationException("Invalid username or password");

        String countryName;
        try {
            countryName = userRepository.getCountryNameById(user.getCountryId());
        } catch (Exception e) {
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

    public void updateUser(UUID userId, UpdateUserForm updateUserForm) throws UserModificationException, DatabaseException {
        UserValidation.validateUpdateUserForm(updateUserForm);

        String hashedPassword = PasswordHasher.hash(updateUserForm.password());
        UpdateUserForm registerFormHashed = updateUserForm.withHashedPassword(hashedPassword);

        try {
            userRepository.updateUser(userId, registerFormHashed);
        } catch (Exception e) {
            throw new DatabaseException(e);
        }
    }

    public int userTokenCount(UUID userId) throws DatabaseException {
        try {
            return userRepository.userTokenCount(userId);
        } catch (Exception e) {
            throw new DatabaseException(e);
        }
    }

    public boolean isUserAdmin(UUID userId) throws DatabaseException {
        try {
            return userRepository.isUserAdmin(userId);
        } catch (Exception e) {
            throw new DatabaseException(e);
        }
    }

    public List<String> getUsernamesWithoutUser(UUID userId) throws DatabaseException {
        try {
            return userRepository.getUsernamesWithoutUser(userId);
        } catch (Exception e) {
            throw new DatabaseException(e);
        }
    }

    public List<String> getRoleNames() throws DatabaseException {
        try {
            return userRepository.getRoleNames();
        } catch (Exception e) {
            throw new DatabaseException(e);
        }
    }

    public String getUserRole(StringProperty chosenUser) throws DatabaseException {
        try {
            return userRepository.getUserRole(chosenUser);
        } catch (Exception e) {
            throw new DatabaseException(e);
        }
    }

    public void setRoleForUser(String username, String role) throws DatabaseException {
        try {
            userRepository.setRoleForUser(username, role);
        } catch (Exception e) {
            throw new DatabaseException(e);
        }
    }

    public void deleteUser(String username) throws DatabaseException, UserModificationException {
        UUID userId;
        boolean isAdmin;
        try {
            userId = userRepository.findByUsername(username).getId();
            isAdmin = userRepository.isUserAdmin(userId);
        } catch (Exception e) {
            throw new DatabaseException(e);
        }

        if (isAdmin) {
            throw new UserModificationException("Cannot delete admin user");
        } else {
            try {
                userRepository.deleteUser(userId);
            } catch (Exception e) {
                throw new DatabaseException(e);
            }
        }
    }

    public UserDto getUser(UUID userId) throws DatabaseException {
        try {
            return userRepository.getUser(userId);
        } catch (Exception e) {
            throw new DatabaseException(e);
        }
    }
}
