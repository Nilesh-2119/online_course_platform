package com.courseplatform.resource;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "resource_files")
public class ResourceFileEntity {

    @Id
    @Column(name = "file_name", length = 255, nullable = false)
    private String fileName;

    @Column(name = "original_file_name", length = 255, nullable = false)
    private String originalFileName;

    @Column(name = "content_type", length = 100)
    private String contentType;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Lob
    @Column(name = "file_data", columnDefinition = "LONGBLOB", nullable = false)
    private byte[] fileData;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    public ResourceFileEntity() {
    }

    public ResourceFileEntity(String fileName, String originalFileName, String contentType, Long fileSize, byte[] fileData) {
        this.fileName = fileName;
        this.originalFileName = originalFileName;
        this.contentType = contentType;
        this.fileSize = fileSize;
        this.fileData = fileData;
        this.createdAt = Instant.now();
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getOriginalFileName() {
        return originalFileName;
    }

    public void setOriginalFileName(String originalFileName) {
        this.originalFileName = originalFileName;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public byte[] getFileData() {
        return fileData;
    }

    public void setFileData(byte[] fileData) {
        this.fileData = fileData;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
