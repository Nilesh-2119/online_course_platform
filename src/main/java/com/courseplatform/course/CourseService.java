package com.courseplatform.course;

import com.courseplatform.common.exception.ResourceNotFoundException;
import com.courseplatform.course.dto.CourseDetailResponse;
import com.courseplatform.course.dto.CourseSectionResponse;
import com.courseplatform.course.dto.CourseSummaryResponse;
import com.courseplatform.course.dto.VideoMetadataResponse;
import com.courseplatform.payment.CoursePurchaseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CourseService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CourseService.class);

    private final CourseRepository courseRepository;
    private final CourseSectionRepository sectionRepository;
    private final VideoRepository videoRepository;
    private final CoursePurchaseRepository purchaseRepository;
    private final com.courseplatform.video.VdoCipherSyncService vdoCipherSyncService;

    public CourseService(CourseRepository courseRepository,
                         CourseSectionRepository sectionRepository,
                         VideoRepository videoRepository,
                         CoursePurchaseRepository purchaseRepository,
                         com.courseplatform.video.VdoCipherSyncService vdoCipherSyncService) {
        this.courseRepository = courseRepository;
        this.sectionRepository = sectionRepository;
        this.videoRepository = videoRepository;
        this.purchaseRepository = purchaseRepository;
        this.vdoCipherSyncService = vdoCipherSyncService;
    }

    @Transactional(readOnly = true)
    public List<CourseSummaryResponse> getAllPublishedCourses() {
        if (vdoCipherSyncService != null) {
            vdoCipherSyncService.syncVideosIfStale(5000L);
        }
        List<CourseEntity> courses = courseRepository.findByStatus(CourseStatus.PUBLISHED);

        return courses.stream()
                .map(this::mapToSummary)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CourseDetailResponse getCourseDetail(Long courseId, Long optionalUserId, boolean isAdmin) {
        if (vdoCipherSyncService != null) {
            vdoCipherSyncService.syncVideosIfStale(5000L);
        }
        CourseEntity course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", courseId));

        if (course.getStatus() != CourseStatus.PUBLISHED && !isAdmin) {
            throw new ResourceNotFoundException("Course", courseId);
        }

        boolean isPurchased = optionalUserId != null && purchaseRepository.hasUserPurchasedCourse(optionalUserId, courseId);
        List<CourseSectionEntity> sections = sectionRepository.findSectionsWithVideosByCourseId(courseId);

        List<CourseSectionResponse> sectionResponses = sections.stream()
                .map(section -> mapToSectionResponse(section, isPurchased))
                .collect(Collectors.toList());

        return CourseDetailResponse.builder()
                .id(course.getId())
                .title(course.getTitle())
                .description(course.getDescription())
                .price(course.getPrice())
                .currency(course.getCurrency())
                .status(course.getStatus())
                .isPurchased(isPurchased)
                .sections(sectionResponses)
                .build();
    }

    @Transactional(readOnly = true)
    public List<CourseSectionResponse> getCourseSections(Long courseId, Long optionalUserId, boolean isAdmin) {
        CourseEntity course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", courseId));

        if (course.getStatus() != CourseStatus.PUBLISHED && !isAdmin) {
            throw new ResourceNotFoundException("Course", courseId);
        }

        boolean isPurchased = optionalUserId != null && purchaseRepository.hasUserPurchasedCourse(optionalUserId, courseId);
        List<CourseSectionEntity> sections = sectionRepository.findSectionsWithVideosByCourseId(courseId);

        return sections.stream()
                .map(section -> mapToSectionResponse(section, isPurchased))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<VideoMetadataResponse> getSectionVideos(Long sectionId, Long optionalUserId, boolean isAdmin) {
        CourseSectionEntity section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Section", sectionId));

        CourseEntity course = section.getCourse();
        if (course.getStatus() != CourseStatus.PUBLISHED && !isAdmin) {
            throw new ResourceNotFoundException("Section", sectionId);
        }

        boolean isPurchased = optionalUserId != null && purchaseRepository.hasUserPurchasedCourse(optionalUserId, course.getId());
        List<VideoEntity> videos = videoRepository.findBySectionIdOrderByDisplayOrderAsc(sectionId);

        return videos.stream()
                .map(video -> mapToVideoResponse(video, isPurchased))
                .collect(Collectors.toList());
    }

    private CourseSummaryResponse mapToSummary(CourseEntity course) {
        int totalSections = course.getSections() != null ? course.getSections().size() : 0;
        int totalVideos = 0;
        if (course.getSections() != null) {
            totalVideos = course.getSections().stream()
                    .mapToInt(s -> s.getVideos() != null ? s.getVideos().size() : 0)
                    .sum();
        }

        return CourseSummaryResponse.builder()
                .id(course.getId())
                .title(course.getTitle())
                .description(course.getDescription())
                .price(course.getPrice())
                .currency(course.getCurrency())
                .status(course.getStatus())
                .totalSections(totalSections)
                .totalVideos(totalVideos)
                .build();
    }

    private CourseSectionResponse mapToSectionResponse(CourseSectionEntity section, boolean isPurchased) {
        List<VideoMetadataResponse> videoResponses = section.getVideos() != null
                ? section.getVideos().stream()
                        .map(v -> mapToVideoResponse(v, isPurchased))
                        .collect(Collectors.toList())
                : List.of();

        return CourseSectionResponse.builder()
                .id(section.getId())
                .courseId(section.getCourse().getId())
                .title(section.getTitle())
                .description(section.getDescription())
                .displayOrder(section.getDisplayOrder())
                .totalVideos(videoResponses.size())
                .videos(videoResponses)
                .build();
    }

    private VideoMetadataResponse mapToVideoResponse(VideoEntity video, boolean isPurchased) {
        boolean isLocked = !isPurchased && !video.isFree();

        return VideoMetadataResponse.builder()
                .id(video.getId())
                .sectionId(video.getSection().getId())
                .title(video.getTitle())
                .description(video.getDescription())
                .durationSeconds(video.getDurationSeconds())
                .displayOrder(video.getDisplayOrder())
                .isFree(video.isFree())
                .locked(isLocked)
                .status(video.getStatus())
                .build();
    }
}
