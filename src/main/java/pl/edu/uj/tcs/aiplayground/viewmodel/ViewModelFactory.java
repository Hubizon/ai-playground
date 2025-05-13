package pl.edu.uj.tcs.aiplayground.viewmodel;

import org.jooq.DSLContext;
import pl.edu.uj.tcs.aiplayground.repository.IUserRepository;
import pl.edu.uj.tcs.aiplayground.repository.JooqFactory;
import pl.edu.uj.tcs.aiplayground.repository.UserRepository;
import pl.edu.uj.tcs.aiplayground.service.UserService;

public class ViewModelFactory {
    private final DSLContext dsl;
    private final UserViewModel userViewModel;
    private final MainViewModel mainViewModel;

    public ViewModelFactory() {
        this.dsl = JooqFactory.getDSLContext();

        var userRepository = new UserRepository(dsl);
        var userService = new UserService(userRepository);
        this.userViewModel = new UserViewModel(userService);

        this.mainViewModel = new MainViewModel();
    }

    public UserViewModel getUserViewModel() {
        return userViewModel;
    }

    public MainViewModel getMainViewModel() {
        return mainViewModel;
    }
}
