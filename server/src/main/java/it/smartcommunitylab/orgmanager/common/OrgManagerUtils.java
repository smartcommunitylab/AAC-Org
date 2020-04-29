package it.smartcommunitylab.orgmanager.common;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.stereotype.Service;

import it.smartcommunitylab.aac.model.Role;
import it.smartcommunitylab.orgmanager.dto.AACRoleDTO;
import it.smartcommunitylab.orgmanager.dto.UserRightsDTO;

@Service
public class OrgManagerUtils {

    /**
     * Returns true if the authenticated user has administrator rights. Only
     * administrators may perform certain operations, such as
     * creating/disabling/enabling/deleting organizations, configuring tenants, or
     * granting/revoking owner status.
     * 
     * @return - true if the authenticated user has administrator rights, false
     *         otherwise
     */
    public static boolean userHasAdminRights() {
        // user has admin rights if they are admin or have the organization
        // management scope
        return (userHasOrganizationMgmtScope() || userIsAdmin());
    }

    /**
     * Returns true if the authenticated user is admin. User is admin if they have
     * organizations:ROLE_PROVIDER role.
     * 
     * @return - True if the authenticated user is admin, false otherwise
     */
    private static boolean userIsAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String orgManager = new AACRoleDTO(null, Constants.ROOT_ORGANIZATIONS, Constants.ROLE_PROVIDER).getAuthority();
        return authentication.getAuthorities().stream().anyMatch(ga -> orgManager.equals(ga.getAuthority()));
    }

    /**
     * Checks whether or not the authenticated user has the organization management
     * scope.
     * 
     * @return - true if the authenticated user has the organization management
     *         scope, false otherwise
     */
    private static boolean userHasOrganizationMgmtScope() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof OAuth2Authentication) {
            OAuth2Request request = ((OAuth2Authentication) authentication).getOAuth2Request();
            if (request != null) {
                Set<String> scopeSet = request.getScope();
                if (scopeSet != null) {
                    // searches for the scope
                    return scopeSet.stream()
                            .anyMatch(s -> s.equalsIgnoreCase(Constants.SCOPE_ORG_MANAGEMENT));
                }
            }
        }
        return false;
    }

    /*
     * Ownership
     */

    /**
     * Returns true if the authenticated user is owner of the space
     * 
     * @param organization - The organization whose owner might be the authenticated
     *                     user
     * @return - true if the authenticated user is owner, false otherwise
     */
    public static boolean userIsOwner(String context, String space) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String orgOwner = AACRoleDTO.ownerRole(context, space).getAuthority();
        return authentication.getAuthorities().stream().anyMatch(ga -> orgOwner.equals(ga.getAuthority()));
    }

    /**
     * Returns true if the authenticated user is owner of the organization
     * 
     * @param organization - The organization whose owner might be the authenticated
     *                     user
     * @return - true if the authenticated user is owner, false otherwise
     */
    public static boolean userIsOwner(String organization) {
        return userIsOwner(AACRoleDTO.ORGANIZATION_PREFIX, organization);
    }

    /**
     * Returns true if the authenticated user is owner of the space
     * 
     * @param organization - The organization whose owner might be the authenticated
     *                     user
     * @return - true if the authenticated user is owner, false otherwise
     */
    public static boolean userIsProvider(String context, String space) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String orgOwner = AACRoleDTO.providerRole(context, space).getAuthority();
        return authentication.getAuthorities().stream().anyMatch(ga -> orgOwner.equals(ga.getAuthority()));
    }

    /**
     * Returns true if the authenticated user is owner of the organization
     * 
     * @param organization - The organization whose owner might be the authenticated
     *                     user
     * @return - true if the authenticated user is owner, false otherwise
     */
    public static boolean userIsProvider(String organization) {
        return userIsProvider(AACRoleDTO.ORGANIZATION_PREFIX, organization);
    }

    /*
     * Membership
     */
    public static boolean userIsMember(String context, String space) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Role orgMember = AACRoleDTO.memberRole(context, space);
        // any role is valid for membership
        return authentication.getAuthorities().stream()
                .anyMatch(ga -> AACRoleDTO.matchContextSpace(orgMember, ga.getAuthority()));
    }

    public static boolean userIsMember(String organization) {
        return userIsMember(AACRoleDTO.ORGANIZATION_PREFIX, organization);
    }

    /**
     * Returns an object that indicates whether or not the authenticated user has
     * administrator rights and a list of IDs of organizations the user is owner of.
     * 
     * @return - Object that contains the authenticated user's rights
     */
    public static UserRightsDTO getUserRights() {
        return new UserRightsDTO(getAuthenticatedUserName(),
                userHasAdminRights(),
                findOwnedOrganizations());
    }

    /**
     * Returns the ID used by the identity provider to identify the currently
     * authenticated user.
     * 
     * @return - ID used by the identity provider to identify the currently
     *         authenticated user
     * @throws IdentityProviderAPIException
     */
    @SuppressWarnings("unchecked")
    public static String getAuthenticatedUserId() throws IdentityProviderAPIException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        // claims
        if (!(auth instanceof OAuth2Authentication)) { // cannot find token value, needed to find the ID
            throw new IdentityProviderAPIException(
                    "Unable to call identity provider's API to retrieve authenticated user's name.");
        }
        OAuth2Authentication oauth = (OAuth2Authentication) auth;
        Map<String, Object> claims = (Map<String, Object>) oauth.getUserAuthentication().getDetails();
        if (claims == null || !claims.containsKey("sub")) {
            throw new IdentityProviderAPIException("Incorrect OAuth2 claims.");
        }

        return claims.get("sub").toString();
    }

    /**
     * Returns the user name used by the identity provider to identify the currently
     * authenticated user.
     * 
     * @return - User name used by the identity provider to identify the currently
     *         authenticated user
     */
    public static String getAuthenticatedUserName() {
        Authentication obj = SecurityContextHolder.getContext().getAuthentication(); // retrieves token
        return obj.getPrincipal().toString();
    }

    /**
     * @return
     */
    public static Collection<String> findOwnedOrganizations() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getAuthorities().stream()
                .map(ga -> AACRoleDTO.parse(ga.getAuthority()))
                .filter(r -> Constants.ROOT_ORGANIZATIONS.equals(r.getContext())
                        && Constants.ROLE_PROVIDER.equals(r.getRole()))
                .map(r -> r.getSpace())
                .collect(Collectors.toSet());
    }

}
