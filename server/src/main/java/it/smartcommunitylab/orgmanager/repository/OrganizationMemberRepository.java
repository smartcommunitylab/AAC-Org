package it.smartcommunitylab.orgmanager.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import it.smartcommunitylab.orgmanager.model.Organization;
import it.smartcommunitylab.orgmanager.model.OrganizationMember;

@Repository
public interface OrganizationMemberRepository extends JpaRepository<OrganizationMember, Long> {
	
	List<OrganizationMember> findByUsername(String username);
	
	OrganizationMember findByUsernameAndOrganization(String username, Organization organization);
	
	List<OrganizationMember> findByOrganizationAndUsernameIgnoreCaseContaining(Organization organization, String username);
	
	OrganizationMember findByIdAndOrganization(Long id, Organization organization);
	
	List<OrganizationMember> findByOrganization(Organization organization);
	
	void deleteByOrganization(Organization organization);
	
//	@Query("select m from OrganizationMember m inner join m.roles mr where m.organization=:org and lower(m.username) like lower('%' || :username || '%') and mr.roleId.organizationMemberId=m.id")
//	List<OrganizationMember> findByOrganizationAndUsernameIgnoreCaseContainingJoinRoles(@Param("org") Organization organization, @Param("username") String username);
}
