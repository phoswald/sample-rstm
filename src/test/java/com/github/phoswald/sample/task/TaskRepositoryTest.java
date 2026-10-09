package com.github.phoswald.sample.task;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.github.phoswald.sample.ApplicationModule;

class TaskRepositoryTest {

    private static final ApplicationModule module = new ApplicationModule();

    @Test
    void testCrud() {
        Instant older = Instant.ofEpochMilli(1_700_000_000_000L);
        Instant newer = Instant.ofEpochMilli(1_700_000_060_000L);

        try (TaskRepository testee = new TaskRepository(module.getConnection())) {
            assertEquals(0, testee.selectTasksByUser("guest").size());

            testee.createTask(Task.builder()
                    .taskId(Task.newTaskId())
                    .userId("guest")
                    .timestamp(older)
                    .title("Test Title 1")
                    .description("Test Description 1")
                    .build());

            testee.createTask(Task.builder()
                    .taskId(Task.newTaskId())
                    .userId("guest")
                    .timestamp(newer)
                    .title("Test Title 2")
                    .description("Test Description 2")
                    .build());
        }

        try (TaskRepository testee = new TaskRepository(module.getConnection())) {
            List<Task> tasks = testee.selectTasksByUser("guest");
            assertEquals(2, tasks.size());
            assertEquals("guest", tasks.get(0).userId());
            assertEquals(newer, tasks.get(0).timestamp());
            assertEquals("Test Title 2", tasks.get(0).title());
            assertEquals("Test Description 2", tasks.get(0).description());
            assertEquals(older, tasks.get(1).timestamp());
            assertEquals("Test Title 1", tasks.get(1).title());

            assertEquals(0, testee.selectTasksByUser("other").size());
        }
    }
}
