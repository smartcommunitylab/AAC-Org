package it.smartcommunitylab.orgmanager.dto;

import it.smartcommunitylab.aac.model.BasicProfile;

public class UserDTO {

    private String id;
    private String username;
    private String fullName;
    private String name;
    private String surname;
    private String emailAddress;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    /*
     * Builders
     */
    public static UserDTO from(BasicProfile profile) {
        UserDTO dto = new UserDTO();
        dto.id = profile.getUserId();
        dto.username = profile.getUsername();
        dto.name = profile.getName();
        dto.surname = profile.getSurname();
        dto.fullName = profile.getName() + " " + profile.getSurname();

        return dto;
    };

}
