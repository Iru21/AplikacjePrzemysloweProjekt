package me.iru.datingapp.service;

import lombok.Getter;
import me.iru.datingapp.exception.FileStorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Getter
@Service
public class FileStorageService {

    private static final Logger log = LoggerFactory.getLogger(FileStorageService.class);

    private final Path fileStorageLocation;

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "gif");
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    public FileStorageService(@Value("${app.file.upload-dir:uploads}") String uploadDir) {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.fileStorageLocation);
            log.info("File storage directory created at: {}", this.fileStorageLocation);
        } catch (IOException e) {
            log.error("Could not create upload directory: {}", e.getMessage());
            throw new FileStorageException("Could not create the directory where the uploaded files will be stored.", e);
        }
    }

    /**
     * Stores a file and returns the stored filename
     *
     * @param file MultipartFile to store
     * @return Stored filename
     * @throws FileStorageException if storage fails
     */
    public String store(MultipartFile file) {
        log.info("Attempting to store file: {}", file.getOriginalFilename());

        validateFile(file);

        String originalFilename = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));

        try {
            if (originalFilename.contains("..")) {
                log.error("Invalid filename: {}", originalFilename);
                throw new FileStorageException("Filename contains invalid path sequence: " + originalFilename);
            }

            String fileExtension = getFileExtension(originalFilename);
            String newFilename = UUID.randomUUID() + "." + fileExtension;

            Path targetLocation = this.fileStorageLocation.resolve(newFilename);
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING);
            }

            log.info("File stored successfully: {}", newFilename);
            return newFilename;

        } catch (IOException e) {
            log.error("Failed to store file {}: {}", originalFilename, e.getMessage());
            throw new FileStorageException("Could not store file " + originalFilename + ". Please try again!", e);
        }
    }

    /**
     * Loads a file as a Resource
     *
     * @param filename Filename to load
     * @return Resource
     * @throws FileStorageException if a file not found
     */
    public Resource load(String filename) {
        log.debug("Loading file: {}", filename);

        try {
            Path filePath = this.fileStorageLocation.resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                log.debug("File loaded successfully: {}", filename);
                return resource;
            } else {
                log.error("File not found or not readable: {}", filename);
                throw new FileStorageException("File not found: " + filename);
            }
        } catch (MalformedURLException e) {
            log.error("File not found {}: {}", filename, e.getMessage());
            throw new FileStorageException("File not found: " + filename, e);
        }
    }

    /**
     * Deletes a file
     *
     * @param filename Filename to delete
     * @throws FileStorageException if deletion fails
     */
    public void delete(String filename) {
        log.info("Attempting to delete file: {}", filename);

        try {
            Path filePath = this.fileStorageLocation.resolve(filename).normalize();
            Files.deleteIfExists(filePath);
            log.info("File deleted successfully: {}", filename);
        } catch (IOException e) {
            log.error("Failed to delete file {}: {}", filename, e.getMessage());
            throw new FileStorageException("Could not delete file: " + filename, e);
        }
    }

    /**
     * Validates uploaded file
     *
     * @param file File to validate
     * @throws FileStorageException if validation fails
     */
    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            log.error("File is empty");
            throw new FileStorageException("Cannot store empty file");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            log.error("File size {} exceeds maximum allowed size {}", file.getSize(), MAX_FILE_SIZE);
            throw new FileStorageException("File size exceeds maximum allowed size of " + (MAX_FILE_SIZE / 1024 / 1024) + "MB");
        }

        String filename = file.getOriginalFilename();
        String extension = getFileExtension(filename);

        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            log.error("File extension {} is not allowed", extension);
            throw new FileStorageException("File type not allowed. Allowed types: " + ALLOWED_EXTENSIONS);
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            log.error("Invalid content type: {}", contentType);
            throw new FileStorageException("Only image files are allowed");
        }

        log.debug("File validation passed for: {}", filename);
    }

    /**
     * Extracts file extension from filename
     *
     * @param filename Filename
     * @return File extension
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }

        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }

        return filename.substring(lastDotIndex + 1);
    }

    /**
     * Checks if a file exists
     *
     * @param filename Filename to check
     * @return true if the file exists
     */
    public boolean exists(String filename) {
        Path filePath = this.fileStorageLocation.resolve(filename).normalize();
        return Files.exists(filePath);
    }
}

