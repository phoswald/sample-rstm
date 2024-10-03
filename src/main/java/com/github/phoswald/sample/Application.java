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
import static com.github.phoswald.rstm.http.server.HttpServerConfig.post;
import static com.github.phoswald.rstm.http.server.HttpServerConfig.postHtml;
import static com.github.phoswald.rstm.http.server.HttpServerConfig.postRest;
import static com.github.phoswald.rstm.http.server.HttpServerConfig.putRest;
import static com.github.phoswald.rstm.http.server.HttpServerConfig.resources;
import static com.github.phoswald.rstm.http.server.HttpServerConfig.route;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.phoswald.rstm.config.ConfigProvider;
import com.github.phoswald.rstm.http.HttpResponse;
import com.github.phoswald.rstm.http.server.HttpFilter;
import com.github.phoswald.rstm.http.server.HttpServer;
import com.github.phoswald.rstm.http.server.HttpServerConfig;
import com.github.phoswald.rstm.security.IdentityProvider;
import com.github.phoswald.sample.sample.EchoRequest;
import com.github.phoswald.sample.sample.SampleController;
import com.github.phoswald.sample.sample.SampleResource;
import com.github.phoswald.sample.task.TaskController;
import com.github.phoswald.sample.task.TaskEntity;
import com.github.phoswald.sample.task.TaskResource;
 
public class Application {

    private static final Logger logger = LoggerFactory.getLogger(Application.class);

    private final int port;
    private final SampleResource sampleResource;
    private final SampleController sampleController;
    private final TaskResource taskResource;
    private final TaskController taskController;
    private final IdentityProvider identityProvider;
    private HttpServer httpServer;
 
    public Application( //
            ConfigProvider config, //
            SampleResource sampleResource, //
            SampleController sampleController, //
            TaskResource taskResource, //
            TaskController taskController, //
            IdentityProvider identityProvider) {
        this.port = Integer.parseInt(config.getConfigProperty("app.http.port").orElse("8080"));
        this.sampleResource = sampleResource;
        this.sampleController = sampleController;
        this.taskResource = taskResource;
        this.taskController = taskController;
        this.identityProvider = identityProvider;
    }

    public static void main(String[] args) {
        var module = new ApplicationModule();
        module.getApplication().start();
    }

    void start() {
        logger.info("sample-rstm is starting, port=" + port);
        httpServer = new HttpServer(HttpServerConfig.builder() //
                .httpPort(port) //
                .filter(getRoutes()) //
                .identityProvider(identityProvider) //
                .build());
    }
    
    private HttpFilter getRoutes() {
        return combine( //
                route("/", //
                        resources("/html/")), //
                route("/login", login()), //
                route("/app/rest/sample/time", //
                        get(req -> HttpResponse.text(200, sampleResource.getTime()))), //
                route("/app/rest/sample/config", //
                        get(req -> HttpResponse.text(200, sampleResource.getConfig()))), //
                route("/app/rest/sample/echo-xml", //
                        post(req -> HttpResponse.body(200, xml(), sampleResource.postEcho(req.body(xml(), EchoRequest.class))))), //
                route("/app/rest/sample/echo-json", //
                        post(req -> HttpResponse.body(200, json(), sampleResource.postEcho(req.body(json(), EchoRequest.class))))), //
                route("/app/rest/sample/me", auth("user", //
                        get(req -> HttpResponse.text(200, sampleResource.getMe(req.principal()))))), //
                route("/app/rest/tasks", //
                        getRest(json(), req -> taskResource.getTasks()), //
                        postRest(json(), TaskEntity.class, (req, reqBody) -> taskResource.postTasks(reqBody))), //
                route("/app/rest/tasks/{id}", //
                        getRest(json(), req -> taskResource.getTask(req.pathParam("id").get())), //
                        putRest(json(), TaskEntity.class, (req, reqBody) -> taskResource.putTask(req.pathParam("id").get(), reqBody)), //
                        delete(req -> HttpResponse.text(200, taskResource.deleteTask(req.pathParam("id").get())))), //
                route("/app/pages", auth("user", //
                        route("/sample", //
                                get(req -> HttpResponse.html(200, sampleController.getSamplePage(req.principal()))))), //
                        route("/tasks", //
                                getHtml(req -> taskController.getTasksPage()), //
                                postHtml(req -> taskController.postTasksPage( //
                                        req.formParam("title").get(), //
                                        req.formParam("description").orElse(null)))), //
                        route("/tasks/{id}", //
                                getHtml(req -> taskController.getTaskPage( //
                                        req.pathParam("id").get(), //
                                        req.queryParam("action").orElse(null))), //
                                postHtml(req -> taskController.postTaskPage( //
                                        req.pathParam("id").get(), //
                                        req.formParam("action").get(), //
                                        req.formParam("title").get(), //
                                        req.formParam("description").get(), //
                                        req.formParam("done").orElse(null))))) //
        );
    }

    void stop() {
        httpServer.close();
    }
}
