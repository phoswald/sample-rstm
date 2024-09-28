package com.github.phoswald.sample;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.function.Supplier;

import com.github.phoswald.rstm.config.ConfigProvider;
import com.github.phoswald.rstm.security.IdentityProvider;
import com.github.phoswald.rstm.security.JdbcIdentityProvider;
import com.github.phoswald.sample.sample.SampleController;
import com.github.phoswald.sample.sample.SampleResource;
import com.github.phoswald.sample.task.TaskController;
import com.github.phoswald.sample.task.TaskRepository;
import com.github.phoswald.sample.task.TaskResource;
 
public class ApplicationModule {

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
        return new JdbcIdentityProvider(() -> getConnection());
    }

    public Connection getConnection() {
        try {
            var config = getConfigProvider();
            String url = config.getConfigProperty("app.jdbc.url").orElse("jdbc:h2:mem:test " + hashCode()
                    + " ;DB_CLOSE_DELAY=-1;INIT=RUNSCRIPT FROM 'src/main/resources/schema.sql'");
            String username = config.getConfigProperty("app.jdbc.username").orElse("sa");
            String password = config.getConfigProperty("app.jdbc.password").orElse("sa");
            return DriverManager.getConnection(url, username, password);
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }
}
