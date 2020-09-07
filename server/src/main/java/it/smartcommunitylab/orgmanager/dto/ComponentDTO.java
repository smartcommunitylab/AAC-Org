package it.smartcommunitylab.orgmanager.dto;

import java.util.ArrayList;
import java.util.List;
import it.smartcommunitylab.orgmanager.common.Constants;

public class ComponentDTO extends ModelDTO {
    private String organization; // domain of org
    private String owner;
    // TODO replace with proper SpaceDTO
    private List<SpaceDTO> spaces;

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

    public List<SpaceDTO> getSpaces() {
        return spaces;
    }

    public void setSpaces(List<SpaceDTO> spaces) {
        this.spaces = spaces;
    }

    public String getPath() {
        return Constants.ROOT_COMPONENTS + Constants.PATH_SEPARATOR + id;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
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

    public static ComponentDTO from(String component, String organization, String owner, List<String> roles) {
        ComponentDTO dto = new ComponentDTO();
        dto.name = component;
        dto.id = component;
        dto.organization = organization;
        dto.owner = owner;
        dto.roles = new ArrayList<>();

        if (roles != null && !roles.isEmpty()) {
            dto.roles.addAll(roles);
        }

        return dto;
    }

}
