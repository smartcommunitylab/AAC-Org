# AAC-Org (client)

This project is AAC Organization Management application console.

## Install the Application

* Install `Node.js`.

* Run `npm install` to install app dependencies.

* Run `npm install -g @angular/cli` to install angular CLI.

* `Clone` or download this project and go to client folder.

## Configuration

You have to update some values in the enviromant file:

* `aacUrl` by default it is `http://localhost:8080/aac/`

* `aacClientId` this is the client ID of AAC.

* `redirectUrl` by default it is `http://localhost:4200/`

* `scope` by default it is `profile.basicprofile.me,user.roles.me`

* `locUrl` by default it is `http://localhost:7979/api/`


## Development server

Run `ng serve` for a dev server. Navigate to `http://localhost:4200/`. The app will automatically reload if you change any of the source files.

## Build

Run `ng build` to build the project. The build artifacts will be stored in the `dist/` directory. Use the `--prod` flag for a production build.
