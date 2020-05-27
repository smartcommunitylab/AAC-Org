package it.smartcommunitylab.orgmanager.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import it.smartcommunitylab.aac.model.Role;
import it.smartcommunitylab.orgmanager.common.Constants;

public class ComponentDTO extends ModelDTO {

    public ComponentDTO() {
        super();
    }

    public ComponentDTO(String name, String componentId, List<String> roles) {
        super(name, componentId, roles);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public String getPath() {
        return Constants.ROOT_COMPONENTS + Constants.PATH_SEPARATOR + id;
    }

    @Override
    public String toString() {
        return getPath();
    }

    /*
     * Builder
     */
//    public static ComponentDTO from(List<Role> roles) {
//        ComponentDTO dto = new ComponentDTO();
//
//        if (roles.size() == 0) {
//            return null;
//        }
//
//        // fetch from first as component
//        String component = roles.get(0).getSpace();
//
//        dto.name = component;
//        dto.id = component;
//
//        // map roles
//        dto.roles = roles.stream().map(r -> r.getRole()).collect(Collectors.toList());
//
//        return dto;
//    }

    public static ComponentDTO from(String component, List<String> roles) {
        ComponentDTO dto = new ComponentDTO();
        dto.name = component;
        dto.id = component;
        dto.roles = new ArrayList<>();

        if (roles != null && !roles.isEmpty()) {
            dto.roles.addAll(roles);
        }

        return dto;
    }

}
