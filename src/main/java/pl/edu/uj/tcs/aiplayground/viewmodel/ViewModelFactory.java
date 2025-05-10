package pl.edu.uj.tcs.aiplayground.viewmodel;

import org.jooq.DSLContext;
import pl.edu.uj.tcs.aiplayground.repository.IUserRepository;
import pl.edu.uj.tcs.aiplayground.repository.JooqFactory;
import pl.edu.uj.tcs.aiplayground.repository.UserRepository;
import pl.edu.uj.tcs.aiplayground.service.UserService;

public class ViewModelFactory {
    private static final DSLContext dsl = JooqFactory.getDSLContext();

    public static UserViewModel createUserViewModel(IUserRepository userRepository) {
        UserService userService = new UserService(userRepository);
        return new UserViewModel(userService);
    }

    public static UserViewModel createUserViewModel() {
        IUserRepository userRepository = new UserRepository(dsl);
        return createUserViewModel(userRepository);
    }
}
