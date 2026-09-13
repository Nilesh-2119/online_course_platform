package com.courseplatform.resource;

import com.courseplatform.common.BaseAuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "free_resources")
public class FreeResourceEntity extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "resource_type", nullable = false, length = 50)
    private ResourceType resourceType;

    @Column(name = "resource_url", nullable = false, length = 1024)
    private String resourceUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ResourceStatus status = ResourceStatus.PUBLISHED;

    @Column(name = "file_name", length = 255)
    private String fileName;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "download_count", nullable = false)
    private Long downloadCount = 0L;

    public FreeResourceEntity() {
    }

    public FreeResourceEntity(Long id, String title, String description, ResourceType resourceType, String resourceUrl, ResourceStatus status, String fileName, Long fileSize, Long downloadCount) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.resourceType = resourceType;
        this.resourceUrl = resourceUrl;
        this.status = status != null ? status : ResourceStatus.PUBLISHED;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.downloadCount = downloadCount != null ? downloadCount : 0L;
    }

    public FreeResourceEntity(Long id, String title, String description, ResourceType resourceType, String resourceUrl, ResourceStatus status) {
        this(id, title, description, resourceType, resourceUrl, status, null, null, 0L);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ResourceType getResourceType() {
        return resourceType;
    }

    public void setResourceType(ResourceType resourceType) {
        this.resourceType = resourceType;
    }

    public String getResourceUrl() {
        return resourceUrl;
    }

    public void setResourceUrl(String resourceUrl) {
        this.resourceUrl = resourceUrl;
    }

    public ResourceStatus getStatus() {
        return status;
    }

    public void setStatus(ResourceStatus status) {
        this.status = status;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public Long getDownloadCount() {
        return downloadCount != null ? downloadCount : 0L;
    }

    public void setDownloadCount(Long downloadCount) {
        this.downloadCount = downloadCount != null ? downloadCount : 0L;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private String title;
        private String description;
        private ResourceType resourceType;
        private String resourceUrl;
        private ResourceStatus status = ResourceStatus.PUBLISHED;
        private String fileName;
        private Long fileSize;
        private Long downloadCount = 0L;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder resourceType(ResourceType resourceType) {
            this.resourceType = resourceType;
            return this;
        }

        public Builder resourceUrl(String resourceUrl) {
            this.resourceUrl = resourceUrl;
            return this;
        }

        public Builder status(ResourceStatus status) {
            this.status = status;
            return this;
        }

        public Builder fileName(String fileName) {
            this.fileName = fileName;
            return this;
        }

        public Builder fileSize(Long fileSize) {
            this.fileSize = fileSize;
            return this;
        }

        public Builder downloadCount(Long downloadCount) {
            this.downloadCount = downloadCount;
            return this;
        }

        public FreeResourceEntity build() {
            return new FreeResourceEntity(id, title, description, resourceType, resourceUrl, status, fileName, fileSize, downloadCount);
        }
    }
}
