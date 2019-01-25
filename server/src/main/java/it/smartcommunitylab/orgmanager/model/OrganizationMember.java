package it.smartcommunitylab.orgmanager.model;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

import it.smartcommunitylab.orgmanager.dto.OrganizationMemberDTO;

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
	
	public OrganizationMember() {}
	
	public OrganizationMember(String username, Organization organization) {
		this.username = username;
		this.organization = organization;
	}
	
	public OrganizationMember(OrganizationMemberDTO memberDTO) {
		this(memberDTO.getUsername(), null);
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
	
	@Override
	public String toString() {
		return this.getClass().getSimpleName() + " [" + id + "]: Username=" + username;
	}
}
