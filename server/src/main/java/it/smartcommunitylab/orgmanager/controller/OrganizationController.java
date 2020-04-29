package it.smartcommunitylab.orgmanager.controller;

import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import it.smartcommunitylab.aac.model.Page;
import it.smartcommunitylab.orgmanager.common.Constants;
import it.smartcommunitylab.orgmanager.common.IdentityProviderAPIException;
import it.smartcommunitylab.orgmanager.common.InvalidArgumentException;
import it.smartcommunitylab.orgmanager.common.NoSuchOrganizationException;
import it.smartcommunitylab.orgmanager.common.NoSuchUserException;
import it.smartcommunitylab.orgmanager.common.OrgManagerUtils;
import it.smartcommunitylab.orgmanager.common.SystemException;
import it.smartcommunitylab.orgmanager.dto.OrganizationDTO;
import it.smartcommunitylab.orgmanager.dto.SpaceDTO;
import it.smartcommunitylab.orgmanager.service.OrganizationService;
import it.smartcommunitylab.orgmanager.service.SpaceService;

@RestController
public class OrganizationController {

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private SpaceService spaceService;

    @GetMapping("/api/organizations")
    public Page<OrganizationDTO> listOrganizations() throws IdentityProviderAPIException {
        List<OrganizationDTO> list = organizationService.listOrganizations();
        Page page = new Page<OrganizationDTO>();
        page.setFirst(true);
        page.setLast(true);
        page.setNumber(1);
        page.setNumberOfElements(list.size());
        page.setSize(1000);
        page.setTotalElements(list.size());
        page.setTotalPages(1);

        page.setContent(list);

        return page;
    
    }

    @PostMapping("/api/organizations")
    public OrganizationDTO createOrganization(@RequestBody OrganizationDTO organizationDTO)
            throws SystemException, InvalidArgumentException, IdentityProviderAPIException, NoSuchUserException {
        // extract data
        String name = organizationDTO.getName();
        String owner = organizationDTO.getOwner();
        String slug = organizationDTO.getSlug();

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

        // TODO save the name
        OrganizationDTO organization = organizationService.addOrganization(slug, owner);

        return organization;
    }

    // DEPRECATED
//    @PutMapping("api/organizations/{id}/enable")
//    public OrganizationDTO enableOrganization(@PathVariable long id) throws NoSuchOrganizationException {
//        return organizationService.enableOrganization(id);
//    }
//
//    @PutMapping("api/organizations/{id}/disable")
//    public OrganizationDTO disableOrganization(@PathVariable long id) throws NoSuchOrganizationException {
//        return organizationService.disableOrganization(id);
//    }

    @DeleteMapping("api/organizations/{slug}")
    public void deleteOrganization(@PathVariable String slug)
            throws NoSuchOrganizationException, InvalidArgumentException, SystemException,
            IdentityProviderAPIException {
        organizationService.deleteOrganization(slug);
    }

    @GetMapping("api/organizations/{slug}/spaces")
    public List<String> getSpaces(@PathVariable String slug)
            throws NoSuchOrganizationException, IdentityProviderAPIException {
        return spaceService.getSpaces(slug).stream().map(s -> s.getSlug()).collect(Collectors.toList());
    }

    @PutMapping("api/organizations/{slug}/spaces")
    public List<String> addSpace(@PathVariable String slug, @RequestParam String space)
            throws NoSuchOrganizationException, IdentityProviderAPIException {
        // validate and normalize space
        space = space.trim().replaceAll("\\s+", " ");
        Pattern pattern = Pattern.compile(Constants.SLUG_PATTERN);
        space = pattern.matcher(space).replaceAll("_");

        SpaceDTO s = spaceService.addSpace(slug, space);

        // return all spaces
        return getSpaces(slug);
    }

    @DeleteMapping("api/organizations/{slug}/spaces")
    public List<String> deleteSpace(@PathVariable String slug, @RequestParam String space)
            throws NoSuchOrganizationException, IdentityProviderAPIException {

        spaceService.deleteSpace(slug, space);

        // return all spaces
        return getSpaces(slug);

    }

}
