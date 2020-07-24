package it.smartcommunitylab.orgmanager.controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.validation.Valid;
import javax.validation.constraints.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
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
import it.smartcommunitylab.orgmanager.common.NoSuchSpaceException;
import it.smartcommunitylab.orgmanager.common.NoSuchUserException;
import it.smartcommunitylab.orgmanager.common.OrgManagerUtils;
import it.smartcommunitylab.orgmanager.common.SystemException;
import it.smartcommunitylab.orgmanager.dto.OrganizationDTO;
import it.smartcommunitylab.orgmanager.dto.SpaceDTO;
import it.smartcommunitylab.orgmanager.manager.OrganizationManager;

@RestController
@Validated
public class OrganizationController {

    @Autowired
    private OrganizationManager orgManager;

//
//    @GetMapping("api/organizations")
//    public Page<OrganizationDTO> listOrganizations() throws IdentityProviderAPIException {
//        List<OrganizationDTO> list = organizationService.listOrganizations();
//        Page page = new Page<OrganizationDTO>();
//        page.setFirst(true);
//        page.setLast(true);
//        page.setNumber(1);
//        page.setNumberOfElements(list.size());
//        page.setSize(1000);
//        page.setTotalElements(list.size());
//        page.setTotalPages(1);
//
//        page.setContent(list);
//
//        return page;
//
//    }

    /*
     * Org
     */
    @GetMapping("api/organizations")
    public List<OrganizationDTO> listOrganizations() throws IdentityProviderAPIException {
        return orgManager.listOrganizations();

    }

    @PostMapping("api/organizations")
    public OrganizationDTO createOrganization(
            @RequestBody @Valid OrganizationDTO organizationDTO)
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
//        // checks if the name contains illegal characters
//        Pattern pattern = Pattern.compile(Constants.NAME_PATTERN);
//        if (!pattern.matcher(name).matches()) {
//            throw new InvalidArgumentException("Organization name " + name
//                    + " is not allowed, please use only alphanumeric characters, space ( ), dash (-) or underscore (_).");
//        }
//
//        // checks that the slug is either null or valid
//        if (slug == null || slug.equals("")) {
//            // generated slug is normalized
//            slug = name.replaceAll(" ", "_").replaceAll("-", "_").toLowerCase();
//        }
//        // validate slug
//        pattern = Pattern.compile(Constants.SLUG_PATTERN);
//        if (!pattern.matcher(slug).matches()) {
//            throw new InvalidArgumentException(
//                    "The slug contains illegal characters (only lowercase alphanumeric characters and underscore are allowed): "
//                            + slug);
//        }

        // TODO save the name
        OrganizationDTO organization = orgManager.addOrganization(slug, owner);

        return organization;
    }

    @PutMapping("api/organizations/{slug}")
    public OrganizationDTO addOrganization(
            @PathVariable @Valid @Pattern(regexp = Constants.SLUG_PATTERN) String slug)
            throws IdentityProviderAPIException, NoSuchUserException, InvalidArgumentException {

        // set current user as owner
        String owner = OrgManagerUtils.getAuthenticatedUserName();

//        // validate slug
//        Pattern pattern = Pattern.compile(Constants.SLUG_PATTERN);
//        if (!pattern.matcher(slug).matches()) {
//            throw new InvalidArgumentException(
//                    "The slug contains illegal characters (only lowercase alphanumeric characters and underscore are allowed): "
//                            + slug);
//        }

        OrganizationDTO organization = orgManager.addOrganization(slug, owner);

        return organization;

    }

    @GetMapping("api/organizations/{slug}")
    public OrganizationDTO getOrganization(
            @PathVariable @Valid @Pattern(regexp = Constants.SLUG_PATTERN) String slug)
            throws NoSuchOrganizationException, InvalidArgumentException, SystemException,
            IdentityProviderAPIException {
        return orgManager.getOrganization(slug);
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
    public void deleteOrganization(
            @PathVariable @Valid @Pattern(regexp = Constants.SLUG_PATTERN) String slug,
            @RequestParam(required = false, defaultValue = "false") boolean cleanup)
            throws NoSuchOrganizationException, InvalidArgumentException, SystemException,
            IdentityProviderAPIException {
        orgManager.deleteOrganization(slug, cleanup);
    }

    /*
     * Org spaces
     */

    @GetMapping("api/organizations/{slug}/spaces")
    public List<String> getSpaces(
            @PathVariable @Valid @Pattern(regexp = Constants.SLUG_PATTERN) String slug)
            throws NoSuchOrganizationException, IdentityProviderAPIException {
        return orgManager.listSpaces(slug).stream().map(s -> s.getSlug()).collect(Collectors.toList());
    }

    @PostMapping("api/organizations/{slug}/spaces")
    public List<String> addSpaces(
            @PathVariable @Valid @Pattern(regexp = Constants.SLUG_PATTERN) String slug,
            @RequestBody @Valid Collection<@Pattern(regexp = Constants.SLUG_PATTERN) String> spaces)
            throws NoSuchOrganizationException, IdentityProviderAPIException, NoSuchUserException {
        // set current user as owner
        String owner = OrgManagerUtils.getAuthenticatedUserName();

        List<String> list = new ArrayList<>();
        for (String space : spaces) {
            SpaceDTO s = orgManager.addSpace(slug, space, owner);

            list.add(s.getSlug());
        }

        return list;
    }

    @PutMapping("api/organizations/{slug}/spaces")
    public String addSpace(
            @PathVariable @Valid @Pattern(regexp = Constants.SLUG_PATTERN) String slug,
            @RequestParam @Valid @Pattern(regexp = Constants.SLUG_PATTERN) String space)
            throws NoSuchOrganizationException, IdentityProviderAPIException, NoSuchUserException,
            InvalidArgumentException {

        // set current user as owner
        String owner = OrgManagerUtils.getAuthenticatedUserName();

        SpaceDTO s = orgManager.addSpace(slug, space, owner);

        return s.getSlug();
    }

    @DeleteMapping("api/organizations/{slug}/spaces")
    public List<String> deleteSpace(
            @PathVariable @Valid @Pattern(regexp = Constants.SLUG_PATTERN) String slug,
            @RequestParam @Valid @Pattern(regexp = Constants.SLUG_PATTERN) String space,
            @RequestParam(required = false, defaultValue = "false") boolean cleanup)
            throws NoSuchOrganizationException, IdentityProviderAPIException, NoSuchSpaceException {

        orgManager.deleteSpace(slug, space, cleanup);

        // return all spaces
        return getSpaces(slug);

    }

}
