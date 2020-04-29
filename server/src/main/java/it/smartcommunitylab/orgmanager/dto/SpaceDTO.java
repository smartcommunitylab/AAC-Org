package it.smartcommunitylab.orgmanager.dto;

import org.springframework.util.StringUtils;

import it.smartcommunitylab.aac.model.Role;

public class SpaceDTO {
    private String name;
    private String slug; // domain of the space
    private String organization; // domain of org

    public SpaceDTO() {
        name = "";
        slug = "";
        organization = "";

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

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public String getPath() {
        return AACRoleDTO.ORGANIZATION_PREFIX + organization + "/" + slug;
    }

    @Override
    public String toString() {
        return getPath();
    }

    /*
     * Builder
     */
    public static SpaceDTO from(Role role) {
        if (AACRoleDTO.isOrgRole(role, false) && StringUtils.hasText(role.getSpace())) {
            SpaceDTO dto = new SpaceDTO();
            // TODO extract from attributes when available
            dto.name = role.getSpace();
            dto.slug = role.getSpace();
            dto.organization = role.getContext()
                    .substring(AACRoleDTO.ORGANIZATION_PREFIX.length())
                    .replace("/" + AACRoleDTO.SPACES_PATH, "");

            return dto;
        } else {
            return null;
        }
    }

}
