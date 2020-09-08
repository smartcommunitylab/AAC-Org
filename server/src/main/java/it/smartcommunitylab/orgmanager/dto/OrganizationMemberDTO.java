package it.smartcommunitylab.orgmanager.dto;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;

import it.smartcommunitylab.aac.model.BasicProfile;
import it.smartcommunitylab.orgmanager.common.Constants;

public class OrganizationMemberDTO {

    @NotNull
    private String id;
    private String username;
    private String fullName;
    private Collection<RoleDTO> roles;
    private boolean owner; // true if the member is owner of the organization
    private String organization; // domain of org

    public OrganizationMemberDTO() {
        id = null;
        username = null;
        roles = Collections.emptySet();
        owner = false;
        organization = null;
    };

    public OrganizationMemberDTO(String id, String username, Collection<RoleDTO> roles, boolean owner) {
        this.id = id;
        this.username = username;
        this.roles = roles;
        this.owner = owner;
        this.fullName = null;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Collection<RoleDTO> getRoles() {
        if (roles == null)
            roles = new HashSet<>();
        return roles;
    }

    public void setRoles(Collection<RoleDTO> roles) {
        this.roles = roles;
    }

    public boolean getOwner() {
        return owner;
    }

    public void setOwner(boolean owner) {
        this.owner = owner;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + " [" + id + "]: Username=" + username + ", Owner=" + owner;
    }

    /*
     * Builders
     */
    public static OrganizationMemberDTO from(String organization, BasicProfile profile, List<AACRoleDTO> roles) {
        OrganizationMemberDTO dto = new OrganizationMemberDTO();
        dto.organization = organization;
        dto.id = profile.getUserId();
        dto.username = profile.getUsername();
        dto.fullName = profile.getName() + " " + profile.getSurname();
        if (roles != null) {
            dto.roles = roles.stream()
                    .map(r -> RoleDTO.from(r))
                    .sorted()
                    .collect(Collectors.toSet());
            ;

            dto.owner = dto.roles.stream().anyMatch(r -> RoleDTO.TYPE_ORG.equals(r.getType())
                    && r.getSpace() == null && Constants.ROLE_OWNER.equals(r.getRole()));
        }

        return dto;
    };

//    public static OrganizationMemberDTO from(User user) {
//        OrganizationMemberDTO dto = new OrganizationMemberDTO();
//        dto.id = user.getUserId();
//        dto.username = user.getUsername();
//        if (user.getRoles() != null) {
//            dto.roles = user.getRoles().stream()
//                    .map(r -> new RoleDTO(r.canonicalSpace(), r.getRole()))
//                    .collect(Collectors.toSet());
//
//            dto.owner = dto.roles.stream().anyMatch(r -> r.getType().equals(Constants.ROOT_ORGANIZATIONS)
//                    && r.getSpace() == null && r.getRole().equals(Constants.ROLE_OWNER));
//        }
//
//        return dto;
//    };
}
