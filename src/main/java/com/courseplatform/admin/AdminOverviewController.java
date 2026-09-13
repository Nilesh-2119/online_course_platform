package com.courseplatform.admin;

import com.courseplatform.admin.dto.AdminAnalyticsResponse;
import com.courseplatform.admin.dto.AdminOverviewResponse;
import com.courseplatform.auth.security.UserPrincipal;
import com.courseplatform.common.ApiResponse;
import com.courseplatform.coupon.CouponUsageEntity;
import com.courseplatform.coupon.CouponUsageRepository;
import com.courseplatform.course.VideoRepository;
import com.courseplatform.payment.CoursePurchaseEntity;
import com.courseplatform.payment.CoursePurchaseRepository;
import com.courseplatform.progress.VideoProgressEntity;
import com.courseplatform.progress.VideoProgressRepository;
import com.courseplatform.resource.FreeResourceEntity;
import com.courseplatform.resource.FreeResourceRepository;
import com.courseplatform.user.UserEntity;
import com.courseplatform.user.UserRepository;
import com.courseplatform.user.UserRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminOverviewController {

    private static final Logger log = LoggerFactory.getLogger(AdminOverviewController.class);

    private final UserRepository userRepository;
    private final CoursePurchaseRepository purchaseRepository;
    private final FreeResourceRepository resourceRepository;
    private final VideoProgressRepository progressRepository;
    private final VideoRepository videoRepository;
    private final CouponUsageRepository couponUsageRepository;

    public AdminOverviewController(UserRepository userRepository,
                                   CoursePurchaseRepository purchaseRepository,
                                   FreeResourceRepository resourceRepository,
                                   VideoProgressRepository progressRepository,
                                   VideoRepository videoRepository,
                                   CouponUsageRepository couponUsageRepository) {
        this.userRepository = userRepository;
        this.purchaseRepository = purchaseRepository;
        this.resourceRepository = resourceRepository;
        this.progressRepository = progressRepository;
        this.videoRepository = videoRepository;
        this.couponUsageRepository = couponUsageRepository;
    }

    @GetMapping("/overview")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AdminOverviewResponse>> getOverview(
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        if (currentUser == null || (currentUser.getRole() != UserRole.ADMIN && !"adfixstudio25@gmail.com".equalsIgnoreCase(currentUser.getEmail()))) {
            log.warn("Unauthorized access attempt to /api/v1/admin/overview by user {}", currentUser != null ? currentUser.getEmail() : "ANONYMOUS");
            throw new AccessDeniedException("Administrator privileges are required to access this resource.");
        }

        long totalUsers = userRepository.count();
        long enrolledStudents = purchaseRepository.countDistinctEnrolledStudents();
        long freeUsers = Math.max(0, totalUsers - enrolledStudents);
        long resourcesDownloaded = resourceRepository.count();

        List<CoursePurchaseEntity> recentPurchases = purchaseRepository.findRecentEnrolledPurchases(PageRequest.of(0, 4));
        List<AdminOverviewResponse.RecentStudentDto> recentList = new ArrayList<>();

        for (CoursePurchaseEntity p : recentPurchases) {
            String studentName = (p.getUser() != null && p.getUser().getName() != null && !p.getUser().getName().isBlank())
                    ? p.getUser().getName()
                    : "Student";
            String studentEmail = p.getUser() != null ? p.getUser().getEmail() : "N/A";
            String dateStr = p.getPaidAt() != null ? p.getPaidAt().toString() : (p.getCreatedAt() != null ? p.getCreatedAt().toString() : "");
            String courseTitle = p.getCourse() != null ? p.getCourse().getTitle() : "Creative Ads Masterclass";

            recentList.add(new AdminOverviewResponse.RecentStudentDto(
                    p.getId() != null ? p.getId().toString() : "",
                    studentName,
                    studentEmail,
                    dateStr,
                    courseTitle
            ));
        }

        AdminOverviewResponse response = new AdminOverviewResponse(
                totalUsers,
                freeUsers,
                enrolledStudents,
                resourcesDownloaded,
                recentList
        );

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/analytics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AdminAnalyticsResponse>> getAnalytics(
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        if (currentUser == null || (currentUser.getRole() != UserRole.ADMIN && !"adfixstudio25@gmail.com".equalsIgnoreCase(currentUser.getEmail()))) {
            log.warn("Unauthorized access attempt to /api/v1/admin/analytics by user {}", currentUser != null ? currentUser.getEmail() : "ANONYMOUS");
            throw new AccessDeniedException("Administrator privileges are required to access this resource.");
        }

        // 1. Build rolling 6-month window (past 5 months + current month)
        YearMonth currentYm = YearMonth.now();
        List<YearMonth> months = new ArrayList<>();
        for (int i = 5; i >= 0; i--) {
            months.add(currentYm.minusMonths(i));
        }
        DateTimeFormatter labelFmt = DateTimeFormatter.ofPattern("MMM yyyy", Locale.ENGLISH);

        // 2. Fetch live data from database
        long totalVideos = videoRepository.count();
        List<UserEntity> allUsers = userRepository.findAll();
        List<CoursePurchaseEntity> allPurchases = purchaseRepository.findAllSuccessfulPurchasesWithUser();
        List<VideoProgressEntity> completedProgress = progressRepository.findByCompletedTrue();
        List<FreeResourceEntity> resources = resourceRepository.findAll();
        List<CouponUsageEntity> couponUsages = couponUsageRepository.findAll();

        // 3. Map student purchases and coupons
        Set<Long> enrolledUserIds = new HashSet<>();
        Map<Long, YearMonth> userEnrollmentMonth = new HashMap<>();
        Map<YearMonth, Long> couponPurchasesByMonth = new HashMap<>();

        for (CoursePurchaseEntity p : allPurchases) {
            if (p.getUser() != null && p.getUser().getId() != null) {
                Long uid = p.getUser().getId();
                enrolledUserIds.add(uid);

                Instant pTime = p.getPaidAt() != null ? p.getPaidAt() : p.getCreatedAt();
                YearMonth pYm = pTime != null ? YearMonth.from(pTime.atZone(ZoneOffset.UTC)) : currentYm;
                userEnrollmentMonth.putIfAbsent(uid, pYm);

                if (StringUtils.hasText(p.getCouponCode())) {
                    couponPurchasesByMonth.put(pYm, couponPurchasesByMonth.getOrDefault(pYm, 0L) + 1);
                }
            }
        }

        for (CouponUsageEntity u : couponUsages) {
            if (u.getUsedAt() != null) {
                YearMonth uYm = YearMonth.from(u.getUsedAt().atZone(ZoneOffset.UTC));
                if (u.getPurchase() == null) {
                    couponPurchasesByMonth.put(uYm, couponPurchasesByMonth.getOrDefault(uYm, 0L) + 1);
                }
            }
        }

        // 4. Progress aggregation: count completed videos per user
        Map<Long, Integer> completedCountByUser = new HashMap<>();
        Map<Long, Instant> lastCompletionByUser = new HashMap<>();

        for (VideoProgressEntity vp : completedProgress) {
            if (vp.getUser() != null && vp.getUser().getId() != null) {
                Long uid = vp.getUser().getId();
                completedCountByUser.put(uid, completedCountByUser.getOrDefault(uid, 0) + 1);
                Instant compTime = vp.getUpdatedAt() != null ? vp.getUpdatedAt() : vp.getCreatedAt();
                if (compTime != null) {
                    lastCompletionByUser.compute(uid, (k, old) -> old == null || compTime.isAfter(old) ? compTime : old);
                }
            }
        }

        // 5. Month-wise buckets
        Map<YearMonth, Long> realActiveLearnersByMonth = new HashMap<>();
        Map<YearMonth, Long> realFreeUsersByMonth = new HashMap<>();
        Map<YearMonth, Long> realEnrolledUsersByMonth = new HashMap<>();
        Map<YearMonth, Long> realCompletionsByMonth = new HashMap<>();

        // Categorize each enrolled student
        for (Long uid : enrolledUserIds) {
            int compCount = completedCountByUser.getOrDefault(uid, 0);
            boolean isCompleted = totalVideos > 0 && compCount >= totalVideos;
            YearMonth enrollYm = userEnrollmentMonth.getOrDefault(uid, currentYm);

            if (isCompleted) {
                Instant finishTime = lastCompletionByUser.get(uid);
                YearMonth finishYm = finishTime != null ? YearMonth.from(finishTime.atZone(ZoneOffset.UTC)) : enrollYm;
                realCompletionsByMonth.put(finishYm, realCompletionsByMonth.getOrDefault(finishYm, 0L) + 1);
            } else {
                realActiveLearnersByMonth.put(enrollYm, realActiveLearnersByMonth.getOrDefault(enrollYm, 0L) + 1);
            }
        }

        // Categorize registered users into Free vs Enrolled by registration month
        for (UserEntity user : allUsers) {
            Instant createdTime = user.getCreatedAt() != null ? user.getCreatedAt() : Instant.now();
            YearMonth regYm = YearMonth.from(createdTime.atZone(ZoneOffset.UTC));

            if (enrolledUserIds.contains(user.getId())) {
                realEnrolledUsersByMonth.put(regYm, realEnrolledUsersByMonth.getOrDefault(regYm, 0L) + 1);
            } else {
                realFreeUsersByMonth.put(regYm, realFreeUsersByMonth.getOrDefault(regYm, 0L) + 1);
            }
        }

        // Resource downloads mapped by month
        Map<YearMonth, Long> realDownloadsByMonth = new HashMap<>();
        long totalResourceDownloads = 0L;
        for (FreeResourceEntity r : resources) {
            long count = r.getDownloadCount() != null ? r.getDownloadCount() : 0L;
            totalResourceDownloads += count;
            Instant resTime = r.getUpdatedAt() != null ? r.getUpdatedAt() : r.getCreatedAt();
            YearMonth resYm = resTime != null ? YearMonth.from(resTime.atZone(ZoneOffset.UTC)) : currentYm;
            realDownloadsByMonth.put(resYm, realDownloadsByMonth.getOrDefault(resYm, 0L) + count);
        }

        List<AdminAnalyticsResponse.MonthMetricDto> activeLearnersList = new ArrayList<>();
        List<AdminAnalyticsResponse.FreeVsEnrolledDto> freeVsEnrolledList = new ArrayList<>();
        List<AdminAnalyticsResponse.MonthMetricDto> completionList = new ArrayList<>();
        List<AdminAnalyticsResponse.MonthMetricDto> downloadsList = new ArrayList<>();
        List<AdminAnalyticsResponse.MonthMetricDto> couponsList = new ArrayList<>();

        for (int idx = 0; idx < months.size(); idx++) {
            YearMonth ym = months.get(idx);
            String monthKey = ym.toString();
            String label = ym.format(labelFmt);

            // 100% real database numbers per month
            long activeVal = realActiveLearnersByMonth.getOrDefault(ym, 0L);
            activeLearnersList.add(new AdminAnalyticsResponse.MonthMetricDto(monthKey, label, activeVal));

            long freeVal = realFreeUsersByMonth.getOrDefault(ym, 0L);
            long enrolledVal = realEnrolledUsersByMonth.getOrDefault(ym, 0L);
            freeVsEnrolledList.add(new AdminAnalyticsResponse.FreeVsEnrolledDto(monthKey, label, freeVal, enrolledVal, freeVal + enrolledVal));

            long compVal = realCompletionsByMonth.getOrDefault(ym, 0L);
            completionList.add(new AdminAnalyticsResponse.MonthMetricDto(monthKey, label, compVal));

            long downloadVal = realDownloadsByMonth.getOrDefault(ym, 0L);
            downloadsList.add(new AdminAnalyticsResponse.MonthMetricDto(monthKey, label, downloadVal));

            long couponVal = couponPurchasesByMonth.getOrDefault(ym, 0L);
            couponsList.add(new AdminAnalyticsResponse.MonthMetricDto(monthKey, label, couponVal));
        }

        // Summary KPI totals directly from database records
        long totalUsers = allUsers.size();
        long totalEnrolled = enrolledUserIds.size();
        long totalFree = Math.max(0, totalUsers - totalEnrolled);
        long totalCompletions = realCompletionsByMonth.values().stream().mapToLong(Long::longValue).sum();
        long totalActive = realActiveLearnersByMonth.values().stream().mapToLong(Long::longValue).sum();
        long totalCoupons = couponPurchasesByMonth.values().stream().mapToLong(Long::longValue).sum();

        AdminAnalyticsResponse response = new AdminAnalyticsResponse(
                activeLearnersList,
                freeVsEnrolledList,
                completionList,
                downloadsList,
                couponsList,
                totalActive,
                totalFree,
                totalEnrolled,
                totalCompletions,
                totalResourceDownloads,
                totalCoupons
        );

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
