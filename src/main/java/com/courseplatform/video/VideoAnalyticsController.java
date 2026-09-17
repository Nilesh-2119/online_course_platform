package com.courseplatform.video;

import com.courseplatform.auth.security.UserPrincipal;
import com.courseplatform.common.ApiResponse;
import com.courseplatform.user.UserEntity;
import com.courseplatform.user.UserRepository;
import com.courseplatform.video.dto.RecordVideoViewRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/analytics")
public class VideoAnalyticsController {

    private static final Logger log = LoggerFactory.getLogger(VideoAnalyticsController.class);

    private final VideoViewRepository videoViewRepository;
    private final UserRepository userRepository;

    public VideoAnalyticsController(VideoViewRepository videoViewRepository, UserRepository userRepository) {
        this.videoViewRepository = videoViewRepository;
        this.userRepository = userRepository;
    }

    @PostMapping("/video-view")
    public ResponseEntity<ApiResponse<Map<String, Object>>> recordVideoView(
            @Valid @RequestBody RecordVideoViewRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser,
            HttpServletRequest httpRequest
    ) {
        String videoType = request.getVideoType() != null ? request.getVideoType().trim().toUpperCase() : "UNKNOWN";
        String videoId = request.getVideoId() != null ? request.getVideoId().trim() : null;

        UserEntity user = null;
        if (currentUser != null && currentUser.getId() != null) {
            user = userRepository.findById(currentUser.getId()).orElse(null);
        }

        String ipAddress = httpRequest.getHeader("X-Forwarded-For");
        if (!StringUtils.hasText(ipAddress) || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = httpRequest.getRemoteAddr();
        } else if (ipAddress.contains(",")) {
            ipAddress = ipAddress.split(",")[0].trim();
        }
        if (ipAddress != null && ipAddress.length() > 64) {
            ipAddress = ipAddress.substring(0, 64);
        }

        String userAgent = httpRequest.getHeader("User-Agent");
        if (userAgent != null && userAgent.length() > 512) {
            userAgent = userAgent.substring(0, 512);
        }

        VideoViewEntity entity = new VideoViewEntity(videoType, videoId, user, ipAddress, userAgent);
        videoViewRepository.save(entity);

        log.info("Recorded video view: type={}, videoId={}, userId={}, ip={}",
                videoType, videoId, user != null ? user.getId() : "ANONYMOUS", ipAddress);

        return ResponseEntity.ok(ApiResponse.success("Video view recorded successfully", Map.of(
                "recorded", true,
                "videoType", videoType
        )));
    }
}
