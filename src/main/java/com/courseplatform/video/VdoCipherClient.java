package com.courseplatform.video;

import com.courseplatform.common.exception.VdoCipherApiException;
import com.courseplatform.config.AppProperties;
import com.courseplatform.video.dto.VdoCipherOtpRequest;
import com.courseplatform.video.dto.VdoCipherOtpResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Component
public class VdoCipherClient {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(VdoCipherClient.class);

    private final RestClient restClient;
    private final AppProperties appProperties;

    public VdoCipherClient(RestClient.Builder restClientBuilder, AppProperties appProperties) {
        this.appProperties = appProperties;
        this.restClient = restClientBuilder
                .baseUrl("https://dev.vdocipher.com/api")
                .build();
    }

    /**
     * Calls VdoCipher server-side video OTP endpoint to request playback credentials.
     * When seed/mock video asset identifiers (e.g. starting with "vdo_") or placeholder secrets are used,
     * returns a mock OTP response so player, resume, and progress tracking work seamlessly in dev.
     *
     * @param vdocipherVideoId The VdoCipher video asset identifier
     * @param userId The authenticated user identifier for audit tracking
     * @return VdoCipherOtpResponse containing otp and playbackInfo
     */
    public VdoCipherOtpResponse generatePlaybackOtp(String vdocipherVideoId, Long userId) {
        if (!StringUtils.hasText(vdocipherVideoId)) {
            throw new IllegalArgumentException("VdoCipher video ID must not be null or empty");
        }

        String apiSecret = appProperties.video().vdocipher().apiSecret();
        if (!StringUtils.hasText(apiSecret)) {
            log.error("VdoCipher API secret is not configured in backend properties");
            throw new VdoCipherApiException("VdoCipher streaming provider configuration is missing");
        }

        long ttl = appProperties.video().vdocipher().otpTtlSeconds() > 0
                ? appProperties.video().vdocipher().otpTtlSeconds()
                : 300L;

        String whitelistUrl = appProperties.video().vdocipher().whitelistUrl();

        // If using seed demo video IDs (starting with "vdo_") or placeholder credentials, generate development OTP
        if (vdocipherVideoId.startsWith("vdo_") || apiSecret.contains("placeholder") || apiSecret.contains("yourVdoCipherApiSecretHere")) {
            log.info("Development video identifier [{}] detected. Generating local playback credentials for testing.", vdocipherVideoId);
            return VdoCipherOtpResponse.builder()
                    .otp("dev_test_otp_" + System.currentTimeMillis())
                    .playbackInfo("eyJ2aWRlb0lkIjoi" + vdocipherVideoId + "\"}")
                    .build();
        }

        VdoCipherOtpRequest requestBody = VdoCipherOtpRequest.builder()
                .ttl(ttl)
                .userId(userId != null ? "user_" + userId : null)
                .whitelist(StringUtils.hasText(whitelistUrl) ? whitelistUrl : null)
                .build();

        log.debug("Requesting playback OTP from VdoCipher for video asset ID {} with TTL {}s", vdocipherVideoId, ttl);

        try {
            VdoCipherOtpResponse response = restClient.post()
                    .uri("/videos/{videoId}/otp", vdocipherVideoId)
                    .header(HttpHeaders.AUTHORIZATION, "Apisecret " + apiSecret)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(VdoCipherOtpResponse.class);

            if (response == null || !StringUtils.hasText(response.getOtp()) || !StringUtils.hasText(response.getPlaybackInfo())) {
                log.error("VdoCipher returned an empty or incomplete OTP response for video ID: {}", vdocipherVideoId);
                throw new VdoCipherApiException("Received invalid response from VdoCipher streaming provider");
            }

            log.info("Successfully generated VdoCipher playback credentials for video asset ID {}", vdocipherVideoId);
            return response;

        } catch (RestClientResponseException ex) {
            log.error("VdoCipher API responded with HTTP status {} for video asset ID {}: {}",
                    ex.getStatusCode(), vdocipherVideoId, ex.getResponseBodyAsString());
            throw new VdoCipherApiException("VdoCipher API rejected OTP generation request with status: " + ex.getStatusCode(), ex);
        } catch (Exception ex) {
            log.error("Failed to connect to VdoCipher API for video asset ID {}: {}", vdocipherVideoId, ex.getMessage());
            throw new VdoCipherApiException("Could not communicate with VdoCipher playback provider", ex);
        }
    }

    /**
     * Fetches all uploaded videos from the VdoCipher account.
     *
     * @param limit Maximum number of videos to fetch (default 100)
     * @return VdoCipherListResponseDto containing count and rows of video metadata
     */
    public com.courseplatform.video.dto.VdoCipherListResponseDto fetchVideosList(int limit) {
        String apiSecret = appProperties.video().vdocipher().apiSecret();
        if (!StringUtils.hasText(apiSecret)) {
            log.error("VdoCipher API secret is missing in backend configuration");
            throw new VdoCipherApiException("VdoCipher API secret is not configured");
        }

        int maxLimit = limit > 0 ? limit : 100;
        try {
            return restClient.get()
                    .uri("/videos?limit={limit}", maxLimit)
                    .header(HttpHeaders.AUTHORIZATION, "Apisecret " + apiSecret)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(com.courseplatform.video.dto.VdoCipherListResponseDto.class);
        } catch (Exception ex) {
            log.error("Failed to query VdoCipher /api/videos: {}", ex.getMessage());
            throw new VdoCipherApiException("Failed to retrieve video catalog from VdoCipher: " + ex.getMessage(), ex);
        }
    }

    /**
     * Fetches metadata for a single video from VdoCipher.
     *
     * @param videoId VdoCipher 32-character video identifier
     * @return VdoCipherVideoItemDto with video metadata
     */
    public com.courseplatform.video.dto.VdoCipherVideoItemDto fetchVideo(String videoId) {
        String apiSecret = appProperties.video().vdocipher().apiSecret();
        if (!StringUtils.hasText(apiSecret)) {
            throw new VdoCipherApiException("VdoCipher API secret is not configured");
        }

        try {
            return restClient.get()
                    .uri("/videos/{videoId}", videoId)
                    .header(HttpHeaders.AUTHORIZATION, "Apisecret " + apiSecret)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(com.courseplatform.video.dto.VdoCipherVideoItemDto.class);
        } catch (Exception ex) {
            log.error("Failed to fetch VdoCipher video metadata for ID {}: {}", videoId, ex.getMessage());
            throw new VdoCipherApiException("Failed to fetch VdoCipher video details: " + ex.getMessage(), ex);
        }
    }
}
