FROM maven:3.3-jdk-8 AS mvn
COPY . /tmp
WORKDIR /tmp
RUN cd /tmp/model/ && mvn clean install -DskipTests && \
    cd /tmp/connectors/orgmanager-wso2connector && mvn clean install -DskipTests && \
    cd /tmp/connectors/nificonnector && mvn clean install -DskipTests && \
    cd /tmp/server/ && mvn clean install -DskipTests

FROM openjdk:8-jdk-alpine
ENV FOLDER=/tmp/server/target
ENV APP=orgmanager-1.0.0.jar
ARG USER=aac-org
ARG USER_ID=1002
ARG USER_GROUP=aac-org
ARG USER_GROUP_ID=1002
ARG USER_HOME=/home/${USER}

RUN  addgroup -g ${USER_GROUP_ID} ${USER_GROUP}; \
     adduser -u ${USER_ID} -D -g '' -h ${USER_HOME} -G ${USER_GROUP} ${USER} ;

WORKDIR  /tmp/server/target
RUN chown ${USER}:${USER_GROUP} /tmp/server/target
RUN apk update && apk add curl openssl && rm -rf /var/cache/apk/*
COPY --chown=aac-org:aac-org ./init.sh /tmp/server/target/init.sh
#COPY ./app.jar /tmp/server/target/app.jar
COPY --from=mvn --chown=aac-org:aac-org ${FOLDER}/${APP} /tmp/server/target/app.jar

USER aac-org
ENTRYPOINT ["./init.sh"]
