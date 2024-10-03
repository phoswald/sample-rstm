# sample-rstm

Experiments with the RSTM (Really Simple & Totally Minimalistic) Framework and Docker, featuring:

- Static web content
- Dynamic web content
- REST endpoints
- Postgres or H2 database using JDBC

## Run Standalone

~~~
$ mvn clean verify
$ export APP_SAMPLE_CONFIG=ValueFromShell
$ java \
  -cp $(echo target/sample-rstm-*-dist/lib)/"*" \
  -Dapp.http.port=8080 \
  -Dapp.jdbc.url=jdbc:h2:./databases/task-db \
  com.github.phoswald.sample.Application
$ java \
  -cp $(echo target/sample-rstm-*-dist/lib)/"*" \
  -Dapp.http.port=8080 \
  -Dapp.jdbc.url=jdbc:postgresql://localhost:5432/mydb \
  -Dapp.jdbc.username=myuser \
  -Dapp.jdbc.password=mypassword \
  -Dapp.jwt.issuer=surin.home/sample \
  -Dapp.jwt.secret=mysecretforhmac \
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
$ docker run -it --name sample-rstm --rm \
  -p 8080:8080 \
  -e APP_SAMPLE_CONFIG=ValueFromDockerRun \
  -e APP_JDBC_URL=jdbc:postgresql://surin.home:5432/mydb \
  -e APP_JDBC_USERNAME=myuser \
  -e APP_JDBC_PASSWORD=mypassword \
  -e APP_JWT_ISSUER=surin.home/sample \
  -e APP_JWT_SECRET=mysecretforhmac \
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
$ curl 'http://localhost:8080/app/rest/tasks' -i
$ curl 'http://localhost:8080/app/rest/tasks' -i -X POST \
  -H 'content-type: application/json' \
  -d '{"title":"Some task","description":"This is CURL","done":true}'
$ curl 'http://localhost:8080/app/rest/tasks/5b89f266-c566-4d1f-8545-451bc443cf26' -i
$ curl 'http://localhost:8080/app/rest/tasks/5b89f266-c566-4d1f-8545-451bc443cf26' -i -X PUT \
  -H 'content-type: application/json' \
  -d '{"title":"Some updated task","description":"This is still CURL","done":false}'
$ curl 'http://localhost:8080/app/rest/tasks/5b89f266-c566-4d1f-8545-451bc443cf26' -i -X DELETE
~~~

## Database:

See [SQL DDL](src/main/resources/schema.postgresql.sql) for Postgres.

Create passwords for form-based authentication (table `user_`):

~~~
$ java -cp $(echo target/sample-rstm-*-dist/lib)/"*" \
  com.github.phoswald.rstm.security.jdbc.PasswordUtility
~~~
