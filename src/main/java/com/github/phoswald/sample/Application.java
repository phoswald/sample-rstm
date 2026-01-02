package com.github.phoswald.sample;

import static com.github.phoswald.rstm.http.codec.json.JsonCodec.json;
import static com.github.phoswald.rstm.http.codec.xml.XmlCodec.xml;
import static com.github.phoswald.rstm.http.server.HttpServerConfig.auth;
import static com.github.phoswald.rstm.http.server.HttpServerConfig.combine;
import static com.github.phoswald.rstm.http.server.HttpServerConfig.delete;
import static com.github.phoswald.rstm.http.server.HttpServerConfig.get;
import static com.github.phoswald.rstm.http.server.HttpServerConfig.getHtml;
import static com.github.phoswald.rstm.http.server.HttpServerConfig.getRest;
import static com.github.phoswald.rstm.http.server.HttpServerConfig.login;
import static com.github.phoswald.rstm.http.server.HttpServerConfig.oidc;
import static com.github.phoswald.rstm.http.server.HttpServerConfig.post;
import static com.github.phoswald.rstm.http.server.HttpServerConfig.postHtml;
import static com.github.phoswald.rstm.http.server.HttpServerConfig.postRest;
import static com.github.phoswald.rstm.http.server.HttpServerConfig.putRest;
import static com.github.phoswald.rstm.http.server.HttpServerConfig.resources;
import static com.github.phoswald.rstm.http.server.HttpServerConfig.route;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.phoswald.rstm.config.ConfigProvider;
import com.github.phoswald.rstm.http.HttpRequest;
import com.github.phoswald.rstm.http.HttpResponse;
import com.github.phoswald.rstm.http.server.HttpFilter;
import com.github.phoswald.rstm.http.server.HttpServer;
import com.github.phoswald.rstm.http.server.HttpServerConfig;
import com.github.phoswald.rstm.security.IdentityProvider;
import com.github.phoswald.sample.sample.EchoRequest;
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
        return combine(
                route("/",
                        resources("/html/")),
                route("/login", login()),
                route("/oauth/callback", oidc()),
                route("/app/rest/sample/time",
                        get(_ -> HttpResponse.text(200, sampleResource.getTime()))),
                route("/app/rest/sample/config",
                        get(_ -> HttpResponse.text(200, sampleResource.getConfig()))),
                route("/app/rest/sample/echo-xml",
                        post(this::handleRestSampleEchoXml)),
                route("/app/rest/sample/echo-json",
                        post(this::handleRestSampleEchoJson)),
                route("/app/rest/sample/me", auth("user",
                        get(req -> HttpResponse.text(200, sampleResource.getMe(req.principal()))))),
                route("/app/rest/tasks",
                        getRest(json(), this::handleRestTasksGet),
                        postRest(json(), Task.class, this::handleRestTasksPost)),
                route("/app/rest/tasks/{id}",
                        getRest(json(), this::handleRestTasksByIdGet),
                        putRest(json(), Task.class, this::handleRestTasksByIdPut),
                        delete(this::handleRestTasksByIdDelete)),
                route("/app/pages", auth("user",
                        route("/sample",
                                get(this::handlePagesSampleGet)),
                        route("/tasks",
                                getHtml(this::handlePagesTasksGetHtml),
                                postHtml(this::handlePagesTasksPostHtml)),
                        route("/tasks/{id}",
                                getHtml(this::handlePagesTasksByIdGetHtml),
                                postHtml(this::handlePagesTasksByIdPostHtml)))),
                healthCheckProvider.createRoute(),
                metricsProvider.createRoute()
        );
    }

    private HttpResponse handleRestSampleEchoXml(HttpRequest req) {
        return HttpResponse.body(200, xml(), sampleResource.postEcho(req.body(xml(), EchoRequest.class)));
    }

    private HttpResponse handleRestSampleEchoJson(HttpRequest req) {
        return HttpResponse.body(200, json(), sampleResource.postEcho(req.body(json(), EchoRequest.class)));
    }

    private TaskList handleRestTasksGet(HttpRequest req) {
        return taskResource.getTasks();
    }

    private Task handleRestTasksPost(HttpRequest req, Task reqBody) {
        return taskResource.postTasks(reqBody);
    }

    private Task handleRestTasksByIdGet(HttpRequest req) {
        return taskResource.getTask(req.pathParam("id").orElseThrow());
    }

    private Task handleRestTasksByIdPut(HttpRequest req, Task reqBody) {
        return taskResource.putTask(req.pathParam("id").orElseThrow(), reqBody);
    }

    private HttpResponse handleRestTasksByIdDelete(HttpRequest req) {
        return HttpResponse.text(200, taskResource.deleteTask(req.pathParam("id").orElseThrow()));
    }

    private HttpResponse handlePagesSampleGet(HttpRequest req) {
        return HttpResponse.html(200, sampleController.getSamplePage(req.principal()));
    }

    private String handlePagesTasksGetHtml(HttpRequest req) {
        return taskController.getTasksPage();
    }

    private String handlePagesTasksPostHtml(HttpRequest req) {
        return taskController.postTasksPage(
                req.formParam("title").orElseThrow(),
                req.formParam("description").orElse(null));
    }

    private String handlePagesTasksByIdGetHtml(HttpRequest req) {
        return taskController.getTaskPage(
                req.pathParam("id").orElseThrow(),
                req.queryParam("action").orElse(null));
    }

    private Object handlePagesTasksByIdPostHtml(HttpRequest req) {
        return taskController.postTaskPage(
                req.pathParam("id").orElseThrow(),
                req.formParam("action").orElseThrow(),
                req.formParam("title").orElseThrow(),
                req.formParam("description").orElseThrow(),
                req.formParam("done").orElse(null));
    }

    void stop() {
        httpServer.close();
    }
}
