package it.smartcommunitylab.orgmanager.dto;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

import it.smartcommunitylab.orgmanager.model.Organization;

public class OrganizationDTO {
	private Long id;
	private String name;
	private String slug; // domain of the organization
	private String description;
	private Contacts contacts;
	private String[] tag;
	private boolean active; // only disabled organizations may be deleted
	
	public OrganizationDTO() {
		this(null, null, null, new Contacts());
	}
	
	public OrganizationDTO(String name, String slug, String description, Contacts contacts) {
		this(name, slug, description, contacts, null);
	}
	
	public OrganizationDTO(String name, String slug, String description, Contacts contacts, String[] tag) {
		this(null, name, slug, description, contacts, tag, true);
	}
	
	public OrganizationDTO(Long id, String name, String slug, String description, Contacts contacts, String[] tag, boolean active) {
		this.id = id;
		this.name = name;
		this.slug = slug;
		this.description = description;
		this.contacts = contacts;
		this.tag = tag;
		this.active = active;
	}
	
	public OrganizationDTO(OrganizationDTO org) {
		this(new Long(org.getId().longValue()), org.getName(), org.getSlug(), org.getDescription(), new Contacts(org.getContacts()),
				org.getTag() != null ? Arrays.copyOf(org.getTag(), org.getTag().length) : new String[0],
				org.getActive());
	}
	
	public OrganizationDTO(Organization org) {
		this(new Long(org.getId().longValue()), org.getName(), org.getSlug(), org.getDescription(),
				// Creates Contacts object
				new Contacts(org.getContactsEmail(), org.getContactsName(), org.getContactsSurname(), org.getContactsWeb(), org.getContactsPhone() != null ? org.getContactsPhone() : new String[0], org.getContactsLogo()),
				org.getTag() != null ? Arrays.copyOf(org.getTag(), org.getTag().length) : new String[0],
				org.getActive());
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
	
	public Contacts getContacts() {
		return contacts;
	}
	
	public void setContacts(Contacts contacts) {
		this.contacts = contacts;
	}
	
	public String[] getTag() {
		return tag;
	}
	
	public void setTag(String[] tag) {
		this.tag = tag != null ? Arrays.copyOf(tag, tag.length) : null;
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
		return this.getClass().getSimpleName() + " [" + id + "] (" + enabled + "): Name=" + name + ", Slug=" + slug + ", Description=" + description + ", Contacts=" + contacts;
	}
	
	public static class Contacts {
		private String email; // at creation of the organization, email will be interpreted as the owner of the organization
		private String name;
		private String surname;
		private String web;
		private String[] phone;
		private String logo;
		
		public Contacts() {}
		
		public Contacts(String email, String name, String surname, String web, String[] phone, String logo) {
			this.email = email;
			this.name = name;
			this.surname = surname;
			this.web = web;
			this.phone = phone != null ? Arrays.copyOf(phone, phone.length) : new String[0];
			this.logo = logo;
		}
		
		public Contacts(Contacts c) {
			this(c.getEmail(), c.getName(), c.getSurname(), c.getWeb(), c.getPhone(), c.getLogo());
		}
		
		public String getEmail() {
			return email;
		}
		
		public void setEmail(String email) {
			this.email = email;
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
		
		public String getWeb() {
			return web;
		}
		
		public void setWeb(String web) {
			this.web = web;
		}
		
		public String[] getPhone() {
			return phone;
		}
		
		public void setPhone(String[] phone) {
			this.phone = phone;
		}
		
		public String getLogo() {
			return logo;
		}
		
		public void setLogo(String logo) {
			this.logo = logo;
		}
		
		@Override
		public String toString() {
			return "[E-mail" + email + ", Name=" + name + ", Surname=" + surname + "]";
		}
	}
}
