# Organization Management
The Smart Community Lab's Organization Management application is a tool to centralize tenant and user management for various services. It requires the [AAC](https://github.com/smartcommunitylab/AAC) identity provider to work.

The Organization Management application's structure is divided as follows:
- [Client](https://github.com/smartcommunitylab/AAC-Org/tree/master/client) - The front-end console to perform all operations provided by the service.
- [Connectors](https://github.com/smartcommunitylab/AAC-Org/tree/master/connectors) - Each connector takes care of reflecting operation issued by the back-end to its corresponding component.
- [Model](https://github.com/smartcommunitylab/AAC-Org/tree/master/model) - The model connectors are based on. Functions as interface between back-end and connectors.
- [Server](https://github.com/smartcommunitylab/AAC-Org/tree/master/server) - The back-end, whose APIs are used by the front-end, issues commands to the connectors to apply tenant management-related operations on all components.
