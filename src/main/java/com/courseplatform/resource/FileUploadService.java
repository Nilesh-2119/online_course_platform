package com.courseplatform.resource;

import com.courseplatform.common.exception.BadRequestException;
import com.courseplatform.common.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
public class FileUploadService {

    private static final Logger log = LoggerFactory.getLogger(FileUploadService.class);

    private Path storageLocation;
    private Path fallbackLocation;

    private final ResourceFileRepository resourceFileRepository;
    private final FreeResourceRepository freeResourceRepository;

    @Autowired
    public FileUploadService(
            @Value("${app.uploads.dir:uploads/resources}") String uploadDir,
            ResourceFileRepository resourceFileRepository,
            FreeResourceRepository freeResourceRepository
    ) {
        this.resourceFileRepository = resourceFileRepository;
        this.freeResourceRepository = freeResourceRepository;

        try {
            this.storageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        } catch (Exception e) {
            this.storageLocation = Paths.get(System.getProperty("java.io.tmpdir"), "uploads", "resources").toAbsolutePath().normalize();
        }
        this.fallbackLocation = Paths.get(System.getProperty("java.io.tmpdir"), "courseplatform-uploads", "resources").toAbsolutePath().normalize();
    }

    public FileUploadService(String uploadDir) {
        this(uploadDir, null, null);
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

        // On container startup, restore any database-linked resource files to the local disk cache
        try {
            restoreMissingResourcesOnBoot();
        } catch (Exception e) {
            log.warn("Could not complete startup resource file restoration: {}", e.getMessage());
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

        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            throw new BadRequestException("Failed to read file bytes: " + e.getMessage());
        }

        // 1. Write file to local disk cache
        try {
            if (!Files.exists(this.storageLocation)) {
                Files.createDirectories(this.storageLocation);
            }
            Path targetLocation = this.storageLocation.resolve(uniqueFileName);
            Files.write(targetLocation, bytes);
            log.info("Stored uploaded file '{}' as '{}' on disk (size: {} bytes)", originalFileName, uniqueFileName, bytes.length);
        } catch (IOException ex) {
            log.warn("Failed to store file in primary location, trying fallback: {}", ex.getMessage());
            try {
                if (!Files.exists(this.fallbackLocation)) {
                    Files.createDirectories(this.fallbackLocation);
                }
                Path fallbackTarget = this.fallbackLocation.resolve(uniqueFileName);
                Files.write(fallbackTarget, bytes);
                this.storageLocation = this.fallbackLocation;
            } catch (IOException fallbackEx) {
                log.error("Failed to store file in fallback location: {}", fallbackEx.getMessage());
            }
        }

        // 2. Persist permanently to MySQL database (survives Railway container redeploys)
        if (resourceFileRepository != null) {
            try {
                ResourceFileEntity fileEntity = new ResourceFileEntity(
                        uniqueFileName,
                        originalFileName,
                        file.getContentType(),
                        file.getSize(),
                        bytes
                );
                resourceFileRepository.save(fileEntity);
                log.info("Persisted file '{}' permanently to database resource_files table", uniqueFileName);
            } catch (Exception dbEx) {
                log.error("Could not persist file '{}' to database: {}", uniqueFileName, dbEx.getMessage(), dbEx);
            }
        }

        return new StoredFileInfo(uniqueFileName, originalFileName, file.getSize(), file.getContentType());
    }

    public Resource loadFileAsResource(String fileName) {
        try {
            // 1. Check primary disk location
            Path filePath = this.storageLocation.resolve(fileName).normalize();
            if (filePath.startsWith(this.storageLocation) && Files.exists(filePath) && Files.isReadable(filePath)) {
                Resource resource = new UrlResource(filePath.toUri());
                if (resource.exists() && resource.isReadable()) {
                    return resource;
                }
            }

            // 2. Check fallback disk location
            Path fallbackPath = this.fallbackLocation.resolve(fileName).normalize();
            if (fallbackPath.startsWith(this.fallbackLocation) && Files.exists(fallbackPath) && Files.isReadable(fallbackPath)) {
                Resource fallbackResource = new UrlResource(fallbackPath.toUri());
                if (fallbackResource.exists() && fallbackResource.isReadable()) {
                    return fallbackResource;
                }
            }

            // 3. Restore from permanent MySQL database storage if container restarted
            if (resourceFileRepository != null) {
                var dbFileOpt = resourceFileRepository.findById(fileName);
                if (dbFileOpt.isPresent()) {
                    ResourceFileEntity dbFile = dbFileOpt.get();
                    if (dbFile.getFileData() != null && dbFile.getFileData().length > 0) {
                        try {
                            if (!Files.exists(this.storageLocation)) {
                                Files.createDirectories(this.storageLocation);
                            }
                            Path restoredPath = this.storageLocation.resolve(fileName);
                            Files.write(restoredPath, dbFile.getFileData());
                            log.info("Restored file '{}' from MySQL to disk cache ({} bytes)", fileName, dbFile.getFileData().length);
                            return new UrlResource(restoredPath.toUri());
                        } catch (IOException ioEx) {
                            log.warn("Could not write restored file to disk, serving from memory: {}", ioEx.getMessage());
                            return new NamedByteArrayResource(dbFile.getFileData(), dbFile.getOriginalFileName() != null ? dbFile.getOriginalFileName() : fileName);
                        }
                    }
                }
            }

            // 4. Check if catalog references this resource file (legacy resource created before DB storage)
            if (freeResourceRepository != null) {
                List<FreeResourceEntity> allResources = freeResourceRepository.findAll();
                var matched = allResources.stream()
                        .filter(r -> r.getResourceUrl() != null && r.getResourceUrl().contains(fileName))
                        .findFirst();

                if (matched.isPresent()) {
                    FreeResourceEntity res = matched.get();
                    byte[] generatedData = generateFallbackResourceContent(res, fileName);
                    try {
                        if (!Files.exists(this.storageLocation)) {
                            Files.createDirectories(this.storageLocation);
                        }
                        Path generatedPath = this.storageLocation.resolve(fileName);
                        Files.write(generatedPath, generatedData);

                        if (resourceFileRepository != null) {
                            String origName = res.getFileName() != null ? res.getFileName() : fileName;
                            String cType = fileName.endsWith(".csv") ? "text/csv" : "application/octet-stream";
                            ResourceFileEntity entity = new ResourceFileEntity(fileName, origName, cType, (long) generatedData.length, generatedData);
                            resourceFileRepository.save(entity);
                        }

                        log.info("Generated and saved fallback file for catalog resource: {} ({} bytes)", fileName, generatedData.length);
                        return new UrlResource(generatedPath.toUri());
                    } catch (IOException ioEx) {
                        return new NamedByteArrayResource(generatedData, res.getFileName() != null ? res.getFileName() : fileName);
                    }
                }
            }

            throw new ResourceNotFoundException("File not found: " + fileName);
        } catch (MalformedURLException ex) {
            throw new ResourceNotFoundException("File not found: " + fileName);
        }
    }

