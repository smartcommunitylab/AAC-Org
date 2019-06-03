========
Overview
========
The Smart Community Lab's *Organization Manager* (*OrgManager* for short) is a tool to centralize tenant and user management for various services. It requires the `AAC <https://github.com/smartcommunitylab/AAC>`_ identity provider to work.

OrgManager is structured as follows:

- `Client <https://github.com/smartcommunitylab/AAC-Org/tree/master/client>`_ - The front-end console to perform all operations offered by the service.
- `Connectors <https://github.com/smartcommunitylab/AAC-Org/tree/master/connectors>`_ - Each connector takes care of reflecting operations issued by the back-end to its corresponding component.
- `Model <https://github.com/smartcommunitylab/AAC-Org/tree/master/model>`_ - The model connectors are based on. Functions as interface between back-end and connectors.
- `Server <https://github.com/smartcommunitylab/AAC-Org/tree/master/server>`_ - The back-end, whose APIs are used by the front-end, issues commands to the connectors to apply tenant management-related operations on all components.
