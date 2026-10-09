package com.github.phoswald.sample.location;

import java.util.List;

import com.github.phoswald.record.builder.RecordBuilder;

@RecordBuilder
public record Leaflet(
        String titleLayerUrlTemplate,
        LeafletTitleLayerOptions titleLayerOptions,
        List<Point> polyLineCoords,
        LeafletPolyLineOptions polyLineOptions,
        Point markerCoords
) {
    public static Leaflet createWithPolyLine(List<Point> points, Point marker) {
        return new LeafletBuilder()
                .titleLayerUrlTemplate("https://tile.openstreetmap.org/{z}/{x}/{y}.png")
                .titleLayerOptions(new LeafletTitleLayerOptionsBuilder()
                        .maxZoom(19)
                        .attribution("&copy; <a href=\"https://www.openstreetmap.org/copyright\">OpenStreetMap</a> contributors")
                        .build())
                .polyLineCoords(points)
                .polyLineOptions(new LeafletPolyLineOptionsBuilder()
                        .color("#6d4aaa")
                        .weight(3)
                        .build())
                .markerCoords(marker)
                .build();
    }

    public static Point point(double lat, double lng) {
        return new Point(lat, lng);
    }

    public record Point(double lat, double lng) { }

    @RecordBuilder
    public record LeafletTitleLayerOptions(Integer maxZoom, String attribution) { }

    @RecordBuilder
    public record LeafletPolyLineOptions(String color, Integer weight) { }
}
