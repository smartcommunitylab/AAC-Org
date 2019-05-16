# WSO2 Connector 

This document explains how to configure WSO2 products(APIM,DSS) and Organization Manager in order to have them integrated correctly

## WSO2 User Store Manager extension

In order to provide the necessary infrastructure for allowing WSO2 to interact with Organization Manager it is necessary to include in repository/components/dropins the jar of the following submodules :

	<module>apim.custom.user.store</module>
  	<module>apim.custom.user.store.stub</module>
  	
After putting the jars at the proper folder you should update the proper component configuration in src/main/resources/application-components.yml accordingly for APIM and DSS components:

	usermgmtEndpoint: /services/CustomUserStoreManagerService