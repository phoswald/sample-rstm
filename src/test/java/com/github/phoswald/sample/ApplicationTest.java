package com.github.phoswald.sample;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.matchesRegex;
import static org.hamcrest.Matchers.startsWith;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.phoswald.rstm.config.ConfigProvider;
import com.github.phoswald.rstm.security.IdentityProvider;
import com.github.phoswald.rstm.security.SimpleIdentityProvider;
import com.github.phoswald.sample.task.TaskEntity;
 
class ApplicationTest {

    private static final ApplicationModule module = new TestModule();

    private final Application testee = module.getApplication();

    @BeforeEach
    void start() {
        testee.start();
    }

    @AfterEach
    void cleanup() {
        testee.stop();
    }

    @Test
    void getIndexPage() {
        when().
            get("/").
        then().
            statusCode(200).
            contentType("text/html").
            body(startsWith("<!doctype html>"), containsString("<title>RSTM Sample Service</title>"));
    }

    @Test
    void getTime() {
        when().
            get("/app/rest/sample/time").
        then().
            statusCode(200).
            body(matchesRegex("[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}\\.[0-9]+(\\+|\\-)[0-9]{2}:[0-9]{2}\\[.+\\]\\n"));
    }

    @Test
    void getConfig() {
        when().
            get("/app/rest/sample/config").
        then().
            statusCode(200).
            body(equalTo("Test Config Value\n"));
    }

    @Test
    void postEchoXml() {
        given().
            contentType("text/xml").
            body("<echoRequest><input>Test Input</input></echoRequest>").
        when().
            post("/app/rest/sample/echo-xml").
        then().
            statusCode(200).
            contentType("text/xml").
            body(equalTo("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n<echoResponse>\n    <output>Received Test Input</output>\n</echoResponse>\n"));
    }

    @Test
    void postEchoJson() {
        given().
            contentType("application/json").
            body("{\"input\":\"Test Input\"}").
        when().
            post("/app/rest/sample/echo-json").
        then().
            statusCode(200).
            contentType("application/json").
            body(equalTo("{\"output\":\"Received Test Input\"}"));
    }

    @Test
    void getSamplePage() {
        given().
            auth().preemptive().basic("username", "password").
        when().
            get("/app/pages/sample").
        then().
            statusCode(200).
            contentType("text/html").
            body(startsWith("<!doctype html>"),
                containsString("<title>Sample Page</title>"),
                containsString("<span>Hello</span>, <span>username</span>!"), // #greeting, username
                containsString("<td>Test Config Value</td>")); // sampleConfig
    }

    @Test
    void crudTaskResource() {
        var taskId = new AtomicReference<String>();
        var request = new TaskEntity();
        request.setTitle("Test title");
        given().
            contentType("application/json").
            body(request).
        when().
            post("/app/rest/tasks").
        then().
            statusCode(200).
            contentType("application/json").
            body("taskId", PeekMatcher.peek(taskId::set)).
            body("taskId", matchesRegex("[0-9a-f-]{36}")).
            body("userId", equalTo("guest")).
            body("title", equalTo("Test title"));

        when().
            get("/app/rest/tasks").
        then().
            statusCode(200).
            contentType("application/json").
            body("$.size()", equalTo(1));

        request = new TaskEntity();
        request.setTitle("Test title, updated");
        given().
            contentType("application/json").
            body(request).
        when().
            put("/app/rest/tasks/" + taskId.get()).
        then().
            statusCode(200).
            contentType("application/json").
            body("taskId", equalTo(taskId.get())).
            body("userId", equalTo("guest")).
            body("title", equalTo("Test title, updated"));

        when().
            get("/app/rest/tasks/" + taskId.get()).
        then().
            statusCode(200).
            contentType("application/json").
            body("taskId", equalTo(taskId.get())).
            body("userId", equalTo("guest")).
            body("title", equalTo("Test title, updated"));

        when().
            delete("/app/rest/tasks/" + taskId.get()).
        then().
            statusCode(200).
            body(equalTo(""));

        when().
            get("/app/rest/tasks").
        then().
            statusCode(200).
            contentType("application/json").
            body("$.size()", equalTo(0));

        when().
            get("/app/rest/tasks/" + taskId.get()).
        then().
            statusCode(404).
            body(equalTo(""));
    }

    private static class TestModule extends ApplicationModule {
        @Override
        public ConfigProvider getConfigProvider() {
            return new ConfigProvider() {
                @Override
                public Optional<String> getConfigProperty(String name) {
                    return switch(name) {
                        case "app.sample.config" -> Optional.of("Test Config Value");
                        default -> super.getConfigProperty(name);
                    };
                }
            };
        }
        @Override
        public IdentityProvider getIdentityProvider() {
            return new SimpleIdentityProvider() //
                    .add("username", "password", List.of("user"));
        }
    }
}
