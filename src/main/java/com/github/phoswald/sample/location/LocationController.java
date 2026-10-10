package com.github.phoswald.sample.location;

import static com.github.phoswald.sample.location.Leaflet.point;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Supplier;

import com.github.phoswald.rstm.databind.Databinder;
import com.github.phoswald.rstm.http.HttpRequest;
import com.github.phoswald.rstm.template.Template;
import com.github.phoswald.rstm.template.TemplateEngine;

public class LocationController {

    private static final TemplateEngine templateEngine = new TemplateEngine();
    private static final Databinder databinder = new Databinder().pretty(false);

    private final Supplier<LocationRepository> repositoryFactory;

    public LocationController(Supplier<LocationRepository> repositoryFactory) {
        this.repositoryFactory = repositoryFactory;
    }

    public String getLocationsPage(HttpRequest request) {
        Template<LocationViewModel> template = templateEngine.compile(LocationViewModel.class, "locations");
        List<Location> locations = selectLocations(request);
        Leaflet mapData = createLeaflet(locations);
        return template.evaluate(new LocationViewModelBuilder()
                .userId(locations == null ? "nobody" : locations.getFirst().userId())
                .timeRange(locations == null ? "never" : formatTimeRange(locations.getLast().timestamp(), locations.getFirst().timestamp()))
                .mapDataJson(databinder.toJson(mapData))
                .build());
    }

    private List<Location> selectLocations(HttpRequest request) {
        try (LocationRepository repository = repositoryFactory.get()) {
            List<String> userIds = repository.selectUsersByViewer(request.principal().name());
            for(String userId : userIds) {
                List<Location> locations = repository.selectLocationsByUser(userId, 24);
                if(!locations.isEmpty()) {
                    return locations;
                }
            }
        }
        return null;
    }

    private Leaflet createLeaflet(List<Location> locations) {
        var points = locations == null ? null : locations.stream()
                .map(location -> point(location.latitude(), location.longitude()))
                .toList();
        var marker = locations == null ? null :
                point(locations.getFirst().latitude(), locations.getFirst().longitude());
        return Leaflet.createWithPolyLine(points, marker);
    }

    private String formatTimeRange(Instant beg, Instant end) {
        var formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        var zone = ZoneId.systemDefault();
        return formatter.format(beg.atZone(zone)) + " - " + formatter.format(end.atZone(zone));
    }
}
