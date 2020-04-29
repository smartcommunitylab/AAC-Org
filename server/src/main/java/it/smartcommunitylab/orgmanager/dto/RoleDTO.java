package it.smartcommunitylab.orgmanager.dto;

import it.smartcommunitylab.orgmanager.common.Constants;

public class RoleDTO {

    public static final String TYPE_ORG = "organization";
    public static final String TYPE_RESOURCE = "resource";
    public static final String TYPE_COMPONENT = "component";
    public static final String TYPE_SPACE = "space";

    private String type;
    private String role;

    private String space;
    private String component;
    private String resource;

    public RoleDTO() {
        type = "";
        role = "";

        space = null;
        component = null;
        resource = null;
    }

//    public RoleDTO(String context, String role) {
//        super();
//        this.role = role;
//        if (context.startsWith(ORG_PREFIX)) {
//            this.type = Constants.ROOT_ORGANIZATIONS;
//            int idx = context.indexOf('/', ORG_PREFIX.length());
//            this.space = idx > 0 ? context.substring(idx + 1) : null;
//        }
//        if (context.startsWith(RESOURCE_PREFIX)) {
//            this.type = Constants.ROOT_RESOURCES;
//            int idx = context.indexOf('/', RESOURCE_PREFIX.length());
//            this.space = idx > 0 ? context.substring(idx + 1) : null;
//        }
//        if (context.startsWith(COMPONENT_PREFIX)) {
//            int idx = context.indexOf('/', COMPONENT_PREFIX.length());
//            this.type = context.substring(0, idx);
//            String sub = context.substring(idx + 1);
//            idx = sub.indexOf('/');
//            this.component = this.type.substring(this.type.indexOf('/') + 1);
//            this.space = idx > 0 ? sub.substring(idx + 1) : null;
//        }
//    }

    public String getSpace() {
        return space;
    }

    public void setSpace(String space) {
        this.space = space;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getComponent() {
        return component;
    }

    public void setComponent(String component) {
        this.component = component;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    @Override
    public String toString() {
        if (TYPE_ORG.equals(type)) {
            return type + ":" + role;
        }

        if (TYPE_SPACE.equals(type)) {
            return type + "/" + space + ":" + role;
        }

        if (TYPE_COMPONENT.equals(type)) {
            return type + "/" + component + "/" + space + ":" + role;
        }

        if (TYPE_RESOURCE.equals(type)) {
            return type + "/" + resource + "/" + space + ":" + role;
        }

        return role;
    }

    /*
     * Builders
     */
    public static RoleDTO from(AACRoleDTO aacRole) {
        // we won't keep org details, roleDTO is bounded to org
        RoleDTO dto = new RoleDTO();
        dto.role = aacRole.getRole();

        // get type and parse
        if (AACRoleDTO.isOrgRole(aacRole, false)) {
            // check if space
            if (AACRoleDTO.isOrgRole(aacRole, true)) {
                // org role
                dto.type = TYPE_ORG;
            } else {
                // space
                if (aacRole.getContext().endsWith(AACRoleDTO.SPACES_PATH)) {
                    dto.type = TYPE_SPACE;
                    dto.space = aacRole.getSpace();
                }
            }
        }

        if (AACRoleDTO.isComponentRole(aacRole)) {
            dto.type = TYPE_COMPONENT;
            dto.space = aacRole.getSpace();

            // need to extract component from context
            dto.component = AACRoleDTO.componentName(aacRole);
        }

        if (AACRoleDTO.isResourceRole(aacRole)) {
            dto.type = TYPE_RESOURCE;
            dto.space = aacRole.getSpace();

            // need to extract resource from context
            dto.resource = AACRoleDTO.componentName(aacRole);
        }

        return dto;

    }

    public static AACRoleDTO to(String organization, RoleDTO dto) {

        String role = dto.getRole();

        // build context and space from type and org
        String context = null;
        String space = null;

        if (TYPE_ORG.equals(dto.getType())) {
            context = Constants.ROOT_ORGANIZATIONS;
            space = organization;
        }

        if (TYPE_SPACE.equals(dto.getType())) {
            context = AACRoleDTO.ORGANIZATION_PREFIX + organization + AACRoleDTO.SPACES_PATH;
            space = dto.getSpace();
        }

        if (TYPE_COMPONENT.equals(dto.getType())) {
            context = AACRoleDTO.COMPONENTS_PREFIX + dto.getComponent();
            space = dto.getSpace();
        }

        if (TYPE_RESOURCE.equals(dto.getType())) {
            context = AACRoleDTO.RESOURCES_PREFIX + dto.getResource();
            space = dto.getSpace();
        }

        return new AACRoleDTO(context, space, role);

    }

}
