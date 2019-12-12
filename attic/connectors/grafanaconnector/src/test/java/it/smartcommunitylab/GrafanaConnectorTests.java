package it.smartcommunitylab;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.yaml.snakeyaml.Yaml;
import it.smartcommunitylab.grafanaconnector.GrafanaConnector;
import it.smartcommunitylab.orgmanager.componentsmodel.UserInfo;

public class GrafanaConnectorTests {

	private Map<String,String> properties;
	private GrafanaConnector grafanaConn = new GrafanaConnector();
	private static final String organization 	= "MyOrganization";
	private static final String username 		= "testing@gmail.com";
	private static final String name 	 		= "testName";
	private static final String surname  		= "testSurname";
	private static final String ROLE_PROVIDER 	= "organization:ROLE_PROVIDER";
	private static final String ROLE_NON_PROVIDER 	= "organiation:ROLE_NON_PROVIDER";
	private UserInfo userInfo = new UserInfo(username, name, surname);
	
	@Before
	public void initialize() {
		Yaml yaml = new Yaml();
		InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("application.yml");
		properties = yaml.load(inputStream);
		grafanaConn.init(properties);
	}
	
	@Test
	public void testOrganizationCreation() {
		String resp = grafanaConn.createOrganization(organization, userInfo);
		assertEquals("grafana: Successful operation - Organization has been successfully created. Organization created", resp);
	}
	
	@Test
	public void testOrganizationDelete() {
		String resp = grafanaConn.deleteOrganization(organization, null);
		assertEquals("grafana: Successful operation - Organization has been successfully deleted. Organization deleted", resp);
	}
	
	@Test
	public void testUserCreate() {
		String resp = grafanaConn.createUser(userInfo);
		assertEquals("grafana: Successful operation - User successfully created: User created", resp);
	}
	
	@Test
	public void testaddOwnerUserToOrg() {
		String resp = grafanaConn.addOwner(userInfo,organization);
		assertEquals("grafana: Successful operation - User has successfully been converted to owner", resp);
	}
	
	@Test
	public void testaddNonOwnerUserToOrg() {
		String resp = grafanaConn.removeOwner(userInfo,organization);
		assertEquals("grafana: Successful operation - User has successfully been converted to viewer", resp);
	}
	
	@Test
	public void testAssignRoleProvider2User() {
		grafanaConn.assignRoleToUser(ROLE_PROVIDER, organization, userInfo);
	}
	
	@Test
	public void testAssignRoleNONProvider2User() {
		grafanaConn.assignRoleToUser(ROLE_NON_PROVIDER, organization, userInfo);
	}
	
	@Test
	public void testRemoveUserFromOrg() {
		String resp = grafanaConn.removeUserFromOrganization(userInfo, organization, null);
		assertEquals("grafana: Successful operation - User successfully removed: User removed from organization", resp);
	}
}
