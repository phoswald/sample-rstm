package com.github.phoswald.sample.location;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class LocationRepository implements AutoCloseable {

    private final Connection conn;

    public LocationRepository(Connection conn) {
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

    public List<Location> selectLocationsByUser(String userId) {
        try {
            PreparedStatement stmt = conn.prepareStatement("""
                    SELECT user_id_, timestamp_, latitude_, longitude_
                    FROM location_
                    WHERE user_id_ = ?
                    ORDER BY timestamp_ DESC
                    """);
            stmt.setString(1, userId);
            stmt.setMaxRows(1000);
            ResultSet resultSet = stmt.executeQuery();
            List<Location> locations = new ArrayList<>();
            while (resultSet.next()) {
                Location location = Location.builder()
                        .userId(resultSet.getString("user_id_"))
                        .timestamp(convertTimestamp(resultSet.getTimestamp("timestamp_")))
                        .latitude(resultSet.getDouble("latitude_"))
                        .longitude(resultSet.getDouble("longitude_"))
                        .build();
                locations.add(location);
            }
            resultSet.close();
            return locations;
        } catch (SQLException e) {
            throw new SqlException(e);
        }
    }

    public void createLocation(Location location) {
        try {
            PreparedStatement stmt = conn.prepareStatement("""
                    INSERT INTO location_ (user_id_, timestamp_, latitude_, longitude_)
                    VALUES (?, ?, ?, ?)
                    """);
            stmt.setString(1, location.userId());
            stmt.setTimestamp(2, convertTimestamp(location.timestamp()));
            stmt.setDouble(3, location.latitude());
            stmt.setDouble(4, location.longitude());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new SqlException(e);
        }
    }

    public List<String> selectUsersByViewer(String viewerUserId) {
        try {
            PreparedStatement stmt = conn.prepareStatement("""
                    SELECT user_id_
                    FROM location_perm_
                    WHERE user_id_view_ = ?
                    ORDER BY user_id_
                    """);
            stmt.setString(1, viewerUserId);
            ResultSet resultSet = stmt.executeQuery();
            List<String> userIds = new ArrayList<>();
            while (resultSet.next()) {
                userIds.add(resultSet.getString("user_id_"));
            }
            userIds.add(viewerUserId);
            resultSet.close();
            return userIds;
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
