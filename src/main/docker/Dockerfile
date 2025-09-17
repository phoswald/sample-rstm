FROM alpine:latest

RUN apk add musl zlib

# RUN apk add openjdk21-jre-headless

# RUN apk add java-common java-cacerts
# /usr/bin/java -> ../lib/jvm/default-jvm/bin/java
# /etc/ca-certificates/update.d/java-cacerts

ARG JRE=zulu25.28.85-ca-jre25.0.0-linux_musl_x64
ADD ${JRE}.tar.gz  /usr/local/
RUN mv /usr/local/${JRE} /usr/local/jre

ENV TZ                Europe/Zurich
ENV LANG              de_CH.UTF-8
ENV LC_ALL            de_CH.UTF-8
ENV JAVA_TOOL_OPTIONS -XX:MaxRAMPercentage=50
ENV APP_SAMPLE_CONFIG ValueFromDockerfile

USER daemon:daemon
EXPOSE 8080

WORKDIR /usr/local/application/
COPY maven .
CMD ["/usr/local/jre/bin/java", "-cp", "lib/*", "com.github.phoswald.sample.Application"]
