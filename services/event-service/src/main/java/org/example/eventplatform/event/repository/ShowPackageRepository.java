package org.example.eventplatform.event.repository;

import org.example.eventplatform.event.entity.ShowPackage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShowPackageRepository extends JpaRepository<ShowPackage, Long> {

    List<ShowPackage> findByTenantId(Long tenantId);

    Optional<ShowPackage> findByIdAndTenantId(Long id, Long tenantId);
}
