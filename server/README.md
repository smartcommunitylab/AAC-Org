

# Organization Manager

Organization Manager complements AAC with the functionality of managing multiple organizations that operate the 
associated platform components and resources. Org Manager distinguishes three type of assets

* Organization **spaces**, which define various subsets of data and functionality managed by the organization (e.g., different applications and the associated resources).
* Organization **components**, which correspond to some platform-level instruments that are made available to the organization within the platform. The organization members may operate components (optionally, in relation to spaces) with different roles specific to the components.
* Organization **resources**, that represent the managed assets (e.g., DBs, Data Lake storages) grouped by spaces. 

For this model Org Manager relies on the AAC role model, and allows for associating users to component or resource roles. 

## Data Model

Each organization is uniquely identified by a domain name and it is associated with 

- a set of spaces
- a set of enabled platform components and tools
- a set of users that operate on behalf of organizations.

Each user may have different roles with respect to resource spaces and with respect to components. E.g., a user may have a 
management role for a space 'space1' and be able to use a 'DSS' tool in that space. If mulit-tenancy is supported by the tool, the component will have a dedicated tenant corresponding to the space.

## Set up

### Configuring AAC

Org-Manager requires the [AAC](https://github.com/smartcommunitylab/AAC) identity provider to work. It is also necessary that
AAC works in JWT token mode. A dedicated client app  should be created for the Org Manager with
- redirect URL matching the Org Manager endpoint, e.g., `http://localhost:7979/login`;
- Grant Types that include `Authorization Code`, and `Client credentials`; 
- scopes enabled for role and profile management 

```
scopes:
- user.roles.read
- user.roles.manage.all
- openid
- profile
- profile.basicprofile.me
- profile.basicprofile.all
- user.roles.write
- user.roles.read.all
- user.roles.me
```

Do note that in order to obtain the `profile.basicprofile.all` scope it is mandatory that the client owner (ie the developer) possesses the required role inside AAC.


The organization creation and the component assignment may be performed only by the user with `organizations:ROLE_PROVIDER` role
or the API token should be associated with the `orgmanagement` scope. 

### Setting up the server

The `application.yml` file contains various properties used by the server. The following is a brief explanation of the main properties and what values should be given to them. 

* `server.port` – The port the server is running at. Default value: `7979`
* `application.url` - The external (user facing) URL, when used behind a proxy
* `security.oauth2.resourceserver.client-id` – OAuth2 Client id
* `security.oauth2.resourceserver.jwt.issuer-uri` – Issuer url for OAuth2 (usually AAC)
* `aac.uri`: AAC host, better if given a direct connection (ie behind proxy)
* `aac.client-id`: AAC Client Id for client-credentials
* `aac.client-secret`: AAC Client secret for client-credentials

It is possible to configure some additonal properties, such as logging level or Swagger metadata.


Alternatively to manual configuration, parameters can be passed via ENV variables, for example when deployed via Docker.


* `SERVER_PORT` – The port the server is running at. Default value: `7979`
* `APPLICATION_URL` - The external (user facing) URL, when used behind a proxy
* `OAUTH2_CLIENT_ID` – OAuth2 Client id
* `OAUTH2_ISSUER_URI` – Issuer url for OAuth2 (usually AAC)
* `AAC_URI`: AAC host, better if given a direct connection (ie behind proxy)
* `AAC_CLIENT_ID`: AAC Client Id for client-credentials
* `AAC_CLIENT_SECRET`: AAC Client secret for client-credentials



## Org Manager APIs

This section describes the available APIs. Make sure to include the **Authorization** header containing `Bearer <token value>` in every request. In case an error occurs, each API will return a response with an appropriate status code,  with some extra details in the response body.

Assuming the server is being hosted on _localhost_ at port _7979_, the **Swagger UI** for the Org-Manager APIs is available at **http://localhost:7979/swagger-ui.html**.

Most APIs have security restrictions that require the user to be owner of the organization they are attempting to alter, or to have administrator privileges.
The owner of a specific organization is defined as a user with the following role in AAC: `organizations/<organization_slug>:ROLE_PROVIDER`\
A user has administrator privileges when the access token they are using is a client token with the `orgmanagement` scope, or when they have the following role in AAC:
`organizations:ROLE_PROVIDER`

Also, for these APIs to work correctly, the access token used must have the following scopes: `openid profile user.roles.me`.


