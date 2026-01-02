package com.github.phoswald.sample.sample;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import com.github.phoswald.rstm.security.Principal;

public record SampleViewModel(
        String now,
        String sampleConfig,
        String username,
        Map<String, ?> env,
        Map<Object, ?> props
) {

    public static SampleViewModel create(String sampleConfig, Principal principal) {
        return new SampleViewModel(
                ZonedDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME),
                sampleConfig,
                principal.name(),
                sortAndMaskSecrets(System.getenv()),
                sortAndMaskSecrets(System.getProperties()));
    }

    private static <K, V> Map<K, ?> sortAndMaskSecrets(Map<K, V> map) {
        return new TreeMap<K, Object>(map.entrySet().stream()
                .map(e -> isSecret(e) ? Map.entry(e.getKey(), "???") : e)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
    }

    private static boolean isSecret(Map.Entry<?, ?> e) {
        String key = e.getKey().toString().toLowerCase();
        return key.contains("password") || key.contains("secret");
    }
}
