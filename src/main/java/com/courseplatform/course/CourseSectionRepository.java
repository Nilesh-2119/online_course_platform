package com.courseplatform.course;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface CourseSectionRepository extends JpaRepository<CourseSectionEntity, Long> {

    List<CourseSectionEntity> findByCourseIdOrderByDisplayOrderAsc(Long courseId);

    @Query("SELECT DISTINCT s FROM CourseSectionEntity s LEFT JOIN FETCH s.videos v WHERE s.course.id = :courseId ORDER BY s.displayOrder ASC")
    List<CourseSectionEntity> findSectionsWithVideosByCourseId(@Param("courseId") Long courseId);
}
