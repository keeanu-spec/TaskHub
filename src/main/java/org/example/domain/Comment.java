package org.example.domain;


import java.time.LocalDateTime;
import java.util.UUID;

public class Comment {

    private UUID id;
    private String content;
    private User author;
    private Task task;
    private  LocalDateTime createdAt;

    public Comment( String content, User author, Task task) {
        this.id = UUID.randomUUID();
        this.content = content;
        this.author = author;
        this.task = task;
        this.createdAt = LocalDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}