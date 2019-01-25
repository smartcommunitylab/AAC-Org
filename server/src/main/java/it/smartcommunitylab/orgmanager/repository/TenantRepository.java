package it.smartcommunitylab.orgmanager.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import it.smartcommunitylab.orgmanager.model.Organization;
import it.smartcommunitylab.orgmanager.model.Tenant;

@Repository
public interface TenantRepository extends JpaRepository<Tenant, Long> {
	
	@Query("select t from Tenant t where lower(t.tenantId.componentId)=lower(?1) and lower(t.tenantId.name)=lower(?2)")
	Tenant findByComponentIdAndName(String componentId, String name);
	
	List<Tenant> findByOrganization(Organization organization);
	
	void deleteByOrganization(Organization organization);
}
