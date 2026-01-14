package me.iru.datingapp.service;

import me.iru.datingapp.dto.MatchDto;
import me.iru.datingapp.entity.Match;
import me.iru.datingapp.entity.User;
import me.iru.datingapp.exception.ResourceNotFoundException;
import me.iru.datingapp.mapper.MatchMapper;
import me.iru.datingapp.repository.MatchRepository;
import me.iru.datingapp.repository.MessageRepository;
import me.iru.datingapp.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MatchServiceTest {

    @Mock
    private MatchRepository matchRepository;

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MatchMapper matchMapper;

    @InjectMocks
    private MatchService matchService;

    private User user1;
    private User user2;
    private Match match;
    private MatchDto matchDto;

    @BeforeEach
    void setUp() {
        user1 = new User();
        user1.setId(1L);
        user1.setEmail("user1@example.com");

        user2 = new User();
        user2.setId(2L);
        user2.setEmail("user2@example.com");

        match = new Match();
        match.setId(1L);
        match.setUser1(user1);
        match.setUser2(user2);
        match.setIsActive(true);

        matchDto = new MatchDto();
        matchDto.setId(1L);
        matchDto.setIsActive(true);
    }

    @Test
    void testGetActiveMatches_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        when(matchRepository.findActiveMatchesByUserId(1L)).thenReturn(List.of(match));
        when(matchMapper.toDto(any(Match.class), eq(user1))).thenReturn(matchDto);

        List<MatchDto> result = matchService.getActiveMatches(1L);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getId()).isEqualTo(1L);
        verify(matchRepository).findActiveMatchesByUserId(1L);
    }

    @Test
    void testGetActiveMatches_UserNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> matchService.getActiveMatches(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");

        verify(matchRepository, never()).findActiveMatchesByUserId(anyLong());
    }

    @Test
    void testGetActiveMatches_EmptyList() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        when(matchRepository.findActiveMatchesByUserId(1L)).thenReturn(List.of());

        List<MatchDto> result = matchService.getActiveMatches(1L);

        assertThat(result).isEmpty();
    }

    @Test
    void testGetAllMatches_Success() {
        Match inactiveMatch = new Match();
        inactiveMatch.setId(2L);
        inactiveMatch.setUser1(user1);
        inactiveMatch.setUser2(user2);
        inactiveMatch.setIsActive(false);

        MatchDto inactiveMatchDto = new MatchDto();
        inactiveMatchDto.setId(2L);
        inactiveMatchDto.setIsActive(false);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        when(matchRepository.findAllMatchesByUserId(1L)).thenReturn(List.of(match, inactiveMatch));
        when(matchMapper.toDto(eq(match), eq(user1))).thenReturn(matchDto);
        when(matchMapper.toDto(eq(inactiveMatch), eq(user1))).thenReturn(inactiveMatchDto);

        List<MatchDto> result = matchService.getAllMatches(1L);

        assertThat(result).hasSize(2);
        verify(matchRepository).findAllMatchesByUserId(1L);
    }

    @Test
    void testUnmatch_Success() {
        when(matchRepository.findById(1L)).thenReturn(Optional.of(match));
        when(matchRepository.save(any(Match.class))).thenReturn(match);

        matchService.unmatch(1L, 1L);

        verify(matchRepository).save(argThat(m -> !m.getIsActive()));
    }

    @Test
    void testUnmatch_MatchNotFound() {
        when(matchRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> matchService.unmatch(1L, 999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Match not found");
    }

    @Test
    void testUnmatch_UserNotInMatch() {
        User user3 = new User();
        user3.setId(3L);

        when(matchRepository.findById(1L)).thenReturn(Optional.of(match));

        assertThatThrownBy(() -> matchService.unmatch(3L, 1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("access denied");
    }

    @Test
    void testGetMatchById_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        when(matchRepository.findById(1L)).thenReturn(Optional.of(match));
        when(matchMapper.toDto(any(Match.class), eq(user1))).thenReturn(matchDto);

        MatchDto result = matchService.getMatchById(1L, 1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(matchRepository).findById(1L);
    }

    @Test
    void testGetMatchById_MatchNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        when(matchRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> matchService.getMatchById(999L, 1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Match not found");
    }

    @Test
    void testUnmatch_VerifiesUserInMatch() {
        when(matchRepository.findById(1L)).thenReturn(Optional.of(match));
        when(matchRepository.save(any(Match.class))).thenReturn(match);

        matchService.unmatch(1L, 1L);

        verify(matchRepository).findById(1L);
        verify(matchRepository).save(any(Match.class));
    }

    @Test
    void testDeleteMatch_Success() {
        when(matchRepository.findById(1L)).thenReturn(Optional.of(match));
        doNothing().when(matchRepository).delete(any(Match.class));

        matchService.deleteMatch(1L, 1L);

        verify(matchRepository).findById(1L);
        verify(matchRepository).delete(match);
    }
}

