package com.courseplatform.resource;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResourceFileRepository extends JpaRepository<ResourceFileEntity, String> {
}
