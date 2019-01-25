package it.smartcommunitylab.orgmanager.model;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

@Entity
public class Tenant implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@EmbeddedId
	private TenantId tenantId;
	
	@ManyToOne
	@JoinColumn(name = "organization_id")
	private Organization organization;
	
	public Tenant() {}
	
	public Tenant(String componentId, String tenant, Organization organization) {
		this.tenantId = new TenantId(componentId, tenant);
		this.organization = organization;
	}
	
	public TenantId getTenantId() {
		return tenantId;
	}
	
	public void setTenantId(TenantId tenantId) {
		this.tenantId = tenantId;
	}
	
	public Organization getOrganization() {
		return organization;
	}
	
	public void setOrganization(Organization organization) {
		this.organization = organization;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o != null && (o instanceof Tenant)) {
			Tenant t = (Tenant) o;
			if (tenantId.equals(t.getTenantId()))
				return true;
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(tenantId);
	}
	
	public String toString() {
		return tenantId.toString();
	}
	
	@Embeddable
	public static class TenantId implements Serializable {
		private static final long serialVersionUID = 1L;
		
		@Column(name = "component_id")
		@NotNull
		private String componentId;
		
		@Column(name = "name")
		@NotNull
		private String name;
		
		public TenantId() {}
		
		public TenantId(String componentId, String name) {
			this.componentId = componentId.toLowerCase();
			this.name = name.toLowerCase();
		}
		
		public String getComponentId() {
			return componentId;
		}
		
		public void setComponentId(String componentId) {
			this.componentId = componentId.toLowerCase();
		}
		
		public String getName() {
			return name;
		}
		
		public void setName(String name) {
			this.name = name.toLowerCase();
		}
		
		@Override
		public boolean equals(Object o) {
			if (o != null && (o instanceof TenantId)) {
				TenantId tid = (TenantId) o;
				if (componentId.equals(tid.getComponentId()) && name.equals(tid.getName()))
					return true;
			}
			return false;
		}
		
		@Override
		public int hashCode() {
			return Objects.hash(componentId, name);
		}
		
		@Override
		public String toString() {
			return componentId + "/" + name;
		}
	}
}
