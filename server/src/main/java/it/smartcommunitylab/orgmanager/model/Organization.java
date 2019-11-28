package it.smartcommunitylab.orgmanager.model;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.validation.constraints.NotNull;

import it.smartcommunitylab.orgmanager.dto.OrganizationDTO;
import it.smartcommunitylab.orgmanager.dto.OrganizationDTO.Contacts;

@Entity
public class Organization implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private String name;
//	
//	@NotNull
//	@Column(unique=true)
//	private String nameNormalized; // contains the name trimmed and in lower case
//	
//	/**
//	 * Makes the name unique regardless of case and leading/trailing spaces
//	 */
//	@PrePersist @PreUpdate private void normalize() {
//		nameNormalized = name.toLowerCase().trim();
//	}

    @NotNull
    @Column(unique = true)
    private String slug;

    @Lob
    private String description;

    @NotNull
    private String owner;

    private String contactsEmail;

    private String contactsName;

    private String contactsSurname;

    private String contactsWeb;

    private String[] contactsPhone;

    private String contactsLogo;

    private String[] tags;

    private boolean active = true;

    public Organization() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getContactsEmail() {
        return contactsEmail;
    }

    public void setContactsEmail(String contactsEmail) {
        this.contactsEmail = contactsEmail;
    }

    public String getContactsName() {
        return contactsName;
    }

    public void setContactsName(String contactsName) {
        this.contactsName = contactsName;
    }

    public String getContactsSurname() {
        return contactsSurname;
    }

    public void setContactsSurname(String contactsSurname) {
        this.contactsSurname = contactsSurname;
    }

    public String getContactsWeb() {
        return contactsWeb;
    }

    public void setContactsWeb(String contactsWeb) {
        this.contactsWeb = contactsWeb;
    }

    public String[] getContactsPhone() {
        return contactsPhone;
    }

    public void setContactsPhone(String[] contactsPhone) {
        this.contactsPhone = contactsPhone;
    }

    public String getContactsLogo() {
        return contactsLogo;
    }

    public void setContactsLogo(String contactsLogo) {
        this.contactsLogo = contactsLogo;
    }

    public String[] getTags() {
        return tags;
    }

    public void setTags(String[] tags) {
        this.tags = tags;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

//    /**
//     * Copies an array into a new array without any of the original's null elements
//     * or empty strings.
//     * 
//     * @param strings - Array of strings that may contain null elements or empty
//     *                strings
//     * @return - Array copy without any null elements or empty strings
//     */
//    private String[] copyValidStrings(String[] strings) {
//        String[] validStrings = null;
//        if (strings != null) { // elements have been specified
//            int i = 0;
//            for (String s : strings) {
//                if (s != null && !s.trim().equals("")) {
//                    if (validStrings == null)
//                        validStrings = new String[strings.length];
//                    validStrings[i] = s.trim();
//                    i++;
//                }
//            }
//            if (validStrings != null)
//                validStrings = Arrays.copyOfRange(validStrings, 0, i);
//        }
//        return validStrings;
//    }

    @Override
    public String toString() {
        String enabled = active ? "enabled" : "disabled";
        return this.getClass().getSimpleName() + " [" + id + "] (" + enabled + "): Name=" + name + ", Slug=" + slug
                + ", Description=" + description;
    }

    public static Organization from(OrganizationDTO dto) {
        Organization org = new Organization();
        org.name = dto.getName();
        org.slug = dto.getSlug();
        org.description = dto.getDescription();
        org.owner = dto.getOwner();

        // Copies contents of the Contacts object
        Contacts contacts = dto.getContacts(); // in the view version, contacts info is an object, rather
                                               // than being spread in multiple fields
        org.contactsEmail = contacts.getEmail();
        org.contactsName = contacts.getName();
        org.contactsSurname = contacts.getSurname();
        org.contactsWeb = contacts.getWeb();
        org.contactsPhone = contacts.getPhone();
        org.contactsLogo = contacts.getLogo();

        org.tags = dto.getTags();

        org.active = dto.isActive();

        return org;
    }
}
