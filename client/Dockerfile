FROM node:alpine
COPY . /tmp
WORKDIR /tmp
RUN npm install && npm install -g @angular/cli
EXPOSE 4200
ENTRYPOINT ng serve --host 0.0.0.0 --disable-host-check
