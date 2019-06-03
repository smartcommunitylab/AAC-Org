# Organization Management
The Smart Community Lab's _Organization Manager_ (_OrgManager_ for short) is a tool to centralize tenant and user management for various services. It requires the [AAC](https://github.com/smartcommunitylab/AAC) identity provider to work.

OrgManager is structured as follows:
- [Client](https://github.com/smartcommunitylab/AAC-Org/tree/master/client) - The front-end console to perform all operations offered by the service.
- [Connectors](https://github.com/smartcommunitylab/AAC-Org/tree/master/connectors) - Each connector takes care of reflecting operations issued by the back-end to its corresponding component.
- [Model](https://github.com/smartcommunitylab/AAC-Org/tree/master/model) - The model connectors are based on. Functions as interface between back-end and connectors.
- [Server](https://github.com/smartcommunitylab/AAC-Org/tree/master/server) - The back-end, whose APIs are used by the front-end, issues commands to the connectors to apply tenant management-related operations on all components.
