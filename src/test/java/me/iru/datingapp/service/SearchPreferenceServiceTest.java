package me.iru.datingapp.service;

import me.iru.datingapp.dto.SearchPreferenceDto;
import me.iru.datingapp.entity.SearchPreference;
import me.iru.datingapp.entity.User;
import me.iru.datingapp.exception.ResourceNotFoundException;
import me.iru.datingapp.mapper.SearchPreferenceMapper;
import me.iru.datingapp.repository.SearchPreferenceRepository;
import me.iru.datingapp.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SearchPreferenceServiceTest {

    @Mock
    private SearchPreferenceRepository searchPreferenceRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SearchPreferenceMapper searchPreferenceMapper;

    @InjectMocks
    private SearchPreferenceService searchPreferenceService;

    private User testUser;
    private SearchPreference testPreference;
    private SearchPreferenceDto testPreferenceDto;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setGender(User.Gender.MALE);
        testUser.setAge(25);
        testUser.setCity("Warsaw");

        testPreference = new SearchPreference();
        testPreference.setId(1L);
        testPreference.setUser(testUser);
        testPreference.setPreferredGender(User.Gender.FEMALE);
        testPreference.setMinAge(20);
        testPreference.setMaxAge(30);
        testPreference.setMaxDistance(50);

        testPreferenceDto = new SearchPreferenceDto();
        testPreferenceDto.setId(1L);
        testPreferenceDto.setPreferredGender(User.Gender.FEMALE);
        testPreferenceDto.setMinAge(20);
        testPreferenceDto.setMaxAge(30);
        testPreferenceDto.setMaxDistance(50);
    }


    @Test
    void testGetPreferences_Success_ExistingPreferences() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(searchPreferenceRepository.findByUserId(1L)).thenReturn(Optional.of(testPreference));
        when(searchPreferenceMapper.toDto(testPreference)).thenReturn(testPreferenceDto);

        SearchPreferenceDto result = searchPreferenceService.getPreferences(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getPreferredGender()).isEqualTo(User.Gender.FEMALE);
        assertThat(result.getMinAge()).isEqualTo(20);
        assertThat(result.getMaxAge()).isEqualTo(30);
        assertThat(result.getMaxDistance()).isEqualTo(50);

        verify(userRepository).findById(1L);
        verify(searchPreferenceRepository).findByUserId(1L);
        verify(searchPreferenceMapper).toDto(testPreference);
    }

    @Test
    void testGetPreferences_Success_CreatesDefaultPreferences() {
        SearchPreference defaultPreference = SearchPreference.defaultForUser(testUser);
        SearchPreferenceDto defaultDto = new SearchPreferenceDto();
        defaultDto.setMinAge(20);
        defaultDto.setMaxAge(30);
        defaultDto.setMaxDistance(50);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(searchPreferenceRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(searchPreferenceRepository.save(any(SearchPreference.class))).thenReturn(defaultPreference);
        when(searchPreferenceMapper.toDto(any(SearchPreference.class))).thenReturn(defaultDto);

        SearchPreferenceDto result = searchPreferenceService.getPreferences(1L);

        assertThat(result).isNotNull();
        assertThat(result.getMinAge()).isEqualTo(20);
        assertThat(result.getMaxAge()).isEqualTo(30);

        verify(userRepository).findById(1L);
        verify(searchPreferenceRepository).findByUserId(1L);
        verify(searchPreferenceRepository).save(any(SearchPreference.class));
        verify(searchPreferenceMapper).toDto(any(SearchPreference.class));
    }

    @Test
    void testGetPreferences_UserNotFound_ThrowsException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> searchPreferenceService.getPreferences(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found with ID: 999");

        verify(userRepository).findById(999L);
        verify(searchPreferenceRepository, never()).findByUserId(anyLong());
    }


    @Test
    void testUpdatePreferences_Success_ExistingPreferences() {
        SearchPreferenceDto updateDto = new SearchPreferenceDto();
        updateDto.setPreferredGender(User.Gender.FEMALE);
        updateDto.setMinAge(22);
        updateDto.setMaxAge(32);
        updateDto.setMaxDistance(60);

        SearchPreference updatedPreference = new SearchPreference();
        updatedPreference.setId(1L);
        updatedPreference.setUser(testUser);
        updatedPreference.setPreferredGender(User.Gender.FEMALE);
        updatedPreference.setMinAge(22);
        updatedPreference.setMaxAge(32);
        updatedPreference.setMaxDistance(60);

        SearchPreferenceDto updatedDto = new SearchPreferenceDto();
        updatedDto.setId(1L);
        updatedDto.setPreferredGender(User.Gender.FEMALE);
        updatedDto.setMinAge(22);
        updatedDto.setMaxAge(32);
        updatedDto.setMaxDistance(60);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(searchPreferenceRepository.findByUserId(1L)).thenReturn(Optional.of(testPreference));
        when(searchPreferenceRepository.save(any(SearchPreference.class))).thenReturn(updatedPreference);
        when(searchPreferenceMapper.toDto(updatedPreference)).thenReturn(updatedDto);

        SearchPreferenceDto result = searchPreferenceService.updatePreferences(1L, updateDto);

        assertThat(result).isNotNull();
        assertThat(result.getMinAge()).isEqualTo(22);
        assertThat(result.getMaxAge()).isEqualTo(32);
        assertThat(result.getMaxDistance()).isEqualTo(60);

        verify(userRepository).findById(1L);
        verify(searchPreferenceRepository).findByUserId(1L);
        verify(searchPreferenceMapper).updateEntityFromDto(updateDto, testPreference);
        verify(searchPreferenceRepository).save(any(SearchPreference.class));
        verify(searchPreferenceMapper).toDto(updatedPreference);
    }

    @Test
    void testUpdatePreferences_Success_CreateNew() {
        SearchPreferenceDto newDto = new SearchPreferenceDto();
        newDto.setPreferredGender(User.Gender.FEMALE);
        newDto.setMinAge(25);
        newDto.setMaxAge(35);
        newDto.setMaxDistance(100);

        SearchPreference newPreference = new SearchPreference();
        newPreference.setUser(testUser);
        newPreference.setPreferredGender(User.Gender.FEMALE);
        newPreference.setMinAge(25);
        newPreference.setMaxAge(35);
        newPreference.setMaxDistance(100);

        SearchPreference savedPreference = new SearchPreference();
        savedPreference.setId(2L);
        savedPreference.setUser(testUser);
        savedPreference.setPreferredGender(User.Gender.FEMALE);
        savedPreference.setMinAge(25);
        savedPreference.setMaxAge(35);
        savedPreference.setMaxDistance(100);

        SearchPreferenceDto savedDto = new SearchPreferenceDto();
        savedDto.setId(2L);
        savedDto.setPreferredGender(User.Gender.FEMALE);
        savedDto.setMinAge(25);
        savedDto.setMaxAge(35);
        savedDto.setMaxDistance(100);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(searchPreferenceRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(searchPreferenceRepository.save(any(SearchPreference.class))).thenReturn(savedPreference);
        when(searchPreferenceMapper.toDto(savedPreference)).thenReturn(savedDto);

        SearchPreferenceDto result = searchPreferenceService.updatePreferences(1L, newDto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(2L);
        assertThat(result.getMinAge()).isEqualTo(25);
        assertThat(result.getMaxAge()).isEqualTo(35);

        verify(userRepository).findById(1L);
        verify(searchPreferenceRepository).findByUserId(1L);
        verify(searchPreferenceMapper).updateEntityFromDto(eq(newDto), any(SearchPreference.class));
        verify(searchPreferenceRepository).save(any(SearchPreference.class));
    }

    @Test
    void testUpdatePreferences_UserNotFound_ThrowsException() {
        SearchPreferenceDto updateDto = new SearchPreferenceDto();
        updateDto.setMinAge(25);
        updateDto.setMaxAge(35);

        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> searchPreferenceService.updatePreferences(999L, updateDto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found with ID: 999");

        verify(userRepository).findById(999L);
        verify(searchPreferenceRepository, never()).save(any());
    }

    @Test
    void testUpdatePreferences_InvalidAgeRange_MinGreaterThanMax_ThrowsException() {
        SearchPreferenceDto invalidDto = new SearchPreferenceDto();
        invalidDto.setPreferredGender(User.Gender.FEMALE);
        invalidDto.setMinAge(40);
        invalidDto.setMaxAge(30);

        SearchPreference preference = new SearchPreference();
        preference.setUser(testUser);
        preference.setMinAge(40);
        preference.setMaxAge(30);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(searchPreferenceRepository.findByUserId(1L)).thenReturn(Optional.of(testPreference));
        doAnswer(invocation -> {
            SearchPreference pref = invocation.getArgument(1);
            pref.setMinAge(40);
            pref.setMaxAge(30);
            return null;
        }).when(searchPreferenceMapper).updateEntityFromDto(eq(invalidDto), any(SearchPreference.class));

        assertThatThrownBy(() -> searchPreferenceService.updatePreferences(1L, invalidDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Min age cannot be greater than max age");

        verify(userRepository).findById(1L);
        verify(searchPreferenceRepository, never()).save(any());
    }

    @Test
    void testUpdatePreferences_MinAgeBelow18_ThrowsException() {
        SearchPreferenceDto invalidDto = new SearchPreferenceDto();
        invalidDto.setPreferredGender(User.Gender.FEMALE);
        invalidDto.setMinAge(16);
        invalidDto.setMaxAge(30);

        SearchPreference preference = new SearchPreference();
        preference.setUser(testUser);
        preference.setMinAge(16);
        preference.setMaxAge(30);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(searchPreferenceRepository.findByUserId(1L)).thenReturn(Optional.of(testPreference));
        doAnswer(invocation -> {
            SearchPreference pref = invocation.getArgument(1);
            pref.setMinAge(16);
            pref.setMaxAge(30);
            return null;
        }).when(searchPreferenceMapper).updateEntityFromDto(eq(invalidDto), any(SearchPreference.class));

        assertThatThrownBy(() -> searchPreferenceService.updatePreferences(1L, invalidDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Min age must be at least 18");

        verify(userRepository).findById(1L);
        verify(searchPreferenceRepository, never()).save(any());
    }

    @Test
    void testUpdatePreferences_NullAgeValues_DoesNotValidate() {
        SearchPreferenceDto updateDto = new SearchPreferenceDto();
        updateDto.setPreferredGender(User.Gender.FEMALE);
        updateDto.setMaxDistance(50);

        SearchPreference preference = new SearchPreference();
        preference.setUser(testUser);
        preference.setPreferredGender(User.Gender.FEMALE);
        preference.setMaxDistance(50);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(searchPreferenceRepository.findByUserId(1L)).thenReturn(Optional.of(testPreference));
        when(searchPreferenceRepository.save(any(SearchPreference.class))).thenReturn(preference);
        when(searchPreferenceMapper.toDto(any(SearchPreference.class))).thenReturn(testPreferenceDto);
        doAnswer(invocation -> {
            SearchPreference pref = invocation.getArgument(1);
            pref.setPreferredGender(User.Gender.FEMALE);
            pref.setMaxDistance(50);
            return null;
        }).when(searchPreferenceMapper).updateEntityFromDto(eq(updateDto), any(SearchPreference.class));

        SearchPreferenceDto result = searchPreferenceService.updatePreferences(1L, updateDto);

        assertThat(result).isNotNull();
        verify(searchPreferenceRepository).save(any(SearchPreference.class));
    }

    @Test
    void testUpdatePreferences_ValidAgeRange_Success() {
        SearchPreferenceDto validDto = new SearchPreferenceDto();
        validDto.setMinAge(18);
        validDto.setMaxAge(100);

        SearchPreference preference = new SearchPreference();
        preference.setUser(testUser);
        preference.setMinAge(18);
        preference.setMaxAge(100);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(searchPreferenceRepository.findByUserId(1L)).thenReturn(Optional.of(testPreference));
        when(searchPreferenceRepository.save(any(SearchPreference.class))).thenReturn(preference);
        when(searchPreferenceMapper.toDto(any(SearchPreference.class))).thenReturn(testPreferenceDto);
        doAnswer(invocation -> {
            SearchPreference pref = invocation.getArgument(1);
            pref.setMinAge(18);
            pref.setMaxAge(100);
            return null;
        }).when(searchPreferenceMapper).updateEntityFromDto(eq(validDto), any(SearchPreference.class));

        SearchPreferenceDto result = searchPreferenceService.updatePreferences(1L, validDto);

        assertThat(result).isNotNull();
        verify(searchPreferenceRepository).save(any(SearchPreference.class));
    }


    @Test
    void testDeletePreferences_Success() {
        when(userRepository.existsById(1L)).thenReturn(true);

        searchPreferenceService.deletePreferences(1L);

        verify(userRepository).existsById(1L);
        verify(searchPreferenceRepository).deleteByUserId(1L);
    }

    @Test
    void testDeletePreferences_UserNotFound_ThrowsException() {
        when(userRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> searchPreferenceService.deletePreferences(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found with ID: 999");

        verify(userRepository).existsById(999L);
        verify(searchPreferenceRepository, never()).deleteByUserId(anyLong());
    }


    @Test
    void testHasPreferences_ReturnsTrue() {
        when(searchPreferenceRepository.existsByUserId(1L)).thenReturn(true);

        boolean result = searchPreferenceService.hasPreferences(1L);

        assertThat(result).isTrue();
        verify(searchPreferenceRepository).existsByUserId(1L);
    }

    @Test
    void testHasPreferences_ReturnsFalse() {
        when(searchPreferenceRepository.existsByUserId(1L)).thenReturn(false);

        boolean result = searchPreferenceService.hasPreferences(1L);

        assertThat(result).isFalse();
        verify(searchPreferenceRepository).existsByUserId(1L);
    }


    @Test
    void testResetToDefault_Success() {
        SearchPreference defaultPreference = SearchPreference.defaultForUser(testUser);
        defaultPreference.setId(2L);

        SearchPreferenceDto defaultDto = new SearchPreferenceDto();
        defaultDto.setId(2L);
        defaultDto.setMinAge(20);
        defaultDto.setMaxAge(30);
        defaultDto.setMaxDistance(50);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(searchPreferenceRepository.save(any(SearchPreference.class))).thenReturn(defaultPreference);
        when(searchPreferenceMapper.toDto(defaultPreference)).thenReturn(defaultDto);

        SearchPreferenceDto result = searchPreferenceService.resetToDefault(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(2L);
        assertThat(result.getMinAge()).isEqualTo(20);
        assertThat(result.getMaxAge()).isEqualTo(30);

        verify(userRepository).findById(1L);
        verify(searchPreferenceRepository).deleteByUserId(1L);
        verify(searchPreferenceRepository).save(any(SearchPreference.class));
        verify(searchPreferenceMapper).toDto(defaultPreference);
    }

    @Test
    void testResetToDefault_UserNotFound_ThrowsException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> searchPreferenceService.resetToDefault(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found with ID: 999");

        verify(userRepository).findById(999L);
        verify(searchPreferenceRepository, never()).deleteByUserId(anyLong());
        verify(searchPreferenceRepository, never()).save(any());
    }

    @Test
    void testResetToDefault_DeletesExistingPreferences() {
        SearchPreference defaultPreference = SearchPreference.defaultForUser(testUser);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(searchPreferenceRepository.save(any(SearchPreference.class))).thenReturn(defaultPreference);
        when(searchPreferenceMapper.toDto(any(SearchPreference.class))).thenReturn(testPreferenceDto);

        searchPreferenceService.resetToDefault(1L);

        verify(searchPreferenceRepository).deleteByUserId(1L);
        verify(searchPreferenceRepository).save(any(SearchPreference.class));
    }


    @Test
    void testUpdatePreferences_WithAllGenderOptions() {
        SearchPreferenceDto maleDto = new SearchPreferenceDto();
        maleDto.setPreferredGender(User.Gender.MALE);
        maleDto.setMinAge(25);
        maleDto.setMaxAge(35);

        SearchPreference malePreference = new SearchPreference();
        malePreference.setUser(testUser);
        malePreference.setPreferredGender(User.Gender.MALE);
        malePreference.setMinAge(25);
        malePreference.setMaxAge(35);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(searchPreferenceRepository.findByUserId(1L)).thenReturn(Optional.of(testPreference));
        when(searchPreferenceRepository.save(any(SearchPreference.class))).thenReturn(malePreference);
        when(searchPreferenceMapper.toDto(any(SearchPreference.class))).thenReturn(testPreferenceDto);
        doAnswer(invocation -> {
            SearchPreference pref = invocation.getArgument(1);
            pref.setPreferredGender(User.Gender.MALE);
            pref.setMinAge(25);
            pref.setMaxAge(35);
            return null;
        }).when(searchPreferenceMapper).updateEntityFromDto(eq(maleDto), any(SearchPreference.class));

        SearchPreferenceDto result = searchPreferenceService.updatePreferences(1L, maleDto);

        assertThat(result).isNotNull();
        verify(searchPreferenceRepository).save(any(SearchPreference.class));
    }

    @Test
    void testUpdatePreferences_WithNullPreferredGender() {
        SearchPreferenceDto anyGenderDto = new SearchPreferenceDto();
        anyGenderDto.setPreferredGender(null);
        anyGenderDto.setMinAge(20);
        anyGenderDto.setMaxAge(30);

        SearchPreference preference = new SearchPreference();
        preference.setUser(testUser);
        preference.setPreferredGender(null);
        preference.setMinAge(20);
        preference.setMaxAge(30);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(searchPreferenceRepository.findByUserId(1L)).thenReturn(Optional.of(testPreference));
        when(searchPreferenceRepository.save(any(SearchPreference.class))).thenReturn(preference);
        when(searchPreferenceMapper.toDto(any(SearchPreference.class))).thenReturn(testPreferenceDto);
        doAnswer(invocation -> {
            SearchPreference pref = invocation.getArgument(1);
            pref.setPreferredGender(null);
            pref.setMinAge(20);
            pref.setMaxAge(30);
            return null;
        }).when(searchPreferenceMapper).updateEntityFromDto(eq(anyGenderDto), any(SearchPreference.class));

        SearchPreferenceDto result = searchPreferenceService.updatePreferences(1L, anyGenderDto);

        assertThat(result).isNotNull();
        verify(searchPreferenceRepository).save(any(SearchPreference.class));
    }

    @Test
    void testUpdatePreferences_MinAgeEqualsMaxAge_Success() {
        SearchPreferenceDto sameAgeDto = new SearchPreferenceDto();
        sameAgeDto.setMinAge(30);
        sameAgeDto.setMaxAge(30);

        SearchPreference preference = new SearchPreference();
        preference.setUser(testUser);
        preference.setMinAge(30);
        preference.setMaxAge(30);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(searchPreferenceRepository.findByUserId(1L)).thenReturn(Optional.of(testPreference));
        when(searchPreferenceRepository.save(any(SearchPreference.class))).thenReturn(preference);
        when(searchPreferenceMapper.toDto(any(SearchPreference.class))).thenReturn(testPreferenceDto);
        doAnswer(invocation -> {
            SearchPreference pref = invocation.getArgument(1);
            pref.setMinAge(30);
            pref.setMaxAge(30);
            return null;
        }).when(searchPreferenceMapper).updateEntityFromDto(eq(sameAgeDto), any(SearchPreference.class));

        SearchPreferenceDto result = searchPreferenceService.updatePreferences(1L, sameAgeDto);

        assertThat(result).isNotNull();
        verify(searchPreferenceRepository).save(any(SearchPreference.class));
    }

    @Test
    void testUpdatePreferences_MinAgeExactly18_Success() {
        SearchPreferenceDto boundaryDto = new SearchPreferenceDto();
        boundaryDto.setMinAge(18);
        boundaryDto.setMaxAge(25);

        SearchPreference preference = new SearchPreference();
        preference.setUser(testUser);
        preference.setMinAge(18);
        preference.setMaxAge(25);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(searchPreferenceRepository.findByUserId(1L)).thenReturn(Optional.of(testPreference));
        when(searchPreferenceRepository.save(any(SearchPreference.class))).thenReturn(preference);
        when(searchPreferenceMapper.toDto(any(SearchPreference.class))).thenReturn(testPreferenceDto);
        doAnswer(invocation -> {
            SearchPreference pref = invocation.getArgument(1);
            pref.setMinAge(18);
            pref.setMaxAge(25);
            return null;
        }).when(searchPreferenceMapper).updateEntityFromDto(eq(boundaryDto), any(SearchPreference.class));

        SearchPreferenceDto result = searchPreferenceService.updatePreferences(1L, boundaryDto);

        assertThat(result).isNotNull();
        verify(searchPreferenceRepository).save(any(SearchPreference.class));
    }

    @Test
    void testGetPreferences_VerifiesDefaultCreationForNewUser() {
        User youngUser = new User();
        youngUser.setId(2L);
        youngUser.setAge(20);

        SearchPreference defaultPreference = SearchPreference.defaultForUser(youngUser);

        when(userRepository.findById(2L)).thenReturn(Optional.of(youngUser));
        when(searchPreferenceRepository.findByUserId(2L)).thenReturn(Optional.empty());
        when(searchPreferenceRepository.save(any(SearchPreference.class))).thenReturn(defaultPreference);
        when(searchPreferenceMapper.toDto(any(SearchPreference.class))).thenReturn(testPreferenceDto);

        SearchPreferenceDto result = searchPreferenceService.getPreferences(2L);

        assertThat(result).isNotNull();
        verify(searchPreferenceRepository).save(argThat(pref ->
                pref.getUser().getId().equals(2L)
        ));
    }
}

