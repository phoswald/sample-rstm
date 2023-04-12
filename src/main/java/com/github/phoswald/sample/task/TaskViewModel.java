package com.github.phoswald.sample.task;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class TaskViewModel {

    private final String taskId;
    private final String timestamp;
    private final String title;
    private final String description;
    private final boolean done;

    private TaskViewModel(TaskEntity entity) {
        this.taskId = entity.getTaskId();
        this.timestamp = format(entity.getTimestamp());
        this.title = entity.getTitle();
        this.description = entity.getDescription();
        this.done = entity.isDone();
    }

    public static TaskViewModel ofEntity(TaskEntity entity) {
        return new TaskViewModel(entity);
    }

    public static TaskListViewModel ofEntityList(List<TaskEntity> entities) {
        return new TaskListViewModel(entities.stream().map(TaskViewModel::ofEntity).collect(Collectors.toList()));
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
