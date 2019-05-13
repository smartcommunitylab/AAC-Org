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
&nbsp; &nbsp;`port: ${OMC_SERVER_PORT:7979}`

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

This section describes the available APIs. Make sure to include the **Authorization** header containing `Bearer <token value>` in every request. In case an error occurs, each API will return a response with an appropriate status code, usually with some details in the response body.

The **Swagger UI** for the Org-Manager API is available at **http://localhost:7979/swagger-ui.html**.

Most APIs have security restrictions that require the user to be owner of the organization they are attempting to alter, or to have administrator privileges.
The owner of a specific organization is defined as a user with the following role in AAC: `components/<organization_slug>:ROLE_PROVIDER`\
A user has administrator privileges when the access token they are using is a client token with the `organization.mgmt` scope, or when they have the following role in AAC:
`organizations:ROLE_PROVIDER`

Also keep in mind that, for some of these APIs to work correctly, the access token used needs to have the following scopes:\
`profile`, `email`, `user.roles.me`, `profile.basicprofile.me`, `profile.accountprofile.me`

### Creating an organization
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

**Sample request body**:\
`{`\
&nbsp; &nbsp;`"name":"My Organization",`\
&nbsp; &nbsp;`"slug":"my_org",`\
&nbsp; &nbsp;`"description":"This is my test organization.",`\
&nbsp; &nbsp;`"contacts": {`\
&nbsp; &nbsp; &nbsp; &nbsp;`"email":"jsmith@my_org.com ",`\
&nbsp; &nbsp; &nbsp; &nbsp;`"name":"John",`\
&nbsp; &nbsp; &nbsp; &nbsp;`"surname":"Smith",`\
&nbsp; &nbsp; &nbsp; &nbsp;`"web":"http://www.example.com",`\
&nbsp; &nbsp; &nbsp; &nbsp;`"phone":["12345","67890"],`\
&nbsp; &nbsp; &nbsp; &nbsp;`"logo":"http://www.example.com/images/logo.png"`\
&nbsp; &nbsp;`},`\
&nbsp; &nbsp;`"tag":["test","testing"],`\
&nbsp; &nbsp;`"active":"true"`\
`}`

### Search organizations
API for searching organizations. Organizations are searched by name. Responses are returned as pages of size 20. Useful to find the `id` of an organization when only the `name` is known.
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
**Sample request URL**: `http://localhost:7979/api/organizations/1/info`\
**Sample request body**:\
`{`\
&nbsp; &nbsp;`"description":"New description.",`\
&nbsp; &nbsp;`"contacts": {`\
&nbsp; &nbsp; &nbsp; &nbsp;`"web":"http://www.test.com",`\
&nbsp; &nbsp; &nbsp; &nbsp;`"phone":["12345","57575"]`\
&nbsp; &nbsp;`},`\
&nbsp; &nbsp;`"tag":["testing"]`\
`}`

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
Deletes an organization. Also unregisters all members belonging to it, deletes all their roles within it, and deletes all tenants within it.

**Requirements**: must have administrator privileges\
**End-point**: /api/organizations/<organization_id>\
**Method**: DELETE\
**Sample request URL**: `http://localhost:7979/api/organizations/1`

### Listing available components
Lists available components, together with a few properties for each of them.

**End-point**: /api/components\
**Method**: GET

### List possible roles for a component
Returns a list of strings, representing what roles may be attributed to a user when added to a tenant within a specific component.

**End-point**: /api/components/<component_id>/roles\
**Method**: GET\
**Sample request URL**: `http://localhost:7979/api/components/nifi/roles`

### Configuring tenants for an organization
Allows configuring which tenants an organization should have.

**Requirements**: must have administrator privileges\
**End-point**: /api/organizations/<organization_id>/configuration\
**Method**: POST\
**Body**: JSON object containing components and tenants for each component.
1. `componentId` – Identifies the component. Must be chosen among the values that can be found by calling the **Listing available components** API. Note that if a component is not specified in the body, it will not be altered.
2. `tenants` – Array of strings for the tenants. If a component previously contained tenants that are not present in this new array, those tenants will be removed.

