package it.smartcommunitylab.orgmanager.dto;

import org.springframework.util.StringUtils;

import it.smartcommunitylab.aac.model.Role;

public class OrganizationDTO {
    private String name;
    private String slug; // domain of the space
    private String owner;

    public OrganizationDTO() {
        name = "";
        slug = "";

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
        return AACRoleDTO.ORGANIZATION_PREFIX + slug;
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

            return dto;
        } else {
            return null;
        }
    }
}
