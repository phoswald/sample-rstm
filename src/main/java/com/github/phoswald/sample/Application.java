package com.github.phoswald.sample;

import static com.github.phoswald.rstm.http.server.HttpServerConfig.combine;
import static com.github.phoswald.rstm.http.server.HttpServerConfig.get;
import static com.github.phoswald.rstm.http.server.HttpServerConfig.post;
import static com.github.phoswald.rstm.http.server.HttpServerConfig.resources;
import static com.github.phoswald.rstm.http.server.HttpServerConfig.route;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.phoswald.rstm.config.ConfigProvider;
import com.github.phoswald.rstm.http.HttpResponse;
import com.github.phoswald.rstm.http.server.HttpServer;
import com.github.phoswald.rstm.http.server.HttpServerConfig;
import com.github.phoswald.sample.sample.EchoRequest;
import com.github.phoswald.sample.sample.SampleResource;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.xml.bind.JAXB;

public class Application {

    private static final Logger logger = LoggerFactory.getLogger(Application.class);
    private static final Jsonb json = JsonbBuilder.create();

    private final int port;
    private final SampleResource sampleResource;
    private HttpServer httpServer;

    public Application( //
            ConfigProvider config, //
            SampleResource sampleResource) {
        this.port = Integer.parseInt(config.getConfigProperty("app.http.port").orElse("8080"));
        this.sampleResource = sampleResource;
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
                        route("/app/rest/sample/time", //
                                get(request -> HttpResponse.text(200, sampleResource.getTime()))), //
                        route("/app/rest/sample/config", //
                                get(request -> HttpResponse.text(200, sampleResource.getConfig()))), //
                        route("/app/rest/sample/echo-xml", //
                                post(request -> HttpResponse.builder() //
                                        .status(200) //
                                        .contentType("text/xml") //
                                        .body(serializeXml(sampleResource.postEcho( //
                                                deserializeXml(EchoRequest.class, request.body())))) //
                                        .build())), //
                        route("/app/rest/sample/echo-json", //
                                post(request -> HttpResponse.builder() //
                                        .status(200) //
                                        .contentType("application/json") //
                                        .body(serializeJson(sampleResource.postEcho( //
                                                deserializeJson(EchoRequest.class, request.body())))) //
                                        .build())), //
                        route("/", //
                                resources("/html/")) //
                )) //
                .build());
    }

    void stop() {
        httpServer.close();
    }

    private static byte[] serializeXml(Object object) {
        var buffer = new ByteArrayOutputStream();
        JAXB.marshal(object, buffer);
        return buffer.toByteArray();
    }

    private static <T> T deserializeXml(Class<T> clazz, byte[] bytes) {
        return JAXB.unmarshal(new ByteArrayInputStream(bytes), clazz);
    }

    private static byte[] serializeJson(Object object) {
        var buffer = new ByteArrayOutputStream();
        json.toJson(object, buffer);
        return buffer.toByteArray();
    }

    private static <T> T deserializeJson(Class<T> clazz, byte[] bytes) {
        return json.fromJson(new ByteArrayInputStream(bytes), clazz);
    }
}
