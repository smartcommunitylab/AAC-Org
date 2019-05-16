
/**
 * CustomUserStoreManagerServiceUserStoreExceptionException.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1-wso2v12  Built on : Mar 19, 2015 (08:32:27 UTC)
 */

package it.smartcommunitylab.apim.user.stub;

public class CustomUserStoreManagerServiceUserStoreExceptionException extends java.lang.Exception{

    private static final long serialVersionUID = 1558006493613L;
    
    private it.smartcommunitylab.apim.user.stub.CustomUserStoreManagerServiceUserStoreException faultMessage;

    
        public CustomUserStoreManagerServiceUserStoreExceptionException() {
            super("CustomUserStoreManagerServiceUserStoreExceptionException");
        }

        public CustomUserStoreManagerServiceUserStoreExceptionException(java.lang.String s) {
           super(s);
        }

        public CustomUserStoreManagerServiceUserStoreExceptionException(java.lang.String s, java.lang.Throwable ex) {
          super(s, ex);
        }

        public CustomUserStoreManagerServiceUserStoreExceptionException(java.lang.Throwable cause) {
            super(cause);
        }
    

    public void setFaultMessage(it.smartcommunitylab.apim.user.stub.CustomUserStoreManagerServiceUserStoreException msg){
       faultMessage = msg;
    }
    
    public it.smartcommunitylab.apim.user.stub.CustomUserStoreManagerServiceUserStoreException getFaultMessage(){
       return faultMessage;
    }
}
    