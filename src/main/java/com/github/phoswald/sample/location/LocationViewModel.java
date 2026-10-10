package com.github.phoswald.sample.location;

import com.github.phoswald.record.builder.RecordBuilder;

@RecordBuilder
public record LocationViewModel(
        String userId,
        String timeRange,
        String mapDataJson
) { }
