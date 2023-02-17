package com.github.phoswald.sample.sample;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Map;

public record SampleViewModel( //
        String greeting, //
        String now, //
        String sampleConfig, //
        Pair[] env, //
        Pair[] props //
) {

    public static SampleViewModel create(String sampleConfig) {
        return new SampleViewModel( //
                "Hello, World!", // TODO: support resource bundles in rstm-template
                ZonedDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME), //
                sampleConfig, //
                toPairs(System.getenv()), //
                toPairs(System.getProperties()));
    }

    private static Pair[] toPairs(Map<?, ?> map) {
        return map.entrySet().stream() //
                .map(e -> new Pair(e.getKey().toString(), e.getValue().toString())) //
                .sorted(Comparator.comparing(Pair::key)) //
                .toArray(Pair[]::new);
    }

    public record Pair(String key, String value) { } // TODO: support Map<?,?> in rstm-template
}
