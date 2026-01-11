package me.iru.datingapp.controller.api;

import lombok.RequiredArgsConstructor;
import me.iru.datingapp.service.ExportImportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/export")
@RequiredArgsConstructor
public class ExportController {

    private static final Logger log = LoggerFactory.getLogger(ExportController.class);

    private final ExportImportService exportImportService;

    @GetMapping("/profile/{userId}")
    public ResponseEntity<byte[]> exportUserData(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "json") String format) {
        log.info("REST API: Export user data for user ID: {} in format: {}", userId, format);

        byte[] data = exportImportService.exportUserData(userId, format);

        HttpHeaders headers = new HttpHeaders();
        String filename = "user_" + userId + "_profile." + format.toLowerCase();
        headers.setContentDispositionFormData("attachment", filename);

        if ("xml".equalsIgnoreCase(format)) {
            headers.setContentType(MediaType.APPLICATION_XML);
        } else {
            headers.setContentType(MediaType.APPLICATION_JSON);
        }

        return ResponseEntity.ok()
                .headers(headers)
                .body(data);
    }

    @GetMapping("/profile/{userId}/csv")
    public ResponseEntity<byte[]> exportUserDataToCsv(@PathVariable Long userId) {
        log.info("REST API: Export user data to CSV for user ID: {}", userId);

        byte[] data = exportImportService.exportToCsv(userId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentDispositionFormData("attachment", "user_" + userId + "_profile.csv");
        headers.setContentType(MediaType.parseMediaType("text/csv"));

        return ResponseEntity.ok()
                .headers(headers)
                .body(data);
    }


    @PostMapping("/import/profile")
    public ResponseEntity<String> importUserData(@RequestParam("file") MultipartFile file) {
        log.info("REST API: Import user data from file: {}", file.getOriginalFilename());

        exportImportService.importUserData(file);

        return ResponseEntity.ok("User data imported successfully");
    }
}

