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

    public List<TaskEntity> selectAllTasks() {
        try {
            PreparedStatement stmt = conn.prepareStatement("""
                    SELECT task_id_, user_id_, timestamp_, title_, description_, done_
                    FROM task_
                    ORDER BY timestamp_ DESC
                    """);
            stmt.setMaxRows(1000);
            ResultSet resultSet = stmt.executeQuery();
            List<TaskEntity> resultList = new ArrayList<>();
            while (resultSet.next()) {
                TaskEntity resultEntity = new TaskEntity();
                resultEntity.setTaskId(resultSet.getString("task_id_"));
                resultEntity.setUserId(resultSet.getString("user_id_"));
                resultEntity.setTimestamp(convertTimestamp(resultSet.getTimestamp("timestamp_")));
                resultEntity.setTitle(resultSet.getString("title_"));
                resultEntity.setDescription(resultSet.getString("description_"));
                resultEntity.setDone(resultSet.getBoolean("done_"));
                resultList.add(resultEntity);
            }
            resultSet.close();
            return resultList;
        } catch (SQLException e) {
            throw new SqlException(e);
        }
    }

    public TaskEntity selectTaskById(String taskId) {
        try {
            PreparedStatement stmt = conn.prepareStatement("""
                    SELECT task_id_, user_id_, timestamp_, title_, description_, done_
                    FROM task_
                    WHERE task_id_ = ?
                    """);
            stmt.setString(1, taskId);
            ResultSet resultSet = stmt.executeQuery();
            if (resultSet.next()) {
                TaskEntity resultEntity = new TaskEntity();
                resultEntity.setTaskId(resultSet.getString("task_id_"));
                resultEntity.setUserId(resultSet.getString("user_id_"));
                resultEntity.setTimestamp(convertTimestamp(resultSet.getTimestamp("timestamp_")));
                resultEntity.setTitle(resultSet.getString("title_"));
                resultEntity.setDescription(resultSet.getString("description_"));
                resultEntity.setDone(resultSet.getBoolean("done_"));
                resultSet.close();
                return resultEntity;
            } else {
                resultSet.close();
                return null;
            }
        } catch (SQLException e) {
            throw new SqlException(e);
        }
    }

    public void createTask(TaskEntity entity) {
        try {
            PreparedStatement stmt = conn.prepareStatement("""
                    INSERT INTO task_ (task_id_, user_id_, timestamp_, title_, description_, done_)
                    VALUES (?, ?, ?, ?, ?, ?)
                    """);
            stmt.setString(1, entity.getTaskId());
            stmt.setString(2, entity.getUserId());
            stmt.setTimestamp(3, convertTimestamp(entity.getTimestamp()));
            stmt.setString(4, entity.getTitle());
            stmt.setString(5, entity.getDescription());
            stmt.setBoolean(6, entity.isDone());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new SqlException(e);
        }
    }

    public void deleteTask(TaskEntity entity) {
        try {
            PreparedStatement stmt = conn.prepareStatement("""
                    DELETE FROM task_
                    WHERE task_id_ = ?
                    """);
            stmt.setString(1, entity.getTaskId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new SqlException(e);
        }
    }

    public void updateTask(TaskEntity entity) {
        try {
            PreparedStatement stmt = conn.prepareStatement("""
                    UPDATE task_
                    SET user_id_ = ?, timestamp_ = ?, title_ = ?, description_ = ?, done_ = ?
                    WHERE task_id_ = ?
                    """);
            stmt.setString(6, entity.getTaskId());
            stmt.setString(1, entity.getUserId());
            stmt.setTimestamp(2, convertTimestamp(entity.getTimestamp()));
            stmt.setString(3, entity.getTitle());
            stmt.setString(4, entity.getDescription());
            stmt.setBoolean(5, entity.isDone());
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
