package com.github.phoswald.sample.location;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.github.phoswald.sample.ApplicationModule;

class LocationRepositoryTest {

    private static final ApplicationModule module = new ApplicationModule();

    @Test
    void testCrud() {
        Instant older = Instant.ofEpochMilli(1_700_000_000_000L);
        Instant newer = Instant.ofEpochMilli(1_700_000_060_000L);

        try (LocationRepository testee = new LocationRepository(module.getConnection())) {
            assertEquals(0, testee.selectLocationsByUser("guest", 1).size());

            testee.createLocation(Location.builder()
                    .userId("guest")
                    .timestamp(older)
                    .latitude(1.0)
                    .longitude(2.0)
                    .build());

            testee.createLocation(Location.builder()
                    .userId("guest")
                    .timestamp(newer)
                    .latitude(3.0)
                    .longitude(4.0)
                    .build());
        }

        try (LocationRepository testee = new LocationRepository(module.getConnection())) {
            List<Location> locations = testee.selectLocationsByUser("guest", 1);
            assertEquals(2, locations.size());
            assertEquals("guest", locations.get(0).userId());
            assertEquals(newer, locations.get(0).timestamp());
            assertEquals(3.0, locations.get(0).latitude());
            assertEquals(4.0, locations.get(0).longitude());
            assertEquals(older, locations.get(1).timestamp());
            assertEquals(1.0, locations.get(1).latitude());

            assertEquals(0, testee.selectLocationsByUser("other", 1).size());
        }
    }
}
