package todo.repository;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import todo.model.Task;

public class TaskRepository {
    private final Path filePath;

    public TaskRepository(String fileName) {
        this.filePath = Path.of(fileName);
    }

    public List<Task> loadTasks() throws IOException {
        List<Task> tasks = new ArrayList<>();

        if (!Files.exists(filePath)) {
            return tasks;
        }

        try (BufferedReader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
            List<String> block = new ArrayList<>();
            String line;

            while ((line = reader.readLine()) != null) {
                if (line.equals("---")) {
                    if (!block.isEmpty()) {
                        tasks.add(parseTask(block));
                        block.clear();
                    }
                } else {
                    block.add(line);
                }
            }

            if (!block.isEmpty()) {
                tasks.add(parseTask(block));
            }
        }

        return tasks;
    }

    public void saveTasks(List<Task> tasks) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8)) {
            for (Task task : tasks) {
                writer.write("id=" + task.getId());
                writer.newLine();
                writer.write("title=" + task.getTitle());
                writer.newLine();
                writer.write("description=" + task.getDescription());
                writer.newLine();
                writer.write("completed=" + task.isCompleted());
                writer.newLine();
                writer.write("createdAt=" + task.getCreatedAt());
                writer.newLine();
                writer.write("completedAt=" + toStorageValue(task.getCompletedAt()));
                writer.newLine();
                writer.write("---");
                writer.newLine();
            }
        }
    }

    private Task parseTask(List<String> lines) throws IOException {
        try {
            Integer id = null;
            String title = "";
            String description = "";
            Boolean completed = null;
            LocalDateTime createdAt = null;
            LocalDateTime completedAt = null;

            for (String line : lines) {
                String[] parts = line.split("=", 2);
                if (parts.length != 2) {
                    throw new IOException("Invalid line in data file: " + line);
                }

                String key = parts[0];
                String value = parts[1];

                switch (key) {
                    case "id" -> id = Integer.parseInt(value);
                    case "title" -> title = value;
                    case "description" -> description = value;
                    case "completed" -> completed = Boolean.parseBoolean(value);
                    case "createdAt" -> createdAt = LocalDateTime.parse(value);
                    case "completedAt" -> {
                        if (!value.isBlank()) {
                            completedAt = LocalDateTime.parse(value);
                        }
                    }
                    default -> {
                    }
                }
            }

            if (id == null || completed == null || createdAt == null || title.isBlank()) {
                throw new IOException("Task data is missing required fields.");
            }

            return new Task(id, title, description, completed, createdAt, completedAt);
        } catch (NumberFormatException | DateTimeParseException e) {
            throw new IOException("Task data has an invalid value.", e);
        }
    }

    private String toStorageValue(LocalDateTime dateTime) {
        return dateTime == null ? "" : dateTime.toString();
    }
}
