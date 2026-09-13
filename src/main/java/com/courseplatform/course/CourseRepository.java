package com.courseplatform.course;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<CourseEntity, Long> {

    List<CourseEntity> findByStatus(CourseStatus status);

    @Query("SELECT c FROM CourseEntity c LEFT JOIN FETCH c.sections s WHERE c.id = :id AND c.status = 'PUBLISHED'")
    Optional<CourseEntity> findPublishedCourseWithSections(@Param("id") Long id);
}
