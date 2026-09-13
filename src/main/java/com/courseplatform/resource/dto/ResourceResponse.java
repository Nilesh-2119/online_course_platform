package com.courseplatform.resource.dto;

import com.courseplatform.resource.ResourceStatus;
import com.courseplatform.resource.ResourceType;

import java.time.Instant;

public class ResourceResponse {

    private Long id;
    private String title;
    private String description;
    private ResourceType resourceType;
    private String resourceUrl;
    private ResourceStatus status;
    private String fileName;
    private Long fileSize;
    private Long downloadCount;
    private Instant createdAt;

    public ResourceResponse() {
    }

    public ResourceResponse(Long id, String title, String description, ResourceType resourceType, String resourceUrl, ResourceStatus status, String fileName, Long fileSize, Long downloadCount, Instant createdAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.resourceType = resourceType;
        this.resourceUrl = resourceUrl;
        this.status = status;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.downloadCount = downloadCount != null ? downloadCount : 0L;
        this.createdAt = createdAt;
    }

    public ResourceResponse(Long id, String title, String description, ResourceType resourceType, String resourceUrl, ResourceStatus status) {
        this(id, title, description, resourceType, resourceUrl, status, null, null, 0L, null);
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

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
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
        private ResourceStatus status;
        private String fileName;
        private Long fileSize;
        private Long downloadCount = 0L;
        private Instant createdAt;

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

        public Builder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public ResourceResponse build() {
            return new ResourceResponse(id, title, description, resourceType, resourceUrl, status, fileName, fileSize, downloadCount, createdAt);
        }
    }
}
