# Context Handler API

The Context Handler (CTX) API is a RESTful web service  component responsible for sending XACML response context that includes authorization decisions along with applied policy obligations to PEP components. Context Handler sends the XACML request context to the PDP which then evaluates the applicable policies from PCM against the request context and returns the response context that includes authorization decisions along with obligations of applied policies to Context Handler, Context Handler sends XACML response context back to PEP component. The PDP uses [HERAS-AF](https://bitbucket.org/herasaf/herasaf-xacml-core/overview), an open source XACML 2.0 implementation, for XACML evaluation and uses Patient Consent Management (PCM) database as a local policy repository to retrieve XACML policies that are generated from patients’ consents.
   

## Build

### Prerequisites

+ [Oracle Java JDK 8 with Java Cryptography Extension (JCE) Unlimited Strength Jurisdiction Policy](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
+ [Docker Engine](https://docs.docker.com/engine/installation/) (for building a Docker image from the project)

### Commands

This is a Maven project and requires [Apache Maven](https://maven.apache.org/) 3.3.3 or greater to build it. It is recommended to use the *Maven Wrapper* scripts provided with this project. *Maven Wrapper* requires an internet connection to download Maven and project dependencies for the very first build.

To build the project, navigate to the folder that contains `pom.xml` file using terminal/command line.

+ To build a JAR:
    + For Windows, run `mvnw.cmd clean install`
    + For *nix systems, run `mvnw clean install`
+ To build a Docker Image (this will create an image with `bhits/context-handler:latest` tag):
    + For Windows, run `mvnw.cmd clean package docker:build`
    + For *nix systems, run `mvnw clean package docker:build`

## Run

### Prerequisites

This API uses the Patient Consent Management (PCM) database as a local policy repository to retrieve XACML policies, so it needs to have a database user configuration with read privileges to the PCM database. Please see [Configure](#configure) section for details of configuring the data source.

### Commands

This is a [Spring Boot](https://projects.spring.io/spring-boot/) project and serves the API via an embedded Tomcat instance, therefore there is no need for a separate application server to run this service.
+ Run as a JAR file: `java -jar context-handler-x.x.x-SNAPSHOT.jar <additional program arguments>`
+ Run as a Docker Container: `docker run -d bhits/context-handler:latest <additional program arguments>`

*NOTE: In order for this API to fully function as a microservice in C2S Application, it is also required to setup the dependency microservices and support level infrastructure. Please refer to the C2S Deployment Guide for instructions to setup the C2S infrastructure.*

## Configure

This API runs with some default configuration that is primarily targeted for development environment. However, [Spring Boot](https://projects.spring.io/spring-boot/) supports several methods to override the default configuration to configure the API for a certain deployment environment.

Please see the [default configuration](context-handler/src/main/resources/application.yml) for this API as a guidance and override the specific configuration per environment as needed. Also, please refer to [Spring Boot Externalized Configuration](http://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-external-config.html) documentation to see how Spring Boot applies the order to load the properties and [Spring Boot Common Properties](http://docs.spring.io/spring-boot/docs/current/reference/html/common-application-properties.html) documentation to see the common properties used by Spring Boot.

### Examples for Overriding a Configuration in Spring Boot

#### Override a Configuration Using Program Arguments While Running as a JAR:

+ `java -jar context-handler-x.x.x-SNAPSHOT.jar --server.port=80 `

#### Override a Configuration Using Program Arguments While Running as a Docker Container:

+ `docker run -d bhits/context-handler:latest --server.port=80 `

+ In a `docker-compose.yml`, this can be provided as:
```yml
version: '2'
services:
...
  context-handler.c2s.com:
    image: "bhits/context-handler:latest"
    command: ["--server.port=80"]
...
```
*NOTE: Please note that these additional arguments will be appended to the default `ENTRYPOINT` specified in the `Dockerfile` unless the `ENTRYPOINT` is overridden.*

### Enable SSL

For simplicity in development and testing environments, SSL is **NOT** enabled by default configuration. SSL can easily be enabled following the examples below:

#### Enable SSL While Running as a JAR

+ `java -jar context-handler-x.x.x-SNAPSHOT.jar --spring.profiles.active=ssl --server.ssl.key-store=/path/to/ssl_keystore.keystore --server.ssl.key-store-password=strongkeystorepassword`

#### Enable SSL While Running as a Docker Container

+ `docker run -d -v "/path/on/dockerhost/ssl_keystore.keystore:/path/to/ssl_keystore.keystore" bhits/context-handler:latest --spring.profiles.active=ssl --server.ssl.key-store=/path/to/ssl_keystore.keystore --server.ssl.key-store-password=strongkeystorepassword`
+ In a `docker-compose.yml`, this can be provided as:
```yml
version: '2'
services:
...
  context-handler.c2s.com:
    image: "bhits/context-handler:latest"
    command: ["--spring.profiles.active=ssl","--server.ssl.key-store=/path/to/ssl_keystore.keystore", "--server.ssl.key-store-password=strongkeystorepassword"]
    volumes:
      - /path/on/dockerhost/ssl_keystore.keystore:/path/to/ssl_keystore.keystore
...
```

*NOTE: As seen in the examples above, `/path/to/ssl_keystore.keystore` is made available to the container via a volume mounted from the Docker host running this container.*

### Override Java CA Certificates Store In Docker Environment

Java has a default CA Certificates Store that allows it to trust well-known certificate authorities. For development and testing purposes, one might want to trust additional self-signed certificates. In order to override the default Java CA Certificates Store in a Docker container, one can mount a custom `cacerts` file over the default one in the Docker image as `docker run -d -v "/path/on/dockerhost/to/custom/cacerts:/etc/ssl/certs/java/cacerts" bhits/context-handler:latest`

*NOTE: The `cacerts` references given in the both sides of volume mapping above are files, not directories.*

[//]: # (## API Documentation)

[//]: # (## Notes)

[//]: # (## Contribute)

## Contact

If you have any questions, comments, or concerns please see [Consent2Share]() project site.

## Report Issues

Please use [GitHub Issues](https://github.com/bhits/context-handler/issues) page to report issues.

[//]: # (License)
