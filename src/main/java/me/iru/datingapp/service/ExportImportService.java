package me.iru.datingapp.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.RequiredArgsConstructor;
import me.iru.datingapp.dto.UserProfileDto;
import me.iru.datingapp.entity.*;
import me.iru.datingapp.exception.FileStorageException;
import me.iru.datingapp.exception.ResourceNotFoundException;
import me.iru.datingapp.mapper.UserMapper;
import me.iru.datingapp.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExportImportService {

    private static final Logger log = LoggerFactory.getLogger(ExportImportService.class);

    private final UserRepository userRepository;
    private final MatchRepository matchRepository;
    private final MessageRepository messageRepository;
    private final RatingRepository ratingRepository;
    private final SearchPreferenceRepository searchPreferenceRepository;
    private final UserInterestRepository userInterestRepository;
    private final UserMapper userMapper;
    private final ObjectMapper objectMapper;

    /**
     * Exports user data in the specified format (JSON or XML)
     *
     * @param userId User ID
     * @param format Export format ("json" or "xml")
     * @return Byte array of exported data
     * @throws ResourceNotFoundException if user not found
     * @throws FileStorageException     if export fails
     */
    public byte[] exportUserData(Long userId, String format) {
        log.info("Exporting user data for user ID: {} in format: {}", userId, format);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("User not found with ID: {}", userId);
                    return new ResourceNotFoundException("User not found with ID: " + userId);
                });

        Map<String, Object> userData = buildUserDataMap(user);

        try {
            byte[] data;
            if ("xml".equalsIgnoreCase(format)) {
                data = exportToXml(userData);
            } else {
                data = exportToJson(userData);
                format = "json";
            }

            log.info("Successfully exported user data for user ID: {} in {} format", userId, format);
            return data;

        } catch (IOException e) {
            log.error("Failed to export user data for user ID {}: {}", userId, e.getMessage());
            throw new FileStorageException("Failed to export user data: " + e.getMessage(), e);
        }
    }

    /**
     * Exports user data to CSV format
     *
     * @param userId User ID
     * @return CSV byte array
     * @throws ResourceNotFoundException if user not found
     */
    public byte[] exportToCsv(Long userId) {
        log.info("Exporting user data to CSV for user ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("User not found with ID: {}", userId);
                    return new ResourceNotFoundException("User not found with ID: " + userId);
                });

        StringBuilder csv = new StringBuilder();

        csv.append("Field,Value\n");

        csv.append("ID,").append(user.getId()).append("\n");
        csv.append("Email,").append(escapeCSV(user.getEmail())).append("\n");
        csv.append("First Name,").append(escapeCSV(user.getFirstName())).append("\n");
        csv.append("Last Name,").append(escapeCSV(user.getLastName())).append("\n");
        csv.append("Gender,").append(user.getGender()).append("\n");
        csv.append("Age,").append(user.getAge()).append("\n");
        csv.append("City,").append(escapeCSV(user.getCity())).append("\n");
        csv.append("Bio,").append(escapeCSV(user.getBio())).append("\n");
        csv.append("Created At,").append(user.getCreatedAt()).append("\n");

        List<String> interests = userInterestRepository.findByUserId(userId).stream()
                .map(ui -> ui.getInterest().getName())
                .collect(Collectors.toList());
        csv.append("Interests,\"").append(String.join(", ", interests)).append("\"\n");

        long matchCount = matchRepository.findActiveMatchesByUserId(userId).size();
        long messageCount = messageRepository.findBySenderId(userId).size();
        csv.append("Active Matches,").append(matchCount).append("\n");
        csv.append("Messages Sent,").append(messageCount).append("\n");

        log.info("Successfully exported user data to CSV for user ID: {}", userId);
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Imports user data from a file (JSON or XML)
     *
     * @implNote Unimplemented
     * @param file Uploaded file containing user data
     * @throws FileStorageException if import fails
     */
    @Transactional
    public void importUserData(MultipartFile file) {
        log.info("Importing user data from file: {}", file.getOriginalFilename());

        try {
            String content = new String(file.getBytes(), StandardCharsets.UTF_8);

            Map<String, Object> userData;
            if (Objects.requireNonNull(file.getOriginalFilename()).endsWith(".xml")) {
                XmlMapper xmlMapper = new XmlMapper();
                userData = xmlMapper.readValue(content, Map.class);
            } else {
                userData = objectMapper.readValue(content, Map.class);
            }

            log.info("Successfully imported user data from file: {}", file.getOriginalFilename());
            // TODO: Implement actual data restoration logic based on requirements

        } catch (IOException e) {
            log.error("Failed to import user data: {}", e.getMessage());
            throw new FileStorageException("Failed to import user data: " + e.getMessage(), e);
        }
    }

    private Map<String, Object> buildUserDataMap(User user) {
        Map<String, Object> data = new HashMap<>();

        UserProfileDto profile = userMapper.toDto(user);
        data.put("profile", profile);

        searchPreferenceRepository.findByUserId(user.getId())
                .ifPresent(pref -> {
                    Map<String, Object> preferences = new HashMap<>();
                    preferences.put("preferredGender", pref.getPreferredGender());
                    preferences.put("minAge", pref.getMinAge());
                    preferences.put("maxAge", pref.getMaxAge());
                    preferences.put("maxDistance", pref.getMaxDistance());
                    data.put("searchPreferences", preferences);
                });

        List<Match> matches = matchRepository.findActiveMatchesByUserId(user.getId());
        data.put("activeMatches", matches.size());

        long sentMessages = messageRepository.findBySenderId(user.getId()).size();
        long receivedMessages = messageRepository.findByReceiverId(user.getId()).size();
        Map<String, Long> messageStats = new HashMap<>();
        messageStats.put("sent", sentMessages);
        messageStats.put("received", receivedMessages);
        data.put("messageStats", messageStats);

        long likesGiven = ratingRepository.findByRaterId(user.getId()).stream()
                .filter(r -> r.getRatingType() == Rating.RatingType.LIKE)
                .count();
        long likesReceived = ratingRepository.findByRatedUserId(user.getId()).stream()
                .filter(r -> r.getRatingType() == Rating.RatingType.LIKE)
                .count();
        Map<String, Long> ratingStats = new HashMap<>();
        ratingStats.put("likesGiven", likesGiven);
        ratingStats.put("likesReceived", likesReceived);
        data.put("ratingStats", ratingStats);

        return data;
    }

    private byte[] exportToJson(Map<String, Object> data) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.findAndRegisterModules();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        mapper.writeValue(outputStream, data);
        return outputStream.toByteArray();
    }

    private byte[] exportToXml(Map<String, Object> data) throws IOException {
        XmlMapper xmlMapper = new XmlMapper();
        xmlMapper.enable(SerializationFeature.INDENT_OUTPUT);
        xmlMapper.findAndRegisterModules();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        xmlMapper.writeValue(outputStream, data);
        return outputStream.toByteArray();
    }

    private String escapeCSV(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}

