package it.smartcommunitylab.orgmanager.dto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import it.smartcommunitylab.orgmanager.common.Constants;

public class ModelDTO {

    @Pattern(regexp = Constants.NAME_PATTERN)
    protected String name;
    @NotNull
    @Pattern(regexp = Constants.SLUG_PATTERN)
    protected String id;
    protected List<String> roles;

    public ModelDTO() {
        this.name = null;
        this.id = null;
        this.roles = Collections.emptyList();
    }

    public ModelDTO(String name, String id, List<String> roles) {
        this.name = name;
        this.id = id;
        this.roles = roles;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    @Override
    public String toString() {
        return this.id;
    }

    public static ModelDTO from(String component, List<String> roles) {
        ModelDTO dto = new ModelDTO();
        dto.name = component;
        dto.id = component;
        dto.roles = new ArrayList<>();

        if (roles != null && !roles.isEmpty()) {
            dto.roles.addAll(roles);
        }

        return dto;
    }
}
