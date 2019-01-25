package it.smartcommunitylab.orgmanager.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import it.smartcommunitylab.orgmanager.model.Organization;
import it.smartcommunitylab.orgmanager.model.OrganizationMember;

@Repository
public interface OrganizationMemberRepository extends JpaRepository<OrganizationMember, Long> {
	
	List<OrganizationMember> findByUsername(String username);
	
	OrganizationMember findByUsernameAndOrganization(String username, Organization organization);
	
	OrganizationMember findByIdAndOrganization(Long id, Organization organization);
	
	List<OrganizationMember> findByOrganization(Organization organization);

	void deleteByOrganization(Organization organization);

}
