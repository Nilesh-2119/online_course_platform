package com.courseplatform.video;

import com.courseplatform.auth.security.UserPrincipal;
import com.courseplatform.common.exception.VdoCipherApiException;
import com.courseplatform.course.CourseAccessService;
import com.courseplatform.course.VideoAccessResult;
import com.courseplatform.course.VideoEntity;
import com.courseplatform.video.dto.PlaybackCredentialsResponse;
import com.courseplatform.video.dto.VdoCipherOtpResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class PlaybackService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PlaybackService.class);

    private final CourseAccessService courseAccessService;
    private final VdoCipherClient vdoCipherClient;

    public PlaybackService(CourseAccessService courseAccessService, VdoCipherClient vdoCipherClient) {
        this.courseAccessService = courseAccessService;
        this.vdoCipherClient = vdoCipherClient;
    }

    /**
     * Authorizes and generates secure playback credentials for video playback.
     * Enforces the complete security chain:
     * 1. User authentication & activity validation
     * 2. Course & video lifecycle verification
     * 3. CourseAccessService purchase/free entitlement verification
     * 4. Upstream server-to-server VdoCipher OTP request with configured TTL and whitelist
     *
     * @param videoId Target internal video ID
     * @param currentUser Authenticated user principal
     * @return PlaybackCredentialsResponse containing only videoId, otp, and playbackInfo
     */
    @Transactional(readOnly = true)
    public PlaybackCredentialsResponse getPlaybackCredentials(Long videoId, UserPrincipal currentUser) {
        Long userId = currentUser != null ? currentUser.getId() : 0L;
        log.info("Processing playback authorization for video ID {} by user {}", videoId, userId);

        // 1. Authoritative entitlement check via CourseAccessService
        VideoAccessResult accessResult = courseAccessService.verifyVideoAccess(videoId, currentUser);
        VideoEntity video = accessResult.getVideo();

        // 2. Validate VdoCipher streaming provider asset ID
        String vdocipherVideoId = video.getVdocipherVideoId();
        if (!StringUtils.hasText(vdocipherVideoId)) {
            log.error("Video ID {} does not have a configured VdoCipher asset identifier", videoId);
            throw new VdoCipherApiException("Video streaming configuration is incomplete for this lesson");
        }

        // 3. Request OTP and playbackInfo from VdoCipher
        VdoCipherOtpResponse otpResponse = vdoCipherClient.generatePlaybackOtp(vdocipherVideoId, userId);

        // 4. Return strictly required playback tokens to frontend player
        return PlaybackCredentialsResponse.builder()
                .videoId(videoId)
                .otp(otpResponse.getOtp())
                .playbackInfo(otpResponse.getPlaybackInfo())
                .build();
    }
}
