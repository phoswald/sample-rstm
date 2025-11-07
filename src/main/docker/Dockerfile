FROM alpine:latest

RUN apk add openjdk25-jre-headless --repository http://dl-cdn.alpinelinux.org/alpine/edge/testing

ENV TZ                  Europe/Zurich
ENV LANG                de_CH.UTF-8
ENV LC_ALL              de_CH.UTF-8
#   JAVA_HOME           /usr/lib/jvm/default-jvm
#   JRE_CACERTS_PATH    ${JAVA_HOME}/lib/security/cacerts
ENV JAVA_TOOL_OPTIONS   -XX:MaxRAMPercentage=50
ENV APP_SAMPLE_CONFIG   ValueFromDockerfile

WORKDIR /usr/local/application/
COPY maven .
CMD ["java", "-cp", "lib/*", "com.github.phoswald.sample.Application"]

USER daemon:daemon
EXPOSE 8080
