package com.github.phoswald.sample.sample;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.TreeMap;

public record SampleViewModel( //
        String now, //
        String sampleConfig, //
        Map<String,String> env, //
        Map<Object,Object> props //
) {

    public static SampleViewModel create(String sampleConfig) {
        return new SampleViewModel( //
                ZonedDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME), //
                sampleConfig, //
                toPairs(System.getenv()), //
                toPairs(System.getProperties()));
    }

    private static <K,V> Map<K, V> toPairs(Map<K, V> map) {
        return new TreeMap<>(map);
    }
}
