package com.courseplatform.video;

import com.courseplatform.course.CourseEntity;
import com.courseplatform.course.CourseRepository;
import com.courseplatform.course.CourseSectionEntity;
import com.courseplatform.course.CourseSectionRepository;
import com.courseplatform.course.VideoEntity;
import com.courseplatform.course.VideoRepository;
import com.courseplatform.course.VideoStatus;
import com.courseplatform.video.dto.VdoCipherListResponseDto;
import com.courseplatform.video.dto.VdoCipherVideoItemDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class VdoCipherSyncService {

    private static final Logger log = LoggerFactory.getLogger(VdoCipherSyncService.class);
    private static final Pattern ORDER_PREFIX_PATTERN = Pattern.compile("^\\s*([0-9]+)\\s*[-._)\\s]+(.*)$");
    private static final Pattern FILE_EXT_PATTERN = Pattern.compile("(?i)\\.(mp4|mov|mkv|webm|avi|m4v)$");

    private final VdoCipherClient vdoCipherClient;
    private final VideoRepository videoRepository;
    private final CourseSectionRepository courseSectionRepository;
    private final CourseRepository courseRepository;
    private final org.springframework.core.env.Environment environment;
    private volatile long lastSyncTimestamp = 0;

    public VdoCipherSyncService(VdoCipherClient vdoCipherClient,
                                VideoRepository videoRepository,
                                CourseSectionRepository courseSectionRepository,
                                CourseRepository courseRepository,
                                org.springframework.core.env.Environment environment) {
        this.vdoCipherClient = vdoCipherClient;
        this.videoRepository = videoRepository;
        this.courseSectionRepository = courseSectionRepository;
        this.courseRepository = courseRepository;
        this.environment = environment;
    }

    /**
     * Automatic sync on application startup to ensure database matches VdoCipher catalog.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        if (java.util.Arrays.asList(environment.getActiveProfiles()).contains("test")) {
            log.debug("Skipping VdoCipher startup sync in test profile environment.");
            return;
        }

        try {
            log.info("Starting initial VdoCipher course catalog synchronization...");
            SyncResult result = syncVideos();
            log.info("VdoCipher initial sync completed: total={}, updated={}, new={}",
                    result.getTotalProcessed(), result.getUpdatedCount(), result.getCreatedCount());
        } catch (Exception ex) {
            log.warn("Automatic VdoCipher catalog sync on startup deferred: {}", ex.getMessage());
        }
    }

    /**
     * Syncs videos if the cache/sync is older than the specified duration.
     * Keeps local dev and live environments seamlessly up-to-date on page refreshes.
     */
    public SyncResult syncVideosIfStale(long minIntervalMillis) {
        if (java.util.Arrays.asList(environment.getActiveProfiles()).contains("test")) {
            return new SyncResult(0, 0, 0);
        }
        long now = System.currentTimeMillis();
        if (now - lastSyncTimestamp < minIntervalMillis) {
            return new SyncResult(0, 0, 0);
        }
        lastSyncTimestamp = now;
        try {
            return syncVideos();
        } catch (Exception ex) {
            log.warn("Periodic VdoCipher sync encountered error: {}", ex.getMessage());
            return new SyncResult(0, 0, 0);
        }
    }

    /**
     * Syncs all ready videos from VdoCipher into the course database.
     */
    @Transactional
    public SyncResult syncVideos() {
        if (java.util.Arrays.asList(environment.getActiveProfiles()).contains("test")) {
            return new SyncResult(0, 0, 0);
        }
        lastSyncTimestamp = System.currentTimeMillis();
        VdoCipherListResponseDto listResponse = vdoCipherClient.fetchVideosList(100);
        List<VdoCipherVideoItemDto> items = listResponse.getRows();

        if (items.isEmpty()) {
            log.info("No videos found in VdoCipher account during sync.");
            return new SyncResult(0, 0, 0);
        }

        // Get default Course and primary Course Section (Course ID 1)
        CourseEntity course = courseRepository.findById(1L)
                .orElseGet(() -> courseRepository.findAll().stream().findFirst().orElse(null));

        if (course == null) {
            log.warn("No active course found in database. Skipping VdoCipher catalog synchronization.");
            return new SyncResult(0, 0, 0);
        }

        List<CourseSectionEntity> sections = courseSectionRepository.findByCourseIdOrderByDisplayOrderAsc(course.getId());
        CourseSectionEntity defaultSection;
        if (sections.isEmpty()) {
            defaultSection = courseSectionRepository.save(new CourseSectionEntity(
                    null, course, "Core Curriculum", "Complete course lessons and masterclass modules", 1, new java.util.ArrayList<>()
            ));
        } else {
            defaultSection = sections.get(0);
        }

        java.util.Set<String> activeVdoIds = items.stream()
                .filter(item -> "ready".equalsIgnoreCase(item.getStatus()))
                .map(VdoCipherVideoItemDto::getId)
                .collect(java.util.stream.Collectors.toSet());

        // Remove any video deleted from VdoCipher or legacy placeholders
        List<VideoEntity> allDbVideos = videoRepository.findAll();
        for (VideoEntity video : allDbVideos) {
            String vdoId = video.getVdocipherVideoId();
            if (vdoId == null || vdoId.startsWith("vdo_") || (!activeVdoIds.isEmpty() && !activeVdoIds.contains(vdoId))) {
                log.info("Purging video removed from VdoCipher: id={}, title='{}', vdoId={}",
                        video.getId(), video.getTitle(), vdoId);
                videoRepository.delete(video);
            }
        }

        // Clean up duplicate entities sharing the same vdocipher_video_id
        java.util.Map<String, List<VideoEntity>> groupedByVdoId = videoRepository.findAll().stream()
                .filter(v -> StringUtils.hasText(v.getVdocipherVideoId()))
                .collect(java.util.stream.Collectors.groupingBy(VideoEntity::getVdocipherVideoId));

        for (java.util.Map.Entry<String, List<VideoEntity>> entry : groupedByVdoId.entrySet()) {
            List<VideoEntity> duplicates = entry.getValue();
            if (duplicates.size() > 1) {
                for (int i = 1; i < duplicates.size(); i++) {
                    VideoEntity dup = duplicates.get(i);
                    log.info("Purging duplicate video entity: id={}, title='{}', vdoId={}",
                            dup.getId(), dup.getTitle(), dup.getVdocipherVideoId());
                    videoRepository.delete(dup);
                }
            }
        }

        int created = 0;
        int updated = 0;
        int fallbackOrder = 1;

        for (VdoCipherVideoItemDto item : items) {
            if (!"ready".equalsIgnoreCase(item.getStatus())) {
                log.debug("Skipping video ID {} with status {}", item.getId(), item.getStatus());
                continue;
            }

            String vdoId = item.getId();
            String rawTitle = item.getTitle() != null ? item.getTitle().trim() : "Lesson " + fallbackOrder;
            int order = extractDisplayOrder(rawTitle, fallbackOrder);
            boolean isFree = determineIfFree(rawTitle, item.getTags(), order);
            String cleanTitle = formatCleanTitle(rawTitle, order);
            String description = StringUtils.hasText(item.getDescription())
                    ? item.getDescription().trim()
                    : "Video lesson from the course masterclass.";
            int duration = item.getLength() != null ? item.getLength() : 600;

            List<VideoEntity> matched = videoRepository.findAllByVdocipherVideoId(vdoId);
            VideoEntity existing = !matched.isEmpty() ? matched.get(0) : null;

            if (existing != null) {
                existing.setTitle(cleanTitle);
                if (StringUtils.hasText(item.getDescription())) {
                    existing.setDescription(description);
                }
                existing.setDurationSeconds(duration);
                existing.setDisplayOrder(order);
                existing.setFree(isFree);
                existing.setStatus(VideoStatus.PUBLISHED);
                videoRepository.save(existing);
                updated++;
                log.info("Updated existing lesson for VdoCipher video ID [{}]: '{}' (Order: {}, Free: {})",
                        vdoId, cleanTitle, order, isFree);
            } else {
                VideoEntity newVideo = new VideoEntity(
                        null,
                        defaultSection,
                        cleanTitle,
                        description,
                        vdoId,
                        duration,
                        order,
                        isFree,
                        VideoStatus.PUBLISHED
                );
                videoRepository.save(newVideo);
                created++;
                log.info("Created new lesson from VdoCipher video ID [{}]: '{}' (Order: {}, Free: {})",
                        vdoId, cleanTitle, order, isFree);
            }

            fallbackOrder++;
        }

        return new SyncResult(items.size(), created, updated);
    }

    /**
     * Extracts lesson display order from title (e.g., '01 - Hook', '1. Intro' -> 1).
     */
    public int extractDisplayOrder(String title, int fallbackOrder) {
        if (!StringUtils.hasText(title)) {
            return fallbackOrder;
        }
        Matcher matcher = ORDER_PREFIX_PATTERN.matcher(title.trim());
        if (matcher.find()) {
            try {
                return Integer.parseInt(matcher.group(1));
            } catch (NumberFormatException ignored) {
            }
        }
        return fallbackOrder;
    }

    /**
     * Determines whether video is free based on '[Free]' in title, tags, or order == 1.
     */
    public boolean determineIfFree(String title, List<String> tags, int order) {
        if (StringUtils.hasText(title)) {
            String lower = title.toLowerCase();
            if (lower.contains("[free]") || lower.contains("(free)") || lower.contains("preview") || lower.contains("free preview")) {
                return true;
            }
        }
        if (tags != null) {
            for (String tag : tags) {
                if ("free".equalsIgnoreCase(tag) || "preview".equalsIgnoreCase(tag)) {
                    return true;
                }
            }
        }
        // First lesson default preview if no tags specified
        return order == 1;
    }

    /**
     * Cleans file extensions (.mp4) and converts file names to readable titles.
     */
    public String formatCleanTitle(String rawTitle, int order) {
        if (!StringUtils.hasText(rawTitle)) {
            return "Lesson " + order;
        }

        String cleaned = FILE_EXT_PATTERN.matcher(rawTitle.trim()).replaceAll("");
        cleaned = cleaned.replaceAll("(?i)\\[free\\]|\\(free\\)", "").trim();

        Matcher matcher = ORDER_PREFIX_PATTERN.matcher(cleaned);
        if (matcher.find()) {
            String remainder = matcher.group(2).trim();
            if (StringUtils.hasText(remainder)) {
                return remainder.replace('_', ' ');
            }
        }

        return cleaned.replace('_', ' ');
    }

    public static class SyncResult {
        private final int totalProcessed;
        private final int createdCount;
        private final int updatedCount;

        public SyncResult(int totalProcessed, int createdCount, int updatedCount) {
            this.totalProcessed = totalProcessed;
            this.createdCount = createdCount;
            this.updatedCount = updatedCount;
        }

        public int getTotalProcessed() {
            return totalProcessed;
        }

        public int getCreatedCount() {
            return createdCount;
        }

        public int getUpdatedCount() {
            return updatedCount;
        }
    }
}
