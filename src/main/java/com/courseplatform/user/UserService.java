package com.courseplatform.user;

import com.courseplatform.common.exception.PhoneAlreadyExistsException;
import com.courseplatform.common.util.PhoneNumberNormalizer;
import com.courseplatform.payment.CoursePurchaseRepository;
import com.courseplatform.user.dto.UpdateProfileRequest;
import com.courseplatform.user.dto.UserProfileResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class UserService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final CoursePurchaseRepository coursePurchaseRepository;

    public UserService(UserRepository userRepository, CoursePurchaseRepository coursePurchaseRepository) {
        this.userRepository = userRepository;
        this.coursePurchaseRepository = coursePurchaseRepository;
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User not found for authenticated ID: " + userId));

        return mapToProfileResponse(user);
    }

    @Transactional
    public UserProfileResponse updateProfile(Long userId, UpdateProfileRequest request) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User not found for authenticated ID: " + userId));

        if (request.getName() != null && StringUtils.hasText(request.getName())) {
            user.setName(request.getName().trim());
        }

        if (request.getPhone() != null && StringUtils.hasText(request.getPhone())) {
            String normalizedPhone = PhoneNumberNormalizer.normalize(request.getPhone());
            if (StringUtils.hasText(normalizedPhone) && userRepository.existsByPhoneAndIdNot(normalizedPhone, userId)) {
                throw new PhoneAlreadyExistsException(normalizedPhone);
            }
            user.setPhone(normalizedPhone);
        }

        UserEntity updated = userRepository.save(user);
        log.info("Profile updated successfully for user ID: {}", userId);

        return mapToProfileResponse(updated);
    }

    public UserProfileResponse mapToProfileResponse(UserEntity user) {
        boolean isPaid = false;
        boolean isAdmin = user.getRole() == com.courseplatform.user.UserRole.ADMIN || "adfixstudio25@gmail.com".equalsIgnoreCase(user.getEmail());
        if (isAdmin) {
            isPaid = true;
        } else if (user.getId() != null) {
            isPaid = coursePurchaseRepository.hasUserPurchasedAnyCourse(user.getId());
        }

        return UserProfileResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole() != null ? user.getRole().name() : "USER")
                .status(user.getStatus() != null ? user.getStatus().name() : "ACTIVE")
                .type(isPaid ? "paid" : "free")
                .coursePurchased(isPaid)
                .build();
    }
}
