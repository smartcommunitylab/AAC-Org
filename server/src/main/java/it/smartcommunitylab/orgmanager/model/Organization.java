package it.smartcommunitylab.orgmanager.model;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
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
	
	@NotNull
	@Column(unique=true)
	private String nameNormalized; // contains the name trimmed and in lower case
	
	/**
	 * Makes the name unique regardless of case and leading/trailing spaces
	 */
	@PrePersist @PreUpdate private void normalize() {
		nameNormalized = name.toLowerCase().trim();
	}
	
	@NotNull
	@Column(unique=true)
	private String slug;
	
	@Lob
	@NotNull
	private String description;
	
	@NotNull
	private String contactsEmail;
	
	@NotNull
	private String contactsName;
	
	@NotNull
	private String contactsSurname;
	
	private URL contactsWeb;
	
	private String[] contactsPhone;
	
	private URL contactsLogo;
	
	private String[] tag;
	
	private boolean active = true;
	
	public Organization() {}
	
	public Organization(OrganizationDTO organizationDTO) {
		name = organizationDTO.getName();
		slug = organizationDTO.getSlug();
		description = organizationDTO.getDescription();
		
		// Copies contents of the Contacts object
		Contacts contacts = organizationDTO.getContacts(); // in the view version, contacts info is an object, rather than being spread in multiple fields
		contactsEmail = contacts.getEmail();
		contactsName = contacts.getName();
		contactsSurname = contacts.getSurname();
		
		URL web = contacts.getWeb();
		try {
			contactsWeb = web != null ? new URL(web.toString()) : null;
		} catch (MalformedURLException e) { /* does nothing */ }
		
		String[] phone = contacts.getPhone();
		contactsPhone = phone != null ? Arrays.copyOf(phone, phone.length) : null;
		
		URL logo = contacts.getLogo();
		try {
			contactsLogo = logo != null ? new URL(logo.toString()) : null;
		} catch (MalformedURLException e) { /* does nothing */ }
		
		// Creates a copy of the tags array
		String[] tags = organizationDTO.getTag();
		tag = tags != null ? Arrays.copyOf(tags, tags.length) : null;
		
		active = organizationDTO.getActive();
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
	
	public URL getContactsWeb() {
		return contactsWeb;
	}
	
	public void setContactsWeb(URL contactsWeb) {
		this.contactsWeb = contactsWeb;
	}
	
	public String[] getContactsPhone() {
		return contactsPhone;
	}
	
	public void setContactsPhone(String[] contactsPhone) {
		this.contactsPhone = contactsPhone;
	}
	
	public URL getContactsLogo() {
		return contactsLogo;
	}
	
	public void setContactsLogo(URL contactsLogo) {
		this.contactsLogo = contactsLogo;
	}
	
	public String[] getTag() {
		return tag;
	}
	
	public void setTag(String[] tag) {
		this.tag = tag;
	}
	
	public boolean getActive() {
		return active;
	}
	
	public void setActive(boolean active) {
		this.active = active;
	}
	
	@Override
	public String toString() {
		String enabled = active ? "enabled" : "disabled";
		return this.getClass().getSimpleName() + " [" + id + "] (" + enabled + "): Name=" + name + ", Slug=" + slug + ", Description=" + description;
	}
}