package pl.edu.uj.tcs.aiplayground.views;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import pl.edu.uj.tcs.aiplayground.viewmodels.LoginViewModel;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LoginViewControllerTest extends ApplicationTest {

    private FakeLoginViewModel fakeViewModel;

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/pl/edu/uj/tcs/aiplayground/views/LoginView.fxml"));
        Parent root = loader.load();
        LoginViewController controller = loader.getController();

        fakeViewModel = new FakeLoginViewModel();
        Field viewModelField = controller.getClass().getDeclaredField("viewModel");
        viewModelField.setAccessible(true);
        viewModelField.set(controller, fakeViewModel);

        stage.setScene(new Scene(root));
        stage.show();
    }

    @Test
    void testLoginButtonCallsLoginOnViewModel() {
        clickOn("#usernameField").write("testUser");
        clickOn("#passwordField").write("pass");
        clickOn("#loginButton");

        assertTrue(fakeViewModel.loginCalled);
        assertEquals("testUser", fakeViewModel.usernameProperty().get());
        assertEquals("pass", fakeViewModel.passwordProperty().get());
    }

    static class FakeLoginViewModel extends LoginViewModel {
        public boolean loginCalled = false;

        @Override
        public boolean login() {
            loginCalled = true;
            return true;
        }
    }
}
