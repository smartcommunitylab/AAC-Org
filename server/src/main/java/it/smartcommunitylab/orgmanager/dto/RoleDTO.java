package it.smartcommunitylab.orgmanager.dto;

import java.util.Comparator;
import java.util.regex.Pattern;

import javax.validation.constraints.NotNull;

import org.springframework.util.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import it.smartcommunitylab.orgmanager.common.Constants;
import it.smartcommunitylab.orgmanager.service.ComponentService;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class RoleDTO implements Comparable<RoleDTO> {

    public static final String TYPE_ORG = "organization";
    public static final String TYPE_RESOURCE = "resource";
    public static final String TYPE_COMPONENT = "component";
    public static final String TYPE_SPACE = "space";

    @NotNull
    private String type;
    @NotNull
    private String role;

    private String space;
    private String component;
    private String resource;

    public RoleDTO() {
        type = null;
        role = null;

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

    @JsonIgnore
    public boolean isOrgRole() {
        return TYPE_ORG.equals(type);
    }

    @JsonIgnore
    public boolean isSpaceRole() {
        return (TYPE_SPACE.equals(type) && StringUtils.hasText(space));
    }

    @JsonIgnore
    public boolean isComponentRole() {
        return (TYPE_COMPONENT.equals(type) && StringUtils.hasText(component));
    }

    @JsonIgnore
    public boolean isResourceRole() {
        return (TYPE_RESOURCE.equals(type) && StringUtils.hasText(resource));
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
     * Comparator
     */
    @Override
    public int compareTo(RoleDTO r) {
        int c = 1;
        if (type != null) {
            c = r.getType() == null ? 1 : type.compareTo(r.getType());
            if (c != 0) {
                return c;
            }
        }
        if (space != null) {
            c = r.getSpace() == null ? 1 : space.compareTo(r.getSpace());
            if (c != 0) {
                return c;
            }
        }
        if (component != null) {
            c = r.getComponent() == null ? 1 : component.compareTo(r.getComponent());
            if (c != 0) {
                return c;
            }
        }
        if (role != null) {
            c = r.getRole() == null ? 1 : role.compareTo(r.getRole());
            return c;
        }
        return 1;
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
                if (aacRole.getContext().endsWith(Constants.ROOT_SPACES)) {
                    dto.type = TYPE_SPACE;
                    dto.space = aacRole.getSpace();
                }
            }
        }

        if (AACRoleDTO.isComponentRole(aacRole)) {
            dto.type = TYPE_COMPONENT;

            // need to extract component from context
            dto.component = AACRoleDTO.componentName(aacRole);

            // need to unpack space from organization
            // we treat separator as literal
            String[] ss = aacRole.getSpace().split(Pattern.quote(Constants.SLUG_SEPARATOR), 2);
            dto.space = ss.length > 1 ? ss[1] : ss[0];

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
            context = Constants.ROOT_ORGANIZATIONS + Constants.PATH_SEPARATOR + organization + Constants.ROOT_SPACES;
            space = dto.getSpace();
        }

        if (TYPE_COMPONENT.equals(dto.getType())) {
            context = Constants.ROOT_COMPONENTS + Constants.PATH_SEPARATOR + dto.getComponent();
            // note this matches componentService handling
            space = organization + Constants.SLUG_SEPARATOR + dto.getSpace();
        }

        // TODO define mapping
        if (TYPE_RESOURCE.equals(dto.getType())) {
            context = Constants.ROOT_RESOURCES + Constants.PATH_SEPARATOR + dto.getResource();
            space = dto.getSpace();
        }

        return new AACRoleDTO(context, space, role);

    }

}
