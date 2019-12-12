# Organization Management - Component model

Connectors for the various services, such as the NiFi connector or the API Manager one, must communicate with the server through the model defined in this sub-project.

Connectors must implement the [Component](/model/src/main/java/it/smartcommunitylab/orgmanager/componentsmodel/Component.java) interface, through which the server will issue commands to them.

[DefaultComponentImpl](/model/src/main/java/it/smartcommunitylab/orgmanager/componentsmodel/DefaultComponentImpl.java) is a no-action implementation of the interface, to be used for components that do not need a connector yet, or to disable interaction with the server.
