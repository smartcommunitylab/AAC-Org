# WSO2 Connector 

This document explains how to configure WSO2 products (APIM,DSS) and Organization Manager in order to have them integrated correctly.

## WSO2 User Store Manager extension

In order to provide the necessary infrastructure for allowing WSO2 products to interact with Org-Manager, it is necessary to include in **repository/components/dropins** the *jar*'s of the following submodules:

	<module>apim.custom.user.store</module>
  	<module>apim.custom.user.store.stub</module>
  	
This extension is done in order to permit the admin account to create, update, delete users and assign/revoke roles within specific tenants and extends the existing [UserStoreManagerService admin](https://github.com/wso2-extensions/identity-user-ws/blob/master/components/org.wso2.carbon.um.ws.service/src/main/java/org/wso2/carbon/um/ws/service/UserStoreManagerService.java).

The configuration steps are the following:
- Build **orgmanager-wso2connector** project with Maven.

- Copy **apim.custom.user.store-0.0.1.jar** from the project *orgmanager-wso2connector/apim.custom.user.store* to the WSO2 directory **repository/components/dropins**.

- Copy **apim.custom.user.store.stub-0.0.1.jar** from the project *orgmanager-wso2connector/apim.custom.user.store.stub* to the WSO2 directory **repository/components/dropins**.

As a result, the new admin stub can be accessed from the following end-point: `https://$APIM_URL/services/CustomUserStoreManagerService`

After putting the *jar*'s in the proper folder, you should update the connector's configuration in [application-components.yml](https://github.com/smartcommunitylab/AAC-Org/blob/master/server/src/main/resources/application-components.yml) accordingly for APIM and DSS components:

	usermgmtEndpoint: /services/CustomUserStoreManagerService
