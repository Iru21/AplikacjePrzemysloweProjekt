package me.iru.datingapp.controller.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import me.iru.datingapp.dto.LoginDto;
import me.iru.datingapp.dto.UserProfileDto;
import me.iru.datingapp.dto.UserRegistrationDto;
import me.iru.datingapp.service.AuthenticationService;
import me.iru.datingapp.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final UserService userService;
    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    public ResponseEntity<UserProfileDto> register(@Valid @RequestBody UserRegistrationDto dto) {
        log.info("REST API: Registration request for email: {}", dto.getEmail());
        UserProfileDto userProfile = userService.registerUser(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(userProfile);
    }

    @PostMapping("/login")
    public ResponseEntity<UserProfileDto> login(@Valid @RequestBody LoginDto dto) {
        log.info("REST API: Login request for email: {}", dto.getEmail());
        UserProfileDto userProfile = authenticationService.login(dto);
        return ResponseEntity.ok(userProfile);
    }
}

