package it.smartcommunitylab.apimconnector.services;

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.authenticator.stub.AuthenticationAdminStub;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.authenticator.stub.LogoutAuthenticationExceptionException;

public class LoginAdminService {  
	
	 private final String serviceName = "AuthenticationAdmin";  
     private AuthenticationAdminStub authenticationAdminStub;  
     private String endPoint;  
     private Log log = LogFactory.getLog(LoginAdminService.class);
   
     public LoginAdminService(String backEndUrl) {  
	       this.endPoint = backEndUrl + "/services/" + serviceName;  
	       try {
				authenticationAdminStub = new AuthenticationAdminStub(endPoint);
		   } catch (AxisFault e) {
				e.printStackTrace();
		   }  
     }  
   
     public String authenticate(String userName, String password) {  
	       String sessionCookie = null;  
	       try {
	    	   URL url = new URL(endPoint);
	    	   String hostname = url.getHost();
		       if (authenticationAdminStub.login(userName, password, hostname)) {  
		    	   log.info("Login Successful");  
			         ServiceContext serviceContext = authenticationAdminStub.  
			             _getServiceClient().getLastOperationContext().getServiceContext();  
			         sessionCookie = (String) serviceContext.getProperty(HTTPConstants.COOKIE_STRING);  
			         log.info(sessionCookie);  
		       }
	       } catch (MalformedURLException | RemoteException | LoginAuthenticationExceptionException e) {
				log.info("Error during authentication of user: " + e.getMessage());
		   }
	       return sessionCookie;  
     }  
   
     public void logOut() throws RemoteException, LogoutAuthenticationExceptionException {  
       authenticationAdminStub.logout();  
     } 
     
}