package com.courseplatform.payment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CoursePurchaseRepository extends JpaRepository<CoursePurchaseEntity, Long> {

    Optional<CoursePurchaseEntity> findByRazorpayOrderId(String razorpayOrderId);

    Optional<CoursePurchaseEntity> findByRazorpayPaymentId(String razorpayPaymentId);

    List<CoursePurchaseEntity> findByUserIdOrderByCreatedAtDesc(Long userId);

    @Query("SELECT p FROM CoursePurchaseEntity p LEFT JOIN FETCH p.course WHERE p.user.id = :userId ORDER BY p.createdAt DESC")
    List<CoursePurchaseEntity> findByUserIdWithCourseOrderByCreatedAtDesc(@Param("userId") Long userId);

    @Query("SELECT COUNT(p) > 0 FROM CoursePurchaseEntity p " +
           "WHERE p.user.id = :userId AND p.course.id = :courseId AND p.status = 'SUCCESS'")
    boolean hasUserPurchasedCourse(@Param("userId") Long userId, @Param("courseId") Long courseId);

    @Query("SELECT COUNT(p) > 0 FROM CoursePurchaseEntity p " +
           "WHERE p.user.id = :userId AND p.status = 'SUCCESS'")
    boolean hasUserPurchasedAnyCourse(@Param("userId") Long userId);

    @Query("SELECT p FROM CoursePurchaseEntity p JOIN FETCH p.course " +
           "WHERE p.user.id = :userId AND p.status = 'SUCCESS' ORDER BY p.paidAt DESC")
    List<CoursePurchaseEntity> findUserActivePurchasesWithCourse(@Param("userId") Long userId);

    Optional<CoursePurchaseEntity> findTopByUserIdAndCourseIdAndStatusOrderByCreatedAtDesc(
            Long userId, Long courseId, PurchaseStatus status
    );

    @Query("SELECT COUNT(DISTINCT p.user.id) FROM CoursePurchaseEntity p WHERE p.status = 'SUCCESS'")
    long countDistinctEnrolledStudents();

    @Query("SELECT p FROM CoursePurchaseEntity p JOIN FETCH p.user JOIN FETCH p.course WHERE p.status = 'SUCCESS' ORDER BY COALESCE(p.paidAt, p.createdAt) DESC")
    List<CoursePurchaseEntity> findRecentEnrolledPurchases(org.springframework.data.domain.Pageable pageable);

    @Query("SELECT p FROM CoursePurchaseEntity p JOIN FETCH p.user LEFT JOIN FETCH p.course WHERE p.status = 'SUCCESS' ORDER BY COALESCE(p.paidAt, p.createdAt) ASC")
    List<CoursePurchaseEntity> findAllSuccessfulPurchasesWithUser();

    @org.springframework.data.jpa.repository.Modifying(clearAutomatically = true)
    @Query("DELETE FROM CoursePurchaseEntity p WHERE p.user.id = :userId")
    int deleteByUserId(@Param("userId") Long userId);
}
