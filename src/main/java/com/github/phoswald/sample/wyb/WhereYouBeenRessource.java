package com.github.phoswald.sample.wyb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.phoswald.rstm.http.HttpRequest;

public class WhereYouBeenRessource {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public String postPosition(HttpRequest request, Location location) {
        logger.info("Received lat/lon={}/{} at time={} for user={}",
                location.latitude(), location.longitude(), location.timestamp(),
                request.principal().name());
        return "";
    }
}
