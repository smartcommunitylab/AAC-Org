package it.smartcommunitylab.orgmanager.dto;

import java.util.Collection;
import java.util.HashSet;

import it.smartcommunitylab.orgmanager.model.OrganizationMember;
import it.smartcommunitylab.orgmanager.model.Role;

public class OrganizationMemberDTO {
	private Long id;
	private String username;
	private HashSet<RoleDTO> roles;
	private boolean owner; // true if the member is owner of the organization
	
	public OrganizationMemberDTO() {};
	
	public OrganizationMemberDTO(Long id, String username, HashSet<RoleDTO> roles, boolean owner) {
		this.id = id;
		this.username = username;
		this.roles = roles;
		this.owner = owner;
	}
	
	public OrganizationMemberDTO(OrganizationMember member, Collection<Role> memberRoles) {
		this(member.getId(), member.getUsername(), convertRoles(memberRoles), member.getOwner());
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
	
	public HashSet<RoleDTO> getRoles() {
		return roles;
	}
	
	public void setRoles(HashSet<RoleDTO> roles) {
		this.roles = roles;
	}
	
	public boolean getOwner() {
		return owner;
	}
	
	public void setOwner(boolean owner) {
		this.owner = owner;
	}
	
	/**
	 * Converts roles from model to view.
	 * 
	 * @param roles - Roles to convert
	 * @return - A collection with the converted roles
	 */
	private static HashSet<RoleDTO> convertRoles(Collection<Role> roles) {
		HashSet<RoleDTO> rolesDTO = new HashSet<RoleDTO>();
		if (roles != null) {
			for (Role r : roles)
				rolesDTO.add(new RoleDTO(r));
		}
		return rolesDTO;
	}
	
	@Override
	public String toString() {
		return this.getClass().getSimpleName() + " [" + id + "]: Username=" + username + ", Owner=" + owner;
	}
}
