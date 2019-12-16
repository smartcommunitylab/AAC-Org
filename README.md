# Organization Manager
The Smart Community Lab's _Organization Manager_ (_Org-Manager_ for short) is a tool to centralize tenant and user management for various services. It requires the [AAC](https://github.com/scc-digitalhun/AAC) identity provider to work.

Org-Manager is structured as follows:
- [Client](https://github.com/scc-digitalhun/AAC-Org/tree/master/client) - The front-end console to perform all operations offered by the service.
- [Server](https://github.com/scc-digitalhun/AAC-Org/tree/master/server) - The back-end, whose APIs are used by the front-end, issues commands to the connectors to apply tenant management-related operations on all components.
