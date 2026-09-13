package com.courseplatform.course;

import com.courseplatform.auth.security.UserPrincipal;
import com.courseplatform.common.ApiResponse;
import com.courseplatform.course.dto.CourseDetailResponse;
import com.courseplatform.course.dto.CourseSectionResponse;
import com.courseplatform.course.dto.CourseSummaryResponse;
import com.courseplatform.course.dto.VideoMetadataResponse;
import com.courseplatform.user.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @GetMapping("/courses")
    public ResponseEntity<ApiResponse<List<CourseSummaryResponse>>> getPublishedCourses() {
        List<CourseSummaryResponse> courses = courseService.getAllPublishedCourses();
        return ResponseEntity.ok(ApiResponse.success(courses));
    }

    @GetMapping("/courses/{courseId}")
    public ResponseEntity<ApiResponse<CourseDetailResponse>> getCourseDetail(
            @PathVariable Long courseId,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        Long userId = currentUser != null ? currentUser.getId() : null;
        boolean isAdmin = currentUser != null && currentUser.getRole() == UserRole.ADMIN;

        CourseDetailResponse course = courseService.getCourseDetail(courseId, userId, isAdmin);
        return ResponseEntity.ok(ApiResponse.success(course));
    }

    @GetMapping("/courses/{courseId}/sections")
    public ResponseEntity<ApiResponse<List<CourseSectionResponse>>> getCourseSections(
            @PathVariable Long courseId,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        Long userId = currentUser != null ? currentUser.getId() : null;
        boolean isAdmin = currentUser != null && currentUser.getRole() == UserRole.ADMIN;

        List<CourseSectionResponse> sections = courseService.getCourseSections(courseId, userId, isAdmin);
        return ResponseEntity.ok(ApiResponse.success(sections));
    }

    @GetMapping("/sections/{sectionId}/videos")
    public ResponseEntity<ApiResponse<List<VideoMetadataResponse>>> getSectionVideos(
            @PathVariable Long sectionId,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        Long userId = currentUser != null ? currentUser.getId() : null;
        boolean isAdmin = currentUser != null && currentUser.getRole() == UserRole.ADMIN;

        List<VideoMetadataResponse> videos = courseService.getSectionVideos(sectionId, userId, isAdmin);
        return ResponseEntity.ok(ApiResponse.success(videos));
    }
}
