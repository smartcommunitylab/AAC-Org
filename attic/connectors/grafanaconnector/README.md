# Grafana Connector

This connector leverages the capabilities to remotely create, update, delete and configure Grafana wich is the open source analytics and monitoring solution for every database.
Grafana allows you to query, visualize, alert on and understand your metrics no matter where they are stored. Create, explore, and share dashboards with your team and foster a data driven culture.
Grafana offers an API to make possible to provision organizations and users within Grafana.

## 1. Admin Organizations API

The Admin Organizations HTTP API does not currently work with an API Token.<br/> 
API Tokens are currently only linked to an organization and an organization role.They cannot be given the permission of server admin, only users can be given that permission.<br/>
So in order to use these API calls you will have to use Basic Auth and the Grafana user must have the Grafana Admin permission (The default admin user is called admin and has permission to use this API).

## 2. Entities Provisioning Flow

In order to do the correct organization and user provisioning in Grafana, this connector provides the following steps:<br/>
1. Create the Organization
2. Create one global user 
3. Add the existing global user to the proper Organization taking in consideration the proper roles: admin and viewer
4. Assign to the existing user within the organization the proper role: admin or viewer, depending on future [AAC](https://github.com/smartcommunitylab/AAC) configurations

## 4. Parameters Set Up

| PROPERTY-NAME | DESCRIPTION |
| --- | --- |
| implementation | The class of the connector that implements the proper logic of the Grafana component |
| host | The URL where Grafana is running |
| username | The Grafana user that has admin permission |
| password | The Grafana user password |
| crudOrganization | The Grafana API endpoint to leverage organizations operations |
| crudUser | The Grafana API endpoint to leverage users operations |
| crudAdminUser | Grafana API endpoint used in the case of global user creation |
| userStaticPassword | The default user password for all grafana users created using this connector |

