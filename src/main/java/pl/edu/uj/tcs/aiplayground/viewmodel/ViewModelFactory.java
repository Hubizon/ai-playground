package pl.edu.uj.tcs.aiplayground.viewmodel;

import org.jooq.DSLContext;
import pl.edu.uj.tcs.aiplayground.repository.*;
import pl.edu.uj.tcs.aiplayground.service.UserService;

public class ViewModelFactory {
    private static final DSLContext dsl = JooqFactory.getDSLContext();

    public static UserViewModel createUserViewModel(IUserRepository userRepository, ICountryRepository countryRepository) {
        UserService userService = new UserService(userRepository, countryRepository);
        return new UserViewModel(userService);
    }

    public static UserViewModel createUserViewModel() {
        IUserRepository userRepository = new UserRepository(dsl);
        ICountryRepository countryRepository = new CountryRepository(dsl);
        return createUserViewModel(userRepository, countryRepository);
    }
}
