package com.github.phoswald.sample.location;

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
            List<Location> locations = repository.selectLocationsByUser(request.principal().name(), 24);
            return new LocationList(locations);
        }
    }

    public Location postLocation(HttpRequest request, Location requestBody) {
        try (LocationRepository repository = repositoryFactory.get()) {
            Location location = requestBody.toBuilder()
                    .userId(request.principal().name())
                    .build();
            repository.createLocation(location);
            return location;
        }
    }
}
