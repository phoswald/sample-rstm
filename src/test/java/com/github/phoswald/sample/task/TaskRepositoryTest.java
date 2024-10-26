package com.github.phoswald.sample.task;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.github.phoswald.sample.ApplicationModule;

class TaskRepositoryTest {

    private static final ApplicationModule module = new ApplicationModule();

    @Test
    void testCrud() {
        try (TaskRepository testee = new TaskRepository(module.getConnection())) {
            assertEquals(0, testee.selectAllTasks().size());

            Task task = Task.builder()
                    .taskId(Task.newTaskId())
                    .title("Test Title")
                    .description("Test Description")
                    .build();
            testee.createTask(task);
        }

        try (TaskRepository testee = new TaskRepository(module.getConnection())) {
            List<Task> tasks = testee.selectAllTasks();

            assertEquals(1, tasks.size());
            assertEquals("Test Title", tasks.get(0).title());
            assertEquals("Test Description", tasks.get(0).description());
        }
    }
}
