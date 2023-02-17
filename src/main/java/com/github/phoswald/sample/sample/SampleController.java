package com.github.phoswald.sample.sample;

import java.util.function.Function;

import com.github.phoswald.rstm.config.ConfigProvider;
import com.github.phoswald.rstm.template.TemplateEngine;

public class SampleController {

    private final String sampleConfig;

    public SampleController(ConfigProvider config) {
        this.sampleConfig = config.getConfigProperty("app.sample.config").orElse("Undefined");
    }

    public String getSamplePage() {
        TemplateEngine templateEngine = new TemplateEngine();
        Function<SampleViewModel, String> template = templateEngine.compile(SampleViewModel.class, "sample");
        return template.apply(SampleViewModel.create(sampleConfig));
    }
}
