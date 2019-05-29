package it.smartcommunitylab.orgmanager.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import it.smartcommunitylab.orgmanager.model.Organization;
import it.smartcommunitylab.orgmanager.model.OrganizationMember;

@Repository
public interface OrganizationMemberRepository extends JpaRepository<OrganizationMember, Long> {
	
	List<OrganizationMember> findByOrganization(Organization organization);
	
	List<OrganizationMember> findByUsername(String username);
	
	OrganizationMember findByUsernameAndOrganization(String username, Organization organization);
	
	OrganizationMember findByIdpIdAndOrganization(Long id, Organization organization);
	
	List<OrganizationMember> findByOrganizationAndOwner(Organization organization, boolean owner);
	
	void deleteByOrganization(Organization organization);
	
	@Query("select m.organization.id from OrganizationMember m where m.idpId=?1 and owner=true")
	Collection<Long> findOwnedOrganizations(Long idpId);
}
