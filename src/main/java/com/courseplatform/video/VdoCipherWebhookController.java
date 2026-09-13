package com.courseplatform.video;

import com.courseplatform.common.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/videos")
public class VdoCipherWebhookController {

    private static final Logger log = LoggerFactory.getLogger(VdoCipherWebhookController.class);

    private final VdoCipherSyncService vdoCipherSyncService;

    public VdoCipherWebhookController(VdoCipherSyncService vdoCipherSyncService) {
        this.vdoCipherSyncService = vdoCipherSyncService;
    }

    /**
     * Webhook endpoint called automatically by VdoCipher when a video finishes uploading/encoding.
     * URL: /api/v1/videos/webhook/vdocipher
     */
    @PostMapping("/webhook/vdocipher")
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleVdoCipherWebhook(
            @RequestBody(required = false) Map<String, Object> payload
    ) {
        log.info("Received VdoCipher webhook event: {}", payload);

        try {
            VdoCipherSyncService.SyncResult result = vdoCipherSyncService.syncVideos();
            log.info("VdoCipher webhook triggered catalog sync: total={}, created={}, updated={}",
                    result.getTotalProcessed(), result.getCreatedCount(), result.getUpdatedCount());

            Map<String, Object> responseData = Map.of(
                    "status", "success",
                    "processed", result.getTotalProcessed(),
                    "created", result.getCreatedCount(),
                    "updated", result.getUpdatedCount()
            );

            return ResponseEntity.ok(ApiResponse.success("VdoCipher webhook processed successfully", responseData));
        } catch (Exception ex) {
            log.error("Error processing VdoCipher webhook: {}", ex.getMessage(), ex);
            return ResponseEntity.ok(ApiResponse.success("Webhook received with deferred sync", Map.of("status", "deferred")));
        }
    }

    /**
     * Manual 1-click sync trigger endpoint.
     */
    @PostMapping("/sync")
    public ResponseEntity<ApiResponse<Map<String, Object>>> triggerManualSync() {
        log.info("Manual VdoCipher catalog sync initiated");
        VdoCipherSyncService.SyncResult result = vdoCipherSyncService.syncVideos();

        Map<String, Object> responseData = Map.of(
                "status", "success",
                "processed", result.getTotalProcessed(),
                "created", result.getCreatedCount(),
                "updated", result.getUpdatedCount()
        );

        return ResponseEntity.ok(ApiResponse.success("VdoCipher catalog synced successfully", responseData));
    }

    @GetMapping("/sync")
    public ResponseEntity<ApiResponse<Map<String, Object>>> triggerManualSyncGet() {
        return triggerManualSync();
    }
}
