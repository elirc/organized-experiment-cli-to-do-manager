package todo.model;

import java.time.LocalDateTime;

public class Task {
    private final int id;
    private final String title;
    private final String description;
    private boolean completed;
    private final LocalDateTime createdAt;
    private LocalDateTime completedAt;

    public Task(int id, String title, String description, boolean completed,
            LocalDateTime createdAt, LocalDateTime completedAt) {
        this.id = id;
        this.title = title;
        this.description = description == null ? "" : description;
        this.completed = completed;
        this.createdAt = createdAt;
        this.completedAt = completedAt;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public boolean isCompleted() {
        return completed;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public boolean hasDescription() {
        return !description.isBlank();
    }

    public boolean markCompleted() {
        if (completed) {
            return false;
        }

        completed = true;
        completedAt = LocalDateTime.now();
        return true;
    }
}
