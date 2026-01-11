package me.iru.datingapp.controller.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import me.iru.datingapp.dto.UserProfileDto;
import me.iru.datingapp.dto.UserUpdateDto;
import me.iru.datingapp.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    @GetMapping("/{id}")
    public ResponseEntity<UserProfileDto> getUserById(@PathVariable Long id) {
        log.info("REST API: Get user by ID: {}", id);
        UserProfileDto userProfile = userService.getUserById(id);
        return ResponseEntity.ok(userProfile);
    }

    @GetMapping
    public ResponseEntity<Page<UserProfileDto>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("REST API: Get all users, page: {}, size: {}", page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<UserProfileDto> users = userService.getAllUsers(pageable);
        return ResponseEntity.ok(users);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserProfileDto> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateDto dto) {
        log.info("REST API: Update user ID: {}", id);
        UserProfileDto userProfile = userService.updateUserProfile(id, dto);
        return ResponseEntity.ok(userProfile);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        log.info("REST API: Delete user ID: {}", id);
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/{id}/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadPhoto(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        log.info("REST API: Upload photo for user ID: {}", id);
        String photoUrl = userService.uploadProfilePhoto(id, file);
        return ResponseEntity.ok(photoUrl);
    }
}

