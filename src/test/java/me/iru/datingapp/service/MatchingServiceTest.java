package me.iru.datingapp.service;

import me.iru.datingapp.dto.UserProfileDto;
import me.iru.datingapp.entity.Rating;
import me.iru.datingapp.entity.SearchPreference;
import me.iru.datingapp.entity.User;
import me.iru.datingapp.exception.ResourceNotFoundException;
import me.iru.datingapp.mapper.UserMapper;
import me.iru.datingapp.repository.RatingRepository;
import me.iru.datingapp.repository.SearchPreferenceRepository;
import me.iru.datingapp.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MatchingServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private SearchPreferenceRepository searchPreferenceRepository;

    @Mock
    private RatingRepository ratingRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private MatchingService matchingService;

    private User currentUser;
    private User suggestedUser1;
    private User suggestedUser2;
    private SearchPreference searchPreference;
    private UserProfileDto profileDto1;
    private UserProfileDto profileDto2;

    @BeforeEach
    void setUp() {
        currentUser = new User();
        currentUser.setId(1L);
        currentUser.setEmail("current@example.com");
        currentUser.setGender(User.Gender.MALE);
        currentUser.setAge(25);

        suggestedUser1 = new User();
        suggestedUser1.setId(2L);
        suggestedUser1.setEmail("user2@example.com");
        suggestedUser1.setGender(User.Gender.FEMALE);
        suggestedUser1.setAge(23);

        suggestedUser2 = new User();
        suggestedUser2.setId(3L);
        suggestedUser2.setEmail("user3@example.com");
        suggestedUser2.setGender(User.Gender.FEMALE);
        suggestedUser2.setAge(26);

        searchPreference = new SearchPreference();
        searchPreference.setUser(currentUser);
        searchPreference.setPreferredGender(User.Gender.FEMALE);
        searchPreference.setMinAge(20);
        searchPreference.setMaxAge(30);

        profileDto1 = new UserProfileDto();
        profileDto1.setId(2L);
        profileDto1.setEmail("user2@example.com");

        profileDto2 = new UserProfileDto();
        profileDto2.setId(3L);
        profileDto2.setEmail("user3@example.com");
    }

    @Test
    void testGetSuggestedUsers_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        List<User> users = List.of(suggestedUser1, suggestedUser2);
        Page<User> userPage = new PageImpl<>(users);

        when(userRepository.findById(1L)).thenReturn(Optional.of(currentUser));
        when(searchPreferenceRepository.findByUserId(1L)).thenReturn(Optional.of(searchPreference));
        when(ratingRepository.findByRaterId(1L)).thenReturn(new ArrayList<>());
        when(userRepository.findBySearchCriteria(
                eq(User.Gender.FEMALE), eq(20), eq(30), any(), eq(pageable)))
                .thenReturn(userPage);
        when(userMapper.toDto(suggestedUser1)).thenReturn(profileDto1);
        when(userMapper.toDto(suggestedUser2)).thenReturn(profileDto2);

        Page<UserProfileDto> result = matchingService.getSuggestedUsers(1L, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        verify(userRepository).findById(1L);
        verify(searchPreferenceRepository).findByUserId(1L);
        verify(ratingRepository).findByRaterId(1L);
    }

    @Test
    void testGetSuggestedUsers_UserNotFound() {
        Pageable pageable = PageRequest.of(0, 10);
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> matchingService.getSuggestedUsers(999L, pageable))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");

        verify(userRepository).findById(999L);
        verify(searchPreferenceRepository, never()).findByUserId(anyLong());
    }

    @Test
    void testGetSuggestedUsers_ExcludesCurrentUser() {
        Pageable pageable = PageRequest.of(0, 10);
        List<User> users = List.of(currentUser, suggestedUser1);
        Page<User> userPage = new PageImpl<>(users);

        when(userRepository.findById(1L)).thenReturn(Optional.of(currentUser));
        when(searchPreferenceRepository.findByUserId(1L)).thenReturn(Optional.of(searchPreference));
        when(ratingRepository.findByRaterId(1L)).thenReturn(new ArrayList<>());
        when(userRepository.findBySearchCriteria(any(), anyInt(), anyInt(), any(), eq(pageable)))
                .thenReturn(userPage);
        when(userMapper.toDto(suggestedUser1)).thenReturn(profileDto1);

        Page<UserProfileDto> result = matchingService.getSuggestedUsers(1L, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getId()).isEqualTo(2L);
    }

    @Test
    void testGetSuggestedUsers_ExcludesRatedUsers() {
        Pageable pageable = PageRequest.of(0, 10);
        List<User> users = List.of(suggestedUser1, suggestedUser2);
        Page<User> userPage = new PageImpl<>(users);

        Rating rating = new Rating();
        rating.setRatedUser(suggestedUser1);
        List<Rating> ratings = List.of(rating);

        when(userRepository.findById(1L)).thenReturn(Optional.of(currentUser));
        when(searchPreferenceRepository.findByUserId(1L)).thenReturn(Optional.of(searchPreference));
        when(ratingRepository.findByRaterId(1L)).thenReturn(ratings);
        when(userRepository.findBySearchCriteria(any(), anyInt(), anyInt(), any(), eq(pageable)))
                .thenReturn(userPage);
        when(userMapper.toDto(suggestedUser2)).thenReturn(profileDto2);

        Page<UserProfileDto> result = matchingService.getSuggestedUsers(1L, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getId()).isEqualTo(3L);
    }

    @Test
    void testGetSuggestedUsers_UsesDefaultPreferences() {
        Pageable pageable = PageRequest.of(0, 10);
        List<User> users = List.of(suggestedUser1);
        Page<User> userPage = new PageImpl<>(users);

        when(userRepository.findById(1L)).thenReturn(Optional.of(currentUser));
        when(searchPreferenceRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(ratingRepository.findByRaterId(1L)).thenReturn(new ArrayList<>());
        when(userRepository.findBySearchCriteria(any(), anyInt(), anyInt(), any(), eq(pageable)))
                .thenReturn(userPage);
        when(userMapper.toDto(suggestedUser1)).thenReturn(profileDto1);

        Page<UserProfileDto> result = matchingService.getSuggestedUsers(1L, pageable);

        assertThat(result).isNotNull();
        verify(userRepository).findBySearchCriteria(any(), anyInt(), anyInt(), any(), eq(pageable));
    }

    @Test
    void testGetAvailableSuggestionsCount_UserNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> matchingService.getAvailableSuggestionsCount(999L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(userRepository).findById(999L);
    }

    @Test
    void testGetSuggestedUsers_EmptyResults() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> emptyPage = new PageImpl<>(new ArrayList<>());

        when(userRepository.findById(1L)).thenReturn(Optional.of(currentUser));
        when(searchPreferenceRepository.findByUserId(1L)).thenReturn(Optional.of(searchPreference));
        when(ratingRepository.findByRaterId(1L)).thenReturn(new ArrayList<>());
        when(userRepository.findBySearchCriteria(any(), anyInt(), anyInt(), any(), eq(pageable)))
                .thenReturn(emptyPage);

        Page<UserProfileDto> result = matchingService.getSuggestedUsers(1L, pageable);

        assertThat(result.getContent()).isEmpty();
    }

    @Test
    void testGetSuggestedUsers_WithPagination() {
        Pageable pageable = PageRequest.of(1, 5);
        List<User> users = List.of(suggestedUser1, suggestedUser2);
        Page<User> userPage = new PageImpl<>(users, pageable, 10);

        when(userRepository.findById(1L)).thenReturn(Optional.of(currentUser));
        when(searchPreferenceRepository.findByUserId(1L)).thenReturn(Optional.of(searchPreference));
        when(ratingRepository.findByRaterId(1L)).thenReturn(new ArrayList<>());
        when(userRepository.findBySearchCriteria(any(), anyInt(), anyInt(), any(), eq(pageable)))
                .thenReturn(userPage);
        when(userMapper.toDto(suggestedUser1)).thenReturn(profileDto1);
        when(userMapper.toDto(suggestedUser2)).thenReturn(profileDto2);

        Page<UserProfileDto> result = matchingService.getSuggestedUsers(1L, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getPageable().getPageNumber()).isEqualTo(1);
        assertThat(result.getPageable().getPageSize()).isEqualTo(5);
    }
}

