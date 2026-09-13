package com.courseplatform.progress;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VideoProgressRepository extends JpaRepository<VideoProgressEntity, Long> {

    Optional<VideoProgressEntity> findByUserIdAndVideoId(Long userId, Long videoId);

    @Query("SELECT vp FROM VideoProgressEntity vp " +
           "JOIN vp.video v " +
           "JOIN v.section s " +
           "WHERE vp.user.id = :userId AND s.course.id = :courseId")
    List<VideoProgressEntity> findUserProgressForCourse(@Param("userId") Long userId, @Param("courseId") Long courseId);

    @Query("SELECT COUNT(vp) FROM VideoProgressEntity vp " +
           "JOIN vp.video v " +
           "JOIN v.section s " +
           "WHERE vp.user.id = :userId AND s.course.id = :courseId AND vp.completed = true")
    long countCompletedVideosInCourse(@Param("userId") Long userId, @Param("courseId") Long courseId);

    List<VideoProgressEntity> findByCompletedTrue();

    @org.springframework.data.jpa.repository.Modifying(clearAutomatically = true)
    @Query("DELETE FROM VideoProgressEntity vp WHERE vp.user.id = :userId")
    int deleteByUserId(@Param("userId") Long userId);
}
