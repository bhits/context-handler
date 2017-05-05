# Short Description
This API sends XACML response having authorization decisions with applied policy obligations to PEP.

# Full Description

# Supported Source Code Tags and Current `Dockerfile` Link

[`2.0.0(latest)`](https://github.com/bhits/context-handler/releases/tag/2.0.0),
 [`1.11.0`](https://github.com/bhits/context-handler/releases/tag/1.11.0), [`1.8.0`](https://github.com/bhits/context-handler/releases/tag/1.8.0)

[`Current Dockerfile`](https://github.com/bhits/context-handler/blob/master/context-handler/src/main/docker/Dockerfile)

For more information about this image, the source code, and its history, please see the [GitHub repository](https://github.com/bhits/context-handler).

# What is Context Handler?

The Context Handler API is a RESTful web service component of Consent2Share (C2S). It is responsible for sending XACML response context that includes authorization decisions along with applied policy obligations to Policy Enforcement Point (PEP) API components. The Context Handler sends the XACML request context to the Policy Decision Point (PDP). The PDP evaluates the applicable policies either from the Patient Consent Management (PCM) or a Fast Healthcare Interoperability Resource (FHIR) server against the request context, and returns the response context that includes authorization decisions along with obligations of applied policies to the Context Handler. The Context Handler sends XACML response context back to PEP component. The PDP uses [HERAS-AF](https://bitbucket.org/herasaf/herasaf-xacml-core/overview), an open source XACML 2.0 implementation, for XACML evaluation and uses either a PCM database as a local policy repository or a FHIR server to retrieve XACML policies that are generated from patients’ consents.

For more information and related downloads for Consent2Share, please visit [Consent2Share](https://bhits.github.io/consent2share/).
# How to use this image


## Start a Context Handler instance

Be sure to familiarize yourself with the repository's [README.md](https://github.com/bhits/context-handler) file before starting the instance.

`docker run  --name context-handler -d bhits/context-handler:latest <additional program arguments>`

*NOTE: In order for this API to fully function as a microservice in the Consent2Share application, it is required to setup the dependency microservices and the support level infrastructure. Please refer to the Consent2Share Deployment Guide in the corresponding Consent2Share release (see [Consent2Share Releases Page](https://github.com/bhits/consent2share/releases)) for instructions to setup the Consent2Share infrastructure.*


## Configure

The Spring profiles `application-default` and `docker` are activated by default when building images.

This API can run with the default configuration which is from three places: `bootstrap.yml`, `application.yml`, and the data which the [`Configuration Server`](https://github.com/bhits/config-server) reads from the `Configuration Data Git Repository`. Both `bootstrap.yml` and `application.yml` files are located in the class path of the running application.

We **recommend** overriding the configuration as needed in the `Configuration Data Git Repository`, which is used by the `Configuration Server`.

Also, [Spring Boot](https://projects.spring.io/spring-boot/) supports other ways to override the default configuration to configure the API for a certain deployment environment. 

The following is an example to override the default database password:

`docker run -d bhits/context-handler:latest --spring.datasource.password=strongpassword`

## Environment Variables

When you start the Context Handler image, you can edit the configuration of the Context Handler instance by passing one or more environment variables on the command line. 

### JAR_FILE

This environment variable is used to setup which jar file will run. you need mount the jar file to the root of container.

`docker run --name context-handler -e JAR_FILE="context-handler-latest.jar" -v "/path/on/dockerhost/context-handler-latest.jar:/context-handler-latest.jar" -d bhits/context-handler:latest`

### JAVA_OPTS 

This environment variable is used to setup JVM argument, such as memory configuration.

`docker run --name context-handler -e "JAVA_OPTS=-Xms512m -Xmx700m -Xss1m" -d bhits/context-handler:latest`

### DEFAULT_PROGRAM_ARGS 

This environment variable is used to setup an application argument. The default value is "--spring.profiles.active=application-default, docker".

`docker run --name context-handler -e DEFAULT_PROGRAM_ARGS="--spring.profiles.active=application-default,ssl,docker" -d bhits/context-handler:latest`

# Supported Docker versions

This image is officially supported on Docker version 1.12.1.

Support for older versions (down to 1.6) is provided on a best-effort basis.

Please see the [Docker installation documentation](https://docs.docker.com/engine/installation/) for details on how to upgrade your Docker daemon.

# License

View [license](https://github.com/bhits/context-handler/blob/master/LICENSE) information for the software contained in this image.

# User Feedback

## Documentation 

Documentation for this image is stored in the [bhits/context-handler](https://github.com/bhits/context-handler) GitHub repository. Be sure to familiarize yourself with the repository's README.md file before attempting a pull request.

## Issues

If you have any problems with or questions about this image, please contact us through a [GitHub issue](https://github.com/bhits/context-handler/issues).