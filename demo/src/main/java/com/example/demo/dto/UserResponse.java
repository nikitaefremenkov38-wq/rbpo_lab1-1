package com.example.demo.dto;

import java.util.Set;

public class UserResponse {

    private String username;
    private Set<String> roles;

    public UserResponse(String username, Set<String> roles) {
        this.username = username;
        this.roles = roles;
    }

    public String getUsername() {
        return username;
    }

    public Set<String> getRoles() {
        return roles;
    }
}
