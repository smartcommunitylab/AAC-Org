package it.smartcommunitylab.grafanaconnector;

import java.util.Arrays;
import java.util.Base64;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/*
 * Class containing utilities for interacting with the API of Grafana component
 */
public class GrafanaConnectorUtils {
	

	private static final Log log = LogFactory.getLog(GrafanaConnectorUtils.class);
	private static String componentId;
	private static String hostURL;
	private static String crudOrg;
	private static String crudUser;
	private static String crudAdminUser;
	private static String username;
	private static String password;
	private static String userStaticPassword;
	
	/**
	 * Initialize the properties of Grafana Component
	 * @param properties
	 */
	public static void initializeProperties(Map<String, String> properties) {
		
		componentId 		= properties.get(GrafanaConnectorContants.COMPONENT_ID);
		hostURL 			= properties.get(GrafanaConnectorContants.HOST);
		crudOrg				= properties.get(GrafanaConnectorContants.ORG_URL);
		crudUser 			= properties.get(GrafanaConnectorContants.USER_URL);
		crudAdminUser 		= properties.get(GrafanaConnectorContants.ADMIN_USER_URL);
		username 			= properties.get(GrafanaConnectorContants.USERNAME);
		password 			= properties.get(GrafanaConnectorContants.PASSWORD);
		userStaticPassword 	= properties.get(GrafanaConnectorContants.USER_STATIC_PASSW);
	}
	
	/**
	 * Method for generating POST, PUT, PATCH requests towards Grafana API
	 * @param url
	 * @param parameters
	 * @throws GrafanaException 
	 */
	public static Map<String, String> requestPOSTApi(String url, Object parameters, HttpMethod method) throws GrafanaException {
		GrafanaConnectorUtils.logInfo("Starting the method requestPOSTApi for URL: " + url + ",method: " + method, log);
		HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();

		RestTemplate restTemplate = new RestTemplate(requestFactory);
		HttpHeaders headers = setHeaderBasicAuth();
		HttpEntity<Object> httpEntity = new HttpEntity<Object>(parameters, headers);
		ResponseEntity<Map> response = null;
		try {
			response = restTemplate.exchange(url, method, httpEntity, Map.class);
			if(response.getStatusCode() == HttpStatus.OK) {
				return (Map<String, String>) response.getBody();
			} else {
				throw new GrafanaException("Error during request POST API url: " + url + ", " + response.getBody().get("message"));
			}
		} catch(RestClientException e) {
			logError("Error during request POST API url: " + url +  e.getMessage(), log);
			throw new GrafanaException("Error during request POST API url: " + url + e.getMessage());
		}
	}
	
	
	/**
	 * Method for generating DELETE requests towards Grafana API 
	 * @param url
	 * @param token
	 * @throws GrafanaException 
	 */
	public static Map<String, String> requestDELETEApi(String url) throws GrafanaException {
		GrafanaConnectorUtils.logInfo("Starting the method requestDELETEApi for URL: " + url , log);
		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = setHeaderBasicAuth();
		HttpEntity<Object> httpEntity = new HttpEntity<Object>(headers);
		try {
			ResponseEntity response = restTemplate.exchange(url, HttpMethod.DELETE, httpEntity, Map.class);
			return (Map<String, String>) response.getBody();
		} catch(Exception e) {
			logError(e.getMessage(), log);
			throw new GrafanaException(e.getMessage());
		}
	}
	
	/**
	 * Method for generating GET requests towards Grafana API
	 * @param url
	 * @param token
	 * @throws GrafanaException 
	 */
	public static Map<String,Object> requestGETApi(String url) throws GrafanaException {
		GrafanaConnectorUtils.logInfo("Starting the method requestGETApi for URL: " + url , log);
		Map<String,Object> resp = null;
		try {
			RestTemplate restTemplate = new RestTemplate();
			HttpHeaders headers = setHeaderBasicAuth();
			HttpEntity<Object> httpEntity = new HttpEntity<Object>(headers);
			ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, httpEntity, Map.class);
			if(response.getStatusCode() == HttpStatus.OK) {
				resp = (Map<String, Object>) response.getBody();
			} else {
				throw new GrafanaException("Error during request GET API url: " + url + ", " + response.getBody().get("message"));
			}
		} catch(Exception e) {
			logError(e.getMessage(), log);
			throw new GrafanaException(e.getMessage());
		}
		return resp;
	}
	
	public static String getComponentId() {
		return componentId;
	}
	
	public static String getHostURL() {
		return hostURL;
	}
	
	public static String getCrudOrganizationURL() {
		return getHostURL() + crudOrg;
	}
	
	public static String getCrudUserURL() {
		return getHostURL() + crudUser;
	}
	
	public static String getCrudAdminUserURL() {
		return getHostURL() + crudAdminUser;
	}
	
	public static String getUsername() {
		return username;
	}
	
	public static String getPassword() {
		return password;
	}
	
	public static String getUserStaticPassword() {
		return userStaticPassword;
	}
	/**
	 * setAuthorization Bearer Headers of the request
	 * @param token
	 * @return
	 */
	public static HttpHeaders setHeader(String token) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		headers.add("Authorization", "Bearer " + token);
		return headers;
	}
	
	public static HttpHeaders setHeaderBasicAuth() {
		String credentials = username + ":" + password;
		String encodedStr = new String(Base64.getEncoder().encode(credentials.getBytes()));
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		headers.add("Authorization", "Basic " + encodedStr);
		return headers;
	}
	
	/**
	 * Method for logging standard information
	 * @param message
	 * @param log
	 */
	public static void logInfo(String message, Log log) {
		log.info(message);
	}
	
	/**
	 * Method for logging error information
	 * @param message
	 * @param log
	 */
	public static void logError(String message, Log log) {
		log.error(message);
	}
	
}
