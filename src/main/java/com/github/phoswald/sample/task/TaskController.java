package com.github.phoswald.sample.task;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.phoswald.rstm.http.HttpRequest;
import com.github.phoswald.rstm.template.Template;
import com.github.phoswald.rstm.template.TemplateEngine;

public class TaskController {

    private static final TemplateEngine templateEngine = new TemplateEngine();

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Supplier<TaskRepository> repositoryFactory;

    public TaskController(Supplier<TaskRepository> repositoryFactory) {
        this.repositoryFactory = repositoryFactory;
    }

    public String getTasksPage(HttpRequest request) {
        try (TaskRepository repository = repositoryFactory.get()) {
            List<Task> tasks = repository.selectTasksByUser(request.principal().name());
            Template<TaskListViewModel> template = templateEngine.compile(TaskListViewModel.class, "task-list");
            return template.evaluate(TaskViewModel.ofTasks(tasks));
        }
    }

    public String postTasksPage(HttpRequest request, PostParams params) {
        logger.info("Received from with title=" + params.title() + ", description=" + params.description());
        try (TaskRepository repository = repositoryFactory.get()) {
            Task task = Task.builder()
                    .taskId(Task.newTaskId())
                    .userId(request.principal().name())
                    .timestamp(Instant.now())
                    .title(params.title())
                    .description(params.description())
                    .done(false)
                    .build();
            repository.createTask(task);
        }
        return getTasksPage(request);
    }

    public String getTaskPage(HttpRequest request, IdParams params) {
        try (TaskRepository repository = repositoryFactory.get()) {
            Task task = repository.selectTaskById(request.principal().name(), params.id());
            if (task == null) {
                return "redirect=/app/pages/tasks";
            }
            if (Objects.equals(params.action(), "edit")) {
                Template<TaskViewModel> template = templateEngine.compile(TaskViewModel.class, "task-edit");
                return template.evaluate(TaskViewModel.ofTask(task));
            } else {
                Template<TaskViewModel> template = templateEngine.compile(TaskViewModel.class, "task");
                return template.evaluate(TaskViewModel.ofTask(task));
            }
        }
    }

    public String postTaskPage(HttpRequest request, IdPostParams params) {
        logger.info("Received from with id=" + params.id() + ", action=" + params.action() + ", title=" + params.title() + ", description=" + params.description() + ", done=" + params.done());
        try (TaskRepository repository = repositoryFactory.get()) {
            Task task = repository.selectTaskById(request.principal().name(), params.id());
            if (task == null) {
                return "redirect=/app/pages/tasks";
            }
            if (Objects.equals(params.action(), "delete")) {
                repository.deleteTask(task);
                return "redirect=/app/pages/tasks";
            }
            if (Objects.equals(params.action(), "store")) {
                task = task.toBuilder()
                        .timestamp(Instant.now())
                        .title(params.title())
                        .description(params.description())
                        .done(Objects.equals(params.done(), "on"))
                        .build();
                repository.updateTask(task);
            }
        }
        return getTaskPage(request, new IdParams(params.action(), params.id()));
    }

    public record PostParams(String title, String description) { }

    public record IdParams(String action, String id) { }

    public record IdPostParams(String action, String id, String title, String description, String done) { }
}
