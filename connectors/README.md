# Connectors
All connectors take their configuration parameters from the server, which retrieves them from a single file, [application-components.yml](https://github.com/smartcommunitylab/AAC-Org/blob/master/server/src/main/resources/application-components.yml).\
When running the server with Docker, this file is replaced with a different one; see the [Running with Docker](https://github.com/smartcommunitylab/AAC-Org/tree/master/server#running-with-docker) section of the server's documentation for more information.

Different connectors have different properties, so configuration for each connector is explained within its correspondent project subfolder.

\
Two properties are however required for all connectors:

`componentId` - Identifies the component.

`implementation` - Full name of the connector class that reflects tenant/user management operations on the component. The following value corresponds to a dummy class that causes no changes, to be used if the component does not need an external class for this purpose, or to simply disable a connector.\
`it.smartcommunitylab.orgmanager.componentsmodel.DefaultComponentImpl`
