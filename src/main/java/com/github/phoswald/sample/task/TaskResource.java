package com.github.phoswald.sample.task;

import java.time.Instant;
import java.util.List;
import java.util.function.Supplier;

public class TaskResource {

    private final Supplier<TaskRepository> repositoryFactory;

    public TaskResource(Supplier<TaskRepository> repositoryFactory) {
        this.repositoryFactory = repositoryFactory;
    }

    public TaskList getTasks() {
        try (TaskRepository repository = repositoryFactory.get()) {
            List<Task> tasks = repository.selectAllTasks();
            return new TaskList(tasks);
        }
    }

    public Task postTasks(Task request) {
        try (TaskRepository repository = repositoryFactory.get()) {
            Task task = Task.builder()
                    .taskId(Task.newTaskId())
                    .userId("guest")
                    .timestamp(Instant.now())
                    .title(request.title())
                    .description(request.description())
                    .done(request.done())
                    .build();
            repository.createTask(task);
            return task;
        }
    }

    public Task getTask(IdParams params) {
        try (TaskRepository repository = repositoryFactory.get()) {
            Task task = repository.selectTaskById(params.id());
            return task;
        }
    }

    public Task putTask(IdParams params, Task request) {
        try (TaskRepository repository = repositoryFactory.get()) {
            Task task = repository.selectTaskById(params.id());
            task = task.toBuilder()
                    .timestamp(Instant.now())
                    .title(request.title())
                    .description(request.description())
                    .done(request.done())
                    .build();
            repository.updateTask(task);
            return task;
        }
    }

    public String deleteTask(IdParams params) {
        try (TaskRepository repository = repositoryFactory.get()) {
            Task task = repository.selectTaskById(params.id());
            repository.deleteTask(task);
            return "";
        }
    }

    public record IdParams(String id) { }
}
