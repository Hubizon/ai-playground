package pl.edu.uj.tcs.aiplayground.viewmodels;

import pl.edu.uj.tcs.aiplayground.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoginViewModelTest {

    private LoginViewModel viewModel;

    @BeforeEach
    void setup() {
        viewModel = new LoginViewModel(new FakeUserService());
    }

    @Test
    void login_success() {
        viewModel.usernameProperty().set("testUser");
        viewModel.passwordProperty().set("pass");
        assertTrue(viewModel.login());
    }

    @Test
    void login_error() {
        viewModel.usernameProperty().set("testUser");
        viewModel.passwordProperty().set("");
        assertFalse(viewModel.login());
    }

    static class FakeUserService extends UserService {
        @Override
        public boolean login(String username, String password) {
            return "testUser".equals(username) && "pass".equals(password);
        }
    }
}
