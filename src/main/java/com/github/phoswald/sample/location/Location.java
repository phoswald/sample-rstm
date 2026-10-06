package com.github.phoswald.sample.location;

import java.time.Instant;

import com.github.phoswald.record.builder.RecordBuilder;

@RecordBuilder
public record Location(Double latitude, Double longitude, Instant timestamp) {

    public static LocationBuilder builder() {
        return new LocationBuilder();
    }
}
