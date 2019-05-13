FROM maven:3.3-jdk-8 AS mvn
COPY . /tmp
WORKDIR /tmp
RUN cd /tmp/model/orgmanager-componentsmodel && mvn clean install && \
    cd /tmp/connectors/orgmanager-apimconnector && mvn clean install && \
    cd /tmp/connectors/nificonnector && mvn clean install && \
    cd /tmp/server/ && mvn clean install

FROM openjdk:8-jdk-alpine
ENV FOLDER=/tmp/server/target
ENV APP=orgmanager-1.0.0.jar
WORKDIR  /tmp/server/target
COPY --from=mvn ${FOLDER}/${APP} /tmp/server/target/app.jar
ENTRYPOINT ["java","-jar","app.jar"]
