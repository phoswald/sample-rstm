package com.github.phoswald.sample.task;

import java.time.Instant;
import java.util.List;
import java.util.function.Supplier;

import com.github.phoswald.rstm.http.HttpRequest;

public class TaskResource {

    private final Supplier<TaskRepository> repositoryFactory;

    public TaskResource(Supplier<TaskRepository> repositoryFactory) {
        this.repositoryFactory = repositoryFactory;
    }

    public TaskList getTasks(HttpRequest request) {
        try (TaskRepository repository = repositoryFactory.get()) {
            List<Task> tasks = repository.selectTasksByUser(request.principal().name());
            return new TaskList(tasks);
        }
    }

    public Task postTasks(HttpRequest request, Task requestBody) {
        try (TaskRepository repository = repositoryFactory.get()) {
            Task task = requestBody.toBuilder()
                    .taskId(Task.newTaskId())
                    .userId(request.principal().name())
                    .timestamp(requestBody.timestamp() != null ? requestBody.timestamp() : Instant.now())
                    .build();
            repository.createTask(task);
            return task;
        }
    }

    public Task getTask(HttpRequest request, IdParams params) {
        try (TaskRepository repository = repositoryFactory.get()) {
            Task task = repository.selectTaskById(request.principal().name(), params.id());
            return task;
        }
    }

    public Task putTask(HttpRequest request, IdParams params, Task requestBody) {
        try (TaskRepository repository = repositoryFactory.get()) {
            Task task = repository.selectTaskById(request.principal().name(), params.id());
            if (task == null) {
                return null;
            }
            task = task.toBuilder()
                    .timestamp(requestBody.timestamp() != null ? requestBody.timestamp() : Instant.now())
                    .title(requestBody.title())
                    .description(requestBody.description())
                    .done(requestBody.done())
                    .build();
            repository.updateTask(task);
            return task;
        }
    }

    public String deleteTask(HttpRequest request, IdParams params) {
        try (TaskRepository repository = repositoryFactory.get()) {
            Task task = repository.selectTaskById(request.principal().name(), params.id());
            if (task == null) {
                return null;
            }
            repository.deleteTask(task);
            return "";
        }
    }

    public record IdParams(String id) { }
}
