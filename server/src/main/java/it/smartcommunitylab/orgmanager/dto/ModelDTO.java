package it.smartcommunitylab.orgmanager.dto;

import java.util.Collections;
import java.util.List;

public class ModelDTO {
    protected String name;
    protected String id;
    protected List<String> roles;

    public ModelDTO() {
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

}
