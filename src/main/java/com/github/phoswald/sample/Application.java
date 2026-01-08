package com.github.phoswald.sample;

import static com.github.phoswald.rstm.http.codec.JsonCodec.json;
import static com.github.phoswald.rstm.http.codec.TextCodec.text;
import static com.github.phoswald.rstm.http.codec.XmlCodec.xml;
import static com.github.phoswald.rstm.http.server.HttpServerConfig.auth;
import static com.github.phoswald.rstm.http.server.HttpServerConfig.deleteRest;
import static com.github.phoswald.rstm.http.server.HttpServerConfig.getHtml;
import static com.github.phoswald.rstm.http.server.HttpServerConfig.getRest;
import static com.github.phoswald.rstm.http.server.HttpServerConfig.login;
import static com.github.phoswald.rstm.http.server.HttpServerConfig.oidc;
import static com.github.phoswald.rstm.http.server.HttpServerConfig.openapi;
import static com.github.phoswald.rstm.http.server.HttpServerConfig.postHtml;
import static com.github.phoswald.rstm.http.server.HttpServerConfig.postRest;
import static com.github.phoswald.rstm.http.server.HttpServerConfig.putRest;
import static com.github.phoswald.rstm.http.server.HttpServerConfig.resources;
import static com.github.phoswald.rstm.http.server.HttpServerConfig.route;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.phoswald.rstm.config.ConfigProvider;
import com.github.phoswald.rstm.http.openapi.OpenApiConfig;
import com.github.phoswald.rstm.http.server.HttpFilter;
import com.github.phoswald.rstm.http.server.HttpServer;
import com.github.phoswald.rstm.http.server.HttpServerConfig;
import com.github.phoswald.rstm.security.IdentityProvider;
import com.github.phoswald.sample.sample.EchoRequest;
import com.github.phoswald.sample.sample.EchoResponse;
import com.github.phoswald.sample.sample.SampleController;
import com.github.phoswald.sample.sample.SampleResource;
import com.github.phoswald.sample.task.Task;
import com.github.phoswald.sample.task.TaskController;
import com.github.phoswald.sample.task.TaskList;
import com.github.phoswald.sample.task.TaskResource;

public class Application {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final int port;
    private final SampleResource sampleResource;
    private final SampleController sampleController;
    private final TaskResource taskResource;
    private final TaskController taskController;
    private final IdentityProvider identityProvider;
    private final HealthCheckProvider healthCheckProvider;
    private final MetricsProvider metricsProvider;
    private HttpServer httpServer;

    public Application(
            ConfigProvider config,
            SampleResource sampleResource,
            SampleController sampleController,
            TaskResource taskResource,
            TaskController taskController,
            IdentityProvider identityProvider,
            HealthCheckProvider healthCheckProvider,
            MetricsProvider metricsProvider) {
        this.port = Integer.parseInt(config.getConfigProperty("app.http.port").orElse("8080"));
        this.sampleResource = sampleResource;
        this.sampleController = sampleController;
        this.taskResource = taskResource;
        this.taskController = taskController;
        this.identityProvider = identityProvider;
        this.healthCheckProvider = healthCheckProvider;
        this.metricsProvider = metricsProvider;
    }

    static void main() {
        var module = new ApplicationModule();
        module.getApplication().start();
    }

    void start() {
        logger.info("sample-rstm is starting, port={}", port);
        httpServer = new HttpServer(HttpServerConfig.builder()
                .httpPort(port)
                .filter(getRoutes())
                .identityProvider(identityProvider)
                .build());
    }

    private HttpFilter getRoutes() {
        OpenApiConfig openApiConfig = OpenApiConfig.builder()
                .title("sample-rstm")
                .description("OpenAPI for RSTM Sample Service")
                .version("0.1.0-SNAPSHOT")
                .urls(List.of("http://localhost:8080", "https://phoswald.ch/rstm"))
                .build();
        return openapi(openApiConfig,
                resources("/html/"),
                route("/login", login()),
                route("/oauth/callback", oidc()),
                route("/app/rest/sample/time",
                        getRest(text(), String.class, sampleResource::getTime)),
                route("/app/rest/sample/config",
                        getRest(text(), String.class, sampleResource::getConfig)),
                route("/app/rest/sample/echo-xml",
                        postRest(xml(), EchoRequest.class, EchoResponse.class, sampleResource::postEcho)),
                route("/app/rest/sample/echo-json",
                        postRest(json(), EchoRequest.class, EchoResponse.class, sampleResource::postEcho)),
                route("/app/rest/sample/me", auth("user",
                        getRest(text(), String.class, req -> sampleResource.getMe(req.principal())))),
                route("/app/rest/tasks",
                        getRest(json(), TaskList.class, taskResource::getTasks),
                        postRest(json(), Task.class, Task.class, taskResource::postTasks)),
                route("/app/rest/tasks/{id}",
                        getRest(json(), TaskResource.IdParams.class, Task.class, taskResource::getTask),
                        putRest(json(), TaskResource.IdParams.class, Task.class, Task.class, taskResource::putTask),
                        deleteRest(json(), TaskResource.IdParams.class, String.class, taskResource::deleteTask)),
                route("/app/pages", auth("user",
                        route("/sample",
                                getHtml(req -> sampleController.getSamplePage(req.principal()))),
                        route("/tasks",
                                getHtml(taskController::getTasksPage),
                                postHtml(TaskController.PostParams.class, taskController::postTasksPage)),
                        route("/tasks/{id}",
                                getHtml(TaskController.IdParams.class, taskController::getTaskPage),
                                postHtml(TaskController.IdPostParams.class, taskController::postTaskPage)))),
                healthCheckProvider.createRoute(),
                metricsProvider.createRoute()
        );
    }

    void stop() {
        httpServer.close();
    }
}
