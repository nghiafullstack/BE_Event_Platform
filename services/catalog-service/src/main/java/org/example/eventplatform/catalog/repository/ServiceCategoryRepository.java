package org.example.eventplatform.catalog.repository;

import org.example.eventplatform.catalog.entity.ServiceCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceCategoryRepository extends JpaRepository<ServiceCategory, Long> {
    boolean existsByName(String name);

    boolean existsByCode(String code);

    Optional<ServiceCategory> findByCode(String code);

    List<ServiceCategory> findByActiveTrue();
}
