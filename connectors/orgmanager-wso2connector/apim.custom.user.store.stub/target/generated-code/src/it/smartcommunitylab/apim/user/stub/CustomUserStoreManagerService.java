

/**
 * CustomUserStoreManagerService.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1-wso2v12  Built on : Mar 19, 2015 (08:32:27 UTC)
 */

    package it.smartcommunitylab.apim.user.stub;

    /*
     *  CustomUserStoreManagerService java interface
     */

    public interface CustomUserStoreManagerService {
          

        /**
          * Auto generated method signature
          * 
                    * @param getRoleListOfUser65
                
             * @throws it.smartcommunitylab.apim.user.stub.CustomUserStoreManagerServiceUserStoreExceptionException : 
         */

         
                     public java.lang.String[] getRoleListOfUser(

                        java.lang.String userName66)
                        throws java.rmi.RemoteException
             
          ,it.smartcommunitylab.apim.user.stub.CustomUserStoreManagerServiceUserStoreExceptionException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getRoleListOfUser65
            
          */
        public void startgetRoleListOfUser(

            java.lang.String userName66,

            final it.smartcommunitylab.apim.user.stub.CustomUserStoreManagerServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param getAllProfileNames69
                
             * @throws it.smartcommunitylab.apim.user.stub.CustomUserStoreManagerServiceUserStoreExceptionException : 
         */

         
                     public java.lang.String[] getAllProfileNames(

                        )
                        throws java.rmi.RemoteException
             
          ,it.smartcommunitylab.apim.user.stub.CustomUserStoreManagerServiceUserStoreExceptionException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getAllProfileNames69
            
          */
        public void startgetAllProfileNames(

            

            final it.smartcommunitylab.apim.user.stub.CustomUserStoreManagerServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param getUserList72
                
             * @throws it.smartcommunitylab.apim.user.stub.CustomUserStoreManagerServiceUserStoreExceptionException : 
         */

         
                     public java.lang.String[] getUserList(

                        java.lang.String claimUri73,java.lang.String claimValue74,java.lang.String profile75)
                        throws java.rmi.RemoteException
             
          ,it.smartcommunitylab.apim.user.stub.CustomUserStoreManagerServiceUserStoreExceptionException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getUserList72
            
          */
        public void startgetUserList(

            java.lang.String claimUri73,java.lang.String claimValue74,java.lang.String profile75,

            final it.smartcommunitylab.apim.user.stub.CustomUserStoreManagerServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     
       /**
         * Auto generated method signature for Asynchronous Invocations
         * 
                 * @throws it.smartcommunitylab.apim.user.stub.CustomUserStoreManagerServiceUserStoreExceptionException : 
         */
        public void  addUserClaimValues(
         java.lang.String arg079,org.wso2.carbon.user.mgt.common.xsd.ClaimValue[] arg180,java.lang.String arg281

        ) throws java.rmi.RemoteException
        
        
               ,it.smartcommunitylab.apim.user.stub.CustomUserStoreManagerServiceUserStoreExceptionException;

        

        /**
          * Auto generated method signature
          * 
                    * @param isExistingUser82
                
             * @throws it.smartcommunitylab.apim.user.stub.CustomUserStoreManagerServiceUserStoreExceptionException : 
         */

         
                     public boolean isExistingUser(

                        java.lang.String userName83)
                        throws java.rmi.RemoteException
             
          ,it.smartcommunitylab.apim.user.stub.CustomUserStoreManagerServiceUserStoreExceptionException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param isExistingUser82
            
          */
        public void startisExistingUser(

            java.lang.String userName83,

            final it.smartcommunitylab.apim.user.stub.CustomUserStoreManagerServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     
       /**
         * Auto generated method signature for Asynchronous Invocations
         * 
                 * @throws it.smartcommunitylab.apim.user.stub.CustomUserStoreManagerServiceUserStoreExceptionException : 
         */
        public void  addUser(
         java.lang.String userName87,java.lang.String credential88,java.lang.String[] roleList89,org.wso2.carbon.user.mgt.common.xsd.ClaimValue[] claims90,java.lang.String profileName91,boolean requirePasswordChange92,int args693,java.lang.String args794

        ) throws java.rmi.RemoteException
        
        
               ,it.smartcommunitylab.apim.user.stub.CustomUserStoreManagerServiceUserStoreExceptionException;

        
       /**
         * Auto generated method signature for Asynchronous Invocations
         * 
                 * @throws it.smartcommunitylab.apim.user.stub.CustomUserStoreManagerServiceUserStoreExceptionException : 
         */
        public void  updateRoleListOfUser(
         java.lang.String userName96,java.lang.String[] deletedRoles97,java.lang.String[] newRoles98,int args399,java.lang.String args4100

        ) throws java.rmi.RemoteException
        
        
               ,it.smartcommunitylab.apim.user.stub.CustomUserStoreManagerServiceUserStoreExceptionException;

        

        /**
          * Auto generated method signature
          * 
                    * @param getRoleNames101
                
             * @throws it.smartcommunitylab.apim.user.stub.CustomUserStoreManagerServiceUserStoreExceptionException : 
         */

         
                     public java.lang.String[] getRoleNames(

                        )
                        throws java.rmi.RemoteException
             
          ,it.smartcommunitylab.apim.user.stub.CustomUserStoreManagerServiceUserStoreExceptionException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getRoleNames101
            
          */
        public void startgetRoleNames(

            

            final it.smartcommunitylab.apim.user.stub.CustomUserStoreManagerServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     
       /**
         * Auto generated method signature for Asynchronous Invocations
         * 
                 * @throws it.smartcommunitylab.apim.user.stub.CustomUserStoreManagerServiceUserStoreExceptionException : 
         */
        public void  updateCredential(
         java.lang.String userName105,java.lang.String newCredential106,java.lang.String oldCredential107

        ) throws java.rmi.RemoteException
        
        
               ,it.smartcommunitylab.apim.user.stub.CustomUserStoreManagerServiceUserStoreExceptionException;

        
       /**
         * Auto generated method signature for Asynchronous Invocations
         * 
                 * @throws it.smartcommunitylab.apim.user.stub.CustomUserStoreManagerServiceUserStoreExceptionException : 
         */
        public void  deleteUserClaimValues(
         java.lang.String userName109,java.lang.String[] claims110,java.lang.String profileName111

        ) throws java.rmi.RemoteException
        
        
               ,it.smartcommunitylab.apim.user.stub.CustomUserStoreManagerServiceUserStoreExceptionException;

        

        /**
          * Auto generated method signature
          * 
                    * @param authenticate112
                
             * @throws it.smartcommunitylab.apim.user.stub.CustomUserStoreManagerServiceUserStoreExceptionException : 
         */

         
                     public boolean authenticate(

                        java.lang.String userName113,java.lang.String credential114)
                        throws java.rmi.RemoteException
             
          ,it.smartcommunitylab.apim.user.stub.CustomUserStoreManagerServiceUserStoreExceptionException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param authenticate112
            
          */
        public void startauthenticate(

            java.lang.String userName113,java.lang.String credential114,

            final it.smartcommunitylab.apim.user.stub.CustomUserStoreManagerServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param getPasswordExpirationTime117
                
             * @throws it.smartcommunitylab.apim.user.stub.CustomUserStoreManagerServiceUserStoreExceptionException : 
         */

         
                     public long getPasswordExpirationTime(

                        java.lang.String username118)
                        throws java.rmi.RemoteException
             
          ,it.smartcommunitylab.apim.user.stub.CustomUserStoreManagerServiceUserStoreExceptionException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getPasswordExpirationTime117
            
          */
        public void startgetPasswordExpirationTime(

            java.lang.String username118,

            final it.smartcommunitylab.apim.user.stub.CustomUserStoreManagerServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param getUserClaimValuesForClaims121
                
             * @throws it.smartcommunitylab.apim.user.stub.CustomUserStoreManagerServiceUserStoreExceptionException : 
         */

         
                     public org.wso2.carbon.user.mgt.common.xsd.ClaimValue[] getUserClaimValuesForClaims(

                        java.lang.String userName122,java.lang.String[] claims123,java.lang.String profileName124)
                        throws java.rmi.RemoteException
             
          ,it.smartcommunitylab.apim.user.stub.CustomUserStoreManagerServiceUserStoreExceptionException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getUserClaimValuesForClaims121
            
          */
        public void startgetUserClaimValuesForClaims(

            java.lang.String userName122,java.lang.String[] claims123,java.lang.String profileName124,

            final it.smartcommunitylab.apim.user.stub.CustomUserStoreManagerServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     
       /**
         * Auto generated method signature for Asynchronous Invocations
         * 
                 * @throws it.smartcommunitylab.apim.user.stub.CustomUserStoreManagerServiceUserStoreExceptionException : 
         */
        public void  setUserClaimValues(
         java.lang.String userName128,org.wso2.carbon.user.mgt.common.xsd.ClaimValue[] claims129,java.lang.String profileName130

        ) throws java.rmi.RemoteException
        
        
               ,it.smartcommunitylab.apim.user.stub.CustomUserStoreManagerServiceUserStoreExceptionException;

        
       /**
         * Auto generated method signature for Asynchronous Invocations
         * 
                 * @throws it.smartcommunitylab.apim.user.stub.CustomUserStoreManagerServiceUserStoreExceptionException : 
         */
        public void  deleteUserClaimValue(
         java.lang.String userName132,java.lang.String claimURI133,java.lang.String profileName134

        ) throws java.rmi.RemoteException
        
        
               ,it.smartcommunitylab.apim.user.stub.CustomUserStoreManagerServiceUserStoreExceptionException;

        

        /**
          * Auto generated method signature
          * 
                    * @param getHybridRoles135
                
             * @throws it.smartcommunitylab.apim.user.stub.CustomUserStoreManagerServiceUserStoreExceptionException : 
         */

         
                     public java.lang.String[] getHybridRoles(

                        )
                        throws java.rmi.RemoteException
             
          ,it.smartcommunitylab.apim.user.stub.CustomUserStoreManagerServiceUserStoreExceptionException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getHybridRoles135
            
          */
        public void startgetHybridRoles(

            

            final it.smartcommunitylab.apim.user.stub.CustomUserStoreManagerServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param isReadOnly138
                
             * @throws it.smartcommunitylab.apim.user.stub.CustomUserStoreManagerServiceUserStoreExceptionException : 
         */

         
                     public boolean isReadOnly(

                        )
                        throws java.rmi.RemoteException
             
          ,it.smartcommunitylab.apim.user.stub.CustomUserStoreManagerServiceUserStoreExceptionException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param isReadOnly138
            
          */
        public void startisReadOnly(

            

            final it.smartcommunitylab.apim.user.stub.CustomUserStoreManagerServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param getTenantId141
                
             * @throws it.smartcommunitylab.apim.user.stub.CustomUserStoreManagerServiceUserStoreExceptionException : 
         */

         
                     public int getTenantId(

                        )
                        throws java.rmi.RemoteException
             
          ,it.smartcommunitylab.apim.user.stub.CustomUserStoreManagerServiceUserStoreExceptionException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getTenantId141
            
          */
        public void startgetTenantId(

            

            final it.smartcommunitylab.apim.user.stub.CustomUserStoreManagerServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param getAxisConfig144
                
         */

         
                     public org.apache.axis2.engine.xsd.AxisConfiguration getAxisConfig(

                        )
                        throws java.rmi.RemoteException
             ;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getAxisConfig144
            
          */
        public void startgetAxisConfig(

            

            final it.smartcommunitylab.apim.user.stub.CustomUserStoreManagerServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     
       /**
         * Auto generated method signature for Asynchronous Invocations
         * 
                 * @throws it.smartcommunitylab.apim.user.stub.CustomUserStoreManagerServiceUserStoreExceptionException : 
         */
        public void  deleteRole(
         java.lang.String roleName148

        ) throws java.rmi.RemoteException
        
        
               ,it.smartcommunitylab.apim.user.stub.CustomUserStoreManagerServiceUserStoreExceptionException;

        

        /**
          * Auto generated method signature
          * 
                    * @param isExistingRole149
                
             * @throws it.smartcommunitylab.apim.user.stub.CustomUserStoreManagerServiceUserStoreExceptionException : 
         */

         
                     public boolean isExistingRole(

                        java.lang.String roleName150)
                        throws java.rmi.RemoteException
             
          ,it.smartcommunitylab.apim.user.stub.CustomUserStoreManagerServiceUserStoreExceptionException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param isExistingRole149
            
          */
        public void startisExistingRole(

            java.lang.String roleName150,

            final it.smartcommunitylab.apim.user.stub.CustomUserStoreManagerServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param getUserListOfRole153
                
             * @throws it.smartcommunitylab.apim.user.stub.CustomUserStoreManagerServiceUserStoreExceptionException : 
         */

         
                     public java.lang.String[] getUserListOfRole(

                        java.lang.String roleName154)
                        throws java.rmi.RemoteException
             
          ,it.smartcommunitylab.apim.user.stub.CustomUserStoreManagerServiceUserStoreExceptionException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getUserListOfRole153
            
          */
        public void startgetUserListOfRole(

            java.lang.String roleName154,

            final it.smartcommunitylab.apim.user.stub.CustomUserStoreManagerServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param getProperties157
                
             * @throws it.smartcommunitylab.apim.user.stub.CustomUserStoreManagerServiceUserStoreExceptionException : 
         */

         
                     public it.smartcommunitylab.apim.user.stub.ArrayOfString[] getProperties(

                        org.apache.axiom.om.OMElement arg0158)
                        throws java.rmi.RemoteException
             
          ,it.smartcommunitylab.apim.user.stub.CustomUserStoreManagerServiceUserStoreExceptionException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getProperties157
            
          */
        public void startgetProperties(

            org.apache.axiom.om.OMElement arg0158,

            final it.smartcommunitylab.apim.user.stub.CustomUserStoreManagerServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     
       /**
         * Auto generated method signature for Asynchronous Invocations
         * 
                 * @throws it.smartcommunitylab.apim.user.stub.CustomUserStoreManagerServiceUserStoreExceptionException : 
         */
        public void  updateUserListOfRole(
         java.lang.String roleName162,java.lang.String[] deletedUsers163,java.lang.String[] newUsers164

        ) throws java.rmi.RemoteException
        
        
               ,it.smartcommunitylab.apim.user.stub.CustomUserStoreManagerServiceUserStoreExceptionException;

        

        /**
          * Auto generated method signature
          * 
                    * @param getConfigContext165
                
         */

         
                     public org.apache.axis2.context.xsd.ConfigurationContext getConfigContext(

                        )
                        throws java.rmi.RemoteException
             ;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getConfigContext165
            
          */
        public void startgetConfigContext(

            

            final it.smartcommunitylab.apim.user.stub.CustomUserStoreManagerServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     
       /**
         * Auto generated method signature for Asynchronous Invocations
         * 
                 * @throws it.smartcommunitylab.apim.user.stub.CustomUserStoreManagerServiceUserStoreExceptionException : 
         */
        public void  addUserClaimValue(
         java.lang.String userName169,java.lang.String claimURI170,java.lang.String claimValue171,java.lang.String profileName172,int args4173,java.lang.String args5174

        ) throws java.rmi.RemoteException
        
        
               ,it.smartcommunitylab.apim.user.stub.CustomUserStoreManagerServiceUserStoreExceptionException;

        
       /**
         * Auto generated method signature for Asynchronous Invocations
         * 
                 * @throws it.smartcommunitylab.apim.user.stub.CustomUserStoreManagerServiceUserStoreExceptionException : 
         */
        public void  updateRoleName(
         java.lang.String roleName176,java.lang.String newRoleName177

        ) throws java.rmi.RemoteException
        
        
               ,it.smartcommunitylab.apim.user.stub.CustomUserStoreManagerServiceUserStoreExceptionException;

        
       /**
         * Auto generated method signature for Asynchronous Invocations
         * 
                 * @throws it.smartcommunitylab.apim.user.stub.CustomUserStoreManagerServiceUserStoreExceptionException : 
         */
        public void  addRole(
         java.lang.String roleName179,java.lang.String[] userList180,org.wso2.carbon.um.ws.service.dao.xsd.PermissionDTO[] permissions181,int args3182,java.lang.String args4183

        ) throws java.rmi.RemoteException
        
        
               ,it.smartcommunitylab.apim.user.stub.CustomUserStoreManagerServiceUserStoreExceptionException;

        

        /**
          * Auto generated method signature
          * 
                    * @param getUserId184
                
             * @throws it.smartcommunitylab.apim.user.stub.CustomUserStoreManagerServiceUserStoreExceptionException : 
         */

         
                     public int getUserId(

                        java.lang.String username185)
                        throws java.rmi.RemoteException
             
          ,it.smartcommunitylab.apim.user.stub.CustomUserStoreManagerServiceUserStoreExceptionException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getUserId184
            
          */
        public void startgetUserId(

            java.lang.String username185,

            final it.smartcommunitylab.apim.user.stub.CustomUserStoreManagerServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param getProfileNames188
                
             * @throws it.smartcommunitylab.apim.user.stub.CustomUserStoreManagerServiceUserStoreExceptionException : 
         */

         
                     public java.lang.String[] getProfileNames(

                        java.lang.String userName189)
                        throws java.rmi.RemoteException
             
          ,it.smartcommunitylab.apim.user.stub.CustomUserStoreManagerServiceUserStoreExceptionException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getProfileNames188
            
          */
        public void startgetProfileNames(

            java.lang.String userName189,

            final it.smartcommunitylab.apim.user.stub.CustomUserStoreManagerServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     
       /**
         * Auto generated method signature for Asynchronous Invocations
         * 
                 * @throws it.smartcommunitylab.apim.user.stub.CustomUserStoreManagerServiceUserStoreExceptionException : 
         */
        public void  deleteUser(
         java.lang.String userName193

        ) throws java.rmi.RemoteException
        
        
               ,it.smartcommunitylab.apim.user.stub.CustomUserStoreManagerServiceUserStoreExceptionException;

        

        /**
          * Auto generated method signature
          * 
                    * @param getUserClaimValues194
                
             * @throws it.smartcommunitylab.apim.user.stub.CustomUserStoreManagerServiceUserStoreExceptionException : 
         */

         
                     public org.wso2.carbon.um.ws.service.dao.xsd.ClaimDTO[] getUserClaimValues(

                        java.lang.String userName195,java.lang.String profileName196)
                        throws java.rmi.RemoteException
             
          ,it.smartcommunitylab.apim.user.stub.CustomUserStoreManagerServiceUserStoreExceptionException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getUserClaimValues194
            
          */
        public void startgetUserClaimValues(

            java.lang.String userName195,java.lang.String profileName196,

            final it.smartcommunitylab.apim.user.stub.CustomUserStoreManagerServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     
       /**
         * Auto generated method signature for Asynchronous Invocations
         * 
                 * @throws it.smartcommunitylab.apim.user.stub.CustomUserStoreManagerServiceUserStoreExceptionException : 
         */
        public void  setUserClaimValue(
         java.lang.String userName200,java.lang.String claimURI201,java.lang.String claimValue202,java.lang.String profileName203

        ) throws java.rmi.RemoteException
        
        
               ,it.smartcommunitylab.apim.user.stub.CustomUserStoreManagerServiceUserStoreExceptionException;

        

        /**
          * Auto generated method signature
          * 
                    * @param getTenantIdofUser204
                
             * @throws it.smartcommunitylab.apim.user.stub.CustomUserStoreManagerServiceUserStoreExceptionException : 
         */

         
                     public int getTenantIdofUser(

                        java.lang.String arg0205)
                        throws java.rmi.RemoteException
             
          ,it.smartcommunitylab.apim.user.stub.CustomUserStoreManagerServiceUserStoreExceptionException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getTenantIdofUser204
            
          */
        public void startgetTenantIdofUser(

            java.lang.String arg0205,

            final it.smartcommunitylab.apim.user.stub.CustomUserStoreManagerServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param listUsers208
                
             * @throws it.smartcommunitylab.apim.user.stub.CustomUserStoreManagerServiceUserStoreExceptionException : 
         */

         
                     public java.lang.String[] listUsers(

                        java.lang.String filter209,int maxItemLimit210)
                        throws java.rmi.RemoteException
             
          ,it.smartcommunitylab.apim.user.stub.CustomUserStoreManagerServiceUserStoreExceptionException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param listUsers208
            
          */
        public void startlistUsers(

            java.lang.String filter209,int maxItemLimit210,

            final it.smartcommunitylab.apim.user.stub.CustomUserStoreManagerServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param getUserClaimValue213
                
             * @throws it.smartcommunitylab.apim.user.stub.CustomUserStoreManagerServiceUserStoreExceptionException : 
         */

         
                     public java.lang.String getUserClaimValue(

                        java.lang.String userName214,java.lang.String claim215,java.lang.String profileName216)
                        throws java.rmi.RemoteException
             
          ,it.smartcommunitylab.apim.user.stub.CustomUserStoreManagerServiceUserStoreExceptionException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getUserClaimValue213
            
          */
        public void startgetUserClaimValue(

            java.lang.String userName214,java.lang.String claim215,java.lang.String profileName216,

            final it.smartcommunitylab.apim.user.stub.CustomUserStoreManagerServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     
       /**
         * Auto generated method signature for Asynchronous Invocations
         * 
                 * @throws it.smartcommunitylab.apim.user.stub.CustomUserStoreManagerServiceUserStoreExceptionException : 
         */
        public void  updateCredentialByAdmin(
         java.lang.String userName220,java.lang.String newCredential221

        ) throws java.rmi.RemoteException
        
        
               ,it.smartcommunitylab.apim.user.stub.CustomUserStoreManagerServiceUserStoreExceptionException;

        

        
       //
       }
    