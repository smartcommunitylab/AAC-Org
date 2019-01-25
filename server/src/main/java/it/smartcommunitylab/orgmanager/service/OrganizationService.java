package it.smartcommunitylab.orgmanager.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.smartcommunitylab.orgmanager.common.OrgManagerUtils;
import it.smartcommunitylab.orgmanager.componentsmodel.Component;
import it.smartcommunitylab.orgmanager.dto.OrganizationDTO;
import it.smartcommunitylab.orgmanager.dto.OrganizationDTO.Contacts;
import it.smartcommunitylab.orgmanager.model.Organization;
import it.smartcommunitylab.orgmanager.model.OrganizationMember;
import it.smartcommunitylab.orgmanager.model.Role;
import it.smartcommunitylab.orgmanager.repository.OrganizationMemberRepository;
import it.smartcommunitylab.orgmanager.repository.OrganizationRepository;
import it.smartcommunitylab.orgmanager.repository.RoleRepository;
import it.smartcommunitylab.orgmanager.repository.TenantRepository;

@Service
@Transactional
public class OrganizationService {
	@Autowired
	private OrganizationRepository organizationRepository;
	
	@Autowired
	private OrganizationMemberRepository organizationMemberRepository;
	
	@Autowired
	private RoleRepository roleRepository;
	
	@Autowired
	private TenantRepository tenantRepository;
	
	@Autowired
	private ApplicationContext context;
	
	@Autowired
	private OrgManagerUtils utils;
	
	@Transactional(readOnly=true)
	/**
	 * Lists the organizations in pages. The organizations may be filtered by name (case insensitive).
	 * If the user has the organization management scope, all organizations will be returned.
	 * Otherwise, only organizations the user is owner of will be returned.
	 * 
	 * @param name - A substring of the wanted organizations, case insensitive
	 * @param pageable - Page number, size, etc.
	 * @return - Organizations the user is allowed to see
	 */
	public Page<OrganizationDTO> listOrganizations(String name, Pageable pageable) {
		if (name == null)
			name = ""; // null is not accepted, but an empty string works just fine when a filter on the name is not desired
		List<Organization> organizations = organizationRepository.findByNameIgnoreCaseContaining(name, pageable);
		List<OrganizationDTO> organizationsListDTO = new ArrayList<OrganizationDTO>();
		
		if (utils.userHasAdminRights()) { // user has admin rights if they are admin or have the organization management scope
			for (Organization o : organizations) // users with admin rights can see all organizations
				organizationsListDTO.add(new OrganizationDTO(o));
		} else { // users without admin rights can only see organizations they are part of
			String userName = utils.getAuthenticatedUserName(); // user name of the authenticated user
			List<OrganizationMember> memberOrgs = organizationMemberRepository.findByUsername(userName); // organizations the user is part of
			Set<Long> belongsToOrgIds = new HashSet<Long>();
			for (OrganizationMember m : memberOrgs)
				belongsToOrgIds.add(m.getOrganization().getId());
			for (Organization o : organizations) {
				if (belongsToOrgIds.contains(o.getId())) // organization is one the user is part of
					organizationsListDTO.add(new OrganizationDTO(o)); // user can view this organization
			}
		}
		return new PageImpl<OrganizationDTO>(organizationsListDTO, pageable, organizationsListDTO.size()); // returns results as a page
	}
	
	/**
	 * Creates an organization. The input organization is in a format fit for viewing, so it will be converted before storage.
	 * 
	 * @param organizationDTO - The organization, as an instance of the view type of Organization
	 * @return - The same organization, after being stored and then re-converted into the view type
	 */
	public OrganizationDTO createOrganization(OrganizationDTO organizationDTO) {
		// Checks if the user has the rights to perform this operation
		if (!utils.userHasAdminRights())
			throw new AccessDeniedException("Access is denied: user does not have administrator rights.");
		
		String name = organizationDTO.getName().trim().replaceAll("\\s+", " "); // normalizes the name
		Pattern pattern = Pattern.compile("^[a-zA-Z0-9 _-]+$");
		if (!pattern.matcher(name).matches()) // checks if the name contains illegal characters
			throw new IllegalArgumentException("Organization name " + name + " is not allowed, please use only alphanumeric characters, space ( ), dash (-) or underscore (_).");
		// While id is the key, the organization's name must also be unique, regardless of case
		Organization organization = organizationRepository.findByNameIgnoreCase(name);
		if (organization != null) // organization with the same normalized name, ignoring case, already exists
			throw new IllegalArgumentException("Organization " + name + " already exists.");
		organizationDTO.setName(name);
		
		String slug = organizationDTO.getSlug(); // checks that the slug is either null or valid
		pattern = Pattern.compile("^[a-z0-9_]*$");
		if (slug == null)
			slug = name.replaceAll(" ", "_").replaceAll("-", "_").toLowerCase(); // generated slug is normalized
		else if (slug != null && !pattern.matcher(slug).matches())
			throw new IllegalArgumentException("The slug contains illegal characters (only lowercase alphanumeric characters and underscore are allowed): " + slug);
		organization = organizationRepository.findBySlug(slug);
		if (organization != null) // organization with the same slug already exists
			throw new IllegalArgumentException("An organization with the slug " + slug + " already exists.");
		organizationDTO.setSlug(slug);
		
		organization = organizationRepository.save(new Organization(organizationDTO)); // converts the organization in a format for storing and stores it
		
		// Creates the owner and gives them the ROLE_PROVIDER role
		OrganizationMember owner = new OrganizationMember(organization.getContactsEmail(), organization);
		owner = organizationMemberRepository.save(owner); // stores the owner
		Role role = new Role(OrgManagerUtils.ROOT_ORGANIZATIONS + "/" + organization.getSlug(), OrgManagerUtils.ROLE_PROVIDER, owner, null);
		roleRepository.save(role); // stores the owner's role
		
		// Updates the identity provider
		String userId = utils.getUserId(owner.getUsername()); // ID used by the identity provider for the owner
		utils.idpAddRole(userId, role); // updates the owner's role in the identity provider as well
		
		// Performs the operation in the components
		Map<String, Component> componentMap = (Map<String, Component>) context.getBean("getComponents");
		for (String s : componentMap.keySet()) {
			componentMap.get(s).createUser(owner.getUsername());
			componentMap.get(s).createOrganization(name, owner.getUsername());
		}
		
		return new OrganizationDTO(organization); // re-converts into view format
	}
	
