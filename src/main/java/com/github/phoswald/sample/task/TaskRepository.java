package com.github.phoswald.sample.task;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class TaskRepository implements AutoCloseable {

    private final Connection conn;

    public TaskRepository(Connection conn) {
        this.conn = conn;
    }

    @Override
    public void close() {
        try {
            conn.close();
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    public List<Task> selectTasksByUser(String userId) {
        try {
            PreparedStatement stmt = conn.prepareStatement("""
                    SELECT task_id_, user_id_, timestamp_, title_, description_, done_
                    FROM task_
                    WHERE user_id_ = ?
                    ORDER BY timestamp_ DESC
                    """);
            stmt.setString(1, userId);
            stmt.setMaxRows(1000);
            ResultSet resultSet = stmt.executeQuery();
            List<Task> tasks = new ArrayList<>();
            while (resultSet.next()) {
                Task task = Task.builder()
                        .taskId(resultSet.getString("task_id_"))
                        .userId(resultSet.getString("user_id_"))
                        .timestamp(convertTimestamp(resultSet.getTimestamp("timestamp_")))
                        .title(resultSet.getString("title_"))
                        .description(resultSet.getString("description_"))
                        .done(resultSet.getBoolean("done_"))
                        .build();
                tasks.add(task);
            }
            resultSet.close();
            return tasks;
        } catch (SQLException e) {
            throw new SqlException(e);
        }
    }

    public Task selectTaskById(String userId, String taskId) {
        try {
            PreparedStatement stmt = conn.prepareStatement("""
                    SELECT task_id_, user_id_, timestamp_, title_, description_, done_
                    FROM task_
                    WHERE user_id_ = ? AND task_id_ = ?
                    """);
            stmt.setString(1, userId);
            stmt.setString(2, taskId);
            ResultSet resultSet = stmt.executeQuery();
            if (resultSet.next()) {
                Task task = Task.builder()
                        .taskId(resultSet.getString("task_id_"))
                        .userId(resultSet.getString("user_id_"))
                        .timestamp(convertTimestamp(resultSet.getTimestamp("timestamp_")))
                        .title(resultSet.getString("title_"))
                        .description(resultSet.getString("description_"))
                        .done(resultSet.getBoolean("done_"))
                        .build();
                resultSet.close();
                return task;
            } else {
                resultSet.close();
                return null;
            }
        } catch (SQLException e) {
            throw new SqlException(e);
        }
    }

    public void createTask(Task task) {
        try {
            PreparedStatement stmt = conn.prepareStatement("""
                    INSERT INTO task_ (task_id_, user_id_, timestamp_, title_, description_, done_)
                    VALUES (?, ?, ?, ?, ?, ?)
                    """);
            stmt.setString(1, task.taskId());
            stmt.setString(2, task.userId());
            stmt.setTimestamp(3, convertTimestamp(task.timestamp()));
            stmt.setString(4, task.title());
            stmt.setString(5, task.description());
            stmt.setBoolean(6, task.done());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new SqlException(e);
        }
    }

    public void deleteTask(Task task) {
        try {
            PreparedStatement stmt = conn.prepareStatement("""
                    DELETE FROM task_
                    WHERE task_id_ = ? AND user_id_ = ?
                    """);
            stmt.setString(1, task.taskId());
            stmt.setString(2, task.userId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new SqlException(e);
        }
    }

    public void updateTask(Task task) {
        try {
            PreparedStatement stmt = conn.prepareStatement("""
                    UPDATE task_
                    SET timestamp_ = ?, title_ = ?, description_ = ?, done_ = ?
                    WHERE task_id_ = ? AND user_id_ = ?
                    """);
            stmt.setString(5, task.taskId());
            stmt.setString(6, task.userId());
            stmt.setTimestamp(1, convertTimestamp(task.timestamp()));
            stmt.setString(2, task.title());
            stmt.setString(3, task.description());
            stmt.setBoolean(4, task.done());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new SqlException(e);
        }
    }

    private Timestamp convertTimestamp(Instant t) {
        return t == null ? null : new Timestamp(t.toEpochMilli());
    }

    private Instant convertTimestamp(Timestamp t) {
        return t == null ? null : Instant.ofEpochMilli(t.getTime());
    }
}
