package it.smartcommunitylab.apimconnector.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import it.smartcommunitylab.apimconnector.APIMConnector;
import it.smartcommunitylab.orgmanager.componentsmodel.UserInfo;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes= {TestConfig.class, IntegrationConfig.class})
public class APIMConnectorTest {

	@Autowired
	private APIMConnector apimConnector;
	private static final String TEST_TENANT_DOMAIN 	= "domain.test8";
	private static final String TEST_ORGANIZATION 	= "organization";
	private static final String TEST_USER 			= "test-mail@test.com";
	private static final String TEST_NAME 			= "testname";
	private static final String TEST_SURNAME 		= "testsurname";
	private static final String TEST_ROLE 			= "roleTest";
	
	@Test
	public void testCreateTenant() {
		UserInfo ownerInfo = new UserInfo(TEST_USER, TEST_NAME, TEST_SURNAME);
		apimConnector.createTenant(TEST_TENANT_DOMAIN, TEST_ORGANIZATION, ownerInfo);
	}
	
	@Test
	public void testDeleteTenant() {
		apimConnector.deleteTenant(TEST_TENANT_DOMAIN, TEST_ORGANIZATION);
	}
	
	@Test
	public void testActivateTenant() {
		apimConnector.activateTenant(TEST_TENANT_DOMAIN);
	}
	
	@Test
	public void testCreateUser() {
		apimConnector.createUser(new UserInfo(TEST_USER, TEST_NAME, TEST_SURNAME));
	}
		
	@Test
	public void testAssignRole2User() {
		apimConnector.assignRoleToUser(TEST_ROLE, TEST_ORGANIZATION, TEST_USER);
	}
}
