package it.smartcommunitylab.orgmanager.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import it.smartcommunitylab.orgmanager.model.Organization;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Long> {
	
	List<Organization> findByNameIgnoreCaseContaining(String name, Pageable pageable);
	
	Organization findByNameIgnoreCase(String name);
	
	Organization findBySlug(String slug);
}
