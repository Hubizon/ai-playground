package pl.edu.uj.tcs.aiplayground.viewmodel;

import org.jooq.DSLContext;
import pl.edu.uj.tcs.aiplayground.service.LeaderboardService;
import pl.edu.uj.tcs.aiplayground.service.ModelService;
import pl.edu.uj.tcs.aiplayground.service.TrainingService;
import pl.edu.uj.tcs.aiplayground.service.UserService;
import pl.edu.uj.tcs.aiplayground.service.repository.*;

import java.sql.Connection;

public class ViewModelFactory {
    private final Connection conn;
    private final DSLContext dsl;
    private final UserViewModel userViewModel;
    private final MainViewModel mainViewModel;
    private final LeaderboardViewModel leaderboardViewModel;

    public ViewModelFactory() {
        this.conn = JooqFactory.getConnection();
        this.dsl = JooqFactory.getDSLContext();

        var userRepository = new UserRepository(dsl);
        var userService = new UserService(userRepository);
        this.userViewModel = new UserViewModel(userService);

        var modelRepository = new ModelRepository(dsl);
        var modelService = new ModelService(modelRepository);
        var trainingRepository = new TrainingRepository(conn, dsl);
        var trainingService = new TrainingService(trainingRepository);
        this.mainViewModel = new MainViewModel(modelService, userService, trainingService);

        var leaderboardRepository = new LeaderboardRepository(dsl);
        var leaderboardService = new LeaderboardService(leaderboardRepository);
        this.leaderboardViewModel = new LeaderboardViewModel(leaderboardService);
    }

    public UserViewModel getUserViewModel() {
        return userViewModel;
    }

    public MainViewModel getMainViewModel() {
        return mainViewModel;
    }

    public LeaderboardViewModel getLeaderboardViewModel() {
        return leaderboardViewModel;
    }
}
