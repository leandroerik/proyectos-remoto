# 1) COMPILO EL BACKEND A .JAR
FROM registry.access.redhat.com/ubi9/openjdk-17 AS build
USER root
WORKDIR /app
COPY ./docker/settings.xml /home/default/.m2/settings.xml
COPY ./docker/settings.xml /etc/maven/settings.xml
COPY ./backend/ .
RUN mvn clean install

# 2) EJECUTO EL .JAR
FROM registry.access.redhat.com/ubi9/openjdk-17-runtime:1.17
USER root
WORKDIR /app

ENV TZ=America/Argentina/Buenos_Aires
RUN microdnf upgrade tzdata -y

RUN update-crypto-policies --set LEGACY
RUN microdnf install procps vi iputils less openssl wget -y

COPY docker/fonts /usr/share/fonts/custom
RUN microdnf install fontconfig -y
RUN fc-cache -f /usr/share/fonts/custom

RUN microdnf update tzdata -y
RUN wget -O opentelemetry-javaagent.jar https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/latest/download/opentelemetry-javaagent.jar

COPY .git .git
COPY --from=build /app/target/backend-1.0.0-jar-with-dependencies.jar backend.jar
RUN date '+%d/%m/%Y %H:%M:%S' > .git/BUILD_TIME

RUN chmod -R 777 /app
USER 1001
EXPOSE 8080
CMD [ "sh", "-c", "java $JVM_PARAMS \
      -Xlog:gc:/tmp/gc.log \
      -Dfile.encoding=UTF8 \
      -Djavax.net.ssl.trustStore=/cert/cacerts \
      -Djavax.net.ssl.trustStorePassword=changeit \
      -Djava.security.disableSystemPropertiesFile=true \
      -Xms${JAVA_XMS:-64m} \
      -Xmx${JAVA_XMX:-3072m} \
      -XX:+UseG1GC \
      -jar backend.jar" ]

# === COMANDOS ===
# CMD [ "tail", "-f", "/dev/null" ]
# docker rm -f backend && docker build . -t backend && docker run -p 8080:8080 --name backend backend
# docker rm -f backend && docker build . -t backend --no-cache && docker run -p 8080:8080 --name backend backend
# docker exec -it backend /bin/bash