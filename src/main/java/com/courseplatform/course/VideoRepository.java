package com.courseplatform.course;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VideoRepository extends JpaRepository<VideoEntity, Long> {

    List<VideoEntity> findBySectionIdOrderByDisplayOrderAsc(Long sectionId);

    Optional<VideoEntity> findByVdocipherVideoId(String vdocipherVideoId);

    List<VideoEntity> findAllByVdocipherVideoId(String vdocipherVideoId);

    @Query("SELECT v FROM VideoEntity v JOIN v.section s WHERE s.course.id = :courseId AND v.isFree = true AND v.status = 'PUBLISHED'")
    List<VideoEntity> findFreePreviewVideosByCourseId(@Param("courseId") Long courseId);

    @Query("SELECT v FROM VideoEntity v JOIN FETCH v.section s JOIN FETCH s.course c WHERE v.id = :videoId")
    Optional<VideoEntity> findVideoWithSectionAndCourse(@Param("videoId") Long videoId);
}
