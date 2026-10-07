package com.github.phoswald.sample.location;

import java.time.Instant;
import java.util.List;
import java.util.function.Supplier;

import com.github.phoswald.rstm.http.HttpRequest;

public class LocationResource {

    private final Supplier<LocationRepository> repositoryFactory;

    public LocationResource(Supplier<LocationRepository> repositoryFactory) {
        this.repositoryFactory = repositoryFactory;
    }

    public LocationList getLocations(HttpRequest request) {
        try (LocationRepository repository = repositoryFactory.get()) {
            List<Location> locations = repository.selectLocationsByUser(request.principal().name());
            return new LocationList(locations);
        }
    }

    public Location postLocation(HttpRequest request, Location location) {
        try (LocationRepository repository = repositoryFactory.get()) {
            Location entity = location.toBuilder()
                    .userId(request.principal().name())
                    // TODO: make timestamp mandatory, requires fixing Instant serialization in REST assured (see ApplicationTest)
                    .timestamp(location.timestamp() != null ? location.timestamp() : Instant.now())
                    .build();
            repository.createLocation(entity);
            return entity;
        }
    }
}
