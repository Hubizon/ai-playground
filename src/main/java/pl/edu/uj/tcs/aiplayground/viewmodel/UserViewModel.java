package pl.edu.uj.tcs.aiplayground.viewmodel;

import pl.edu.uj.tcs.aiplayground.exception.EmailAlreadyUsedException;
import pl.edu.uj.tcs.aiplayground.exception.InvalidLoginOrPasswordException;
import pl.edu.uj.tcs.aiplayground.exception.UsernameTakenException;
import pl.edu.uj.tcs.aiplayground.repository.CountryRepository;
import pl.edu.uj.tcs.aiplayground.repository.JooqFactory;
import pl.edu.uj.tcs.aiplayground.repository.UserRepository;
import pl.edu.uj.tcs.aiplayground.service.AuthService;
import org.example.jooq.tables.records.UsersRecord;
import org.jooq.DSLContext;
import org.mindrot.jbcrypt.BCrypt;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public class UserViewModel {
    private final UserRepository userRepository;
    private final CountryRepository countryRepository;
    private final AuthService authService;

    public UserViewModel() {
        DSLContext dslContext = JooqFactory.getDSLContext();
        this.userRepository = new UserRepository(dslContext);
        this.countryRepository = new CountryRepository(dslContext);
        authService = new AuthService(userRepository, countryRepository);
    }

    public UserViewModel(UserRepository userRepository, CountryRepository countryRepository) {
        this.userRepository = userRepository;
        this.countryRepository = countryRepository;
        authService = new AuthService(userRepository, countryRepository);
    }

    public UsersRecord login(String username, String password) throws InvalidLoginOrPasswordException {
        UsersRecord user = authService.authenticate(username, password);
        if (user == null)
            throw new InvalidLoginOrPasswordException();
        return user;
    }

    private static String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    public void register(String username,
                         String firstName,
                         String lastName,
                         String email,
                         String password,
                         String country,
                         LocalDate birthDate)
            throws UsernameTakenException, EmailAlreadyUsedException, IllegalArgumentException {
        Integer country_id = countryRepository.getCountryIdByName(country);
        if (country_id == null)
            throw new IllegalArgumentException("Country not found");

        String password_hashed = hashPassword(password);

        UUID userId = UUID.randomUUID();
        OffsetDateTime created_at = OffsetDateTime.now();

        UsersRecord user = new UsersRecord(
                userId,
                username,
                firstName,
                lastName,
                email,
                password_hashed,
                country_id,
                birthDate,
                created_at
        );

        authService.register(user);
    }
}