**Sample request body**:\
`[`\
&nbsp; &nbsp;`{`\
&nbsp; &nbsp; &nbsp; &nbsp;`"componentId":"nifi",`\
&nbsp; &nbsp; &nbsp; &nbsp;`"tenants":[`\
&nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;`"trento",`\
&nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;`"ferrara"`\
&nbsp; &nbsp; &nbsp; &nbsp;`]`\
&nbsp; &nbsp;`},{`\
&nbsp; &nbsp; &nbsp; &nbsp;`"componentId":"dss",`\
&nbsp; &nbsp; &nbsp; &nbsp;`"tenants":[`\
&nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;`"reggio"`\
&nbsp; &nbsp; &nbsp; &nbsp;`]`\
&nbsp; &nbsp;`}`\
`]`

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
Grants a user the roles listed in the request body. All roles they previously had within the organization, but that are not present in this new configuration, will be removed. The user will be added to the organization, in case they were previously not registered. This means that AAC must have a user with this username, otherwise an error will occur. The response will also contain the `id` of the member within the organization.

**Requirements**: must have administrator privileges, or be owner of the organization\
**End-point**: /api/organizations/<organization_id>/members\
**Method**: POST\
**Body**: JSON object containing the user’s name and the roles they should have:
1. `username` – Name of the user to add. Must be a valid name recognized by the identity provider.
2. `roles` – Array of JSON objects representing the roles to add. Each role has 2 properties:
   - `contextSpace` – domain of the role. It must be one of the domains registered in the organization. It should have the following structure: `components/<component_id>/<space name>`
   - `role` – Role of the user in the domain
  
**Sample request URL**: `http://localhost:7979/api/organizations/1/members`\
**Sample request body**:\
`{`\
&nbsp; &nbsp;`"username":"bob@test.com",`\
&nbsp; &nbsp;`"roles": [{`\
&nbsp; &nbsp; &nbsp; &nbsp;`"contextSpace":"components/nifi/trento",`\
&nbsp; &nbsp; &nbsp; &nbsp;`"role":"ROLE_MANAGER"`\
&nbsp; &nbsp;`},{`\
&nbsp; &nbsp; &nbsp; &nbsp;`"contextSpace":"components/nifi/ferrara",`\
&nbsp; &nbsp; &nbsp; &nbsp;`"role":"ROLE_USER"`\
&nbsp; &nbsp;`}]`\
`}`

### Remove a user from an organization
Unregisters a user from an organization, stripping them of all roles they had within it. The `id` of the organization, as well as the `id` of the member to remove, must be known.

**Requirements**: must have administrator privileges, or be the owner of the organization\
**End-point**: /api/organizations/<organization_id>/members/<member_id>\
**Method**: DELETE\
**Sample request URL**: `http://localhost:7979/api/organizations/1/members/2`

### Add an owner to an organization
Adds an owner to an organization. If the new owner is not an existing user, they will be created. This means that AAC must have a user with this username, otherwise an error will occur. The response will contain the new owner.

**Requirements**: must have administrator privileges\
**End-point**: /api/organizations/<organization_id>/owners\
**Method**: POST\
**Sample request URL**: `http://localhost:7979/api/organizations/1/owners`\
**Sample request body**:\
`{`\
&nbsp; &nbsp;`"username":"fred@test.com"`\
`}`

### Remove an owner from an organization
Removes an owner from an organization. The `id` of the owner must be known.

**Requirements**: must have administrator privileges\
**End-point**: /api/organizations/<organization_id>/owners/<owner_id>\
**Method**: DELETE\
**Sample request URL**: `http://localhost:7979/api/organizations/1/owners/2`


## Use with Docker

Will be updated once the API Manager connector is configured for running with Docker.
