package com.github.phoswald.sample;

import java.util.List;
import java.util.Optional;

import com.github.phoswald.rstm.config.ConfigProvider;
import com.github.phoswald.rstm.security.IdentityProvider;
import com.github.phoswald.rstm.security.SimpleIdentityProvider;

class TestModule extends ApplicationModule {

    @Override
    public ConfigProvider getConfigProvider() {
        return new ConfigProvider() {
            @Override
            public Optional<String> getConfigProperty(String name) {
                return switch (name) {
                    case "app.sample.config" -> Optional.of("ValueFromTestModule");
                    default -> super.getConfigProperty(name);
                };
            }
        };
    }

    @Override
    public IdentityProvider getIdentityProvider() {
        return new SimpleIdentityProvider() //
                .withUser("username1", "password1", List.of("user"));
    }
}
