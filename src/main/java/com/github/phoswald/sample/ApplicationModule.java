package com.github.phoswald.sample;

import com.github.phoswald.rstm.config.ConfigProvider;
import com.github.phoswald.sample.sample.SampleResource;

public class ApplicationModule {

    public Application getApplication() {
        return new Application(getConfigProvider(), //
                getSampleResource());
    }

    public ConfigProvider getConfigProvider() {
        return new ConfigProvider();
    }

    public SampleResource getSampleResource() {
        return new SampleResource(getConfigProvider());
    }
}
