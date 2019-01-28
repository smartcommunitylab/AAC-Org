package it.smartcommunitylab.orgmanager.repository;

import java.util.HashSet;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import it.smartcommunitylab.orgmanager.model.OrganizationMember;
import it.smartcommunitylab.orgmanager.model.Role;
import it.smartcommunitylab.orgmanager.model.Role.RoleId;

@Repository
public interface RoleRepository extends JpaRepository<Role, RoleId> {
	
	HashSet<Role> findByOrganizationMember(OrganizationMember organizationMember);
	
	@Query("select r from Role r where r.organizationMember=?1 and lower(r.roleId.role)!=lower(?2)")
	HashSet<Role> findByOrganizationMemberAndRoleNotIgnoreCase(OrganizationMember organizationMember, String role);
	
	void deleteByOrganizationMember(OrganizationMember organizationMember);
	
	@Query("select r from Role r where r.roleId.contextSpace=?1")
	List<Role> findByContextSpace(String contextSpace);
	
	@Query("select r from Role r where r.roleId.contextSpace=?1 and r.roleId.role=?2")
	List<Role> findByContextSpaceAndRole(String contextSpace, String role);
}
