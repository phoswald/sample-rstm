package com.github.phoswald.sample.location;

import com.github.phoswald.rstm.http.HttpRequest;
import com.github.phoswald.rstm.template.Template;
import com.github.phoswald.rstm.template.TemplateEngine;

public class LocationController {

    private static final TemplateEngine templateEngine = new TemplateEngine();

    public String getLocationsPage(HttpRequest request) {
        Template<LocationViewModel> template = templateEngine.compile(LocationViewModel.class, "locations");
        return template.evaluate(new LocationViewModel());
    }
}
