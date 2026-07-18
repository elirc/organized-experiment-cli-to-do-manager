package todo;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;
import todo.model.Task;
import todo.repository.TaskRepository;
import todo.service.TaskManager;

public class App {
    private final Scanner scanner;
    private final TaskManager taskManager;
    private final TaskRepository taskRepository;

    public App(TaskRepository taskRepository) {
        this.scanner = new Scanner(System.in);
        this.taskManager = new TaskManager();
        this.taskRepository = taskRepository;
    }

    public static void main(String[] args) {
        TaskRepository repository = new TaskRepository("tasks.txt");
        App app = new App(repository);
        app.run();
    }

    public void run() {
        loadTasksFromDisk();

        try {
            boolean running = true;

            while (running) {
                printMenu();
                String choice = scanner.nextLine().trim();

                switch (choice) {
                    case "1" -> addTask();
                    case "2" -> listTasks();
                    case "3" -> completeTask();
                    case "4" -> deleteTask();
                    case "5" -> running = false;
                    default -> System.out.println("Please enter a number from 1 to 5.");
                }
            }
        } finally {
            saveTasksToDisk();
            scanner.close();
            System.out.println("Goodbye!");
        }
    }

    private void loadTasksFromDisk() {
        try {
            List<Task> loadedTasks = taskRepository.loadTasks();
            taskManager.loadTasks(loadedTasks);
            System.out.println("Loaded " + loadedTasks.size() + " task(s).");
        } catch (IOException e) {
            System.out.println("Could not load tasks. Starting with an empty list.");
            System.out.println("Reason: " + e.getMessage());
            taskManager.loadTasks(List.of());
        }
    }

    private void saveTasksToDisk() {
        try {
            taskRepository.saveTasks(taskManager.getAllTasks());
            System.out.println("Tasks saved to disk.");
        } catch (IOException e) {
            System.out.println("Could not save tasks: " + e.getMessage());
        }
    }

    private void printMenu() {
        System.out.println();
        System.out.println("==== To-Do Manager ====");
        System.out.println("1. Add task");
        System.out.println("2. List tasks");
        System.out.println("3. Complete task");
        System.out.println("4. Delete task");
        System.out.println("5. Exit");
        System.out.print("Choose an option: ");
    }

    private void addTask() {
        String title = readRequiredLine("Enter task title: ");
        System.out.print("Enter description (optional): ");
        String description = scanner.nextLine().trim();

        Task task = taskManager.addTask(title, description);
        System.out.println("Task added with id " + task.getId() + ".");
    }

    private void listTasks() {
        List<Task> tasks = taskManager.getAllTasks();

        if (tasks.isEmpty()) {
            System.out.println("No tasks found.");
            return;
        }

        System.out.println();
        System.out.println("Current tasks:");
        for (Task task : tasks) {
            System.out.println(taskManager.formatTaskForDisplay(task));
        }
    }

    private void completeTask() {
        int taskId = readTaskId("Enter the id of the task to complete: ");
        if (taskId == -1) {
            return;
        }

        boolean completed = taskManager.completeTask(taskId);
        if (completed) {
            System.out.println("Task marked as completed.");
        } else {
            System.out.println("Task not found or already completed.");
        }
    }

    private void deleteTask() {
        int taskId = readTaskId("Enter the id of the task to delete: ");
        if (taskId == -1) {
            return;
        }

        boolean deleted = taskManager.deleteTask(taskId);
        if (deleted) {
            System.out.println("Task deleted.");
        } else {
            System.out.println("Task not found.");
        }
    }

    private String readRequiredLine(String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = scanner.nextLine().trim();

            if (!value.isEmpty()) {
                return value;
            }

            System.out.println("This field cannot be empty.");
        }
    }

    private int readTaskId(String prompt) {
        System.out.print(prompt);
        String input = scanner.nextLine().trim();

        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            System.out.println("Please enter a valid whole number.");
            return -1;
        }
    }
}
