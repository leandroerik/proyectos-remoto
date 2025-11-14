FROM registry.access.redhat.com/ubi9/openjdk-17
USER root
WORKDIR /app
COPY pom.xml .
RUN mvn clean package && rm -rf target && rm pom.xml
#CMD [ "tail", "-f", "/dev/null" ]

# === COMANDOS ===
# docker rm -f bacuni && docker build . -t bacuni && docker run --name bacuni bacuni
# docker exec -it bacuni /bin/bash

# docker build . -t bhbuild/bacuni
# docker push bhbuild/bacuni