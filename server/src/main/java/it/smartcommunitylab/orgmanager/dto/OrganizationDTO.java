package it.smartcommunitylab.orgmanager.dto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.springframework.util.StringUtils;

import it.smartcommunitylab.aac.model.Role;
import it.smartcommunitylab.orgmanager.common.Constants;

public class OrganizationDTO {

    @NotNull
    @Pattern(regexp = Constants.SLUG_PATTERN)
    private String id; // domain of the space

    @Pattern(regexp = Constants.NAME_PATTERN)
    private String name;

    @Pattern(regexp = Constants.SLUG_PATTERN)
    private String slug; // domain of the space
    private String owner;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getPath() {
        return Constants.ROOT_ORGANIZATIONS + Constants.PATH_SEPARATOR + slug;
    }

    @Override
    public String toString() {
        return getPath();
    }

    /*
     * Builder
     */
    public static OrganizationDTO from(String owner, Role role) {
        if (AACRoleDTO.isOrgRole(role, true) && StringUtils.hasText(role.getSpace())) {
            OrganizationDTO dto = new OrganizationDTO();
            // TODO extract from attributes when available
            dto.name = role.getSpace();
            dto.slug = role.getSpace();
            dto.owner = owner;
            dto.id = dto.slug;

            return dto;
        } else {
            return null;
        }
    }
}
