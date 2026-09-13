package com.courseplatform.repository;

import com.courseplatform.course.CourseEntity;
import com.courseplatform.course.CourseRepository;
import com.courseplatform.course.CourseStatus;
import com.courseplatform.course.VideoEntity;
import com.courseplatform.course.VideoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CourseRepositoryTests {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private VideoRepository videoRepository;

    @Test
    @DisplayName("Verify Flyway seeded course, sections, and pricing")
    void verifySeededCourseAndSections() {
        Optional<CourseEntity> courseOpt = courseRepository.findById(1L);
        assertThat(courseOpt).isPresent();

        CourseEntity course = courseOpt.get();
        assertThat(course.getTitle()).contains("Enterprise Backend Architecture");
        assertThat(course.getPrice()).isEqualByComparingTo(new BigDecimal("4000.00"));
        assertThat(course.getCurrency()).isEqualTo("INR");
        assertThat(course.getStatus()).isEqualTo(CourseStatus.PUBLISHED);
        assertThat(course.getSections()).hasSize(3);

        // Section 1 should have 2 videos
        assertThat(course.getSections().get(0).getVideos()).hasSize(2);
    }

    @Test
    @DisplayName("Verify preview free video lookup vs paid video lookup")
    void verifyFreePreviewVideos() {
        List<VideoEntity> freeVideos = videoRepository.findFreePreviewVideosByCourseId(1L);
        assertThat(freeVideos).hasSize(1);
        assertThat(freeVideos.get(0).isFree()).isTrue();
        assertThat(freeVideos.get(0).getVdocipherVideoId()).isEqualTo("9a8898197d5743b7ac095b97a4ae0536");
    }
}
