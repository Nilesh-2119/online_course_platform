package com.courseplatform.progress;

import com.courseplatform.auth.security.UserPrincipal;
import com.courseplatform.common.exception.ResourceNotFoundException;
import com.courseplatform.course.CourseAccessService;
import com.courseplatform.course.CourseEntity;
import com.courseplatform.course.CourseRepository;
import com.courseplatform.course.CourseSectionEntity;
import com.courseplatform.course.CourseSectionRepository;
import com.courseplatform.course.CourseStatus;
import com.courseplatform.course.VideoAccessResult;
import com.courseplatform.course.VideoEntity;
import com.courseplatform.progress.dto.CourseProgressResponse;
import com.courseplatform.progress.dto.SectionProgressResponse;
import com.courseplatform.progress.dto.UpdateProgressRequest;
import com.courseplatform.progress.dto.VideoProgressResponse;
import com.courseplatform.user.UserEntity;
import com.courseplatform.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ProgressService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ProgressService.class);

    private final VideoProgressRepository progressRepository;
    private final CourseAccessService courseAccessService;
    private final CourseRepository courseRepository;
    private final CourseSectionRepository sectionRepository;
    private final UserRepository userRepository;

    public ProgressService(VideoProgressRepository progressRepository,
                           CourseAccessService courseAccessService,
                           CourseRepository courseRepository,
                           CourseSectionRepository sectionRepository,
                           UserRepository userRepository) {
        this.progressRepository = progressRepository;
        this.courseAccessService = courseAccessService;
        this.courseRepository = courseRepository;
        this.sectionRepository = sectionRepository;
        this.userRepository = userRepository;
    }

    /**
     * Retrieves video progress for the authenticated student.
     * Enforces course/video entitlement before returning progress data.
     *
     * @param videoId Target video ID
     * @param currentUser Authenticated user principal
     * @return VideoProgressResponse with resume position and completion state
     */
    @Transactional(readOnly = true)
    public VideoProgressResponse getVideoProgress(Long videoId, UserPrincipal currentUser) {
        courseAccessService.verifyVideoAccess(videoId, currentUser);

        return progressRepository.findByUserIdAndVideoId(currentUser.getId(), videoId)
                .map(this::mapToProgressResponse)
                .orElseGet(() -> VideoProgressResponse.builder()
                        .videoId(videoId)
                        .lastPositionSeconds(0)
                        .completed(false)
                        .build());
    }

    /**
     * Authoritatively updates video playback position and evaluates completion.
     * Guaranteed Server Flow:
     * 1. Authenticate user from principal
     * 2. Authoritatively verify course access entitlement via CourseAccessService
     * 3. Apply last-write semantics safely
     * 4. Enforce monotonic completion (once completed, remains completed)
     *
     * @param videoId Target video ID
     * @param request Progress update payload
     * @param currentUser Authenticated user principal
     * @return Updated VideoProgressResponse
     */
    @Transactional
    public VideoProgressResponse updateVideoProgress(Long videoId, UpdateProgressRequest request, UserPrincipal currentUser) {
        Long userId = currentUser.getId();

        // 1. Authoritative entitlement check
        VideoAccessResult accessResult = courseAccessService.verifyVideoAccess(videoId, currentUser);
        VideoEntity video = accessResult.getVideo();

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        // 2. Locate or initialize progress record
        VideoProgressEntity progress = progressRepository.findByUserIdAndVideoId(userId, videoId)
                .orElseGet(() -> VideoProgressEntity.builder()
                        .user(user)
                        .video(video)
                        .lastPositionSeconds(0)
                        .completed(false)
                        .build());

        // 3. Update position
        progress.setLastPositionSeconds(request.getLastPositionSeconds());

        // 4. Evaluate completion (explicit flag or >= 90% duration threshold)
        if (Boolean.TRUE.equals(request.getCompleted())) {
            progress.setCompleted(true);
        } else if (video.getDurationSeconds() != null && video.getDurationSeconds() > 0) {
            double threshold = video.getDurationSeconds() * 0.90;
            if (request.getLastPositionSeconds() >= threshold) {
                progress.setCompleted(true);
            }
        }

        progress = progressRepository.save(progress);
        log.debug("Progress updated for user {} on video {}: pos={}s, completed={}",
                userId, videoId, progress.getLastPositionSeconds(), progress.isCompleted());

        return mapToProgressResponse(progress);
    }

    /**
     * Calculates backend-verified course and section completion progress.
     *
     * @param courseId Target course ID
     * @param currentUser Authenticated user principal
     * @return Comprehensive CourseProgressResponse with section breakdowns
     */
    @Transactional(readOnly = true)
    public CourseProgressResponse getCourseProgress(Long courseId, UserPrincipal currentUser) {
        Long userId = currentUser.getId();

        CourseEntity course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", courseId));

        if (course.getStatus() != CourseStatus.PUBLISHED) {
            throw new ResourceNotFoundException("Course", courseId);
        }

        List<CourseSectionEntity> sections = sectionRepository.findSectionsWithVideosByCourseId(courseId);
        List<VideoProgressEntity> userProgressList = progressRepository.findUserProgressForCourse(userId, courseId);

        Map<Long, VideoProgressEntity> progressByVideoId = userProgressList.stream()
                .collect(Collectors.toMap(p -> p.getVideo().getId(), p -> p, (p1, p2) -> p1));

        int totalCourseVideos = 0;
        int completedCourseVideos = 0;
        List<SectionProgressResponse> sectionResponses = new ArrayList<>();

        for (CourseSectionEntity section : sections) {
            List<VideoEntity> sectionVideos = section.getVideos() != null ? section.getVideos() : List.of();
            int totalSectionVideos = sectionVideos.size();
            int completedSectionVideos = 0;

            for (VideoEntity video : sectionVideos) {
                totalCourseVideos++;
                VideoProgressEntity vp = progressByVideoId.get(video.getId());
                if (vp != null && vp.isCompleted()) {
                    completedSectionVideos++;
                    completedCourseVideos++;
                }
            }

            boolean isSectionCompleted = totalSectionVideos > 0 && completedSectionVideos == totalSectionVideos;
            sectionResponses.add(SectionProgressResponse.builder()
                    .sectionId(section.getId())
                    .sectionTitle(section.getTitle())
                    .totalVideos(totalSectionVideos)
                    .completedVideos(completedSectionVideos)
                    .completed(isSectionCompleted)
                    .build());
        }

        int percentCompleted = totalCourseVideos > 0
                ? (int) Math.round(((double) completedCourseVideos / totalCourseVideos) * 100)
                : 0;

        boolean isCourseCompleted = totalCourseVideos > 0 && completedCourseVideos == totalCourseVideos;

        return CourseProgressResponse.builder()
                .courseId(course.getId())
                .courseTitle(course.getTitle())
                .totalVideos(totalCourseVideos)
                .completedVideos(completedCourseVideos)
                .percentCompleted(percentCompleted)
                .completed(isCourseCompleted)
                .sections(sectionResponses)
                .build();
    }

    private VideoProgressResponse mapToProgressResponse(VideoProgressEntity entity) {
        return VideoProgressResponse.builder()
                .videoId(entity.getVideo().getId())
                .lastPositionSeconds(entity.getLastPositionSeconds())
                .completed(entity.isCompleted())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
