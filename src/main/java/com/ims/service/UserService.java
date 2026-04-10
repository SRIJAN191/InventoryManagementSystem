package com.ims.service;

import com.ims.dao.UserDAO;
import com.ims.model.User;

import java.util.List;

public class UserService {
    private final UserDAO userDAO = new UserDAO();

    public List<User> getStaffMembers(String search) {
        return userDAO.findAllByRole("Staff", search);
    }

    public void saveStaff(User existingUser, String username, String password, String role) {
        String normalizedUsername = username == null ? "" : username.trim();
        String normalizedPassword = password == null ? "" : password.trim();
        String normalizedRole = role == null ? "" : role.trim();
        boolean editing = existingUser != null && existingUser.getUserId() > 0;

        if (normalizedUsername.isBlank()) {
            throw new IllegalArgumentException("Username is required.");
        }
        if (normalizedRole.isBlank()) {
            throw new IllegalArgumentException("Role is required.");
        }
        if (!"Staff".equalsIgnoreCase(normalizedRole) && !"Admin".equalsIgnoreCase(normalizedRole)) {
            throw new IllegalArgumentException("Role must be either Staff or Admin.");
        }
        if (!editing && normalizedPassword.isBlank()) {
            throw new IllegalArgumentException("Password is required for a new staff account.");
        }
        if (userDAO.existsByUsername(normalizedUsername, editing ? existingUser.getUserId() : null)) {
            throw new IllegalArgumentException("That username is already in use.");
        }

        User user = new User(
            editing ? existingUser.getUserId() : 0,
            normalizedUsername,
            normalizedPassword.isBlank() && editing ? existingUser.getPassword() : normalizedPassword,
            normalizedRole
        );

        if (editing) {
            userDAO.update(user, !normalizedPassword.isBlank());
        } else {
            userDAO.save(user);
        }
    }

    public void deleteStaff(User staffUser, User currentUser) {
        if (staffUser == null) {
            throw new IllegalArgumentException("Select a staff account to delete.");
        }
        if (!staffUser.hasRole("Staff")) {
            throw new IllegalArgumentException("Only staff accounts can be managed here.");
        }
        if (currentUser != null && currentUser.getUserId() == staffUser.getUserId()) {
            throw new IllegalArgumentException("You cannot delete the account you are currently using.");
        }
        userDAO.delete(staffUser.getUserId());
    }
}
