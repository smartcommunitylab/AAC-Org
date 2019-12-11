

# Organization Manager

Organization Manager complements AAC with the functionality of managing multiple organizations that operate the 
associated platform components and resources. Org Manager distinguishes three type of assets

* Organization **spaces**, which define various subsets of data and functionality managed by the organization (e.g., different applications and the associated resources).
* Organization **components**, which correspond to some platform-level instruments that are made available to the organization within the platform. The organization members may operate components (optionally, in relation to spaces) with different roles specific to the components.
* Organization **resources**, that represent the managed assets (e.g., DBs, Data Lake storages) grouped by spaces. 

For this model Org Manager relies on the AAC role model, and allows for associating users to component or resource roles. 

## Data Model

Each organization is uniquely identified by a domain name and isd associated with 

- a set of spaces
- a set of enabled platform components and tools
- a set of users that operate on behalf of organizations.

Each user may have different roles with respect to resource spaces and with respect to components. E.g., a user may have a 
management role for a space 'space1' and be able to use a 'DSS' tool in that space. If mulit-tenancy is supported by the tool, the component will have a dedicated tenant corresponding to the space.

## Set up

### Configuring AAC

Org-Manager requires the [AAC](https://github.com/smartcommunitylab/AAC) identity provider to work. It is also necessary that
AAC works in JWT token mode. A dedicated client app  should be created for the Org Manager with
- redirect URL matching the Org Manager endpoint, e.g., `http://localhost:7979/orgmanager`;
- Grant Types that include `Implicit` and `Client credentials`; 
- scopes enabled for role and profile management (see permissions under `Basic profile service` and `Role Management Services`): `profile.basicprofile.all, user.roles.write, user.roles.read, user.roles.read.all, client.roles.read.all, user.roles.manage.all`.

For more information on running the server inside a Docker container, see the [Running with Docker](#running-with-docker) section.

The organization creation and the component assignment may be performed only by the user with `organizations:ROLE_PROVIDER` role
or the API token should be associated with the `orgmanagement` scope. 

### Setting up the server

The `application.yml` file contains various properties used by the server. The following is a brief explanation of the main properties and what values should be given to them. 

* `server.port` – The port the server is running at. Default value: `7979`
* `server.servlet.session.cookie.name` – Name of the session cookie, used to avoid conflicts with other applications that use the name _JSESSIONID_ and may be running on the same host. Default value: `ORGMANAGERSESSIONID`
* `spring.datasource.url` – Database server for the Org-Manager server. The format may vary depending on the database type. A typical format can look like this: `jdbc:<database type>://<host>:<port>/<database name>`\
Sample value: `jdbc:postgresql://localhost:5432/orgmanager`
* `spring.datasource.username` – Name of the user in the database
* `spring.datasource.password` – Password of the user in the database
* `spring.datasource.driver-class-name`: Driver for the database. Sample value: `org.postgresql.Driver`. PostgreSQL, MySQL, and H2 are supported out of the box.
* `spring.jpa.database-platform` – Dialect for the database. Sample value: `org.hibernate.dialect.PostgreSQLDialect`. There may be more properties under `spring` related to setting up the database.
* `security.oauth2.client.clientId` – Client ID for Org-Manager in AAC.
* `security.oauth2.client.clientSecret` – Client secret for Org-Manager in AAC.
* `security.oauth2.resource.id` – Resource ID of the org management. Should match the client ID above.
* `security.oauth2.resource.jwk.keySetUri` – URL of the key set for JWT token validation.
* `aac.uri`: AAC host

It is possible to configure some additonal properties, such as logging level or Swagger metadata.

## Org Manager APIs

This section describes the available APIs. Make sure to include the **Authorization** header containing `Bearer <token value>` in every request. In case an error occurs, each API will return a response with an appropriate status code,  with some extra details in the response body.

Assuming the server is being hosted on _localhost_ at port _7979_, the **Swagger UI** for the Org-Manager APIs is available at **http://localhost:7979/swagger-ui.html**.

Most APIs have security restrictions that require the user to be owner of the organization they are attempting to alter, or to have administrator privileges.
The owner of a specific organization is defined as a user with the following role in AAC: `organizations/<organization_slug>:ROLE_PROVIDER`\
A user has administrator privileges when the access token they are using is a client token with the `orgmanagement` scope, or when they have the following role in AAC:
`organizations:ROLE_PROVIDER`

Also, for these APIs to work correctly, the access token used must have the following scopes: `profile.basicprofile.me`, `user.roles.me`.

### Create organization
This API is used to create an organization. Its response contains a JSON object that represents the newly created organization. This response will contain an additional field, `id`, necessary to recognize the organization and useful when calling other APIs.

Note that, since the `owner` field will be interpreted as the starting owner of the organization. This means that AAC must have a user with this username, otherwise an error will occur.

**Requirements**: must have administrator privileges

**End-point**: /api/organizations

**Method**: POST

**Body**: JSON object describing the organization. The following fields can be defined:
1. `name` – Name of the organization. Required. May contain alphanumeric characters, space, dash (-) or underscore (_). Cannot have the same name as an already existing organization, even if case is different. Any leading or trailing spaces will be ignored, and multiple consecutive spaces will be replaced with a single space.
2. `slug` – Defines the domain of the organization. Optional: if specified, it can only contain alphanumeric lower case characters and underscores. If left out, it will be generated from the name, converting it to lower case and replacing dashes and spaces with underscores.
3. `owner` - username of the initial owner of the organization.
4. `description` – Description of the organization. Required.
5. `contacts` – Inner JSON object describing the contacts. Required. Its 5 inner properties are:
   - `email` – E-mail. Required. Will be used as name of the owner of the organization.
   - `name` – Name of the contact. Required.
   - `surname` – Surname. Required.
   - `web` – URL. Optional.
   - `phone` – Array of strings for phone numbers. Optional.
   - `logo` – URL. Optional.
6. `tag` – Array of strings for tags. Optional.
7. `active` – Can take `true` or `false` as values. Indicates whether the organization is enabled or disabled. Optional, will default to `true` if omitted.

**Sample request body**:

	{
	  "name":"My Organization",
	  "slug":"my_org",
	  "description":"This is my test organization.",
	  "owner": "rossi@example.com",
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

**End-point**: /api/organizations

**Method**: GET

**Parameters**:
1.	`name` – Name to search. Case insensitive. All organizations with a name that contains this parameter will be returned.
2.	`page` – Page to be returned. Can be omitted, since most of the time the organizations returned will be less than 20. Starts from 0, so if you want the second page, use `page=1`.

**Sample request URL**: `http://localhost:7979/api/organizations?name=Company&page=3`

### Update organization
Updates an organization. Only certain fields may be updated. The `id` of the organization must be known, and used in the request URL.

**Requirements**: must have the administrator privileges, or be owner of the organization.

**End-point**: /api/organizations/<organization_id>/info

**Method**: PUT

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

**Requirements**: must have administrator privileges

**End-point**: /api/organizations/<organization_id>/enable

**Method**: PUT

**Sample request URL**: `http://localhost:7979/api/organizations/3/enable`

### Disable organization
Disables an organization. Simply changes the `active` field to `false`. Other than the endpoint, it is identical to the **Enable organization** API.

**Requirements**: must have administrator privileges

**End-point**: /api/organizations/<organization_id>/disable

**Method**: PUT

**Sample request URL**: `http://localhost:7979/api/organizations/3/disable`

### Delete organization
Deletes an organization. Also unregisters all members belonging to it, deletes all their roles within it, and deletes all tenants within it. An organization must be disabled before it can be deleted.

**Requirements**: must have administrator privileges

**End-point**: /api/organizations/<organization_id>

**Method**: DELETE

**Sample request URL**: `http://localhost:7979/api/organizations/1`

### List organization spaces
List all spaces of the organization.

**Requirements**: must have administrator privileges or be an organization owner

**End-point**: /api/organizations/<organization_id>/spaces

**Method**: GET

**Sample request URL**: `http://localhost:7979/api/organizations/1/spaces`

### Add organization space
Add a space to the organization.

**Requirements**: must have administrator privileges or be an organization owner

**End-point**: /api/organizations/<organization_id>/spaces

**Method**: PUT

**Parameters**:
1.  `space` – space to add. Should be unique within the organization. 

**Sample request URL**: `http://localhost:7979/api/organizations/1/spaces?space=somespace`

### Delete organization space
Delete a space from the organization.

**Requirements**: must have administrator privileges or be an organization owner

**End-point**: /api/organizations/<organization_id>/spaces

**Method**: DELETE

**Parameters**:
1.  `space` – space to delete. Should be unique within the organization. 

**Sample request URL**: `http://localhost:7979/api/organizations/1/spaces?space=somespace`


### List available components
Lists available components, together with a few properties for each of them.

**End-point**: /api/components

**Method**: GET

### List possible roles for a component
Returns a list of strings, representing what roles may be attributed to a user when added to a specific component.

**End-point**: /api/components/<component_id>/roles

**Method**: GET

**Sample request URL**: `http://localhost:7979/api/components/nifi/roles`

### List organization components
List all components enabled for the organization.

**Requirements**: must have administrator privileges or be an organization owner

**End-point**: /api/organizations/<organization_id>/configuration

**Method**: GET

**Sample request URL**: `http://localhost:7979/api/organizations/1/configuration`

### Update organization components
List all components enabled for the organization.

**Requirements**: must have administrator privileges

**End-point**: /api/organizations/<organization_id>/configuration

**Method**: POST

**Body**: JSON with the components to enable. The components should have a form of the object with at leat `componentId` field. 
The components not in the list will be disabled.

**Sample request URL**: `http://localhost:7979/api/organizations/1/configuration`

**Sample Body**:

  [
    {
      "componentId":"nifi"
    },{
      "componentId":"dss"
    }
  ]

### List users in an organization
Lists users that belong to the indicated organization. The `id` of the organization must be known. An optional parameter may be specified to act as a filter on the desired users’ names.

**Requirements**: must have administrator privileges, or be the owner of the organization

**End-point**: /api/organizations/<organization_id>/members

**Method**: GET

**Sample request URL**: `http://localhost:7979/api/organizations/1/members`

### Add a user to an organization
Grants a user the roles listed in the request body. All roles they previously had within the organization, but that are not present in this new configuration, will be removed. The user will be added to the organization, in case they were previously not registered. This means that AAC must have a user with this username, otherwise an error will occur. The response will also contain the `id` of the member within the organization.

It is also possible, for administrators only, to grant/revoke the status of owner of the organization through this API.

The listed roles will be associated to the user. This service manages ONLY the resource and component roles. The roles previously associated to the user and not present in the list will be removed.

**Requirements**: must have administrator privileges, or be owner of the organization (cannot grant/revoke owner status)

**End-point**: /api/organizations/<organization_id>/members

**Method**: POST

**Body**: JSON object containing the user’s name and the roles they should have:
1. `username` – Name of the user to add. Must be a valid name recognized by the identity provider.
2. `roles` – Array of JSON objects representing the roles to add. Each role has the following properties:
   - `type` – type of the role. May be one of `organization`, `resources`, or `components/<componentId>`. 
   - `space` - data space to be associated to the role. May be empty meaning that the role is at the organization level.
   - `role` – Role of the user for the type and space.
3. `owner` - Boolean parameter that can only be set by administrators. If this parameter appears in a call performed without administrator rights, it will be ignored.
  
**Sample request URL**: `http://localhost:7979/api/organizations/1/members`

**Sample request body**:

	{
	  "username":"bob@test.com",
	  "roles": [{
	    "type": "components/nifi",
	    "space":"trento",
	    "role":"ROLE_MANAGER"
	  },{
      "type": "components/nifi",
      "space":"ferrara",
	    "role":"ROLE_USER"
	  }],
	  "owner":"true"
	}

### Remove a user from an organization
Unregisters a user from an organization, stripping them of all roles they had within it. The `id` of the organization, as well as the `userId` of the member to remove, must be known.

**Requirements**: must have administrator privileges, or be the owner of the organization

**End-point**: /api/organizations/<organization_id>/members/<member_id>

**Method**: DELETE

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
