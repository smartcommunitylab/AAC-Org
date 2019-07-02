# Server

This document explains how to set up the server side of Org-Manager and how to use its APIs.

## Configuring the AAC identity provider

Org-Manager requires the [AAC](https://github.com/smartcommunitylab/AAC) identity provider to work. The repository explains how to install and configure it.

Once AAC is running, create a new app for Org-Manager by accessing the **Client Apps** menu and clicking on **NEW APP**.

In the **Settings** tab, under **redirect Web server URLs**, add the redirect URLs for server and client. If you’re running them on _localhost_, for example, add both of the following (assuming the ports are _7979_ and _4200_):\
`http://localhost:7979/login`\
`http://localhost:4200/login`

To run the server within a Docker container, you need to add a third URL with the port (for example _7878_) Docker will expose the service through:\
`http://localhost:7878/login`

For more information on running the server inside a Docker container, see the [Running with Docker](#running-with-docker) section.

For **Grant types**, check `Implicit` and `Client credentials`. For **Enabled identity providers**, check `internal`.

In the **API Access** tab, grant all permissions under `Basic profile service` and under `Role Management Service` and save the app.

Finally, all users that will be administrators of Org-Manager, as well as all organization owners, need the following roles:\
`apimanager/carbon.super:profilemanager`.\
`organizations:ROLE_PROVIDER`.

To create the `apimanager/carbon.super` space, access the **Space Owners** menu, choose `apimanager` as **Parent Space** and click on **NEW USER**. Insert the **Username**, insert `carbon.super` under **New spaces** and click **ADD**. Click **UPDATE** to create this space.

Now that the space has been created, all users who will be administrators of Org-Manager, or owners of an organization, need the `profilemanager` role within this space.

Access the **User Roles** menu, pick `apimanager/carbon.super` as **Role Context**, and then, for each user, click **NEW USER**, insert the **Username**, insert `profilemanager` as **New role**, click **ADD** and then **UPDATE**.

If your AAC installation does not already have an `organizations` space, it might be necessary to create it manually into the database, because it is not supposed to have a parent space and the UI may not allow the **Parent Space** field to be blank.\
Open a SQL tool and connect it to AAC's database, then execute the following query:\
`INSERT INTO space_role (id, context, role, space, USER_ID) VALUES (<role_id>, null, 'ROLE_PROVIDER', 'organizations', <admin_id>);`

For `role_id`, pick any number that has not been used. For `admin_id`, pick the ID of the administrator user. If you don't know it, you can obtain it with the following query:\
`SELECT id FROM user WHERE username=<admin_username>;`

Since the `organizations` space has now been created, you can assign the `ROLE_PROVIDER` role to other administrator users in the same way as you did with the `profilemanager` role.

## Setting up the server

The `application.yml` file contains various properties used by the server. The following is a brief explanation of the main properties and what values should be given to them. While properties in YAML are defined by indentation, this document will translate that indentation with a dot-separated notation, to keep the explanation shorter.

Properties appear with the following format:\
`<property_name>: ${<environment_variable_name>:<default_value>}`

When the server is run, the value for the property is taken from the indicated environment variable (set by Docker), but, if the environment variable cannot be found (for example when not running with Docker), it uses the default value instead.

For example, the property for the port of the service appears as follows:

	server:
	  port: ${OMC_SERVER_PORT:7979}

If you are not running the server inside a Docker container, and want to use a different port, just change the `7979` part. For more information on running the server inside a Docker container, see the [Running with Docker](#running-with-docker) section.

`server.port` – The port the server is running at. Sample value: `7979`

`server.servlet.session.cookie.name` – Name of the session cookie, used to avoid conflicts with other applications that use the name _JSESSIONID_ and may be running on the same host. Sample value: `ORGMANAGERSESSIONID`

`spring.datasource.url` – Database server for the Org-Manager server. The format may vary depending on the database type. A typical format can look like this: `jdbc:<database type>://<host>:<port>/<database name>`\
Sample value: `jdbc:postgresql://localhost:5432/orgmanager`

`spring.datasource.username` – Name of the user in the database

`spring.datasource.password` – Password of the user in the database

`spring.datasource.driver-class-name`: Driver for the database. Sample value: `org.postgresql.Driver`

`spring.jpa.database-platform` – Dialect for the database. Sample value: `org.hibernate.dialect.PostgreSQLDialect`

There may be more properties under `spring` related to setting up the database.

\
`security.oauth2.client.clientId` – Client ID for Org-Manager in the identity provider.

`security.oauth2.client.clientSecret` – Client secret for Org-Manager in the identity provider.

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


## Calling APIs

This section describes the available APIs. Make sure to include the **Authorization** header containing `Bearer <token value>` in every request. In case an error occurs, each API will return a response with an appropriate status code, usually with some details in the response body.

Assuming the server is being hosted on _localhost_ at port _7979_, the **Swagger UI** for the Org-Manager APIs is available at **http://localhost:7979/swagger-ui.html**.

Most APIs have security restrictions that require the user to be owner of the organization they are attempting to alter, or to have administrator privileges.
The owner of a specific organization is defined as a user with the following role in AAC: `components/<organization_slug>:ROLE_PROVIDER`\
A user has administrator privileges when the access token they are using is a client token with the `organization.mgmt` scope, or when they have the following role in AAC:
`organizations:ROLE_PROVIDER`

Also keep in mind that, for some of these APIs to work correctly, the access token used must have the following scopes: `profile`, `email`, `user.roles.me`, `profile.basicprofile.me`, `profile.accountprofile.me`.

### Create organization
This API is used to create an organization. Its response contains a JSON object that represents the newly created organization. This response will contain an additional field, `id`, necessary to recognize the organization and useful when calling other APIs.\
Note that, since the `email` field will be interpreted as the starting owner of the organization, a user with its value as name will be created on the server side. This means that AAC must have a user with this username, otherwise an error will occur.\
The `name` and `surname` fields inside the `contacts` object also must match the corresponding fields in AAC.

**Requirements**: must have administrator privileges\
**End-point**: /api/organizations\
**Method**: POST\
**Body**: JSON object describing the organization. The following fields can be defined:
1. `name` – Name of the organization. Required. May contain alphanumeric characters, space, dash (-) or underscore (_). Cannot have the same name as an already existing organization, even if case is different. Any leading or trailing spaces will be ignored, and multiple consecutive spaces will be replaced with a single space.
2. `slug` – Defines the domain of the organization. Optional: if specified, it can only contain alphanumeric lower case characters and underscores. If left out, it will be generated from the name, converting it to lower case and replacing dashes and spaces with underscores.
3. `description` – Description of the organization. Required.
4. `contacts` – Inner JSON object describing the contacts. Required. Its 5 inner properties are:
   - `email` – E-mail. Required. Will be used as name of the owner of the organization.
   - `name` – Name of the contact. Required.
   - `surname` – Surname. Required.
   - `web` – URL. Optional.
   - `phone` – Array of strings for phone numbers. Optional.
   - `logo` – URL. Optional.
   - `tag` – Array of strings for tags. Optional.
   - `active` – Can take `true` or `false` as values. Indicates whether the organization is enabled or disabled. Optional, will default to `true` if omitted.

**Sample request body**:

	{
	  "name":"My Organization",
	  "slug":"my_org",
	  "description":"This is my test organization.",
	  "contacts": {
	    "email":"jsmith@my_org.com ",
	    "name":"John",
	    "surname":"Smith",
	    "web":"http://www.example.com",
	    "phone":["12345","67890"],
	    "logo":"http://www.example.com/images/logo.png"
	  },
	  "tag":["test","testing"],
	  "active":"true"
	}

### Search organizations
API for searching organizations. Organizations are searched by name. Responses are returned as pages of size 20.

If the authenticated user has administrator privileges, they will see all organizations, otherwise they will see only organizations they are part of.

**End-point**: /api/organizations\
**Method**: GET\
**Parameters**:
1.	`name` – Name to search. Case insensitive. All organizations with a name that contains this parameter will be returned.
2.	`page` – Page to be returned. Can be omitted, since most of the time the organizations returned will be less than 20. Starts from 0, so if you want the second page, use `page=1`.

**Sample request URL**: `http://localhost:7979/api/organizations?name=Company&page=3`

### Update organization
Updates an organization. Only certain fields may be updated. The `id` of the organization must be known, and used in the request URL.

**Requirements**: must have the administrator privileges, or be owner of the organization.\
**End-point**: /api/organizations/<organization_id>/info\
**Method**: PUT\
**Body**: JSON with the fields to change. Only description, contacts and tags may be changed; any other field present in the request will be ignored. Fields will only be updated if present in the input, so if you do not want to change a field, simply omit it from the request.\
**Sample request URL**: `http://localhost:7979/api/organizations/1/info`

**Sample request body**:

	{
	  "description":"New description.",
	  "contacts": {
	    "web":"http://www.test.com",
	    "phone":["12345","57575"]
	  },
	  "tag":["testing"]
	}

### Enable organization
Enables an organization. Simply changes the `active` field to `true`.

**Requirements**: must have administrator privileges\
**End-point**: /api/organizations/<organization_id>/enable\
**Method**: PUT\
**Sample request URL**: `http://localhost:7979/api/organizations/3/enable`

### Disable organization
Disables an organization. Simply changes the `active` field to `false`. Other than the endpoint, it is identical to the **Enable organization** API.

**Requirements**: must have administrator privileges\
**End-point**: /api/organizations/<organization_id>/disable\
**Method**: PUT\
**Sample request URL**: `http://localhost:7979/api/organizations/3/disable`

### Delete organization
Deletes an organization. Also unregisters all members belonging to it, deletes all their roles within it, and deletes all tenants within it. An organization must be disabled before it can be deleted.

**Requirements**: must have administrator privileges\
**End-point**: /api/organizations/<organization_id>\
**Method**: DELETE\
**Sample request URL**: `http://localhost:7979/api/organizations/1`

### List available components
Lists available components, together with a few properties for each of them.

**End-point**: /api/components\
**Method**: GET

### List possible roles for a component
Returns a list of strings, representing what roles may be attributed to a user when added to a tenant within a specific component.

**End-point**: /api/components/<component_id>/roles\
**Method**: GET\
**Sample request URL**: `http://localhost:7979/api/components/nifi/roles`

### Configure tenants for an organization
Allows configuring which tenants an organization should have.

**Requirements**: must have administrator privileges\
**End-point**: /api/organizations/<organization_id>/configuration\
**Method**: POST\
**Body**: JSON object containing components and tenants for each component.
1. `componentId` – Identifies the component. Must be chosen among the values that can be found by calling the **Listing available components** API. Note that if a component is not specified in the body, it will not be altered.
2. `tenants` – Array of strings for the tenants. If a component previously contained tenants that are not present in this new array, those tenants will be removed.

**Sample request body**:

	[
	  {
	    "componentId":"nifi",
	    "tenants":[
	      "trento",
	      "ferrara"
	    ]
	  },{
	    "componentId":"dss",
	    "tenants":[
	      "reggio"
	    ]
	  }
	]

Note that only tenants for the `nifi` and `dss` components will be affected, as no other components are present in the input. For example, tenants for the component `apimanager` will not be altered, since `apimanager` was not specified in the body.

### Display tenants of the organization
Displays the tenants that have been configured for the input organization.

**Requirements**: must have administrator privileges, or be the owner of the organization\
**End-point**: /api/organizations/<organization_id>/configuration\
**Method**: GET\
**Sample request URL**: `http://localhost:7979/api/organizations/1/configuration`

### List users in an organization
Lists users that belong to the indicated organization. The `id` of the organization must be known. An optional parameter may be specified to act as a filter on the desired users’ names.

**Requirements**: must have administrator privileges, or be the owner of the organization\
**End-point**: /api/organizations/<organization_id>/members\
**Method**: GET\
**Parameters**:
1.	`username`: If specified, only members whose user name contains this value (case insensitive) will be returned. If omitted, all members of the organization will be returned.

**Sample request URL**: `http://localhost:7979/api/organizations/1/members?username=john`

### Add a user to an organization
Grants a user the roles listed in the request body. All roles they previously had within the organization, but that are not present in this new configuration, will be removed. The user will be added to the organization, in case they were previously not registered. This means that AAC must have a user with this username, otherwise an error will occur. The response will also contain the `id` of the member within the organization.\
It is also possible, for administrators only, to grant/revoke the status of owner of the organization through this API.

**Requirements**: must have administrator privileges, or be owner of the organization (cannot grant/revoke owner status)\
**End-point**: /api/organizations/<organization_id>/members\
**Method**: POST\
**Body**: JSON object containing the user’s name and the roles they should have:
1. `username` – Name of the user to add. Must be a valid name recognized by the identity provider.
2. `roles` – Array of JSON objects representing the roles to add. Each role has 2 properties:
   - `contextSpace` – domain of the role. It must be one of the domains registered in the organization. It should have the following structure: `components/<component_id>/<space name>`
   - `role` – Role of the user in the domain
3. `owner` - Boolean parameter that can only be set by administrators. If this parameter appears in a call performed without administrator rights, it will be ignored.
  
**Sample request URL**: `http://localhost:7979/api/organizations/1/members`

**Sample request body**:

	{
	  "username":"bob@test.com",
	  "roles": [{
	    "contextSpace":"components/nifi/trento",
	    "role":"ROLE_MANAGER"
	  },{
	    "contextSpace":"components/nifi/ferrara",
	    "role":"ROLE_USER"
	  }],
	  "owner":"true"
	}

### Remove a user from an organization
Unregisters a user from an organization, stripping them of all roles they had within it. The `id` of the organization, as well as the `id` of the member to remove, must be known.

**Requirements**: must have administrator privileges, or be the owner of the organization\
**End-point**: /api/organizations/<organization_id>/members/<member_id>\
**Method**: DELETE\
**Sample request URL**: `http://localhost:7979/api/organizations/1/members/2`

## Running with Docker

The server contains some default configuration that, when running with Docker, cannot be changed without recompiling the whole project.\
To avoid this, two files are necessary for Docker to override this default configuration.

The **first** one is an _env_ file: when Docker runs the container, it will use this file to create several environment variables that the server will read to configure itself.\
The _env_ file to alter is [dockerfiles/orgmanager-config.env](/dockerfiles/orgmanager-config.env), which contains a sample configuration.

Variables appear with the `<NAME>:<value>` format. The uppercase part matches its name as described in the [Setting up the server](#setting-up-the-server) section, while on the right is the value to assign.\
Make sure the `OMC_SECURITY_OAUTH2_CLIENTID` and `OMC_SECURITY_OAUTH2_CLIENTSECRET` variables respectively contain the client ID and secret generated by AAC for the server.\
In addition, replace _host:port_ addresses for the Postgres database and AAC appropriately.

The **second** file will contain the configuration for the components, such as API Manager or Apache NiFi. Unlike the previous file, which creates environment variables for the server to retrieve values from, this one simply replaces a default configuration file.
The file must be in _yml_ format and its structure is identical to the default [application-component.yml](/server/src/main/resources/application-components.yml) file.\
A sample configuration is present in [dockerfiles/application-components.yml](/dockerfiles/application-components.yml).
Replace _host:port_ values with the addresses of the services.

Once you have configured these two files and Docker is running, open a console and change directory to the root folder (`AAC-Org`) of the project and execute this command to build a Docker image:\
`docker build -t orgmanager .`\
This command will take some time to compile the whole project and will create an image named _orgmanager_. If you wish to name it something else, simply replace `orgmanager` with the name you wish to use.\
Note that the final dot of the command, separated by a space, is important: without it, an error will be returned.

All that remains is to run the container using this image. The following command will run the server inside a Docker container, mounting the two configuration files described earlier.

	docker run --env-file dockerfiles/orgmanager-config.env -v <absolute_path_to_project>/dockerfiles/application-components.yml:/tmp/server/target/config/application-components.yml -p 7878:7979 -t orgmanager

Note that you need to replace `<absolute_path_to_project>` with the full path to this project. If you're running it on Windows, the command would look similar to this:

	docker run --env-file dockerfiles/orgmanager-config.env -v //c/Eclipse/Workspace/AAC-Org/dockerfiles/application-components.yml:/tmp/server/target/config/application-components.yml -p 7878:7979 -t orgmanager

If you have configured the NiFi connector to run, you need to provide the [certificates you created](https://github.com/smartcommunitylab/AAC-Org/tree/master/connectors/nificonnector#certificates) for it:

	docker run --env-file dockerfiles/orgmanager-config.env -v <absolute_path_to_certificates_folder>:/certs -v <absolute_path_to_prject>/dockerfiles/application-components.yml:/tmp/server/target/config/application-components.yml -p 7878:7979 -t orgmanager

The command with the NiFi certificates might look like the following:

	docker run --env-file dockerfiles/orgmanager-config.env -v //c/Eclipse/Workspace/AAC-Org/dockerfiles/certs:/certs -v //c/Eclipse/Workspace/AAC-Org/dockerfiles/application-components.yml:/tmp/server/target/config/application-components.yml -p 7878:7979 -t orgmanager
