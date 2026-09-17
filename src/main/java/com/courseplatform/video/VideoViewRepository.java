package com.courseplatform.video;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface VideoViewRepository extends JpaRepository<VideoViewEntity, Long> {

    long countByVideoTypeIgnoreCase(String videoType);

    List<VideoViewEntity> findByViewedAtAfter(Instant viewedAt);

    List<VideoViewEntity> findByVideoTypeIgnoreCaseAndViewedAtAfter(String videoType, Instant viewedAt);
}
