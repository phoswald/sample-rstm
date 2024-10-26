package com.github.phoswald.sample.sample;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import com.github.phoswald.rstm.config.ConfigProvider;
import com.github.phoswald.rstm.security.Principal;

public class SampleResource {

    private final String sampleConfig;

    public SampleResource(ConfigProvider config) {
        this.sampleConfig = config.getConfigProperty("app.sample.config").orElse("Undefined");
    }

    public String getTime() {
        return ZonedDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME) + "\n";
    }

    public String getConfig() {
        return sampleConfig + "\n";
    }

    public String getMe(Principal principal) {
        return principal.name() + "\n" + principal.roles() + "\n" + principal.provider() + "\n" + principal.token() + "\n";
    }

    public EchoResponse postEcho(EchoRequest request) {
        return new EchoResponse("Received " + request.input());
    }
}
