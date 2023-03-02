package com.github.phoswald.sample;

import static com.github.phoswald.rstm.http.codec.json.JsonCodec.json;
import static com.github.phoswald.rstm.http.codec.xml.XmlCodec.xml;
import static com.github.phoswald.rstm.http.server.HttpServerConfig.combine;
import static com.github.phoswald.rstm.http.server.HttpServerConfig.delete;
import static com.github.phoswald.rstm.http.server.HttpServerConfig.get;
import static com.github.phoswald.rstm.http.server.HttpServerConfig.post;
import static com.github.phoswald.rstm.http.server.HttpServerConfig.put;
import static com.github.phoswald.rstm.http.server.HttpServerConfig.resources;
import static com.github.phoswald.rstm.http.server.HttpServerConfig.route;

import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.phoswald.rstm.config.ConfigProvider;
import com.github.phoswald.rstm.http.HttpCodec;
import com.github.phoswald.rstm.http.HttpRequest;
import com.github.phoswald.rstm.http.HttpResponse;
import com.github.phoswald.rstm.http.server.HttpFilter;
import com.github.phoswald.rstm.http.server.HttpServer;
import com.github.phoswald.rstm.http.server.HttpServerConfig;
import com.github.phoswald.rstm.http.server.ThrowingFunction;
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
    private HttpServer httpServer;

    public Application( //
            ConfigProvider config, //
            SampleResource sampleResource, //
            SampleController sampleController, //
            TaskResource taskResource, //
            TaskController taskController) {
        this.port = Integer.parseInt(config.getConfigProperty("app.http.port").orElse("8080"));
        this.sampleResource = sampleResource;
        this.sampleController = sampleController;
        this.taskResource = taskResource;
        this.taskController = taskController;
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
                        route("/app/pages/sample", //
                                get(request -> HttpResponse.html(200, sampleController.getSamplePage()))), //
                        route("/app/rest/tasks", //
                                getX(json(), request -> taskResource.getTasks()), //
                                postX(json(), TaskEntity.class, (request, requestBody) -> taskResource.postTasks(requestBody))), //
                        route("/app/rest/tasks/{id}", //
                                getX(json(), request -> taskResource.getTask(request.pathParam("id").get())), //
                                putX(json(), TaskEntity.class, (request, requestBody) -> taskResource.putTask(request.pathParam("id").get(), requestBody)), //
                                delete(request -> HttpResponse.text(200, taskResource.deleteTask(request.pathParam("id").get())))), //
                        route("/app/pages/tasks", //
                                get(request -> HttpResponse.html(200, taskController.getTasksPage())), //
                                post(request -> HttpResponse.html(200, taskController.postTasksPage( //
                                        request.formParam("title").get(), //
                                        request.formParam("description").orElse(null))))), //
                        route("/app/pages/tasks/{id}", //
                                get(request -> HttpResponse.html(200, taskController.getTaskPage( //
                                        request.pathParam("id").get(), //
                                        request.queryParam("action").orElse(null)))), //
                                postH(request -> taskController.postTaskPage( //
                                        request.pathParam("id").get(), //
                                        request.formParam("action").get(), //
                                        request.formParam("title").get(), //
                                        request.formParam("description").get(), //
                                        request.formParam("done").orElse(null)))) //
                )) //
                .build());
    }

    // TODO cleanup CRUD operation and HTML page handling (codecs, 404, 302)

    static <T> HttpFilter getX(HttpCodec codec, ThrowingFunction<HttpRequest, T> handler) {
        return get(request -> {
            T responseObj = handler.invoke(request);
            return responseObj == null ? HttpResponse.empty(404) : HttpResponse.body(200, codec, responseObj);
        });
    }

    static <A, B> HttpFilter postX(HttpCodec codec, Class<A> clazzA, ThrowingBiFunction<HttpRequest, A, B> handler) {
        return post(request -> {
            A requestObj = request.body(codec, clazzA);
            B responseObj = handler.invoke(request, requestObj);
            return responseObj == null ? HttpResponse.empty(404) : HttpResponse.body(200, codec, responseObj);
        });
    }

    static <A, B> HttpFilter putX(HttpCodec codec, Class<A> clazzA, ThrowingBiFunction<HttpRequest, A, B> handler) {
        return put(request -> {
            A requestObj = request.body(codec, clazzA);
            B responseObj = handler.invoke(request, requestObj);
            return responseObj == null ? HttpResponse.empty(404) : HttpResponse.body(200, codec, responseObj);
        });
    }

    static HttpFilter postH(ThrowingFunction<HttpRequest, Object> handler) {
        return post(request -> {
           Object response = handler.invoke(request);
           if(response instanceof Path location) {
               return HttpResponse.redirect(302, location.toString());
           } else {
               return HttpResponse.html(200, response.toString());
           }
        });
    }

    void stop() {
        httpServer.close();
    }

    public interface ThrowingBiFunction<A, B, C> {

        public C invoke(A request1, B request2) throws Exception;
    }
}
