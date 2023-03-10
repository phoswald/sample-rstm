package com.github.phoswald.sample;

import static com.github.phoswald.rstm.http.codec.json.JsonCodec.json;
import static com.github.phoswald.rstm.http.codec.xml.XmlCodec.xml;
import static com.github.phoswald.rstm.http.server.HttpServerConfig.combine;
import static com.github.phoswald.rstm.http.server.HttpServerConfig.get;
import static com.github.phoswald.rstm.http.server.HttpServerConfig.post;
import static com.github.phoswald.rstm.http.server.HttpServerConfig.resources;
import static com.github.phoswald.rstm.http.server.HttpServerConfig.route;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.phoswald.rstm.config.ConfigProvider;
import com.github.phoswald.rstm.http.HttpResponse;
import com.github.phoswald.rstm.http.server.HttpServer;
import com.github.phoswald.rstm.http.server.HttpServerConfig;
import com.github.phoswald.sample.sample.EchoRequest;
import com.github.phoswald.sample.sample.SampleController;
import com.github.phoswald.sample.sample.SampleResource;

public class Application {

    private static final Logger logger = LoggerFactory.getLogger(Application.class);

    private final int port;
    private final SampleResource sampleResource;
    private final SampleController sampleController;
    private HttpServer httpServer;

    public Application( //
            ConfigProvider config, //
            SampleResource sampleResource, //
            SampleController sampleController) {
        this.port = Integer.parseInt(config.getConfigProperty("app.http.port").orElse("8080"));
        this.sampleResource = sampleResource;
        this.sampleController = sampleController;
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
                                get(request -> HttpResponse.html(200, sampleController.getSamplePage()))) //
                )) //
                .build());
    }

    void stop() {
        httpServer.close();
    }
}
