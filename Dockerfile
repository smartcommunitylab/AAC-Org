FROM node:12-alpine as node
COPY ./admin /app
WORKDIR /app
RUN npm install && npm run build

FROM maven:3.3-jdk-8 AS mvn
COPY ./server /server
WORKDIR /server
COPY --from=node /app/build/ /server/src/main/resources/static/
RUN mvn package -DskipTests

FROM openjdk:8-jdk-alpine
#ENV FOLDER=/tmp/server/target
ENV APP=orgmanager-current.jar
ARG USER=aac-org
ARG USER_ID=1002
ARG USER_GROUP=aac-org
ARG USER_GROUP_ID=1002
ARG USER_HOME=/home/${USER}

RUN  addgroup -g ${USER_GROUP_ID} ${USER_GROUP}; \
     adduser -u ${USER_ID} -D -g '' -h ${USER_HOME} -G ${USER_GROUP} ${USER} ;

WORKDIR  ${USER_HOME}
COPY --chown=aac-org:aac-org --from=mvn /tmp/target/*.jar ${USER_HOME}/${APP}
USER aac-org
EXPOSE 7979
ENTRYPOINT java -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -jar ${APP}
