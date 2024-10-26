package com.github.phoswald.sample.task;

import java.time.Instant;
import java.util.UUID;

import com.github.phoswald.record.builder.RecordBuilder;

@RecordBuilder
public record Task(
        String taskId,
        String userId,
        Instant timestamp,
        String title,
        String description,
        boolean done
) {

    public static TaskBuilder builder() {
        return new TaskBuilder();
    }

    public TaskBuilder toBuilder() {
        return new TaskBuilder(this);
    }

    public static String newTaskId() {
        return UUID.randomUUID().toString();
    }
}
