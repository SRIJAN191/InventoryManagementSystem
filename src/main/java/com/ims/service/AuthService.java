package com.ims.service;

import com.ims.dao.UserDAO;
import com.ims.model.User;

public class AuthService {
    private final UserDAO userDAO = new UserDAO();

    public User login(String username, String password) {
        String normalizedUsername = username == null ? "" : username.trim();
        String normalizedPassword = password == null ? "" : password.trim();
        if (normalizedUsername.isBlank() || normalizedPassword.isBlank()) {
            throw new IllegalArgumentException("Username and password are required.");
        }

        User user = userDAO.findByUsername(normalizedUsername);
        if (user == null || !user.matchesCredentials(normalizedUsername, normalizedPassword)) {
            throw new IllegalArgumentException("Invalid username or password.");
        }
        return user;
    }

    public User login(String username, String password, String expectedRole) {
        User user = login(username, password);
        String normalizedRole = expectedRole == null ? "" : expectedRole.trim();
        if (!normalizedRole.isBlank()
            && !user.hasRole(normalizedRole)
            && !("Admin".equalsIgnoreCase(normalizedRole) && user.hasRole("Owner"))) {
            throw new IllegalArgumentException("Selected role does not match this account.");
        }
        return user;
    }
}
