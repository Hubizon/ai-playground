package pl.edu.uj.tcs.aiplayground.services;

import pl.edu.uj.tcs.aiplayground.core.User;

public class UserService {
    public boolean login(String username, String password) {
        System.out.println("User Login: " + username);
        User user = new User(username, password);
        return !username.isEmpty() && !password.isEmpty();
    }
}
