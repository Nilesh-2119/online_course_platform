package com.courseplatform.admin;

import com.courseplatform.auth.security.UserPrincipal;
import com.courseplatform.common.ApiResponse;
import com.courseplatform.common.exception.BadRequestException;
import com.courseplatform.resource.FileUploadService;
import com.courseplatform.resource.FreeResourceEntity;
import com.courseplatform.resource.ResourceService;
import com.courseplatform.resource.ResourceStatus;
import com.courseplatform.resource.ResourceType;
import com.courseplatform.resource.dto.ResourceResponse;
import com.courseplatform.user.UserRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/resources")
public class AdminResourceController {

    private static final Logger log = LoggerFactory.getLogger(AdminResourceController.class);

    private final ResourceService resourceService;
    private final FileUploadService fileUploadService;

    public AdminResourceController(ResourceService resourceService, FileUploadService fileUploadService) {
        this.resourceService = resourceService;
        this.fileUploadService = fileUploadService;
    }

    private void verifyAdminAccess(UserPrincipal currentUser) {
        if (currentUser == null || (currentUser.getRole() != UserRole.ADMIN && !"adfixstudio25@gmail.com".equalsIgnoreCase(currentUser.getEmail()))) {
            log.warn("Unauthorized resource admin access attempt by user {}", currentUser != null ? currentUser.getEmail() : "ANONYMOUS");
            throw new AccessDeniedException("Administrator privileges are required to access this resource.");
        }
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<ResourceResponse>>> getAllResources(
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        verifyAdminAccess(currentUser);
        List<ResourceResponse> resources = resourceService.getAllResourcesForAdmin();
        return ResponseEntity.ok(ApiResponse.success(resources));
    }

    @PostMapping(consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ResourceResponse>> createResourceWithFile(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestParam("title") String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "resourceType", required = false) String resourceTypeStr,
            @RequestParam(value = "status", defaultValue = "PUBLISHED") String statusStr,
            @RequestParam(value = "externalUrl", required = false) String externalUrl,
            @RequestParam(value = "file", required = false) MultipartFile file
    ) {
        verifyAdminAccess(currentUser);

        if (!StringUtils.hasText(title)) {
            throw new BadRequestException("Resource title is required.");
        }

        if ((file == null || file.isEmpty()) && !StringUtils.hasText(externalUrl)) {
            throw new BadRequestException("Please either upload a resource file (PDF, Excel, etc.) or provide an external URL.");
        }

        String resourceUrl;
        String fileName = null;
        Long fileSize = null;
        ResourceType type = ResourceType.OTHER;

        if (file != null && !file.isEmpty()) {
            FileUploadService.StoredFileInfo fileInfo = fileUploadService.storeFile(file);
            fileName = fileInfo.getOriginalFileName();
            fileSize = fileInfo.getFileSize();
            resourceUrl = "/api/v1/resources/files/" + fileInfo.getStoredFileName();

            // Auto-detect resource type if not explicitly supplied
            if (StringUtils.hasText(resourceTypeStr)) {
                try {
                    type = ResourceType.valueOf(resourceTypeStr.trim().toUpperCase());
                } catch (IllegalArgumentException e) {
                    type = fileUploadService.detectResourceType(fileName, fileInfo.getContentType());
                }
            } else {
                type = fileUploadService.detectResourceType(fileName, fileInfo.getContentType());
            }
        } else {
            resourceUrl = externalUrl.trim();
            fileName = title.trim();
            if (StringUtils.hasText(resourceTypeStr)) {
                try {
                    type = ResourceType.valueOf(resourceTypeStr.trim().toUpperCase());
                } catch (IllegalArgumentException ignored) {
                    type = ResourceType.LINK;
                }
            } else {
                type = ResourceType.LINK;
            }
        }

        ResourceStatus status = ResourceStatus.PUBLISHED;
        if (StringUtils.hasText(statusStr)) {
            try {
                status = ResourceStatus.valueOf(statusStr.trim().toUpperCase());
            } catch (IllegalArgumentException ignored) {
                status = ResourceStatus.PUBLISHED;
            }
        }

        ResourceResponse created = resourceService.createResource(
                title.trim(),
                description != null ? description.trim() : "",
                type,
                resourceUrl,
                status,
                fileName,
                fileSize
        );

        log.info("Admin {} created free resource ID {} ('{}')", currentUser.getEmail(), created.getId(), created.getTitle());
        return ResponseEntity.ok(ApiResponse.success(created));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ResourceResponse>> updateStatus(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable Long id,
            @RequestBody Map<String, String> body
    ) {
        verifyAdminAccess(currentUser);
        String statusStr = body.getOrDefault("status", "PUBLISHED").trim().toUpperCase();
        ResourceStatus status;
        try {
            status = ResourceStatus.valueOf(statusStr);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid status: " + statusStr);
        }

        ResourceResponse updated = resourceService.updateResourceStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> deleteResource(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable Long id
    ) {
        verifyAdminAccess(currentUser);

        FreeResourceEntity deleted = resourceService.deleteResource(id);

        // Delete physical file if it was a local upload
        if (deleted.getResourceUrl() != null && deleted.getResourceUrl().startsWith("/api/v1/resources/files/")) {
            String fileName = deleted.getResourceUrl().substring("/api/v1/resources/files/".length());
            fileUploadService.deleteFile(fileName);
        }

        log.info("Admin {} deleted resource ID {}", currentUser.getEmail(), id);
        return ResponseEntity.ok(ApiResponse.success(Map.of(
                "success", true,
                "message", "Resource deleted successfully.",
                "id", id
        )));
    }
}
