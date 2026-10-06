package com.github.phoswald.sample.wyb;

import java.time.Instant;

import com.github.phoswald.record.builder.RecordBuilder;

@RecordBuilder
public record Location(Double latitude, Double longitude, Instant timestamp) {

    public static LocationBuilder builder() {
        return new LocationBuilder();
    }
}
