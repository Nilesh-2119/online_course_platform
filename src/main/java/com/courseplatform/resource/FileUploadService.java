package com.courseplatform.resource;

import com.courseplatform.common.exception.BadRequestException;
import com.courseplatform.common.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileUploadService {

    private static final Logger log = LoggerFactory.getLogger(FileUploadService.class);

    private Path storageLocation;
    private Path fallbackLocation;

    public FileUploadService(@Value("${app.uploads.dir:uploads/resources}") String uploadDir) {
        try {
            this.storageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        } catch (Exception e) {
            this.storageLocation = Paths.get(System.getProperty("java.io.tmpdir"), "uploads", "resources").toAbsolutePath().normalize();
        }
        this.fallbackLocation = Paths.get(System.getProperty("java.io.tmpdir"), "courseplatform-uploads", "resources").toAbsolutePath().normalize();
    }

    @PostConstruct
    public void init() {
        boolean initialized = false;
        try {
            Files.createDirectories(this.storageLocation);
            log.info("Initialized local file storage directory at: {}", this.storageLocation);
            initialized = true;
        } catch (Exception ex) {
            log.warn("Could not create primary storage directory at {}: {}. Attempting fallback to tmpdir...",
                    this.storageLocation, ex.getMessage());
        }

        if (!initialized) {
            try {
                Files.createDirectories(this.fallbackLocation);
                this.storageLocation = this.fallbackLocation;
                log.info("Initialized fallback file storage directory at: {}", this.storageLocation);
            } catch (Exception ex) {
                log.error("Could not create fallback storage directory at {}: {}. App will continue running.",
                        this.fallbackLocation, ex.getMessage());
            }
        }
    }

    public static class StoredFileInfo {
        private final String storedFileName;
        private final String originalFileName;
        private final long fileSize;
        private final String contentType;

        public StoredFileInfo(String storedFileName, String originalFileName, long fileSize, String contentType) {
            this.storedFileName = storedFileName;
            this.originalFileName = originalFileName;
            this.fileSize = fileSize;
            this.contentType = contentType;
        }

        public String getStoredFileName() {
            return storedFileName;
        }

        public String getOriginalFileName() {
            return originalFileName;
        }

        public long getFileSize() {
            return fileSize;
        }

        public String getContentType() {
            return contentType;
        }
    }

    public StoredFileInfo storeFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Failed to store empty file.");
        }

        String rawName = file.getOriginalFilename();
        if (rawName == null || rawName.trim().isEmpty()) {
            rawName = "resource_" + System.currentTimeMillis();
        }

        String originalFileName = StringUtils.cleanPath(rawName);

        if (originalFileName.contains("..")) {
            throw new BadRequestException("Filename contains invalid path sequence: " + originalFileName);
        }

        // Sanitize filename to avoid problematic characters
        String safeName = originalFileName.replaceAll("[^a-zA-Z0-9._-]", "_");
        String uniqueFileName = UUID.randomUUID().toString().substring(0, 12) + "_" + safeName;

        try {
            if (!Files.exists(this.storageLocation)) {
                Files.createDirectories(this.storageLocation);
            }
            Path targetLocation = this.storageLocation.resolve(uniqueFileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            log.info("Stored uploaded file '{}' as '{}' (size: {} bytes)", originalFileName, uniqueFileName, file.getSize());
            return new StoredFileInfo(uniqueFileName, originalFileName, file.getSize(), file.getContentType());
        } catch (IOException ex) {
            log.warn("Failed to store file in primary location, trying fallback: {}", ex.getMessage());
            try {
                if (!Files.exists(this.fallbackLocation)) {
                    Files.createDirectories(this.fallbackLocation);
                }
                Path fallbackTarget = this.fallbackLocation.resolve(uniqueFileName);
                Files.copy(file.getInputStream(), fallbackTarget, StandardCopyOption.REPLACE_EXISTING);
                this.storageLocation = this.fallbackLocation;
                log.info("Stored uploaded file '{}' in fallback location '{}'", originalFileName, uniqueFileName);
                return new StoredFileInfo(uniqueFileName, originalFileName, file.getSize(), file.getContentType());
            } catch (IOException fallbackEx) {
                log.error("Failed to store file in fallback location: {}", fallbackEx.getMessage());
                throw new RuntimeException("Could not store file " + originalFileName + ". Please try again.", fallbackEx);
            }
        }
    }

    public Resource loadFileAsResource(String fileName) {
        try {
            Path filePath = this.storageLocation.resolve(fileName).normalize();

            if (filePath.startsWith(this.storageLocation)) {
                Resource resource = new UrlResource(filePath.toUri());
                if (resource.exists() && resource.isReadable()) {
                    return resource;
                }
            }

            // Check fallback location
            Path fallbackPath = this.fallbackLocation.resolve(fileName).normalize();
            if (fallbackPath.startsWith(this.fallbackLocation)) {
                Resource fallbackResource = new UrlResource(fallbackPath.toUri());
                if (fallbackResource.exists() && fallbackResource.isReadable()) {
                    return fallbackResource;
                }
            }

            throw new ResourceNotFoundException("File not found: " + fileName);
        } catch (MalformedURLException ex) {
            throw new ResourceNotFoundException("File not found: " + fileName);
        }
    }

    public void deleteFile(String fileName) {
        if (!StringUtils.hasText(fileName)) return;
        try {
            Path filePath = this.storageLocation.resolve(fileName).normalize();
            if (filePath.startsWith(this.storageLocation)) {
                Files.deleteIfExists(filePath);
                log.info("Deleted physical file: {}", fileName);
            }
        } catch (IOException ex) {
            log.warn("Could not delete file {}: {}", fileName, ex.getMessage());
        }

        try {
            Path fallbackPath = this.fallbackLocation.resolve(fileName).normalize();
            if (fallbackPath.startsWith(this.fallbackLocation)) {
                Files.deleteIfExists(fallbackPath);
            }
        } catch (IOException ignored) {
        }
    }

    public ResourceType detectResourceType(String fileName, String contentType) {
        if (!StringUtils.hasText(fileName)) {
            return ResourceType.OTHER;
        }
        String lower = fileName.toLowerCase();
        if (lower.endsWith(".pdf")) {
            return ResourceType.PDF;
        } else if (lower.endsWith(".xlsx") || lower.endsWith(".xls") || lower.endsWith(".csv")) {
            return ResourceType.EXCEL;
        } else if (lower.endsWith(".doc") || lower.endsWith(".docx") || lower.endsWith(".txt")) {
            return ResourceType.DOCUMENT;
        } else if (lower.endsWith(".zip") || lower.endsWith(".rar") || lower.endsWith(".7z")) {
            return ResourceType.TEMPLATE;
        } else if (lower.endsWith(".js") || lower.endsWith(".ts") || lower.endsWith(".py") || lower.endsWith(".html") || lower.endsWith(".css")) {
            return ResourceType.CODE;
        }
        return ResourceType.OTHER;
    }
}
