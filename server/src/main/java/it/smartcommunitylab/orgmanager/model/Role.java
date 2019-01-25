package it.smartcommunitylab.orgmanager.model;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.validation.constraints.NotNull;

import it.smartcommunitylab.orgmanager.dto.RoleDTO;

@Entity
public class Role implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@EmbeddedId
	private RoleId roleId;
	
	@MapsId("organizationMemberId")
	@ManyToOne
	private OrganizationMember organizationMember;
	
	private String componentId;
	
	public Role() {}
	
	/**
	 * Parameter organizationMember must not be null if the role is to be written to database.
	 * If the role will not be written to database, it is acceptable for organizationMember to be null.
	 * 
	 * @param contextSpace - Space to save the role in
	 * @param role - Name of the role (ROLE_PROVIDER, ROLE_USER, etc.)
	 * @param organizationMember - Owner of the role
	 */
	public Role(String contextSpace, String role, OrganizationMember organizationMember, String componentId) {
		Long id = null;
		if (organizationMember != null)
			id = organizationMember.getId();
		roleId = new RoleId(contextSpace, role, id);
		this.organizationMember = organizationMember;
		this.componentId = componentId;
	}
	
	public Role(RoleDTO roleDTO, OrganizationMember organizationMember) {
		this(roleDTO.getContextSpace(), roleDTO.getRole(), organizationMember, roleDTO.extractComponentId());
	}
	
	public RoleId getRoleId() {
		return roleId;
	}
	
	public void setRoleId(RoleId roleId) {
		this.roleId = roleId;
	}
	
	public OrganizationMember getOrganizationMember() {
		return organizationMember;
	}
	
	public String getComponentId() {
		return componentId;
	}
	
	@Override
	public String toString() {
		return roleId.toString();
	}
	
	/**
	 * Returns a string representing the role in a simple form, without the full tenant path.
	 * 
	 * @return - String representing the role in a simple form, without the full tenant path
	 */
	public String getSpaceRole() {
		String roleString = this.toString();
		return roleString.substring(roleString.lastIndexOf("/")+1);
	}
	
	@Override
	public boolean equals(Object o) {
		if (o != null && (o instanceof Role)) {
			Role r = (Role) o;
			if (roleId.equals(r.roleId))
				return true;
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(roleId);
	}
	
	@Embeddable
	public static class RoleId implements Serializable {
		private static final long serialVersionUID = 1L;
		
		@NotNull
		private String contextSpace;
		
		@NotNull
		private String role;
		
		@NotNull
		private Long organizationMemberId;
		
		public RoleId() {}
		
		public RoleId(String contextSpace, String role, Long organizationMemberId) {
			this.contextSpace = contextSpace;
			this.role = role;
			this.organizationMemberId = organizationMemberId;
		}
		
		public String getContextSpace() {
			return contextSpace;
		}
		
		public void setContextSpace(String contextSpace) {
			this.contextSpace = contextSpace;
		}
		
		public String getRole() {
			return role;
		}
		
		public void setRole(String role) {
			this.role = role;
		}
		
		public Long getOrganizationMemberId() {
			return organizationMemberId;
		}
		
		public void setOrganizationMemberId(Long organizationMemberId) {
			this.organizationMemberId = organizationMemberId;
		}
		
		@Override
		public boolean equals(Object o) {
			if (o != null && (o instanceof RoleId)) {
				RoleId rid = (RoleId) o;
				if (contextSpace.equals(rid.contextSpace) && role.equals(rid.role))
					return true;
			}
			return false;
		}
		
		@Override
		public int hashCode() {
			return Objects.hash(contextSpace, role);
		}
		
		@Override
		public String toString() {
			return contextSpace + ":" + role;
		}
	}
}
