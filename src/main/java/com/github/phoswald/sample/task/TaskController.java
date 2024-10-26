package com.github.phoswald.sample.task;

import java.nio.file.Paths;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.phoswald.rstm.template.Template;
import com.github.phoswald.rstm.template.TemplateEngine;

public class TaskController {

    private static final TemplateEngine templateEngine = new TemplateEngine();

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Supplier<TaskRepository> repositoryFactory;

    public TaskController(Supplier<TaskRepository> repositoryFactory) {
        this.repositoryFactory = repositoryFactory;
    }

    public String getTasksPage() {
        try (TaskRepository repository = repositoryFactory.get()) {
            List<Task> tasks = repository.selectAllTasks();
            Template<TaskListViewModel> template = templateEngine.compile(TaskListViewModel.class, "task-list");
            return template.evaluate(TaskViewModel.ofTasks(tasks));
        }
    }

    public String postTasksPage( //
            String title, //
            String description) {
        logger.info("Received from with title=" + title + ", description=" + description);
        try (TaskRepository repository = repositoryFactory.get()) {
            Task task = Task.builder()
                    .taskId(Task.newTaskId())
                    .userId("guest")
                    .timestamp(Instant.now())
                    .title(title)
                    .description(description)
                    .done(false)
                    .build();
            repository.createTask(task);
        }
        return getTasksPage();
    }

    public String getTaskPage( //
            String id, //
            String action) {
        try (TaskRepository repository = repositoryFactory.get()) {
            Task task = repository.selectTaskById(id);
            if (Objects.equals(action, "edit")) {
                Template<TaskViewModel> template = templateEngine.compile(TaskViewModel.class, "task-edit");
                return template.evaluate(TaskViewModel.ofTask(task));
            } else {
                Template<TaskViewModel> template = templateEngine.compile(TaskViewModel.class, "task");
                return template.evaluate(TaskViewModel.ofTask(task));
            }
        }
    }

    public Object postTaskPage( //
            String id, //
            String action, //
            String title, //
            String description, //
            String done) {
        logger.info("Received from with id=" + id + ", action=" + action + ", title=" + title + ", description=" + description + ", done=" + done);
        try (TaskRepository repository = repositoryFactory.get()) {
            Task task = repository.selectTaskById(id);
            if (Objects.equals(action, "delete")) {
                repository.deleteTask(task);
                return Paths.get("/app/pages/tasks");
            }
            if (Objects.equals(action, "store")) {
                task = task.toBuilder()
                        .timestamp(Instant.now())
                        .title(title)
                        .description(description)
                        .done(Objects.equals(done, "on"))
                        .build();
                repository.updateTask(task);
            }
        }
        return getTaskPage(id, null);
    }
}
