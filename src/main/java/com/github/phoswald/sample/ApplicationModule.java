package com.github.phoswald.sample;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.phoswald.rstm.config.ConfigProvider;
import com.github.phoswald.rstm.security.IdentityProvider;
import com.github.phoswald.rstm.security.SimpleIdentityProvider;
import com.github.phoswald.rstm.security.SimpleTokenProvider;
import com.github.phoswald.rstm.security.TokenProvider;
import com.github.phoswald.rstm.security.jdbc.JdbcIdentityProvider;
import com.github.phoswald.rstm.security.jwt.JwtTokenProvider;
import com.github.phoswald.rstm.security.oidc.OidcIdentityProvider;
import com.github.phoswald.sample.sample.SampleController;
import com.github.phoswald.sample.sample.SampleResource;
import com.github.phoswald.sample.task.TaskController;
import com.github.phoswald.sample.task.TaskRepository;
import com.github.phoswald.sample.task.TaskResource;

public class ApplicationModule {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public Application getApplication() {
        return new Application(getConfigProvider(), //
                getSampleResource(), getSampleController(), getTaskResource(), getTaskController(), //
                getIdentityProvider());
    }

    public ConfigProvider getConfigProvider() {
        return new ConfigProvider();
    }

    public SampleResource getSampleResource() {
        return new SampleResource(getConfigProvider());
    }

    public SampleController getSampleController() {
        return new SampleController(getConfigProvider());
    }

    public TaskResource getTaskResource() {
        return new TaskResource(getTaskRepositoryFactory());
    }

    public TaskController getTaskController() {
        return new TaskController(getTaskRepositoryFactory());
    }

    public Supplier<TaskRepository> getTaskRepositoryFactory() {
        return () -> new TaskRepository(getConnection());
    }

    public IdentityProvider getIdentityProvider() {
        ConfigProvider config = getConfigProvider();
        IdentityProvider localIdp = getLocalIdentityProvider();
        Optional<String> oidcRedirctUri = config.getConfigProperty("app.oidc.redirect.uri");
        if (oidcRedirctUri.isPresent()) {
            // Create a federated IDP that wraps the local IDP
            logger.info("Using federated IDP: OIDC");
            OidcIdentityProvider federatedIdp = new OidcIdentityProvider(oidcRedirctUri.get(), localIdp);
            // Add Dex if configured
            Optional<String> dexClientId = config.getConfigProperty("app.oidc.dex.client.id");
            Optional<String> dexClientSecret = config.getConfigProperty("app.oidc.dex.client.secret");
            Optional<String> dexBaseUri = config.getConfigProperty("app.oidc.dex.base.uri");
            if (dexClientId.isPresent() && dexClientSecret.isPresent()) {
                logger.info("Using OIDC provider: Dex");
                federatedIdp.withDex(dexClientId.get(), dexClientSecret.get(), dexBaseUri.get());
            }
            // Add Google if configured
            Optional<String> googleClientId = config.getConfigProperty("app.oidc.google.client.id");
            Optional<String> googleClientSecret = config.getConfigProperty("app.oidc.google.client.secret");
            if (googleClientId.isPresent() && googleClientSecret.isPresent()) {
                logger.info("Using OIDC provider: Google");
                federatedIdp.withGoogle(googleClientId.get(), googleClientSecret.get());
            }
            // Add Microsoft if configured
            Optional<String> microsoftClientId = config.getConfigProperty("app.oidc.microsoft.client.id");
            Optional<String> microsoftClientSecret = config.getConfigProperty("app.oidc.microsoft.client.secret");
            Optional<String> microsoftTenantId = config.getConfigProperty("app.oidc.microsoft.tenant.id");
            if (microsoftClientId.isPresent() && microsoftClientSecret.isPresent()) {
                logger.info("Using OIDC provider: Microsoft");
                federatedIdp.withMicrosoft(microsoftClientId.get(), microsoftClientSecret.get(), microsoftTenantId.get());
            }
            // Add Facebook if configured
            Optional<String> facebookClientId = config.getConfigProperty("app.oidc.facebook.client.id");
            Optional<String> facebookClientSecret = config.getConfigProperty("app.oidc.facebook.client.secret");
            if (facebookClientId.isPresent() && facebookClientSecret.isPresent()) {
                logger.info("Using OIDC provider: Facebook");
                federatedIdp.withFacebook(facebookClientId.get(), facebookClientSecret.get());
            }
            return federatedIdp;
        } else {
            return localIdp;
        }
    }

    private IdentityProvider getLocalIdentityProvider() {
        ConfigProvider config = getConfigProvider();
        if (config.getConfigProperty("app.jdbc.url").isPresent()) {
            logger.info("Using local IDP: JDBC");
            return new JdbcIdentityProvider(getTokenProvider(), this::getConnection);
        } else {
            logger.warn("Using local IDP with login guest:guest");
            return new SimpleIdentityProvider(getTokenProvider()).withUser("guest", "guest", List.of("user"));
        }
    }

    private TokenProvider getTokenProvider() {
        ConfigProvider config = getConfigProvider();
        Optional<String> issuer = config.getConfigProperty("app.jwt.issuer");
        Optional<String> secret = config.getConfigProperty("app.jwt.secret");
        if(issuer.isPresent() && secret.isPresent()) {
            logger.info("Using JWT for local IDP");
            return new JwtTokenProvider(issuer.get(), secret.get());
        } else {
            return new SimpleTokenProvider();
        }
    }

    public Connection getConnection() {
        try {
            ConfigProvider config = getConfigProvider();
            String url = config.getConfigProperty("app.jdbc.url").orElse("jdbc:h2:mem:test " + hashCode() + " ;DB_CLOSE_DELAY=-1;INIT=RUNSCRIPT FROM 'src/main/resources/schema.h2.sql'");
            String username = config.getConfigProperty("app.jdbc.username").orElse("sa");
            String password = config.getConfigProperty("app.jdbc.password").orElse("sa");
            return DriverManager.getConnection(url, username, password);
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }
}
