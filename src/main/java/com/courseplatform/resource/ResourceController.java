package com.courseplatform.resource;

import com.courseplatform.auth.security.UserPrincipal;
import com.courseplatform.common.ApiResponse;
import com.courseplatform.resource.dto.ResourceResponse;
import com.courseplatform.user.UserRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/resources")
public class ResourceController {

    private static final Logger log = LoggerFactory.getLogger(ResourceController.class);

    private final ResourceService resourceService;
    private final FileUploadService fileUploadService;

    public ResourceController(ResourceService resourceService, FileUploadService fileUploadService) {
        this.resourceService = resourceService;
        this.fileUploadService = fileUploadService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ResourceResponse>>> getPublishedResources() {
        List<ResourceResponse> resources = resourceService.getAllPublishedResources();
        return ResponseEntity.ok(ApiResponse.success(resources));
    }

    @GetMapping("/{resourceId}")
    public ResponseEntity<ApiResponse<ResourceResponse>> getResourceDetail(
            @PathVariable Long resourceId,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        boolean isAdmin = currentUser != null && currentUser.getRole() == UserRole.ADMIN;
        ResourceResponse resource = resourceService.getResourceDetail(resourceId, isAdmin);
        return ResponseEntity.ok(ApiResponse.success(resource));
    }

    @GetMapping("/{resourceId}/download")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getResourceDownloadUrl(@PathVariable Long resourceId) {
        ResourceResponse resource = resourceService.recordDownload(resourceId);

        String downloadUrl = resource.getResourceUrl();

        return ResponseEntity.ok(ApiResponse.success(Map.of(
                "id", resource.getId(),
                "title", resource.getTitle(),
                "downloadUrl", downloadUrl,
                "url", downloadUrl,
                "fileName", resource.getFileName() != null ? resource.getFileName() : resource.getTitle(),
                "downloadCount", resource.getDownloadCount()
        )));
    }

    @GetMapping("/files/{fileName:.+}")
    public ResponseEntity<Resource> serveResourceFile(
            @PathVariable String fileName,
            @RequestParam(value = "download", defaultValue = "false") boolean download
    ) {
        Resource fileResource = fileUploadService.loadFileAsResource(fileName);

        MediaType mediaType = MediaTypeFactory.getMediaType(fileResource)
                .orElse(MediaType.APPLICATION_OCTET_STREAM);

        String dispositionType = download ? "attachment" : "inline";

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, dispositionType + "; filename=\"" + fileResource.getFilename() + "\"")
                .header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.CONTENT_DISPOSITION)
                .body(fileResource);
    }
}
