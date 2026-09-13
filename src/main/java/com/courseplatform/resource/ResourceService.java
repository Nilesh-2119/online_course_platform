package com.courseplatform.resource;

import com.courseplatform.common.exception.ResourceNotFoundException;
import com.courseplatform.resource.dto.ResourceResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ResourceService {

    private static final Logger log = LoggerFactory.getLogger(ResourceService.class);

    private final FreeResourceRepository resourceRepository;

    public ResourceService(FreeResourceRepository resourceRepository) {
        this.resourceRepository = resourceRepository;
    }

    @Transactional(readOnly = true)
    public List<ResourceResponse> getAllPublishedResources() {
        return resourceRepository.findByStatusOrderByCreatedAtDesc(ResourceStatus.PUBLISHED)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ResourceResponse> getAllResourcesForAdmin() {
        return resourceRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<ResourceResponse> getPublishedResourcesPaged(Pageable pageable) {
        return resourceRepository.findByStatusOrderByCreatedAtDesc(ResourceStatus.PUBLISHED, pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public ResourceResponse getResourceDetail(Long resourceId, boolean isAdmin) {
        FreeResourceEntity resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new ResourceNotFoundException("Resource", resourceId));

        if (resource.getStatus() != ResourceStatus.PUBLISHED && !isAdmin) {
            log.warn("Access rejected to non-published resource ID {}", resourceId);
            throw new ResourceNotFoundException("Resource", resourceId);
        }

        return mapToResponse(resource);
    }

    @Transactional
    public ResourceResponse recordDownload(Long resourceId) {
        FreeResourceEntity resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new ResourceNotFoundException("Resource", resourceId));

        resource.setDownloadCount(resource.getDownloadCount() + 1);
        FreeResourceEntity updated = resourceRepository.save(resource);
        log.info("Incremented download count for resource ID {} to {}", resourceId, updated.getDownloadCount());
        return mapToResponse(updated);
    }

    @Transactional
    public ResourceResponse createResource(String title,
                                           String description,
                                           ResourceType resourceType,
                                           String resourceUrl,
                                           ResourceStatus status,
                                           String fileName,
                                           Long fileSize) {
        FreeResourceEntity entity = FreeResourceEntity.builder()
                .title(title)
                .description(description)
                .resourceType(resourceType != null ? resourceType : ResourceType.OTHER)
                .resourceUrl(resourceUrl)
                .status(status != null ? status : ResourceStatus.PUBLISHED)
                .fileName(fileName)
                .fileSize(fileSize)
                .downloadCount(0L)
                .build();

        FreeResourceEntity saved = resourceRepository.save(entity);
        log.info("Created new free resource ID {}: '{}' ({})", saved.getId(), saved.getTitle(), saved.getResourceType());
        return mapToResponse(saved);
    }

    @Transactional
    public ResourceResponse updateResourceStatus(Long resourceId, ResourceStatus status) {
        FreeResourceEntity resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new ResourceNotFoundException("Resource", resourceId));

        resource.setStatus(status);
        FreeResourceEntity updated = resourceRepository.save(resource);
        log.info("Updated resource ID {} status to {}", resourceId, status);
        return mapToResponse(updated);
    }

    @Transactional
    public FreeResourceEntity deleteResource(Long resourceId) {
        FreeResourceEntity resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new ResourceNotFoundException("Resource", resourceId));

        resourceRepository.delete(resource);
        log.info("Deleted free resource ID {} ('{}')", resourceId, resource.getTitle());
        return resource;
    }

    public ResourceResponse mapToResponse(FreeResourceEntity entity) {
        return ResourceResponse.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .resourceType(entity.getResourceType())
                .resourceUrl(entity.getResourceUrl())
                .status(entity.getStatus())
                .fileName(entity.getFileName())
                .fileSize(entity.getFileSize())
                .downloadCount(entity.getDownloadCount())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
