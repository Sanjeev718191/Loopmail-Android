package com.example.loopmail.model;

import java.util.List;

public class User {
    private String id;
    private String username;
    private String email;
    private String avatar;
    private List<Task> tasks;

    public User(String id, String username, String email, String avatar, List<Task> tasks) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.avatar = avatar;
        this.tasks = tasks;
    }

    public String getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getAvatar() { return avatar; }
    public List<Task> getTasks() { return tasks; }
}
