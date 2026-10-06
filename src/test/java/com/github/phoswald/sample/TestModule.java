package com.github.phoswald.sample;

import java.util.List;
import java.util.Optional;

import com.github.phoswald.rstm.config.ConfigProvider;
import com.github.phoswald.rstm.security.IdentityProvider;
import com.github.phoswald.rstm.security.SimpleIdentityProvider;

class TestModule extends ApplicationModule {

//    Principal wyb;
//
//    String getWybToken() {
//        return wyb.token();
//    }

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
        // TokenProvider tokenProvider = new SimpleTokenProvider(); // TODO (WYB): add token to simple provider
        // wyb = tokenProvider.createPrincipal("username3", List.of("wyb"));
        return new SimpleIdentityProvider(/* tokenProvider */)
                .withUser("username1", "password1", List.of("user"))
                .withUser("username2", "password2", List.of("wyb")); // TODO (WYB): remove, use token instead
    }
}
