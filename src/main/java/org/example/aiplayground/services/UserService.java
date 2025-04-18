package org.example.aiplayground.services;

import org.example.aiplayground.core.User;

public class UserService {
    public boolean login(String username, String password) {
        System.out.println("User Login: " + username);
        User user = new User(username, password);
        return !username.isEmpty() && !password.isEmpty();
    }
}
