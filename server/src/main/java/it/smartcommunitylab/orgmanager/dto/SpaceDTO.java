package it.smartcommunitylab.orgmanager.dto;

import org.springframework.util.StringUtils;

import it.smartcommunitylab.aac.model.Role;
import it.smartcommunitylab.orgmanager.common.Constants;

public class SpaceDTO {
    private String name;
    private String slug; // domain of the space
    private String organization; // domain of org

    public SpaceDTO() {
        name = "";
        slug = "";
        organization = "";

    }

    public SpaceDTO(String name, String slug, String organization) {
        super();
        this.name = name;
        this.slug = slug;
        this.organization = organization;
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
        return Constants.ROOT_ORGANIZATIONS + organization + Constants.PATH_SEPARATOR + slug;
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
                    .substring(Constants.ROOT_ORGANIZATIONS.length() + 1)
                    .replace(Constants.PATH_SEPARATOR + Constants.ROOT_SPACES, "");

            return dto;
        } else {
            return null;
        }
    }

}