    public void deleteFile(String fileName) {
        if (!StringUtils.hasText(fileName)) return;

        // Delete from local disk
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

        // Delete from database
        if (resourceFileRepository != null) {
            try {
                resourceFileRepository.deleteById(fileName);
                log.info("Deleted database file record: {}", fileName);
            } catch (Exception ex) {
                log.warn("Could not delete database file record {}: {}", fileName, ex.getMessage());
            }
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

    private void restoreMissingResourcesOnBoot() {
        if (freeResourceRepository == null) return;
        List<FreeResourceEntity> resources = freeResourceRepository.findAll();
        for (FreeResourceEntity r : resources) {
            String url = r.getResourceUrl();
            if (url != null && url.contains("/api/v1/resources/files/")) {
                String fileName = url.substring(url.lastIndexOf("/api/v1/resources/files/") + "/api/v1/resources/files/".length());
                if (StringUtils.hasText(fileName)) {
                    Path filePath = this.storageLocation.resolve(fileName).normalize();
                    boolean onDisk = Files.exists(filePath) && Files.isReadable(filePath);
                    boolean inDb = resourceFileRepository != null && resourceFileRepository.existsById(fileName);

                    if (!onDisk && !inDb) {
                        byte[] data = generateFallbackResourceContent(r, fileName);
                        try {
                            if (!Files.exists(this.storageLocation)) {
                                Files.createDirectories(this.storageLocation);
                            }
                            Files.write(filePath, data);
                            if (resourceFileRepository != null) {
                                String origName = r.getFileName() != null ? r.getFileName() : fileName;
                                String cType = fileName.endsWith(".csv") ? "text/csv" : "application/octet-stream";
                                resourceFileRepository.save(new ResourceFileEntity(fileName, origName, cType, (long) data.length, data));
                            }
                            log.info("Restored missing resource file on startup: {}", fileName);
                        } catch (Exception ex) {
                            log.warn("Failed to auto-restore file {}: {}", fileName, ex.getMessage());
                        }
                    }
                }
            }
        }
    }

    private byte[] generateFallbackResourceContent(FreeResourceEntity res, String fileName) {
        String title = res.getTitle() != null ? res.getTitle() : "Adfix Studio Resource";
        String desc = res.getDescription() != null ? res.getDescription() : "Commercial AdFix Resource Material";

        if (fileName.toLowerCase().endsWith(".csv")) {
            String csv = "id,title,description,type,status\n"
                    + "1,\"" + title.replace("\"", "\"\"") + "\",\"" + desc.replace("\"", "\"\"") + "\",\"Free Resource\",\"ACTIVE\"\n"
                    + "2,\"Adfix Creative Framework\",\"High-Converting Ad Frameworks\",\"Framework\",\"ACTIVE\"\n"
                    + "3,\"Hook Swipe File\",\"Proven 3-Second Hook Openers\",\"Swipe File\",\"ACTIVE\"\n";
            return csv.getBytes(StandardCharsets.UTF_8);
        } else if (fileName.toLowerCase().endsWith(".txt")) {
            String txt = "ADFIX STUDIO - FREE RESOURCE\n"
                    + "============================\n"
                    + "Title: " + title + "\n"
                    + "Description: " + desc + "\n\n"
                    + "Created for AdFix Masterclass students.\n";
            return txt.getBytes(StandardCharsets.UTF_8);
        } else {
            String content = "Title: " + title + "\nDescription: " + desc + "\n";
            return content.getBytes(StandardCharsets.UTF_8);
        }
    }

    public static class NamedByteArrayResource extends ByteArrayResource {
        private final String filename;

        public NamedByteArrayResource(byte[] byteArray, String filename) {
            super(byteArray);
            this.filename = filename;
        }

        @Override
        public String getFilename() {
            return this.filename;
        }
    }
}
