package me.iru.datingapp.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import me.iru.datingapp.dto.LoginDto;
import me.iru.datingapp.dto.UserProfileDto;
import me.iru.datingapp.entity.User;
import me.iru.datingapp.exception.InvalidCredentialsException;
import me.iru.datingapp.mapper.UserMapper;
import me.iru.datingapp.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthenticationService implements UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    /**
     * Authenticates user with email and password
     *
     * @param loginDto Login credentials
     * @return UserProfileDto if authentication successful
     * @throws InvalidCredentialsException if credentials are invalid
     */
    public UserProfileDto login(LoginDto loginDto) {
        log.info("Login attempt for email: {}", loginDto.getEmail());

        User user = userRepository.findByEmail(loginDto.getEmail())
                .orElseThrow(() -> {
                    log.error("Login failed: User not found with email: {}", loginDto.getEmail());
                    return new InvalidCredentialsException("Invalid email or password");
                });

        if (!passwordEncoder.matches(loginDto.getPassword(), user.getPassword())) {
            log.error("Login failed: Invalid password for email: {}", loginDto.getEmail());
            throw new InvalidCredentialsException("Invalid email or password");
        }

        log.info("Login successful for user ID: {} with email: {}", user.getId(), user.getEmail());
        return userMapper.toDto(user);
    }

    /**
     * Loads user by username (email) for Spring Security
     *
     * @param email User email (username in our case)
     * @return UserDetails object
     * @throws UsernameNotFoundException if user not found
     */
    @Override
    @NonNull
    public UserDetails loadUserByUsername(@NonNull String email) throws UsernameNotFoundException {
        log.debug("Loading user details for email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("User not found with email: {}", email);
                    return new UsernameNotFoundException("User not found with email: " + email);
                });

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .authorities(Collections.singletonList(
                        new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
                ))
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false)
                .build();
    }

    /**
     * Validates if the user exists by email
     *
     * @param email User email
     * @return true if a user exists
     */
    public boolean userExists(String email) {
        return userRepository.existsByEmail(email);
    }
}

