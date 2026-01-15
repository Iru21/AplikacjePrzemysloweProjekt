package me.iru.datingapp.controller.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import me.iru.datingapp.service.ExportImportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Data Export/Import", description = "User data export and import endpoints")
@RestController
@RequestMapping("/api/export")
@RequiredArgsConstructor
public class ExportController {

    private static final Logger log = LoggerFactory.getLogger(ExportController.class);

    private final ExportImportService exportImportService;

    @Operation(summary = "Export user profile", description = "Export user profile data in JSON or XML format")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Data exported successfully",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Invalid format"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/profile/{userId}")
    public ResponseEntity<byte[]> exportUserData(
            @Parameter(description = "User ID") @PathVariable Long userId,
            @Parameter(description = "Export format (json or xml)") @RequestParam(defaultValue = "json") String format) {
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

    @Operation(summary = "Export user profile to CSV", description = "Export user profile data in CSV format")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "CSV exported successfully",
                    content = @Content(mediaType = "text/csv")),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/profile/{userId}/csv")
    public ResponseEntity<byte[]> exportUserDataToCsv(
            @Parameter(description = "User ID") @PathVariable Long userId) {
        log.info("REST API: Export user data to CSV for user ID: {}", userId);

        byte[] data = exportImportService.exportToCsv(userId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentDispositionFormData("attachment", "user_" + userId + "_profile.csv");
        headers.setContentType(MediaType.parseMediaType("text/csv"));

        return ResponseEntity.ok()
                .headers(headers)
                .body(data);
    }


    @Operation(summary = "Import user profile", description = "Import user profile data from JSON or XML file")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Data imported successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid file or data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping("/import/profile")
    public ResponseEntity<String> importUserData(
            @Parameter(description = "Import file (JSON or XML)") @RequestParam("file") MultipartFile file) {
        log.info("REST API: Import user data from file: {}", file.getOriginalFilename());

        exportImportService.importUserData(file);

        return ResponseEntity.ok("User data imported successfully");
    }
}

