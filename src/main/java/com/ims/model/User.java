package com.ims.model;

public class User {
    private int userId;
    private String username;
    private String password;
    private String role;

    public User() {
    }

    public User(int userId, String username, String password, String role) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public int getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }

    public String getPassword() {
        return password;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean matchesCredentials(String providedUsername, String providedPassword) {
        return username != null
            && password != null
            && username.equals(providedUsername)
            && password.equals(providedPassword);
    }

    public boolean hasRole(String expectedRole) {
        return role != null && expectedRole != null && role.equalsIgnoreCase(expectedRole);
    }
}
