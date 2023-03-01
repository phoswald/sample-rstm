# sample-rstm

Experiments with the RSTM (Really Simple & Totally Minimalistic) Framework and Docker, featuring:

- Static web content
- Dynamic web content
- REST endpoints

## Run Standalone

~~~
$ mvn clean verify
$ export APP_SAMPLE_CONFIG=ValueFromShell
$ java \
  -Dapp.http.port=8080 \
  -Dapp.jdbc.url=jdbc:h2:./databases/task-db \
  -cp 'target/sample-rstm-0.1.0-SNAPSHOT/lib/*' \
  com.github.phoswald.sample.Application
~~~

## Run with Docker

~~~
$ mvn clean verify -P docker
$ docker run -it --name sample-rstm --rm \
  -p 8080:8080 \
  -e APP_SAMPLE_CONFIG=ValueFromDockerRun \
  -e APP_JDBC_URL=jdbc:h2:/databases/task-db \
  -v ./databases/:/databases \
  sample-rstm:0.1.0-SNAPSHOT
~~~

# URLs

- http://localhost:8080/

~~~
$ curl 'http://localhost:8080/app/rest/sample/time' -i
$ curl 'http://localhost:8080/app/rest/sample/config' -i
$ curl 'http://localhost:8080/app/rest/sample/echo-xml' -i -X POST \
  -H 'content-type: text/xml' \
  -d '<echoRequest><input>This is CURL</input></echoRequest>'
$ curl 'http://localhost:8080/app/rest/sample/echo-json' -i -X POST \
  -H 'content-type: application/json' \
  -d '{"input":"This is CURL"}'
~~~
