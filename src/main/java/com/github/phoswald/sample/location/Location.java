package com.github.phoswald.sample.location;

import java.time.Instant;

import com.github.phoswald.record.builder.RecordBuilder;

@RecordBuilder
public record Location(String userId, Double latitude, Double longitude, Instant timestamp) {

    public static LocationBuilder builder() {
        return new LocationBuilder();
    }

    public LocationBuilder toBuilder() {
        return new LocationBuilder(this);
    }
}
