package it.smartcommunitylab.apimconnector.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import it.smartcommunitylab.apimconnector.APIMConnector;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes= {TestConfig.class, IntegrationConfig.class})
public class APIMConnectorTest {

	@Autowired
	private APIMConnector apimConnector;
	private static final String TEST_TENANT_DOMAIN 	= "domain.test8";
	private static final String TEST_ORGANIZATION 	= "organization";
	private static final String TEST_USER 			= "test-mail@test.com";
	private static final String TEST_ROLE 			= "roleTest";
	
	@Test
	public void testCreateTenant() {
		apimConnector.createTenant(TEST_TENANT_DOMAIN,TEST_USER,TEST_ORGANIZATION);
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
		apimConnector.createUser(TEST_USER);
	}
		
	@Test
	public void testAssignRole2User() {
		apimConnector.assignRoleToUser(TEST_ROLE, TEST_ORGANIZATION, TEST_USER);
	}
}
