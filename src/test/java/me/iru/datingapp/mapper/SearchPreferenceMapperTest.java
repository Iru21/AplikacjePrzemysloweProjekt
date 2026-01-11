package me.iru.datingapp.mapper;

import me.iru.datingapp.dto.SearchPreferenceDto;
import me.iru.datingapp.entity.SearchPreference;
import me.iru.datingapp.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class SearchPreferenceMapperTest {

    private SearchPreferenceMapper searchPreferenceMapper;
    private User testUser;
    private SearchPreference testPreference;

    @BeforeEach
    void setUp() {
        searchPreferenceMapper = new SearchPreferenceMapper();

        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setGender(User.Gender.MALE);
        testUser.setAge(25);

        testPreference = new SearchPreference();
        testPreference.setId(1L);
        testPreference.setUser(testUser);
        testPreference.setPreferredGender(User.Gender.FEMALE);
        testPreference.setMinAge(20);
        testPreference.setMaxAge(30);
        testPreference.setMaxDistance(50);
    }


    @Test
    void testToDto_Success() {
        SearchPreferenceDto result = searchPreferenceMapper.toDto(testPreference);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getPreferredGender()).isEqualTo(User.Gender.FEMALE);
        assertThat(result.getMinAge()).isEqualTo(20);
        assertThat(result.getMaxAge()).isEqualTo(30);
        assertThat(result.getMaxDistance()).isEqualTo(50);
    }

    @Test
    void testToDto_WithNullPreference() {
        SearchPreferenceDto result = searchPreferenceMapper.toDto(null);

        assertThat(result).isNull();
    }

    @Test
    void testToDto_WithNullPreferredGender() {
        testPreference.setPreferredGender(null);

        SearchPreferenceDto result = searchPreferenceMapper.toDto(testPreference);

        assertThat(result).isNotNull();
        assertThat(result.getPreferredGender()).isNull();
    }

    @Test
    void testToDto_WithNullMaxDistance() {
        testPreference.setMaxDistance(null);

        SearchPreferenceDto result = searchPreferenceMapper.toDto(testPreference);

        assertThat(result).isNotNull();
        assertThat(result.getMaxDistance()).isNull();
    }

    @Test
    void testToDto_MalePreference() {
        testPreference.setPreferredGender(User.Gender.MALE);

        SearchPreferenceDto result = searchPreferenceMapper.toDto(testPreference);

        assertThat(result.getPreferredGender()).isEqualTo(User.Gender.MALE);
    }

    @Test
    void testToDto_OtherGenderPreference() {
        testPreference.setPreferredGender(User.Gender.OTHER);

        SearchPreferenceDto result = searchPreferenceMapper.toDto(testPreference);

        assertThat(result.getPreferredGender()).isEqualTo(User.Gender.OTHER);
    }

    @Test
    void testToDto_WideAgeRange() {
        testPreference.setMinAge(18);
        testPreference.setMaxAge(100);

        SearchPreferenceDto result = searchPreferenceMapper.toDto(testPreference);

        assertThat(result.getMinAge()).isEqualTo(18);
        assertThat(result.getMaxAge()).isEqualTo(100);
    }

    @Test
    void testToDto_LargeDistance() {
        testPreference.setMaxDistance(1000);

        SearchPreferenceDto result = searchPreferenceMapper.toDto(testPreference);

        assertThat(result.getMaxDistance()).isEqualTo(1000);
    }


    @Test
    void testToEntity_Success() {
        SearchPreferenceDto dto = new SearchPreferenceDto();
        dto.setPreferredGender(User.Gender.FEMALE);
        dto.setMinAge(22);
        dto.setMaxAge(32);
        dto.setMaxDistance(60);

        SearchPreference result = searchPreferenceMapper.toEntity(dto, testUser);

        assertThat(result).isNotNull();
        assertThat(result.getUser()).isEqualTo(testUser);
        assertThat(result.getPreferredGender()).isEqualTo(User.Gender.FEMALE);
        assertThat(result.getMinAge()).isEqualTo(22);
        assertThat(result.getMaxAge()).isEqualTo(32);
        assertThat(result.getMaxDistance()).isEqualTo(60);
        assertThat(result.getId()).isNull();
    }

    @Test
    void testToEntity_WithNullDto() {
        SearchPreference result = searchPreferenceMapper.toEntity(null, testUser);

        assertThat(result).isNull();
    }

    @Test
    void testToEntity_WithNullPreferredGender() {
        SearchPreferenceDto dto = new SearchPreferenceDto();
        dto.setPreferredGender(null);
        dto.setMinAge(20);
        dto.setMaxAge(30);
        dto.setMaxDistance(50);

        SearchPreference result = searchPreferenceMapper.toEntity(dto, testUser);

        assertThat(result).isNotNull();
        assertThat(result.getPreferredGender()).isNull();
    }

    @Test
    void testToEntity_WithNullMaxDistance() {
        SearchPreferenceDto dto = new SearchPreferenceDto();
        dto.setPreferredGender(User.Gender.FEMALE);
        dto.setMinAge(20);
        dto.setMaxAge(30);
        dto.setMaxDistance(null);

        SearchPreference result = searchPreferenceMapper.toEntity(dto, testUser);

        assertThat(result).isNotNull();
        assertThat(result.getMaxDistance()).isNull();
    }

    @Test
    void testToEntity_AssociatesWithUser() {
        SearchPreferenceDto dto = new SearchPreferenceDto();
        dto.setMinAge(20);
        dto.setMaxAge(30);

        SearchPreference result = searchPreferenceMapper.toEntity(dto, testUser);

        assertThat(result.getUser()).isNotNull();
        assertThat(result.getUser().getId()).isEqualTo(1L);
    }


    @Test
    void testUpdateEntityFromDto_UpdatePreferredGender() {
        SearchPreferenceDto dto = new SearchPreferenceDto();
        dto.setPreferredGender(User.Gender.MALE);

        searchPreferenceMapper.updateEntityFromDto(dto, testPreference);

        assertThat(testPreference.getPreferredGender()).isEqualTo(User.Gender.MALE);
        assertThat(testPreference.getMinAge()).isEqualTo(20);
        assertThat(testPreference.getMaxAge()).isEqualTo(30);
        assertThat(testPreference.getMaxDistance()).isEqualTo(50);
    }

    @Test
    void testUpdateEntityFromDto_UpdateMinAge() {
        SearchPreferenceDto dto = new SearchPreferenceDto();
        dto.setMinAge(25);

        searchPreferenceMapper.updateEntityFromDto(dto, testPreference);

        assertThat(testPreference.getMinAge()).isEqualTo(25);
        assertThat(testPreference.getMaxAge()).isEqualTo(30);
    }

    @Test
    void testUpdateEntityFromDto_UpdateMaxAge() {
        SearchPreferenceDto dto = new SearchPreferenceDto();
        dto.setMaxAge(35);

        searchPreferenceMapper.updateEntityFromDto(dto, testPreference);

        assertThat(testPreference.getMaxAge()).isEqualTo(35);
        assertThat(testPreference.getMinAge()).isEqualTo(20);
    }

    @Test
    void testUpdateEntityFromDto_UpdateMaxDistance() {
        SearchPreferenceDto dto = new SearchPreferenceDto();
        dto.setMaxDistance(100);

        searchPreferenceMapper.updateEntityFromDto(dto, testPreference);

        assertThat(testPreference.getMaxDistance()).isEqualTo(100);
        assertThat(testPreference.getMinAge()).isEqualTo(20);
    }

    @Test
    void testUpdateEntityFromDto_UpdateAllFields() {
        SearchPreferenceDto dto = new SearchPreferenceDto();
        dto.setPreferredGender(User.Gender.OTHER);
        dto.setMinAge(18);
        dto.setMaxAge(40);
        dto.setMaxDistance(75);

        searchPreferenceMapper.updateEntityFromDto(dto, testPreference);

        assertThat(testPreference.getPreferredGender()).isEqualTo(User.Gender.OTHER);
        assertThat(testPreference.getMinAge()).isEqualTo(18);
        assertThat(testPreference.getMaxAge()).isEqualTo(40);
        assertThat(testPreference.getMaxDistance()).isEqualTo(75);
    }

    @Test
    void testUpdateEntityFromDto_WithNullDto() {
        User.Gender originalGender = testPreference.getPreferredGender();
        Integer originalMinAge = testPreference.getMinAge();

        searchPreferenceMapper.updateEntityFromDto(null, testPreference);

        assertThat(testPreference.getPreferredGender()).isEqualTo(originalGender);
        assertThat(testPreference.getMinAge()).isEqualTo(originalMinAge);
    }

    @Test
    void testUpdateEntityFromDto_WithNullPreference() {
        SearchPreferenceDto dto = new SearchPreferenceDto();
        dto.setMinAge(25);

        assertThatCode(() -> searchPreferenceMapper.updateEntityFromDto(dto, null))
                .doesNotThrowAnyException();
    }

    @Test
    void testUpdateEntityFromDto_WithNullFields() {
        SearchPreferenceDto dto = new SearchPreferenceDto();
        dto.setPreferredGender(null);
        dto.setMinAge(null);
        dto.setMaxAge(null);
        dto.setMaxDistance(null);

        User.Gender originalGender = testPreference.getPreferredGender();
        Integer originalMinAge = testPreference.getMinAge();
        Integer originalMaxAge = testPreference.getMaxAge();
        Integer originalMaxDistance = testPreference.getMaxDistance();

        searchPreferenceMapper.updateEntityFromDto(dto, testPreference);

        assertThat(testPreference.getPreferredGender()).isEqualTo(originalGender);
        assertThat(testPreference.getMinAge()).isEqualTo(originalMinAge);
        assertThat(testPreference.getMaxAge()).isEqualTo(originalMaxAge);
        assertThat(testPreference.getMaxDistance()).isEqualTo(originalMaxDistance);
    }

    @Test
    void testUpdateEntityFromDto_PartialUpdate() {
        SearchPreferenceDto dto = new SearchPreferenceDto();
        dto.setMinAge(22);
        dto.setMaxAge(32);

        searchPreferenceMapper.updateEntityFromDto(dto, testPreference);

        assertThat(testPreference.getMinAge()).isEqualTo(22);
        assertThat(testPreference.getMaxAge()).isEqualTo(32);
        assertThat(testPreference.getPreferredGender()).isEqualTo(User.Gender.FEMALE);
        assertThat(testPreference.getMaxDistance()).isEqualTo(50);
    }

    @Test
    void testUpdateEntityFromDto_CanSetPreferredGenderToNull() {
        SearchPreferenceDto dto = new SearchPreferenceDto();

        searchPreferenceMapper.updateEntityFromDto(dto, testPreference);

        assertThat(testPreference.getPreferredGender()).isEqualTo(User.Gender.FEMALE);
    }


    @Test
    void testRoundTrip_AllFields() {
        SearchPreferenceDto dto = new SearchPreferenceDto();
        dto.setPreferredGender(User.Gender.FEMALE);
        dto.setMinAge(25);
        dto.setMaxAge(35);
        dto.setMaxDistance(75);

        SearchPreference entity = searchPreferenceMapper.toEntity(dto, testUser);
        SearchPreferenceDto result = searchPreferenceMapper.toDto(entity);

        assertThat(result).isNotNull();
        assertThat(result.getPreferredGender()).isEqualTo(User.Gender.FEMALE);
        assertThat(result.getMinAge()).isEqualTo(25);
        assertThat(result.getMaxAge()).isEqualTo(35);
        assertThat(result.getMaxDistance()).isEqualTo(75);
    }

    @Test
    void testRoundTrip_WithNullGender() {
        SearchPreferenceDto dto = new SearchPreferenceDto();
        dto.setPreferredGender(null);
        dto.setMinAge(20);
        dto.setMaxAge(30);
        dto.setMaxDistance(50);

        SearchPreference entity = searchPreferenceMapper.toEntity(dto, testUser);
        SearchPreferenceDto result = searchPreferenceMapper.toDto(entity);

        assertThat(result.getPreferredGender()).isNull();
        assertThat(result.getMinAge()).isEqualTo(20);
    }


    @Test
    void testUpdateEntityFromDto_MultipleUpdates() {
        SearchPreferenceDto dto1 = new SearchPreferenceDto();
        dto1.setMinAge(22);
        searchPreferenceMapper.updateEntityFromDto(dto1, testPreference);
        assertThat(testPreference.getMinAge()).isEqualTo(22);

        SearchPreferenceDto dto2 = new SearchPreferenceDto();
        dto2.setMaxAge(35);
        searchPreferenceMapper.updateEntityFromDto(dto2, testPreference);
        assertThat(testPreference.getMinAge()).isEqualTo(22);
        assertThat(testPreference.getMaxAge()).isEqualTo(35);
    }

    @Test
    void testToEntity_DifferentUsers() {
        User user2 = new User();
        user2.setId(2L);

        SearchPreferenceDto dto = new SearchPreferenceDto();
        dto.setMinAge(20);
        dto.setMaxAge(30);

        SearchPreference result1 = searchPreferenceMapper.toEntity(dto, testUser);
        SearchPreference result2 = searchPreferenceMapper.toEntity(dto, user2);

        assertThat(result1.getUser().getId()).isEqualTo(1L);
        assertThat(result2.getUser().getId()).isEqualTo(2L);
    }

    @Test
    void testToDto_AllGenderOptions() {
        testPreference.setPreferredGender(User.Gender.MALE);
        assertThat(searchPreferenceMapper.toDto(testPreference).getPreferredGender()).isEqualTo(User.Gender.MALE);

        testPreference.setPreferredGender(User.Gender.FEMALE);
        assertThat(searchPreferenceMapper.toDto(testPreference).getPreferredGender()).isEqualTo(User.Gender.FEMALE);

        testPreference.setPreferredGender(User.Gender.OTHER);
        assertThat(searchPreferenceMapper.toDto(testPreference).getPreferredGender()).isEqualTo(User.Gender.OTHER);

        testPreference.setPreferredGender(null);
        assertThat(searchPreferenceMapper.toDto(testPreference).getPreferredGender()).isNull();
    }

    @Test
    void testUpdateEntityFromDto_BoundaryValues() {
        SearchPreferenceDto dto = new SearchPreferenceDto();
        dto.setMinAge(18);
        dto.setMaxAge(100);
        dto.setMaxDistance(1);

        searchPreferenceMapper.updateEntityFromDto(dto, testPreference);

        assertThat(testPreference.getMinAge()).isEqualTo(18);
        assertThat(testPreference.getMaxAge()).isEqualTo(100);
        assertThat(testPreference.getMaxDistance()).isEqualTo(1);
    }
}

