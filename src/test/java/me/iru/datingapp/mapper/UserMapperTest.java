package me.iru.datingapp.mapper;

import me.iru.datingapp.dto.UserProfileDto;
import me.iru.datingapp.dto.UserRegistrationDto;
import me.iru.datingapp.dto.UserUpdateDto;
import me.iru.datingapp.entity.Interest;
import me.iru.datingapp.entity.User;
import me.iru.datingapp.entity.UserInterest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class UserMapperTest {

    private UserMapper userMapper;
    private User testUser;
    private LocalDateTime testDateTime;

    @BeforeEach
    void setUp() {
        userMapper = new UserMapper();
        testDateTime = LocalDateTime.of(2026, 1, 12, 10, 30);

        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setPassword("hashedPassword123");
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setGender(User.Gender.MALE);
        testUser.setAge(25);
        testUser.setCity("Warsaw");
        testUser.setBio("Test bio");
        testUser.setPhotoUrl("/photos/test.jpg");
        testUser.setCreatedAt(testDateTime);
        testUser.setRole(User.Role.USER);
        testUser.setUserInterests(new ArrayList<>());
    }


    @Test
    void testToDto_Success() {
        UserProfileDto result = userMapper.toDto(testUser);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        assertThat(result.getFirstName()).isEqualTo("John");
        assertThat(result.getLastName()).isEqualTo("Doe");
        assertThat(result.getGender()).isEqualTo(User.Gender.MALE);
        assertThat(result.getAge()).isEqualTo(25);
        assertThat(result.getCity()).isEqualTo("Warsaw");
        assertThat(result.getBio()).isEqualTo("Test bio");
        assertThat(result.getPhotoUrl()).isEqualTo("/photos/test.jpg");
        assertThat(result.getCreatedAt()).isEqualTo(testDateTime);
        assertThat(result.getInterests()).isEmpty();
    }

    @Test
    void testToDto_WithInterests() {
        Interest interest1 = new Interest();
        interest1.setId(1L);
        interest1.setName("Sports");

        Interest interest2 = new Interest();
        interest2.setId(2L);
        interest2.setName("Music");

        UserInterest userInterest1 = new UserInterest();
        userInterest1.setInterest(interest1);
        userInterest1.setUser(testUser);

        UserInterest userInterest2 = new UserInterest();
        userInterest2.setInterest(interest2);
        userInterest2.setUser(testUser);

        testUser.setUserInterests(List.of(userInterest1, userInterest2));

        UserProfileDto result = userMapper.toDto(testUser);

        assertThat(result).isNotNull();
        assertThat(result.getInterests()).hasSize(2);
        assertThat(result.getInterests()).containsExactly("Sports", "Music");
    }

    @Test
    void testToDto_WithNullUser() {
        UserProfileDto result = userMapper.toDto(null);

        assertThat(result).isNull();
    }

    @Test
    void testToDto_WithNullInterests() {
        testUser.setUserInterests(null);

        UserProfileDto result = userMapper.toDto(testUser);

        assertThat(result).isNotNull();
        assertThat(result.getInterests()).isEmpty();
    }

    @Test
    void testToDto_WithNullFields() {
        testUser.setBio(null);
        testUser.setPhotoUrl(null);
        testUser.setCity(null);

        UserProfileDto result = userMapper.toDto(testUser);

        assertThat(result).isNotNull();
        assertThat(result.getBio()).isNull();
        assertThat(result.getPhotoUrl()).isNull();
        assertThat(result.getCity()).isNull();
    }

    @Test
    void testToDto_FemaleGender() {
        testUser.setGender(User.Gender.FEMALE);

        UserProfileDto result = userMapper.toDto(testUser);

        assertThat(result).isNotNull();
        assertThat(result.getGender()).isEqualTo(User.Gender.FEMALE);
    }

    @Test
    void testToDto_OtherGender() {
        testUser.setGender(User.Gender.OTHER);

        UserProfileDto result = userMapper.toDto(testUser);

        assertThat(result).isNotNull();
        assertThat(result.getGender()).isEqualTo(User.Gender.OTHER);
    }


    @Test
    void testToEntity_Success() {
        UserRegistrationDto dto = new UserRegistrationDto();
        dto.setEmail("newuser@example.com");
        dto.setPassword("password123");
        dto.setFirstName("Jane");
        dto.setLastName("Smith");
        dto.setGender(User.Gender.FEMALE);
        dto.setAge(28);
        dto.setCity("Krakow");
        dto.setBio("New user bio");

        User result = userMapper.toEntity(dto);

        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("newuser@example.com");
        assertThat(result.getPassword()).isEqualTo("password123");
        assertThat(result.getFirstName()).isEqualTo("Jane");
        assertThat(result.getLastName()).isEqualTo("Smith");
        assertThat(result.getGender()).isEqualTo(User.Gender.FEMALE);
        assertThat(result.getAge()).isEqualTo(28);
        assertThat(result.getCity()).isEqualTo("Krakow");
        assertThat(result.getBio()).isEqualTo("New user bio");
        assertThat(result.getRole()).isEqualTo(User.Role.USER);
        assertThat(result.getId()).isNull();
    }

    @Test
    void testToEntity_WithNullDto() {
        User result = userMapper.toEntity(null);

        assertThat(result).isNull();
    }

    @Test
    void testToEntity_WithNullBio() {
        UserRegistrationDto dto = new UserRegistrationDto();
        dto.setEmail("test@example.com");
        dto.setPassword("pass");
        dto.setFirstName("John");
        dto.setLastName("Doe");
        dto.setGender(User.Gender.MALE);
        dto.setAge(25);
        dto.setCity("Warsaw");
        dto.setBio(null);

        User result = userMapper.toEntity(dto);

        assertThat(result).isNotNull();
        assertThat(result.getBio()).isNull();
    }

    @Test
    void testToEntity_AlwaysSetRoleToUser() {
        UserRegistrationDto dto = new UserRegistrationDto();
        dto.setEmail("test@example.com");
        dto.setPassword("pass");
        dto.setFirstName("John");
        dto.setLastName("Doe");
        dto.setGender(User.Gender.MALE);
        dto.setAge(25);
        dto.setCity("Warsaw");

        User result = userMapper.toEntity(dto);

        assertThat(result.getRole()).isEqualTo(User.Role.USER);
    }


    @Test
    void testUpdateEntityFromDto_UpdateBio() {
        UserUpdateDto dto = new UserUpdateDto();
        dto.setBio("Updated bio");

        userMapper.updateEntityFromDto(dto, testUser);

        assertThat(testUser.getBio()).isEqualTo("Updated bio");
        assertThat(testUser.getCity()).isEqualTo("Warsaw");
        assertThat(testUser.getPhotoUrl()).isEqualTo("/photos/test.jpg");
    }

    @Test
    void testUpdateEntityFromDto_UpdateCity() {
        UserUpdateDto dto = new UserUpdateDto();
        dto.setCity("Gdansk");

        userMapper.updateEntityFromDto(dto, testUser);

        assertThat(testUser.getCity()).isEqualTo("Gdansk");
        assertThat(testUser.getBio()).isEqualTo("Test bio");
    }

    @Test
    void testUpdateEntityFromDto_UpdatePhotoUrl() {
        UserUpdateDto dto = new UserUpdateDto();
        dto.setPhotoUrl("/photos/newphoto.jpg");

        userMapper.updateEntityFromDto(dto, testUser);

        assertThat(testUser.getPhotoUrl()).isEqualTo("/photos/newphoto.jpg");
        assertThat(testUser.getBio()).isEqualTo("Test bio");
    }

    @Test
    void testUpdateEntityFromDto_UpdateAllFields() {
        UserUpdateDto dto = new UserUpdateDto();
        dto.setBio("New bio");
        dto.setCity("Poznan");
        dto.setPhotoUrl("/photos/updated.jpg");

        userMapper.updateEntityFromDto(dto, testUser);

        assertThat(testUser.getBio()).isEqualTo("New bio");
        assertThat(testUser.getCity()).isEqualTo("Poznan");
        assertThat(testUser.getPhotoUrl()).isEqualTo("/photos/updated.jpg");
    }

    @Test
    void testUpdateEntityFromDto_WithNullDto() {
        String originalBio = testUser.getBio();
        String originalCity = testUser.getCity();

        userMapper.updateEntityFromDto(null, testUser);

        assertThat(testUser.getBio()).isEqualTo(originalBio);
        assertThat(testUser.getCity()).isEqualTo(originalCity);
    }

    @Test
    void testUpdateEntityFromDto_WithNullUser() {
        UserUpdateDto dto = new UserUpdateDto();
        dto.setBio("New bio");

        assertThatCode(() -> userMapper.updateEntityFromDto(dto, null))
                .doesNotThrowAnyException();
    }

    @Test
    void testUpdateEntityFromDto_WithNullFields() {
        UserUpdateDto dto = new UserUpdateDto();
        dto.setBio(null);
        dto.setCity(null);
        dto.setPhotoUrl(null);

        String originalBio = testUser.getBio();
        String originalCity = testUser.getCity();
        String originalPhotoUrl = testUser.getPhotoUrl();

        userMapper.updateEntityFromDto(dto, testUser);

        assertThat(testUser.getBio()).isEqualTo(originalBio);
        assertThat(testUser.getCity()).isEqualTo(originalCity);
        assertThat(testUser.getPhotoUrl()).isEqualTo(originalPhotoUrl);
    }

    @Test
    void testUpdateEntityFromDto_PartialUpdate() {
        UserUpdateDto dto = new UserUpdateDto();
        dto.setBio("Partial update");

        userMapper.updateEntityFromDto(dto, testUser);

        assertThat(testUser.getBio()).isEqualTo("Partial update");
        assertThat(testUser.getCity()).isEqualTo("Warsaw");
        assertThat(testUser.getPhotoUrl()).isEqualTo("/photos/test.jpg");
    }


    @Test
    void testToDtoList_Success() {
        User user2 = new User();
        user2.setId(2L);
        user2.setEmail("user2@example.com");
        user2.setFirstName("Jane");
        user2.setLastName("Smith");
        user2.setGender(User.Gender.FEMALE);
        user2.setAge(28);
        user2.setCity("Krakow");
        user2.setCreatedAt(testDateTime);
        user2.setUserInterests(new ArrayList<>());

        List<User> users = List.of(testUser, user2);

        List<UserProfileDto> result = userMapper.toDtoList(users);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getEmail()).isEqualTo("test@example.com");
        assertThat(result.get(1).getEmail()).isEqualTo("user2@example.com");
    }

    @Test
    void testToDtoList_EmptyList() {
        List<UserProfileDto> result = userMapper.toDtoList(List.of());

        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    @Test
    void testToDtoList_WithNullList() {
        List<UserProfileDto> result = userMapper.toDtoList(null);

        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    @Test
    void testToDtoList_SingleUser() {
        List<UserProfileDto> result = userMapper.toDtoList(List.of(testUser));

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getId()).isEqualTo(1L);
    }

    @Test
    void testToDtoList_PreservesOrder() {
        User user2 = new User();
        user2.setId(2L);
        user2.setEmail("user2@example.com");
        user2.setFirstName("Jane");
        user2.setLastName("Smith");
        user2.setGender(User.Gender.FEMALE);
        user2.setAge(28);
        user2.setUserInterests(new ArrayList<>());

        User user3 = new User();
        user3.setId(3L);
        user3.setEmail("user3@example.com");
        user3.setFirstName("Bob");
        user3.setLastName("Johnson");
        user3.setGender(User.Gender.MALE);
        user3.setAge(30);
        user3.setUserInterests(new ArrayList<>());

        List<User> users = List.of(testUser, user2, user3);

        List<UserProfileDto> result = userMapper.toDtoList(users);

        assertThat(result).hasSize(3);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(1).getId()).isEqualTo(2L);
        assertThat(result.get(2).getId()).isEqualTo(3L);
    }
}

