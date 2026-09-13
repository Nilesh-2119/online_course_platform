package com.courseplatform.progress;

import com.courseplatform.auth.security.UserPrincipal;
import com.courseplatform.common.ApiResponse;
import com.courseplatform.progress.dto.CourseProgressResponse;
import com.courseplatform.progress.dto.UpdateProgressRequest;
import com.courseplatform.progress.dto.VideoProgressResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class ProgressController {

    private final ProgressService progressService;

    public ProgressController(ProgressService progressService) {
        this.progressService = progressService;
    }

    @GetMapping("/videos/{videoId}/progress")
    public ResponseEntity<ApiResponse<VideoProgressResponse>> getVideoProgress(
            @PathVariable Long videoId,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        VideoProgressResponse response = progressService.getVideoProgress(videoId, currentUser);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/videos/{videoId}/progress")
    public ResponseEntity<ApiResponse<VideoProgressResponse>> updateVideoProgress(
            @PathVariable Long videoId,
            @Valid @RequestBody UpdateProgressRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        VideoProgressResponse response = progressService.updateVideoProgress(videoId, request, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Progress saved successfully", response));
    }

    @GetMapping("/courses/{courseId}/progress")
    public ResponseEntity<ApiResponse<CourseProgressResponse>> getCourseProgress(
            @PathVariable Long courseId,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        CourseProgressResponse response = progressService.getCourseProgress(courseId, currentUser);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