	/**
	 * Updates an organization's info. Only description, contacts and tags may be updated, any other field in the input will be ignored.
	 * 
	 * @param id - The ID of the organization to alter
	 * @param organizationDTO - The organization to alter, in view format
	 * @return - The updated organization
	 */
	public OrganizationDTO updateOrganization(Long id, OrganizationDTO organizationDTO) {
		if (organizationDTO == null)
			return null; // nothing to update
		Organization organization = organizationRepository.getOne(id); // finds the organization to change
		
		// Checks if the user has permission to perform this action
		if (!utils.userHasAdminRights() && !utils.userIsOwner(organization))
			throw new AccessDeniedException("Access is denied: user is not registered as owner of the organization and does not have administrator rights.");
		
		// Only description, contacts and tags may be changed
		// Description
		if (organizationDTO.getDescription() != null) { // the field will only be updated if present in the input
			if (organizationDTO.getDescription().equals(""))
				throw new IllegalArgumentException("The description field cannot be an empty string.");
			organization.setDescription(organizationDTO.getDescription());
		}
		// Contacts
		Contacts contacts = organizationDTO.getContacts();
		if (contacts != null) {
			if (contacts.getEmail() != null) {
				if (contacts.getEmail().equals(""))
					throw new IllegalArgumentException("The e-mail field cannot be an empty string.");
				organization.setContactsEmail(contacts.getEmail());
			}
			if (contacts.getName() != null) {
				if (contacts.getName().equals(""))
					throw new IllegalArgumentException("The name field cannot be an empty string.");
				organization.setContactsName(contacts.getName());
			}
			if (contacts.getSurname() != null) {
				if (contacts.getSurname().equals(""))
					throw new IllegalArgumentException("The surname field cannot be an empty string.");
				organization.setContactsSurname(contacts.getSurname());
			}
			if (contacts.getWeb() != null)
				organization.setContactsWeb(contacts.getWeb());
			if (contacts.getPhone() != null)
				organization.setContactsPhone(contacts.getPhone());
			if (contacts.getLogo() != null)
				organization.setContactsLogo(contacts.getLogo());
		}
		// Tags
		if (organizationDTO.getTag() != null)
			organization.setTag(organizationDTO.getTag());
		
		return new OrganizationDTO(organizationRepository.save(organization)); // Updates the organization and returns the updated view version
	}
	
	/**
	 * Enables the organization, identified by its ID.
	 * 
	 * @param id - ID of the organization to enable
	 * @return - The updated organization
	 */
	public OrganizationDTO enableOrganization(Long id) {
		if (!utils.userHasAdminRights())
			throw new AccessDeniedException("Access is denied: user does not have administrator rights.");
		Organization organization = organizationRepository.getOne(id);
		organization.setActive(true);
		return new OrganizationDTO(organizationRepository.save(organization));
	}
	
	/**
	 * Disables the organization, identified by its ID.
	 * 
	 * @param id - ID of the organization to disable
	 * @return - The updated organization
	 */
	public OrganizationDTO disableOrganization(Long id) {
		if (!utils.userHasAdminRights())
			throw new AccessDeniedException("Access is denied: user does not have administrator rights.");
		Organization organization = organizationRepository.getOne(id);
		organization.setActive(false);
		return new OrganizationDTO(organizationRepository.save(organization));
	}
	
	/**
	 * Deletes the specified organization.
	 * 
	 * @param id - ID of the organization
	 */
	public void deleteOrganization(Long id) {
		if (!utils.userHasAdminRights())
			throw new AccessDeniedException("Access is denied: user does not have administrator rights.");
		
		Organization organization = organizationRepository.getOne(id);
		organization.toString(); // sometimes, even if the organization is not found, getOne will not return null: this line will make it throw EntityNotFoundException
		if (organization != null) {
			// Retrieves the list of members belonging to the organization
			List<OrganizationMember> members = organizationMemberRepository.findByOrganization(organization);
			
			// Maps each member, identified by their ID in the identity provider, to the roles they have in the organization
			Map<String, Set<Role>> memberToRolesToRemove = new HashMap<String, Set<Role>>();
			for (OrganizationMember m : members) {
				String userId = utils.getUserId(m.getUsername()); // ID used by the identity provider
				memberToRolesToRemove.put(userId, roleRepository.findByOrganizationMember(m));
				roleRepository.deleteByOrganizationMember(m); // delete all roles within such organization
			}
			
			tenantRepository.deleteByOrganization(organization); // delete all tenants within such organization
			
			organizationMemberRepository.deleteByOrganization(organization); // all members are removed from the organization
			organizationRepository.delete(organization); // deletes the organization
			
			// Updates roles in the identity provider
			for (String userId : memberToRolesToRemove.keySet())
				utils.idpRemoveRoles(userId, memberToRolesToRemove.get(userId));
			
			// Deletes the organization in the components
			Map<String, Component> componentMap = (Map<String, Component>) context.getBean("getComponents");
			for (String s : componentMap.keySet())
				componentMap.get(s).deleteOrganization(organization.getName());
		}
	}
}
