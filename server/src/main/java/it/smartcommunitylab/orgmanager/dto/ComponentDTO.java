package it.smartcommunitylab.orgmanager.dto;

import java.util.ArrayList;
import java.util.List;
import it.smartcommunitylab.orgmanager.common.Constants;

public class ComponentDTO extends ModelDTO {
    private String organization; // domain of org

    private List<String> spaces;

    public ComponentDTO() {
        super();
        spaces = null;
    }

    public ComponentDTO(String name, String componentId, String organization, List<String> roles) {
        super(name, componentId, roles);
        this.organization = organization;
        spaces = null;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public List<String> getSpaces() {
        return spaces;
    }

    public void setSpaces(List<String> spaces) {
        this.spaces = spaces;
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

    public static ComponentDTO from(String component, String organization, List<String> roles) {
        ComponentDTO dto = new ComponentDTO();
        dto.name = component;
        dto.id = component;
        dto.organization = organization;
        dto.roles = new ArrayList<>();

        if (roles != null && !roles.isEmpty()) {
            dto.roles.addAll(roles);
        }

        return dto;
    }

}
