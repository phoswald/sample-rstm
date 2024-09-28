package com.github.phoswald.sample.sample;

import com.github.phoswald.rstm.config.ConfigProvider;
import com.github.phoswald.rstm.security.Principal;
import com.github.phoswald.rstm.template.Template;
import com.github.phoswald.rstm.template.TemplateEngine;

public class SampleController {

    private static final TemplateEngine templateEngine = new TemplateEngine();

    private final String sampleConfig;

    public SampleController(ConfigProvider config) {
        this.sampleConfig = config.getConfigProperty("app.sample.config").orElse("Undefined");
    }

    public String getSamplePage(Principal principal) {
        Template<SampleViewModel> template = templateEngine.compile(SampleViewModel.class, "sample");
        return template.evaluate(SampleViewModel.create(sampleConfig, principal));
    }
}
