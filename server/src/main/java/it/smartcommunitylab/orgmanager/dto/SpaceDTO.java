package it.smartcommunitylab.orgmanager.dto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.springframework.util.StringUtils;

import it.smartcommunitylab.aac.model.Role;
import it.smartcommunitylab.orgmanager.common.Constants;

public class SpaceDTO {

    @Pattern(regexp = Constants.NAME_PATTERN)
    private String name;
    @NotNull
    @Pattern(regexp = Constants.SLUG_PATTERN)
    private String id; // domain of the space
    private String organization; // domain of org
    private String owner;

    public SpaceDTO() {
        name = null;
        id = null;
        organization = null;
        owner = null;

    }

    public SpaceDTO(String name, String slug, String organization, String ownerId) {
        super();
        this.name = name;
        this.id = slug;
        this.organization = organization;
        this.owner = ownerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getPath() {
        return Constants.ROOT_ORGANIZATIONS + Constants.PATH_SEPARATOR + organization + Constants.PATH_SEPARATOR + id;
    }

    @Override
    public String toString() {
        return getPath();
    }

    /*
     * Builder
     */
    public static SpaceDTO from(String organization, String id, String name) {
        SpaceDTO dto = new SpaceDTO();
        // TODO extract from attributes when available
        dto.name = name;
        dto.id = id;
        dto.organization = organization;

        return dto;
    }

    public static SpaceDTO from(Role role, String ownerId) {
        if (AACRoleDTO.isOrgRole(role, false) && StringUtils.hasText(role.getSpace())) {
            SpaceDTO dto = new SpaceDTO();
            // TODO extract from attributes when available
            dto.name = role.getSpace();
            dto.id = role.getSpace();
            dto.organization = role.getContext()
                    .substring(Constants.ROOT_ORGANIZATIONS.length() + 1)
                    .replace(Constants.PATH_SEPARATOR + Constants.ROOT_SPACES, "");

            dto.owner = ownerId;

            return dto;
        } else {
            return null;
        }
    }

}
