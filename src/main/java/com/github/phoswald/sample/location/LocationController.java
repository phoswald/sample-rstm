package com.github.phoswald.sample.location;

import static com.github.phoswald.sample.location.Leaflet.point;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import com.github.phoswald.rstm.http.HttpRequest;
import com.github.phoswald.rstm.template.Template;
import com.github.phoswald.rstm.template.TemplateEngine;

public class LocationController {

    private static final TemplateEngine templateEngine = new TemplateEngine();

    private final Supplier<LocationRepository> repositoryFactory;

    public LocationController(Supplier<LocationRepository> repositoryFactory) {
        this.repositoryFactory = repositoryFactory;
    }

    public String getLocationsPage(HttpRequest request) {
        Template<LocationViewModel> template = templateEngine.compile(LocationViewModel.class, "locations");
        String userId = createLeaflet(request).map(Leaflet::userId).orElse("nobody");
        return template.evaluate(new LocationViewModel(userId));
    }

    public Leaflet getLocationsJson(HttpRequest request) {
        // TODO (refactor): remove JSON endpoint, integrate into view model and template
        return createLeaflet(request).orElse(null);
    }

    private Optional<Leaflet> createLeaflet(HttpRequest request) {
        try (LocationRepository repository = repositoryFactory.get()) {
            List<String> userIds = repository.selectUsersByViewer(request.principal().name());
            for(String userId : userIds) {
                List<Location> locations = repository.selectLocationsByUser(userId);
                if(!locations.isEmpty()) {
                    return Optional.of(createLeaflet(userId, locations));
                }
            }
        }
        return Optional.empty();
    }

    private Leaflet createLeaflet(String userId, List<Location> locations) {
        Location latest = locations.getFirst();
        return Leaflet.createWithPolyLine(
                locations.stream()
                        .map(location -> point(location.latitude(), location.longitude()))
                        .toList(),
                point(latest.latitude(), latest.longitude()),
                userId);
    }
}
