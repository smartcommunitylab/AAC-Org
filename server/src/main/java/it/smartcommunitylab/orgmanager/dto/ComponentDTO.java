package it.smartcommunitylab.orgmanager.dto;

import java.util.List;
import java.util.stream.Collectors;

import it.smartcommunitylab.aac.model.Role;

public class ComponentDTO {
    private String name;
    private String componentId; // identifies the component
    private List<String> roles; // roles that may be assigned within the component

    public ComponentDTO() {
    }

    public ComponentDTO(String name, String componentId, List<String> roles) {
        this.name = name;
        this.componentId = componentId;
        this.roles = roles;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getComponentId() {
        return componentId;
    }

    public void setComponentId(String componentId) {
        this.componentId = componentId;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public String getPath() {
        return AACRoleDTO.COMPONENTS_PREFIX + componentId;
    }

    @Override
    public String toString() {
        return getPath();
    }

    /*
     * Builder
     */
    public static ComponentDTO from(List<Role> roles) {
        ComponentDTO dto = new ComponentDTO();

        if (roles.size() == 0) {
            return null;
        }

        // fetch from first as component
        String component = roles.get(0).getSpace();

        dto.name = component;
        dto.componentId = component;

        // map roles
        dto.roles = roles.stream().map(r -> r.getRole()).collect(Collectors.toList());

        return dto;
    }

    public static ComponentDTO from(String component, List<String> roles) {
        ComponentDTO dto = new ComponentDTO();
        dto.name = component;
        dto.componentId = component;
        dto.roles = roles;

        return dto;
    }

}
