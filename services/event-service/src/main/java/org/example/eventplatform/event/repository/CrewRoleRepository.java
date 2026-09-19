package org.example.eventplatform.event.repository;

import org.example.eventplatform.event.entity.CrewRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CrewRoleRepository extends JpaRepository<CrewRole, Long> {

    List<CrewRole> findByTenantId(Long tenantId);

    Optional<CrewRole> findByIdAndTenantId(Long id, Long tenantId);
}
