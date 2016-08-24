FROM java:8
VOLUME /java
ARG finalName
ADD $finalName /contextHandler.jar
ENTRYPOINT ["java","-Djavax.net.ssl.trustStore=/java/keystore/c2s.keystore","-Djavax.net.ssl.trustStorePassword=changeit","-jar","/contextHandler.jar","--spring.profiles.active=docker"]