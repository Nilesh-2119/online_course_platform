package com.courseplatform.video;

import com.courseplatform.auth.security.UserPrincipal;
import com.courseplatform.common.ApiResponse;
import com.courseplatform.video.dto.PlaybackCredentialsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/videos")
public class VideoPlaybackController {

    private final PlaybackService playbackService;

    public VideoPlaybackController(PlaybackService playbackService) {
        this.playbackService = playbackService;
    }

    @GetMapping("/{videoId}/playback")
    public ResponseEntity<ApiResponse<PlaybackCredentialsResponse>> getPlaybackCredentials(
            @PathVariable Long videoId,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        PlaybackCredentialsResponse response = playbackService.getPlaybackCredentials(videoId, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Playback credentials generated successfully", response));
    }
}
