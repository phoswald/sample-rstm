package com.github.phoswald.sample.task;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class TaskViewModel {

    private final String taskId;
    private final String timestamp;
    private final String title;
    private final String description;
    private final boolean done;

    private TaskViewModel(Task task) {
        this.taskId = task.taskId();
        this.timestamp = format(task.timestamp());
        this.title = task.title();
        this.description = task.description();
        this.done = task.done();
    }

    public static TaskViewModel ofTask(Task task) {
        return new TaskViewModel(task);
    }

    public static TaskListViewModel ofTasks(List<Task> tasks) {
        return new TaskListViewModel(tasks.stream().map(TaskViewModel::ofTask).toList());
    }

    private String format(Instant instant) {
        LocalDateTime dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        return dateTime.format(DateTimeFormatter.ISO_DATE) + " " + dateTime.format(DateTimeFormatter.ISO_TIME);
    }

    public String getTaskId() {
        return taskId;
    }

    public String getTaskIdAsHref() {
        return "tasks/" + taskId; // TODO (templating): support string concatentation?
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getDoneAsChecked() {
        return done ? "checked" : null; // TODO (templating): support flags
    }
}
