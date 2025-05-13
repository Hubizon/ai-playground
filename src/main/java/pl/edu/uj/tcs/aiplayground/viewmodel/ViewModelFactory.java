package pl.edu.uj.tcs.aiplayground.viewmodel;

import org.jooq.DSLContext;
import pl.edu.uj.tcs.aiplayground.repository.IUserRepository;
import pl.edu.uj.tcs.aiplayground.repository.JooqFactory;
import pl.edu.uj.tcs.aiplayground.repository.UserRepository;
import pl.edu.uj.tcs.aiplayground.service.UserService;

public class ViewModelFactory {
    private static final DSLContext dsl = JooqFactory.getDSLContext();

    public static LoginViewModel createLoginViewModel(IUserRepository userRepository) {
        UserService userService = new UserService(userRepository);
        return new LoginViewModel(userService);
    }

    public static LoginViewModel createLoginViewModel() {
        IUserRepository userRepository = new UserRepository(dsl);
        return createLoginViewModel(userRepository);
    }
}
