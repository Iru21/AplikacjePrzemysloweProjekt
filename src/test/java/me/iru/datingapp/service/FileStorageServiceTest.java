package me.iru.datingapp.service;

import me.iru.datingapp.exception.FileStorageException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class FileStorageServiceTest {

    @TempDir
    Path tempDir;

    private FileStorageService fileStorageService;

    @BeforeEach
    void setUp() {
        fileStorageService = new FileStorageService(tempDir.toString());
    }

    @Test
    void testStore_Success() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );

        String filename = fileStorageService.store(file);

        assertThat(filename).isNotNull();
        assertThat(filename).endsWith(".jpg");
        assertThat(Files.exists(tempDir.resolve(filename))).isTrue();
    }

    @Test
    void testStore_ValidatesFileExtension() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );

        String filename = fileStorageService.store(file);

        assertThat(filename).endsWith(".jpg");
    }

    @Test
    void testStore_InvalidFileExtension() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.exe",
                "application/x-msdownload",
                "malicious content".getBytes()
        );

        // When/Then
        assertThatThrownBy(() -> fileStorageService.store(file))
                .isInstanceOf(FileStorageException.class)
                .hasMessageContaining("not allowed");
    }

    @Test
    void testStore_FileSizeExceedsLimit() {
        byte[] largeContent = new byte[11 * 1024 * 1024];
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "large.jpg",
                "image/jpeg",
                largeContent
        );

        assertThatThrownBy(() -> fileStorageService.store(file))
                .isInstanceOf(FileStorageException.class)
                .hasMessageContaining("File size exceeds");
    }

    @Test
    void testStore_EmptyFile() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "empty.jpg",
                "image/jpeg",
                new byte[0]
        );

        assertThatThrownBy(() -> fileStorageService.store(file))
                .isInstanceOf(FileStorageException.class)
                .hasMessageContaining("empty");
    }

    @Test
    void testStore_InvalidPath() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "../../../etc/passwd.jpg",
                "image/jpeg",
                "content".getBytes()
        );

        assertThatThrownBy(() -> fileStorageService.store(file))
                .isInstanceOf(FileStorageException.class)
                .hasMessageContaining("invalid path sequence");
    }

    @Test
    void testLoad_Success() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                "test content".getBytes()
        );
        String storedFilename = fileStorageService.store(file);

        Resource resource = fileStorageService.load(storedFilename);

        assertThat(resource).isNotNull();
        assertThat(resource.exists()).isTrue();
        assertThat(resource.isReadable()).isTrue();
    }

    @Test
    void testLoad_FileNotFound() {
        assertThatThrownBy(() -> fileStorageService.load("nonexistent.jpg"))
                .isInstanceOf(FileStorageException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void testDelete_Success() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                "test content".getBytes()
        );
        String filename = fileStorageService.store(file);

        fileStorageService.delete(filename);

        assertThat(Files.exists(tempDir.resolve(filename))).isFalse();
    }

    @Test
    void testDelete_FileNotFound() {
        // When - Deleting non-existent file should not throw exception (deleteIfExists)
        fileStorageService.delete("nonexistent.jpg");

        // Then - No exception thrown
    }

    @Test
    void testStore_GeneratesUniqueFilenames() {
        MockMultipartFile file1 = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                "content1".getBytes()
        );
        MockMultipartFile file2 = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                "content2".getBytes()
        );

        String filename1 = fileStorageService.store(file1);
        String filename2 = fileStorageService.store(file2);

        assertThat(filename1).isNotEqualTo(filename2);
    }

    @Test
    void testStore_SupportsMultipleImageFormats() {
        String[] extensions = {"jpg", "jpeg", "png", "gif"};

        for (String ext : extensions) {
            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "test." + ext,
                    "image/" + ext,
                    "test content".getBytes()
            );

            String filename = fileStorageService.store(file);

            assertThat(filename).endsWith("." + ext);
        }
    }

    @Test
    void testGetFileStorageLocation() {
        Path location = fileStorageService.getFileStorageLocation();

        assertThat(location).isNotNull();
        assertThat(Files.exists(location)).isTrue();
        assertThat(Files.isDirectory(location)).isTrue();
    }

    @Test
    void testStore_NullFile() {
        assertThatThrownBy(() -> fileStorageService.store(null))
                .isInstanceOf(Exception.class);
    }

    @Test
    void testStore_HandlesSpecialCharactersInFilename() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test file with spaces.jpg",
                "image/jpeg",
                "content".getBytes()
        );

        String filename = fileStorageService.store(file);

        assertThat(filename).isNotNull();
        assertThat(Files.exists(tempDir.resolve(filename))).isTrue();
    }
}

