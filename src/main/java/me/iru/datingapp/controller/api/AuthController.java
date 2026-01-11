package me.iru.datingapp.controller.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "Authentication", description = "Authentication and registration endpoints")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final UserService userService;
    private final AuthenticationService authenticationService;

    @Operation(summary = "Register new user", description = "Create a new user account with email, password, and profile information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User registered successfully",
                    content = @Content(schema = @Schema(implementation = UserProfileDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input or email already exists",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content)
    })
    @PostMapping("/register")
    public ResponseEntity<UserProfileDto> register(@Valid @RequestBody UserRegistrationDto dto) {
        log.info("REST API: Registration request for email: {}", dto.getEmail());
        UserProfileDto userProfile = userService.registerUser(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(userProfile);
    }

    @Operation(summary = "User login", description = "Authenticate user with email and password")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful",
                    content = @Content(schema = @Schema(implementation = UserProfileDto.class))),
            @ApiResponse(responseCode = "401", description = "Invalid credentials",
                    content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid input",
                    content = @Content)
    })
    @PostMapping("/login")
    public ResponseEntity<UserProfileDto> login(@Valid @RequestBody LoginDto dto) {
        log.info("REST API: Login request for email: {}", dto.getEmail());
        UserProfileDto userProfile = authenticationService.login(dto);
        return ResponseEntity.ok(userProfile);
    }
}
