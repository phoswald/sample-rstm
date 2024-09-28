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
                .filter(combine( //
                        route("/", //
                                resources("/html/")), //
                        route("/login", login()), //
                        route("/app/rest/sample/time", //
                                get(request -> HttpResponse.text(200, sampleResource.getTime()))), //
                        route("/app/rest/sample/config", //
                                get(request -> HttpResponse.text(200, sampleResource.getConfig()))), //
                        route("/app/rest/sample/echo-xml", //
                                post(request -> HttpResponse.body(200, xml(), //
                                        sampleResource.postEcho(request.body(xml(), EchoRequest.class))))), //
                        route("/app/rest/sample/echo-json", //
                                post(request -> HttpResponse.body(200, json(), //
                                        sampleResource.postEcho(request.body(json(), EchoRequest.class))))), //
                        route("/app/rest/tasks", //
                                getRest(json(), request -> taskResource.getTasks()), //
                                postRest(json(), TaskEntity.class, (request, requestBody) -> taskResource.postTasks(requestBody))), //
                        route("/app/rest/tasks/{id}", //
                                getRest(json(), request -> taskResource.getTask(request.pathParam("id").get())), //
                                putRest(json(), TaskEntity.class, (request, requestBody) -> taskResource.putTask(request.pathParam("id").get(), requestBody)), //
                                delete(request -> HttpResponse.text(200, taskResource.deleteTask(request.pathParam("id").get())))), //
                        route("/app/pages", auth("user", //
                                route("/sample", //
                                        get(request -> HttpResponse.html(200, sampleController.getSamplePage(request.principal()))))), //
                                route("/tasks", //
                                        getHtml(request -> taskController.getTasksPage()), //
                                        postHtml(request -> taskController.postTasksPage( //
                                                request.formParam("title").get(), //
                                                request.formParam("description").orElse(null)))), //
                                route("/tasks/{id}", //
                                        getHtml(request -> taskController.getTaskPage( //
                                                request.pathParam("id").get(), //
                                                request.queryParam("action").orElse(null))), //
                                        postHtml(request -> taskController.postTaskPage( //
                                                request.pathParam("id").get(), //
                                                request.formParam("action").get(), //
                                                request.formParam("title").get(), //
                                                request.formParam("description").get(), //
                                                request.formParam("done").orElse(null))))) //
                )) //
                .identityProvider(identityProvider) //
                .build());
    }

    void stop() {
        httpServer.close();
    }
}
