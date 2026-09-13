package com.courseplatform.resource;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FreeResourceRepository extends JpaRepository<FreeResourceEntity, Long> {

    List<FreeResourceEntity> findByStatusOrderByCreatedAtDesc(ResourceStatus status);

    Page<FreeResourceEntity> findByStatusOrderByCreatedAtDesc(ResourceStatus status, Pageable pageable);

    List<FreeResourceEntity> findAllByOrderByCreatedAtDesc();
}
