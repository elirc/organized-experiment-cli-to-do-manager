package todo.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import todo.model.Task;

public class TaskManager {
    private static final DateTimeFormatter DISPLAY_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final List<Task> tasks;
    private int nextId;

    public TaskManager() {
        this.tasks = new ArrayList<>();
        this.nextId = 1;
    }

    public void loadTasks(List<Task> loadedTasks) {
        tasks.clear();
        tasks.addAll(loadedTasks);
        tasks.sort(Comparator.comparingInt(Task::getId));
        nextId = calculateNextId();
    }

    public Task addTask(String title, String description) {
        Task task = new Task(
                nextId,
                title,
                description,
                false,
                LocalDateTime.now(),
                null
        );

        tasks.add(task);
        nextId++;
        return task;
    }

    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks);
    }

    public boolean completeTask(int taskId) {
        Task task = findTaskById(taskId);
        if (task == null) {
            return false;
        }

        return task.markCompleted();
    }

    public boolean deleteTask(int taskId) {
        Task task = findTaskById(taskId);
        if (task == null) {
            return false;
        }

        return tasks.remove(task);
    }

    public String formatTaskForDisplay(Task task) {
        StringBuilder builder = new StringBuilder();
        builder.append(task.isCompleted() ? "[X] " : "[ ] ");
        builder.append("ID ").append(task.getId()).append(" - ").append(task.getTitle());
        builder.append(System.lineSeparator());
        builder.append("    Created: ").append(formatDate(task.getCreatedAt()));

        if (task.isCompleted() && task.getCompletedAt() != null) {
            builder.append(System.lineSeparator());
            builder.append("    Completed: ").append(formatDate(task.getCompletedAt()));
        }

        if (task.hasDescription()) {
            builder.append(System.lineSeparator());
            builder.append("    Description: ").append(task.getDescription());
        }

        return builder.toString();
    }

    private Task findTaskById(int taskId) {
        for (Task task : tasks) {
            if (task.getId() == taskId) {
                return task;
            }
        }

        return null;
    }

    private int calculateNextId() {
        int highestId = 0;

        for (Task task : tasks) {
            if (task.getId() > highestId) {
                highestId = task.getId();
            }
        }

        return highestId + 1;
    }

    private String formatDate(LocalDateTime dateTime) {
        return dateTime.format(DISPLAY_FORMAT);
    }
}
