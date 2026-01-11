package me.iru.datingapp.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.iru.datingapp.dto.UserProfileDto;
import me.iru.datingapp.entity.*;
import me.iru.datingapp.exception.FileStorageException;
import me.iru.datingapp.exception.ResourceNotFoundException;
import me.iru.datingapp.mapper.UserMapper;
import me.iru.datingapp.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExportImportServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private MatchRepository matchRepository;

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private RatingRepository ratingRepository;

    @Mock
    private SearchPreferenceRepository searchPreferenceRepository;

    @Mock
    private UserInterestRepository userInterestRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private ExportImportService exportImportService;

    private User testUser;
    private UserProfileDto testUserDto;
    private SearchPreference searchPreference;
    private Match testMatch;
    private Message testMessage;
    private Rating testRating;
    private UserInterest userInterest;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        exportImportService = new ExportImportService(
                userRepository,
                matchRepository,
                messageRepository,
                ratingRepository,
                searchPreferenceRepository,
                userInterestRepository,
                userMapper,
                objectMapper
        );

        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setGender(User.Gender.MALE);
        testUser.setAge(25);
        testUser.setCity("Warsaw");
        testUser.setBio("Test bio");
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setRole(User.Role.USER);

        testUserDto = new UserProfileDto();
        testUserDto.setId(1L);
        testUserDto.setEmail("test@example.com");
        testUserDto.setFirstName("John");
        testUserDto.setLastName("Doe");
        testUserDto.setGender(User.Gender.MALE);
        testUserDto.setAge(25);
        testUserDto.setCity("Warsaw");

        searchPreference = new SearchPreference();
        searchPreference.setId(1L);
        searchPreference.setUser(testUser);
        searchPreference.setPreferredGender(User.Gender.FEMALE);
        searchPreference.setMinAge(20);
        searchPreference.setMaxAge(30);
        searchPreference.setMaxDistance(50);

        User matchedUser = new User();
        matchedUser.setId(2L);
        matchedUser.setEmail("match@example.com");

        testMatch = new Match();
        testMatch.setId(1L);
        testMatch.setUser1(testUser);
        testMatch.setUser2(matchedUser);
        testMatch.setMatchedAt(LocalDateTime.now());

        testMessage = new Message();
        testMessage.setId(1L);
        testMessage.setSender(testUser);
        testMessage.setReceiver(matchedUser);
        testMessage.setContent("Hello");
        testMessage.setSentAt(LocalDateTime.now());

        testRating = new Rating();
        testRating.setId(1L);
        testRating.setRater(testUser);
        testRating.setRatedUser(matchedUser);
        testRating.setRatingType(Rating.RatingType.LIKE);
        testRating.setCreatedAt(LocalDateTime.now());

        Interest interest = new Interest();
        interest.setId(1L);
        interest.setName("Sports");

        userInterest = new UserInterest();
        userInterest.setId(1L);
        userInterest.setUser(testUser);
        userInterest.setInterest(interest);
    }

    @Test
    void testExportUserData_JsonFormat_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userMapper.toDto(testUser)).thenReturn(testUserDto);
        when(searchPreferenceRepository.findByUserId(1L)).thenReturn(Optional.of(searchPreference));
        when(matchRepository.findActiveMatchesByUserId(1L)).thenReturn(List.of(testMatch));
        when(messageRepository.findBySenderId(1L)).thenReturn(List.of(testMessage));
        when(messageRepository.findByReceiverId(1L)).thenReturn(List.of());
        when(ratingRepository.findByRaterId(1L)).thenReturn(List.of(testRating));
        when(ratingRepository.findByRatedUserId(1L)).thenReturn(List.of());

        byte[] result = exportImportService.exportUserData(1L, "json");

        assertThat(result).isNotNull();
        assertThat(result.length).isGreaterThan(0);
        String json = new String(result, StandardCharsets.UTF_8);
        assertThat(json).contains("profile");

        verify(userRepository).findById(1L);
        verify(userMapper).toDto(testUser);
        verify(searchPreferenceRepository).findByUserId(1L);
        verify(matchRepository).findActiveMatchesByUserId(1L);
        verify(messageRepository).findBySenderId(1L);
        verify(messageRepository).findByReceiverId(1L);
        verify(ratingRepository).findByRaterId(1L);
        verify(ratingRepository).findByRatedUserId(1L);
    }


    @Test
    void testExportUserData_UserNotFound_ThrowsException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> exportImportService.exportUserData(999L, "json"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found with ID: 999");

        verify(userRepository).findById(999L);
    }

    @Test
    void testExportUserData_DefaultsToJson_WhenInvalidFormat() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userMapper.toDto(testUser)).thenReturn(testUserDto);
        when(searchPreferenceRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(matchRepository.findActiveMatchesByUserId(1L)).thenReturn(List.of());
        when(messageRepository.findBySenderId(1L)).thenReturn(List.of());
        when(messageRepository.findByReceiverId(1L)).thenReturn(List.of());
        when(ratingRepository.findByRaterId(1L)).thenReturn(List.of());
        when(ratingRepository.findByRatedUserId(1L)).thenReturn(List.of());

        byte[] result = exportImportService.exportUserData(1L, "invalid");

        assertThat(result).isNotNull();
        assertThat(result.length).isGreaterThan(0);
        String json = new String(result, StandardCharsets.UTF_8);
        assertThat(json).contains("profile");

        verify(userRepository).findById(1L);
        verify(userMapper).toDto(testUser);
        verify(searchPreferenceRepository).findByUserId(1L);
        verify(matchRepository).findActiveMatchesByUserId(1L);
        verify(messageRepository).findBySenderId(1L);
        verify(messageRepository).findByReceiverId(1L);
        verify(ratingRepository).findByRaterId(1L);
        verify(ratingRepository).findByRatedUserId(1L);
    }

    @Test
    void testExportToCsv_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userInterestRepository.findByUserId(1L)).thenReturn(List.of(userInterest));
        when(matchRepository.findActiveMatchesByUserId(1L)).thenReturn(List.of(testMatch));
        when(messageRepository.findBySenderId(1L)).thenReturn(List.of(testMessage));

        byte[] result = exportImportService.exportToCsv(1L);

        assertThat(result).isNotNull();
        String csv = new String(result, StandardCharsets.UTF_8);

        assertThat(csv).contains("Field,Value");
        assertThat(csv).contains("ID,1");
        assertThat(csv).contains("Email,test@example.com");
        assertThat(csv).contains("First Name,John");
        assertThat(csv).contains("Last Name,Doe");
        assertThat(csv).contains("Gender,MALE");
        assertThat(csv).contains("Age,25");
        assertThat(csv).contains("City,Warsaw");
        assertThat(csv).contains("Bio,Test bio");
        assertThat(csv).contains("Interests,\"Sports\"");
        assertThat(csv).contains("Active Matches,1");
        assertThat(csv).contains("Messages Sent,1");

        verify(userRepository).findById(1L);
        verify(userInterestRepository).findByUserId(1L);
        verify(matchRepository).findActiveMatchesByUserId(1L);
        verify(messageRepository).findBySenderId(1L);
    }

    @Test
    void testExportToCsv_UserNotFound_ThrowsException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> exportImportService.exportToCsv(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found with ID: 999");

        verify(userRepository).findById(999L);
    }

    @Test
    void testExportToCsv_WithNullValues() {
        testUser.setBio(null);
        testUser.setCity(null);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userInterestRepository.findByUserId(1L)).thenReturn(List.of());
        when(matchRepository.findActiveMatchesByUserId(1L)).thenReturn(List.of());
        when(messageRepository.findBySenderId(1L)).thenReturn(List.of());

        byte[] result = exportImportService.exportToCsv(1L);

        assertThat(result).isNotNull();
        String csv = new String(result, StandardCharsets.UTF_8);

        assertThat(csv).contains("Bio,");
        assertThat(csv).contains("City,");
        assertThat(csv).contains("Interests,\"\"");
        assertThat(csv).contains("Active Matches,0");
        assertThat(csv).contains("Messages Sent,0");

        verify(userRepository).findById(1L);
    }

    @Test
    void testExportToCsv_WithSpecialCharactersInFields() {
        testUser.setBio("Test, bio with \"quotes\" and\nnewlines");
        testUser.setCity("Warsaw, Poland");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userInterestRepository.findByUserId(1L)).thenReturn(List.of());
        when(matchRepository.findActiveMatchesByUserId(1L)).thenReturn(List.of());
        when(messageRepository.findBySenderId(1L)).thenReturn(List.of());

        byte[] result = exportImportService.exportToCsv(1L);

        assertThat(result).isNotNull();
        String csv = new String(result, StandardCharsets.UTF_8);

        assertThat(csv).contains("Bio,\"Test, bio with \"\"quotes\"\" and\nnewlines\"");
        assertThat(csv).contains("City,\"Warsaw, Poland\"");

        verify(userRepository).findById(1L);
    }

    @Test
    void testExportToCsv_WithMultipleInterests() {
        Interest interest2 = new Interest();
        interest2.setId(2L);
        interest2.setName("Music");

        UserInterest userInterest2 = new UserInterest();
        userInterest2.setId(2L);
        userInterest2.setUser(testUser);
        userInterest2.setInterest(interest2);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userInterestRepository.findByUserId(1L)).thenReturn(List.of(userInterest, userInterest2));
        when(matchRepository.findActiveMatchesByUserId(1L)).thenReturn(List.of());
        when(messageRepository.findBySenderId(1L)).thenReturn(List.of());

        byte[] result = exportImportService.exportToCsv(1L);

        assertThat(result).isNotNull();
        String csv = new String(result, StandardCharsets.UTF_8);

        assertThat(csv).contains("Interests,\"Sports, Music\"");

        verify(userRepository).findById(1L);
        verify(userInterestRepository).findByUserId(1L);
    }

    @Test
    void testImportUserData_JsonFile_Success() throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        String jsonContent = "{\"profile\":{\"id\":1,\"email\":\"test@example.com\"}}";

        when(file.getOriginalFilename()).thenReturn("userdata.json");
        when(file.getBytes()).thenReturn(jsonContent.getBytes(StandardCharsets.UTF_8));

        exportImportService.importUserData(file);

        verify(file, atLeastOnce()).getOriginalFilename();
        verify(file).getBytes();
    }

    @Test
    void testImportUserData_XmlFile_Success() throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        String xmlContent = "<userData><id>1</id></userData>";

        when(file.getOriginalFilename()).thenReturn("userdata.xml");
        when(file.getBytes()).thenReturn(xmlContent.getBytes(StandardCharsets.UTF_8));

        exportImportService.importUserData(file);

        verify(file, atLeastOnce()).getOriginalFilename();
        verify(file).getBytes();
    }

    @Test
    void testImportUserData_InvalidFile_ThrowsException() throws IOException {
        MultipartFile file = mock(MultipartFile.class);

        when(file.getOriginalFilename()).thenReturn("userdata.json");
        when(file.getBytes()).thenThrow(new IOException("File read error"));

        assertThatThrownBy(() -> exportImportService.importUserData(file))
                .isInstanceOf(FileStorageException.class)
                .hasMessageContaining("Failed to import user data");

        verify(file).getBytes();
    }

    @Test
    void testImportUserData_InvalidJsonContent_ThrowsException() throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        String invalidJson = "not json at all";

        when(file.getOriginalFilename()).thenReturn("userdata.json");
        when(file.getBytes()).thenReturn(invalidJson.getBytes(StandardCharsets.UTF_8));

        assertThatThrownBy(() -> exportImportService.importUserData(file))
                .isInstanceOf(FileStorageException.class)
                .hasMessageContaining("Failed to import user data");

        verify(file).getBytes();
    }

    @Test
    void testExportUserData_WithCompleteUserData() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userMapper.toDto(testUser)).thenReturn(testUserDto);
        when(searchPreferenceRepository.findByUserId(1L)).thenReturn(Optional.of(searchPreference));
        when(matchRepository.findActiveMatchesByUserId(1L)).thenReturn(List.of(testMatch));
        when(messageRepository.findBySenderId(1L)).thenReturn(List.of(testMessage));
        when(messageRepository.findByReceiverId(1L)).thenReturn(List.of(testMessage));
        when(ratingRepository.findByRaterId(1L)).thenReturn(List.of(testRating));
        when(ratingRepository.findByRatedUserId(1L)).thenReturn(List.of(testRating));

        byte[] result = exportImportService.exportUserData(1L, "json");

        assertThat(result).isNotNull();
        assertThat(result.length).isGreaterThan(0);
        String json = new String(result, StandardCharsets.UTF_8);
        assertThat(json).contains("profile");
        assertThat(json).contains("searchPreferences");
        assertThat(json).contains("messageStats");
        assertThat(json).contains("ratingStats");

        verify(userRepository).findById(1L);
        verify(userMapper).toDto(testUser);
        verify(searchPreferenceRepository).findByUserId(1L);
        verify(matchRepository).findActiveMatchesByUserId(1L);
        verify(messageRepository).findBySenderId(1L);
        verify(messageRepository).findByReceiverId(1L);
        verify(ratingRepository).findByRaterId(1L);
        verify(ratingRepository).findByRatedUserId(1L);
    }

    @Test
    void testExportUserData_WithoutSearchPreferences() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userMapper.toDto(testUser)).thenReturn(testUserDto);
        when(searchPreferenceRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(matchRepository.findActiveMatchesByUserId(1L)).thenReturn(List.of());
        when(messageRepository.findBySenderId(1L)).thenReturn(List.of());
        when(messageRepository.findByReceiverId(1L)).thenReturn(List.of());
        when(ratingRepository.findByRaterId(1L)).thenReturn(List.of());
        when(ratingRepository.findByRatedUserId(1L)).thenReturn(List.of());

        byte[] result = exportImportService.exportUserData(1L, "json");

        assertThat(result).isNotNull();
        assertThat(result.length).isGreaterThan(0);
        String json = new String(result, StandardCharsets.UTF_8);
        assertThat(json).contains("profile");
        assertThat(json).doesNotContain("searchPreferences");

        verify(searchPreferenceRepository).findByUserId(1L);
    }

    @Test
    void testExportUserData_WithMultipleMatches() {
        User matchedUser2 = new User();
        matchedUser2.setId(3L);
        matchedUser2.setEmail("match2@example.com");

        Match testMatch2 = new Match();
        testMatch2.setId(2L);
        testMatch2.setUser1(testUser);
        testMatch2.setUser2(matchedUser2);
        testMatch2.setMatchedAt(LocalDateTime.now());

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userMapper.toDto(testUser)).thenReturn(testUserDto);
        when(searchPreferenceRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(matchRepository.findActiveMatchesByUserId(1L)).thenReturn(List.of(testMatch, testMatch2));
        when(messageRepository.findBySenderId(1L)).thenReturn(List.of());
        when(messageRepository.findByReceiverId(1L)).thenReturn(List.of());
        when(ratingRepository.findByRaterId(1L)).thenReturn(List.of());
        when(ratingRepository.findByRatedUserId(1L)).thenReturn(List.of());

        byte[] result = exportImportService.exportUserData(1L, "json");

        assertThat(result).isNotNull();
        assertThat(result.length).isGreaterThan(0);
        String json = new String(result, StandardCharsets.UTF_8);
        assertThat(json).containsPattern("\"activeMatches\"\\s*:\\s*2");

        verify(matchRepository).findActiveMatchesByUserId(1L);
    }

    @Test
    void testExportUserData_WithDislikeRatings() {
        Rating dislikeRating = new Rating();
        dislikeRating.setId(2L);
        dislikeRating.setRater(testUser);
        dislikeRating.setRatingType(Rating.RatingType.DISLIKE);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userMapper.toDto(testUser)).thenReturn(testUserDto);
        when(searchPreferenceRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(matchRepository.findActiveMatchesByUserId(1L)).thenReturn(List.of());
        when(messageRepository.findBySenderId(1L)).thenReturn(List.of());
        when(messageRepository.findByReceiverId(1L)).thenReturn(List.of());
        when(ratingRepository.findByRaterId(1L)).thenReturn(List.of(testRating, dislikeRating));
        when(ratingRepository.findByRatedUserId(1L)).thenReturn(List.of());

        byte[] result = exportImportService.exportUserData(1L, "json");

        assertThat(result).isNotNull();
        assertThat(result.length).isGreaterThan(0);
        String json = new String(result, StandardCharsets.UTF_8);
        assertThat(json).contains("ratingStats");
        assertThat(json).contains("likesGiven");
        assertThat(json).containsPattern("\"likesGiven\"\\s*:\\s*1");

        verify(ratingRepository).findByRaterId(1L);
    }
}

