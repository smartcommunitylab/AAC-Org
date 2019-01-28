package it.smartcommunitylab.orgmanager.dto;

import java.util.Objects;

import it.smartcommunitylab.orgmanager.model.Role;

public class RoleDTO {
	private String contextSpace; // contains both the context and the space of the domain
	private String role;
	
	public RoleDTO() {}
	
	public RoleDTO(String contextSpace, String role) {
		this.contextSpace = contextSpace;
		this.role = role;
	}
	
	public RoleDTO(Role role) {
		this(role.getRoleId().getContextSpace(), role.getRoleId().getRole());
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
	
	public String extractComponentId() {
		String componentId = null;
		// An example contextSpace may look like this: components/nifi/trento (nifi is the component ID)
		// Sometimes, it may look like this: organizations/my_org (the component ID is missing)
		int firstSlash = contextSpace.indexOf("/");
		int secondSlash = contextSpace.indexOf("/", firstSlash + 1);
		if (secondSlash != -1) // component ID is missing
			componentId = contextSpace.substring(firstSlash + 1, secondSlash);
		return componentId;
	}
	
	@Override
	public String toString() {
		return contextSpace + ":" + role;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o != null && (o instanceof RoleDTO)) {
			RoleDTO roleDTO = (RoleDTO) o;
			if (contextSpace.equals(roleDTO.getContextSpace()) && role.equals(roleDTO.getRole()))
				return true;
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(getContextSpace(), getRole());
	}
}
