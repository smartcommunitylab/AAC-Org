package it.smartcommunitylab.orgmanager.model;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = {"username", "organization_id"}) })
public class OrganizationMember implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@NotNull
	private String username;
	
	@ManyToOne
	@JoinColumn(name = "organization_id")
	private Organization organization;
	
	@NotNull
	private Long idpId; // ID used by the identity provider to identify the user
	
	public OrganizationMember() {}
	
	public OrganizationMember(String username, Organization organization, Long idpId) {
		this.username = username;
		this.organization = organization;
		this.idpId = idpId;
	}
	
	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}
	
	public String getUsername() {
		return username;
	}
	
	public void setUsername(String username) {
		this.username = username;
	}
	
	public Organization getOrganization() {
		return organization;
	}
	
	public void setOrganization(Organization organization) {
		this.organization = organization;
	}
	
	public Long getIdpId() {
		return idpId;
	}
	
	public void setIdpId(Long idpId) {
		this.idpId = idpId;
	}
	
	@Override
	public String toString() {
		return this.getClass().getSimpleName() + " [" + id + "]: Username=" + username + ", Identity Provider ID=" + idpId;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o != null && (o instanceof OrganizationMember)) {
			OrganizationMember m = (OrganizationMember) o;
			if (username.equals(m.username) && organization.getId() == m.getOrganization().getId())
				return true;
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(username, organization.getId());
	}
}
