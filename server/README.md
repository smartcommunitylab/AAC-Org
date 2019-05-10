# AAC-Org (server)

This document explains how to set up the server side of the Organization Management Console and how to use its APIs.

## Identity provider configuration

The identity provider, AAC, needs to be configured to allow the server side of the Organization Management Console to access its information.

Access the _**Client Apps**_ menu and create an app to dedicate to OMC. Under the **redirect Web server URLs** option of the _**Settings**_ tab, add the redirect URLs for the server and client. If you’re running them on localhost, for example, add both of the following (assuming the ports are 7979 and 4200):\
`http://localhost:7979/login`\
`http://localhost:4200/login`

For **Grant types**, allow `Implicit`, `Authorization Code`, `Password` and `Client` credentials.\
Under **Enabled identity providers**, add `internal`.

In the _**API Access**_ tab, grant all permissions under `Basic profile service` and under `Role Management Service` and then save the app.

Finally, all users that will be administrators of the Organization Management Console, as well as all organization owners, will need to have the following role assigned to them: `apimanager/carbon.super:profilemanager`.\
To create the `apimanager/carbon.super` space, access AAC’s _**Space Owners menu**_, choose `apimanager` as **Parent Space**, and click on _**NEW USER**_. Insert the **Username** of the admin, then insert `carbon.super` under **New spaces** and click _**ADD**_. Click _**UPDATE**_ to create this space.\
Now that the space has been created, all users who will be administrators of OMC, or owner of organizations, need the `profilemanager` role within this space. Access the _**User Roles**_ menu, pick `apimanager/carbon.super` as **Role Context**, and then, for each user, click _**NEW USER**_, insert the **Username**, insert `profilemanager` as **New role**, click _**ADD**_ and finally click _**UPDATE**_.


## Setting up the server

The `application.yml` file contains various properties used by the server. The following is a brief explanation of the main properties and what values should be given to them. While properties in YAML are defined by indentation, this document will translate that indentation with a dot-separated notation, to keep the explanation shorter.\

Properties appear with the following format:\
`<property_name>: ${<environment_variable_name>:<default_value>}`

When the server is run, the value for the property is taken from the indicated environment variable (set by Docker), but, if the environment variable cannot be found (for example when not running with Docker), it uses the default value instead.

For example, the property for the port of the service appears as follows:\
`server:`\
&nbsp;&nbsp;`port: ${OMC_SERVER_PORT:7979}`

If you are not running the server inside a Docker container, and want to use a different port, just change the 7979 part. For more information on running the server inside a Docker container, see the _**Use with Docker**_ section.

`server.port` – The port the server is running at. Sample value: `7979`

`server.servlet.session.cookie.name` – Name of the session cookie, used to avoid conflicts with other applications that use the name JSESSIONID and may be running on the same host. Sample value: `ORMANAGERSESSIONID`

`spring.datasource.url` – Database server for the Organization Management server. The format may vary depending on the database type. A typical format can look like this: `jdbc:<database type>://<host>:<port>/<database name>`\
Sample value: `jdbc:postgresql://localhost:5432/orgmanager`

`spring.datasource.username` – Name of the user in the database

`spring.datasource.password` – Password of the user in the database

`spring.datasource.driver-class-name`: Driver for the database. Sample value: `org.postgresql.Driver`

`spring.jpa.database-platform` – Dialect for the database. Sample value: `org.hibernate.dialect.PostgreSQLDialect`

There may be more properties under `spring` related to setting up the database.

\
`security.oauth2.client.clientId` – Client ID of the Organization Management Application in the identity provider.

`security.oauth2.client.clientSecret` – Client secret of the Organization Management Application in the identity provider.

`security.oauth2.client.accessTokenUri` – URI for obtaining the access token

`security.oauth2.client.userAuthorizationUri` – URI to obtain authorization by the identity provider

`security.oauth2.client.tokenInfoUri` – URI to inspect the contents of the token

`security.oauth2.client.tokenName` – Name used by the identity provider to identify the access token

`security.oauth2.client.userIdField` – Name used by the identity provider for the field of the token that contains the ID of the user

`security.oauth2.client.organizationManagementScope` – Identifier for the organization management scope, which grants administrator privileges

`security.oauth2.client.organizationManagementContext` – The AAC context within which component contexts are nested. Having the `ROLE_PROVIDER` role within this context also grants administrator privileges.

`security.oauth2.resource.userInfoUri` - scope for basic profile information

`aac.uri`: AAC host

`aac.apis.manageRolesUri` - AAC API end-point for managing user roles

`aac.apis.userProfilesUri` - AAC API end-point for retrieving profile information, used to associate user name with ID

`aac.apis.currentUserRolesApi` - AAC API end-point for retrieving the authenticated user’s roles

`aac.apis.currentUserProfileApi` - AAC API end-point for retrieving the authenticated user’s profile

\
The `COMPONENTS DATA` section of the file contains the configuration for all components. Two fields are required for each of them:

`componentId` - identifies the component.

`implementation` - full name of the connector class that takes care of reflecting the Organization Management Console’s changes on the component. The following value corresponds to a dummy class that brings no changes, which can be used if the component does not need an external class for this purpose, or to simply disable a connector.\
`it.smartcommunitylab.orgmanager.componentsmodel.DefaultComponentImpl`

\
Other fields are described in the documentation of the specific component’s connector.


## Calling APIs


## Use with Docker

Will be updated once the API Manager connector is configured for running with Docker.
