package it.smartcommunitylab.orgmanager.controller;

import java.util.Set;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import it.smartcommunitylab.orgmanager.common.Constants;
import it.smartcommunitylab.orgmanager.common.IdentityProviderAPIException;
import it.smartcommunitylab.orgmanager.common.InvalidArgumentException;
import it.smartcommunitylab.orgmanager.common.NoSuchOrganizationException;
import it.smartcommunitylab.orgmanager.common.OrgManagerUtils;
import it.smartcommunitylab.orgmanager.common.SystemException;
import it.smartcommunitylab.orgmanager.dto.OrganizationDTO;
import it.smartcommunitylab.orgmanager.dto.OrganizationDTO.Contacts;
import it.smartcommunitylab.orgmanager.service.OrganizationService;

@RestController
public class OrganizationController {

    @Autowired
    private OrganizationService organizationService;

    @GetMapping("/api/organizations")
    public Page<OrganizationDTO> listOrganizations(String name, Pageable pageable) {
        return organizationService.listOrganizations(name, pageable);
    }

    @PostMapping("/api/organizations")
    public OrganizationDTO createOrganization(@RequestBody OrganizationDTO organizationDTO)
            throws SystemException, InvalidArgumentException {
        // extract data
        String name = organizationDTO.getName();
        String description = organizationDTO.getDescription();
        String owner = organizationDTO.getOwner();
        String slug = organizationDTO.getSlug();
        Contacts contacts = organizationDTO.getContacts();
        String[] tags = organizationDTO.getTags();

        // validate
        if (owner.isEmpty()) {
            // set current user as owner
            owner = OrgManagerUtils.getAuthenticatedUserName();
        }
        // normalizes the name
        name = name.trim().replaceAll("\\s+", " ");
        // checks if the name contains illegal characters
        Pattern pattern = Pattern.compile(Constants.NAME_PATTERN);
        if (!pattern.matcher(name).matches()) {
            throw new InvalidArgumentException("Organization name " + name
                    + " is not allowed, please use only alphanumeric characters, space ( ), dash (-) or underscore (_).");
        }

        // checks that the slug is either null or valid
        if (slug == null || slug.equals("")) {
            // generated slug is normalized
            slug = name.replaceAll(" ", "_").replaceAll("-", "_").toLowerCase();
        }
        // validate slug
        pattern = Pattern.compile(Constants.SLUG_PATTERN);
        if (!pattern.matcher(slug).matches()) {
            throw new InvalidArgumentException(
                    "The slug contains illegal characters (only lowercase alphanumeric characters and underscore are allowed): "
                            + slug);
        }

        OrganizationDTO organization = organizationService.createOrganization(
                name, slug, description,
                owner, contacts, tags);

        return organization;
    }

    @PutMapping("/api/organizations/{id}/info")
    public OrganizationDTO updateOrganization(@PathVariable long id, @RequestBody OrganizationDTO organizationDTO)
            throws NoSuchOrganizationException {
        // extract data
        String description = organizationDTO.getDescription();
        Contacts contacts = organizationDTO.getContacts();
        String[] tags = organizationDTO.getTags();

        // no validation
        OrganizationDTO organization = organizationService.updateOrganization(
                id,
                description, contacts, tags);

        return organization;
    }

    @PutMapping("api/organizations/{id}/enable")
    public OrganizationDTO enableOrganization(@PathVariable long id) throws NoSuchOrganizationException {
        return organizationService.enableOrganization(id);
    }

    @PutMapping("api/organizations/{id}/disable")
    public OrganizationDTO disableOrganization(@PathVariable long id) throws NoSuchOrganizationException {
        return organizationService.disableOrganization(id);
    }

    @DeleteMapping("api/organizations/{id}")
    public void deleteOrganization(@PathVariable long id)
            throws NoSuchOrganizationException, InvalidArgumentException, SystemException {
        organizationService.deleteOrganization(id);
    }

    @GetMapping("api/organizations/{id}/spaces")
    public Set<String> getSpaces(@PathVariable long id) throws NoSuchOrganizationException, IdentityProviderAPIException {
    	return organizationService.getOrgSpaces(id);
    }
    @PutMapping("api/organizations/{id}/spaces")
    public Set<String> addSpace(@PathVariable long id, @RequestParam String space) throws NoSuchOrganizationException, IdentityProviderAPIException {
    	return organizationService.addOrgSpace(id, space);
    }
    @DeleteMapping("api/organizations/{id}/spaces")
    public Set<String> deleteSpace(@PathVariable long id, @RequestParam String space) throws NoSuchOrganizationException, IdentityProviderAPIException {
    	return organizationService.deleteOrgSpace(id, space);
    }
    
}
