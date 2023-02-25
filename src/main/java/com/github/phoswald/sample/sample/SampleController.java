package com.github.phoswald.sample.sample;

import com.github.phoswald.rstm.config.ConfigProvider;
import com.github.phoswald.rstm.template.TemplateEngine;
import com.github.phoswald.rstm.template.Template;

public class SampleController {

    private final String sampleConfig;

    public SampleController(ConfigProvider config) {
        this.sampleConfig = config.getConfigProperty("app.sample.config").orElse("Undefined");
    }

    public String getSamplePage() {
        TemplateEngine templateEngine = new TemplateEngine();
        Template<SampleViewModel> template = templateEngine.compile(SampleViewModel.class, "sample");
        return template.evaluate(SampleViewModel.create(sampleConfig));
    }
}
