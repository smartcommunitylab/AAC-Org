package it.smartcommunitylab.orgmanager.dto;

import it.smartcommunitylab.orgmanager.common.Constants;

public class RoleDTO {
	private static final String ORG_PREFIX = Constants.ROOT_ORGANIZATIONS + "/";
	private static final String RESOURCE_PREFIX = Constants.ROOT_RESOURCES + "/";
	private static final String COMPONENT_PREFIX = Constants.ROOT_COMPONENTS + "/";

	private String space; // contains both the context and the space of the domain
	private String role;
	private String type;
	private String component;
	
	public RoleDTO() {}

	public RoleDTO(String context, String role) {
		super();
		this.role = role;
		if (context.startsWith(ORG_PREFIX)) {
			this.type = Constants.ROOT_ORGANIZATIONS;
			int idx = context.indexOf('/', ORG_PREFIX.length()); 
			this.space = idx > 0 ? context.substring(idx + 1) : null;
		}
		if (context.startsWith(RESOURCE_PREFIX)) {
			this.type = Constants.ROOT_RESOURCES;
			int idx = context.indexOf('/', RESOURCE_PREFIX.length()); 
			this.space = idx > 0 ? context.substring(idx + 1) : null;
		}
		if (context.startsWith(COMPONENT_PREFIX)) {
			int idx = context.indexOf('/', COMPONENT_PREFIX.length()); 
			this.type = context.substring(0, idx);
			String sub = context.substring(idx + 1);
			idx = sub.indexOf('/');
			this.component = this.type.substring(this.type.indexOf('/')  +1);
			this.space = idx > 0 ? context.substring(idx + 1) : null;
		}
	}

	public String getSpace() {
		return space;
	}

	public void setSpace(String space) {
		this.space = space;
	}

	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}

	public String getRole() {
		return role;
	}
	
	public void setRole(String role) {
		this.role = role;
	}
	
	/**
	 * @return the component
	 */
	public String getComponent() {
		return component;
	}

	/**
	 * @param component the component to set
	 */
	public void setComponent(String component) {
		this.component = component;
	}

	@Override
	public String toString() {
		return type +"/" + space + ":" + role;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((role == null) ? 0 : role.hashCode());
		result = prime * result + ((space == null) ? 0 : space.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RoleDTO other = (RoleDTO) obj;
		if (role == null) {
			if (other.role != null)
				return false;
		} else if (!role.equals(other.role))
			return false;
		if (space == null) {
			if (other.space != null)
				return false;
		} else if (!space.equals(other.space))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}
	
}
