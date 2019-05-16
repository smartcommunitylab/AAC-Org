
/**
 * CustomUserStoreManagerServiceCallbackHandler.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1-wso2v12  Built on : Mar 19, 2015 (08:32:27 UTC)
 */

    package it.smartcommunitylab.apim.user.stub;

    /**
     *  CustomUserStoreManagerServiceCallbackHandler Callback class, Users can extend this class and implement
     *  their own receiveResult and receiveError methods.
     */
    public abstract class CustomUserStoreManagerServiceCallbackHandler{



    protected Object clientData;

    /**
    * User can pass in any object that needs to be accessed once the NonBlocking
    * Web service call is finished and appropriate method of this CallBack is called.
    * @param clientData Object mechanism by which the user can pass in user data
    * that will be avilable at the time this callback is called.
    */
    public CustomUserStoreManagerServiceCallbackHandler(Object clientData){
        this.clientData = clientData;
    }

    /**
    * Please use this constructor if you don't want to set any clientData
    */
    public CustomUserStoreManagerServiceCallbackHandler(){
        this.clientData = null;
    }

    /**
     * Get the client data
     */

     public Object getClientData() {
        return clientData;
     }

        
           /**
            * auto generated Axis2 call back method for getRoleListOfUser method
            * override this method for handling normal response from getRoleListOfUser operation
            */
           public void receiveResultgetRoleListOfUser(
                    java.lang.String[] result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getRoleListOfUser operation
           */
            public void receiveErrorgetRoleListOfUser(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getAllProfileNames method
            * override this method for handling normal response from getAllProfileNames operation
            */
           public void receiveResultgetAllProfileNames(
                    java.lang.String[] result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getAllProfileNames operation
           */
            public void receiveErrorgetAllProfileNames(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getUserList method
            * override this method for handling normal response from getUserList operation
            */
           public void receiveResultgetUserList(
                    java.lang.String[] result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getUserList operation
           */
            public void receiveErrorgetUserList(java.lang.Exception e) {
            }
                
               // No methods generated for meps other than in-out
                
           /**
            * auto generated Axis2 call back method for isExistingUser method
            * override this method for handling normal response from isExistingUser operation
            */
           public void receiveResultisExistingUser(
                    boolean result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from isExistingUser operation
           */
            public void receiveErrorisExistingUser(java.lang.Exception e) {
            }
                
               // No methods generated for meps other than in-out
                
               // No methods generated for meps other than in-out
                
           /**
            * auto generated Axis2 call back method for getRoleNames method
            * override this method for handling normal response from getRoleNames operation
            */
           public void receiveResultgetRoleNames(
                    java.lang.String[] result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getRoleNames operation
           */
            public void receiveErrorgetRoleNames(java.lang.Exception e) {
            }
                
               // No methods generated for meps other than in-out
                
               // No methods generated for meps other than in-out
                
           /**
            * auto generated Axis2 call back method for authenticate method
            * override this method for handling normal response from authenticate operation
            */
           public void receiveResultauthenticate(
                    boolean result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from authenticate operation
           */
            public void receiveErrorauthenticate(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getPasswordExpirationTime method
            * override this method for handling normal response from getPasswordExpirationTime operation
            */
           public void receiveResultgetPasswordExpirationTime(
                    long result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getPasswordExpirationTime operation
           */
            public void receiveErrorgetPasswordExpirationTime(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getUserClaimValuesForClaims method
            * override this method for handling normal response from getUserClaimValuesForClaims operation
            */
           public void receiveResultgetUserClaimValuesForClaims(
                    org.wso2.carbon.user.mgt.common.xsd.ClaimValue[] result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getUserClaimValuesForClaims operation
           */
            public void receiveErrorgetUserClaimValuesForClaims(java.lang.Exception e) {
            }
                
               // No methods generated for meps other than in-out
                
               // No methods generated for meps other than in-out
                
           /**
            * auto generated Axis2 call back method for getHybridRoles method
            * override this method for handling normal response from getHybridRoles operation
            */
           public void receiveResultgetHybridRoles(
                    java.lang.String[] result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getHybridRoles operation
           */
            public void receiveErrorgetHybridRoles(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for isReadOnly method
            * override this method for handling normal response from isReadOnly operation
            */
           public void receiveResultisReadOnly(
                    boolean result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from isReadOnly operation
           */
            public void receiveErrorisReadOnly(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getTenantId method
            * override this method for handling normal response from getTenantId operation
            */
           public void receiveResultgetTenantId(
                    int result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getTenantId operation
           */
            public void receiveErrorgetTenantId(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getAxisConfig method
            * override this method for handling normal response from getAxisConfig operation
            */
           public void receiveResultgetAxisConfig(
                    org.apache.axis2.engine.xsd.AxisConfiguration result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getAxisConfig operation
           */
            public void receiveErrorgetAxisConfig(java.lang.Exception e) {
            }
                
               // No methods generated for meps other than in-out
                
           /**
            * auto generated Axis2 call back method for isExistingRole method
            * override this method for handling normal response from isExistingRole operation
            */
           public void receiveResultisExistingRole(
                    boolean result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from isExistingRole operation
           */
            public void receiveErrorisExistingRole(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getUserListOfRole method
            * override this method for handling normal response from getUserListOfRole operation
            */
           public void receiveResultgetUserListOfRole(
                    java.lang.String[] result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getUserListOfRole operation
           */
            public void receiveErrorgetUserListOfRole(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getProperties method
            * override this method for handling normal response from getProperties operation
            */
           public void receiveResultgetProperties(
                    it.smartcommunitylab.apim.user.stub.ArrayOfString[] result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getProperties operation
           */
            public void receiveErrorgetProperties(java.lang.Exception e) {
            }
                
               // No methods generated for meps other than in-out
                
           /**
            * auto generated Axis2 call back method for getConfigContext method
            * override this method for handling normal response from getConfigContext operation
            */
           public void receiveResultgetConfigContext(
                    org.apache.axis2.context.xsd.ConfigurationContext result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getConfigContext operation
           */
            public void receiveErrorgetConfigContext(java.lang.Exception e) {
            }
                
               // No methods generated for meps other than in-out
                
               // No methods generated for meps other than in-out
                
               // No methods generated for meps other than in-out
                
           /**
            * auto generated Axis2 call back method for getUserId method
            * override this method for handling normal response from getUserId operation
            */
           public void receiveResultgetUserId(
                    int result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getUserId operation
           */
            public void receiveErrorgetUserId(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getProfileNames method
            * override this method for handling normal response from getProfileNames operation
            */
           public void receiveResultgetProfileNames(
                    java.lang.String[] result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getProfileNames operation
           */
            public void receiveErrorgetProfileNames(java.lang.Exception e) {
            }
                
               // No methods generated for meps other than in-out
                
           /**
            * auto generated Axis2 call back method for getUserClaimValues method
            * override this method for handling normal response from getUserClaimValues operation
            */
           public void receiveResultgetUserClaimValues(
                    org.wso2.carbon.um.ws.service.dao.xsd.ClaimDTO[] result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getUserClaimValues operation
           */
            public void receiveErrorgetUserClaimValues(java.lang.Exception e) {
            }
                
               // No methods generated for meps other than in-out
                
           /**
            * auto generated Axis2 call back method for getTenantIdofUser method
            * override this method for handling normal response from getTenantIdofUser operation
            */
           public void receiveResultgetTenantIdofUser(
                    int result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getTenantIdofUser operation
           */
            public void receiveErrorgetTenantIdofUser(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for listUsers method
            * override this method for handling normal response from listUsers operation
            */
           public void receiveResultlistUsers(
                    java.lang.String[] result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from listUsers operation
           */
            public void receiveErrorlistUsers(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getUserClaimValue method
            * override this method for handling normal response from getUserClaimValue operation
            */
           public void receiveResultgetUserClaimValue(
                    java.lang.String result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getUserClaimValue operation
           */
            public void receiveErrorgetUserClaimValue(java.lang.Exception e) {
            }
                
               // No methods generated for meps other than in-out
                


    }
    