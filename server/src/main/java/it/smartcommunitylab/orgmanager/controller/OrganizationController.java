package it.smartcommunitylab.orgmanager.controller;

import java.util.Arrays;
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
    public OrganizationDTO addOrganization(
            @RequestBody @Valid OrganizationDTO organizationDTO)
            throws SystemException, InvalidArgumentException, IdentityProviderAPIException, NoSuchUserException {
        // extract data
        String name = organizationDTO.getName();
        String ownerId = organizationDTO.getOwner();
        String slug = organizationDTO.getSlug();

        // validate
        if (ownerId.isEmpty()) {
            // set current user as owner
            ownerId = OrgManagerUtils.getAuthenticatedUserId();
        }

        if (name != null) {
            // normalizes the name
            name = name.trim().replaceAll("\\s+", " ");
        }
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
        OrganizationDTO organization = orgManager.addOrganization(slug, ownerId);

        return organization;
    }

    @PutMapping("api/organizations/{slug}")
    public OrganizationDTO createOrUpdateOrganization(
            @PathVariable @Valid @Pattern(regexp = Constants.SLUG_PATTERN) String slug,
            @RequestBody(required = false) OrganizationDTO organizationDTO)
            throws IdentityProviderAPIException, NoSuchUserException, InvalidArgumentException {

        // set current user as owner
        String ownerId = OrgManagerUtils.getAuthenticatedUserId();

        // extract data
        String name = organizationDTO.getName();

        if (name != null) {
            // normalizes the name
            name = name.trim().replaceAll("\\s+", " ");
        }

        try {
            return orgManager.updateOrganization(slug, ownerId, name);
        } catch (NoSuchOrganizationException noex) {
            // TODO save the name
            return orgManager.addOrganization(slug, ownerId);
        }

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
    public List<SpaceDTO> listSpaces(
            @PathVariable @Valid @Pattern(regexp = Constants.SLUG_PATTERN) String slug,
            @RequestParam(name = "id", required = false) String[] ids)
            throws NoSuchOrganizationException, IdentityProviderAPIException {

        if (ids == null) {
            return orgManager.listSpaces(slug);
        } else {
            List<String> filterIds = Arrays.asList(ids);
            return orgManager.listSpaces(slug).stream().filter(s -> filterIds.contains(s.getId()))
                    .collect(Collectors.toList());
        }
    }

    @PostMapping("api/organizations/{slug}/spaces")
    public SpaceDTO addSpace(
            @PathVariable @Valid @Pattern(regexp = Constants.SLUG_PATTERN) String slug,
            @RequestBody @Valid SpaceDTO space)
            throws NoSuchOrganizationException, IdentityProviderAPIException, NoSuchUserException {
        // set current user as owner
        String ownerId = OrgManagerUtils.getAuthenticatedUserId();

        // extract data
        String id = space.getId();
        String name = space.getName();

        if (name != null) {
            // normalizes the name
            name = name.trim().replaceAll("\\s+", " ");
        }

        // TODO save name
        return orgManager.addSpace(slug, id, ownerId);

    }

    @PutMapping("api/organizations/{slug}/spaces/{id}")
    public SpaceDTO createOrUpdateSpace(
            @PathVariable @Valid @Pattern(regexp = Constants.SLUG_PATTERN) String slug,
            @PathVariable @Valid @Pattern(regexp = Constants.SLUG_PATTERN) String id,
            @RequestBody(required = false) SpaceDTO space)
            throws NoSuchOrganizationException, IdentityProviderAPIException, NoSuchUserException,
            InvalidArgumentException {

        // set current user as owner
        String ownerId = OrgManagerUtils.getAuthenticatedUserId();

        // extract data
        String name = space.getName();

        if (name != null) {
            // normalizes the name
            name = name.trim().replaceAll("\\s+", " ");
        }

        try {
            return orgManager.updateSpace(slug, id, ownerId, name);
        } catch (NoSuchSpaceException noex) {
            // TODO save the name
            return orgManager.addSpace(slug, id, ownerId);
        }

    }

    @DeleteMapping("api/organizations/{slug}/spaces/{id}")
    public void deleteSpace(
            @PathVariable @Valid @Pattern(regexp = Constants.SLUG_PATTERN) String slug,
            @PathVariable @Valid @Pattern(regexp = Constants.SLUG_PATTERN) String id,
            @RequestParam(required = false, defaultValue = "false") boolean cleanup)
            throws NoSuchOrganizationException, IdentityProviderAPIException, NoSuchSpaceException {

        orgManager.deleteSpace(slug, id, cleanup);

    }

    @GetMapping("api/organizations/{slug}/spaces/{id}")
    public SpaceDTO getSpace(
            @PathVariable @Valid @Pattern(regexp = Constants.SLUG_PATTERN) String slug,
            @PathVariable @Valid @Pattern(regexp = Constants.SLUG_PATTERN) String id)
            throws NoSuchSpaceException, InvalidArgumentException, SystemException,
            IdentityProviderAPIException {
        return orgManager.getSpace(slug, id);
    }

}
