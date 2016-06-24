FROM java:8
VOLUME /java
ARG finalName
ADD $finalName /context-handler.jar
ENTRYPOINT ["java","-Djavax.net.ssl.trustStore=/java/keystore/mhc.keystore","-Djavax.net.ssl.trustStorePassword=changeit","-jar","/context-handler.jar","--spring.profiles.active=docker"]