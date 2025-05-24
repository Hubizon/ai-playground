package pl.edu.uj.tcs.aiplayground.viewmodel;

import org.jooq.DSLContext;
import pl.edu.uj.tcs.aiplayground.service.ModelService;
import pl.edu.uj.tcs.aiplayground.service.TrainingService;
import pl.edu.uj.tcs.aiplayground.service.UserService;
import pl.edu.uj.tcs.aiplayground.service.repository.JooqFactory;
import pl.edu.uj.tcs.aiplayground.service.repository.ModelRepository;
import pl.edu.uj.tcs.aiplayground.service.repository.TrainingRepository;
import pl.edu.uj.tcs.aiplayground.service.repository.UserRepository;

public class ViewModelFactory {
    private final DSLContext dsl;
    private final UserViewModel userViewModel;
    private final MainViewModel mainViewModel;

    public ViewModelFactory() {
        this.dsl = JooqFactory.getDSLContext();

        var userRepository = new UserRepository(dsl);
        var userService = new UserService(userRepository);
        this.userViewModel = new UserViewModel(userService);

        var modelRepository = new ModelRepository(dsl);
        var modelService = new ModelService(modelRepository);
        var trainingRepository = new TrainingRepository(dsl);
        var trainingService = new TrainingService(trainingRepository);
        this.mainViewModel = new MainViewModel(modelService, trainingService);
    }

    public UserViewModel getUserViewModel() {
        return userViewModel;
    }

    public MainViewModel getMainViewModel() {
        return mainViewModel;
    }
}
