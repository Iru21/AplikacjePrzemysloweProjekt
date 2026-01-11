package me.iru.datingapp.service;

import me.iru.datingapp.entity.Interest;
import me.iru.datingapp.entity.User;
import me.iru.datingapp.entity.UserInterest;
import me.iru.datingapp.exception.ResourceNotFoundException;
import me.iru.datingapp.repository.InterestRepository;
import me.iru.datingapp.repository.UserInterestRepository;
import me.iru.datingapp.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InterestServiceTest {

    @Mock
    private InterestRepository interestRepository;

    @Mock
    private UserInterestRepository userInterestRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private InterestService interestService;

    private Interest testInterest;
    private Interest testInterest2;
    private User testUser;
    private UserInterest testUserInterest;

    @BeforeEach
    void setUp() {
        testInterest = new Interest();
        testInterest.setId(1L);
        testInterest.setName("Sports");
        testInterest.setDescription("Sports and physical activities");

        testInterest2 = new Interest();
        testInterest2.setId(2L);
        testInterest2.setName("Music");
        testInterest2.setDescription("Music and concerts");

        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setFirstName("John");
        testUser.setLastName("Doe");

        testUserInterest = new UserInterest();
        testUserInterest.setId(1L);
        testUserInterest.setUser(testUser);
        testUserInterest.setInterest(testInterest);
    }


    @Test
    void testGetAllInterests_Success() {
        when(interestRepository.findAllByOrderByNameAsc()).thenReturn(List.of(testInterest, testInterest2));

        List<Interest> result = interestService.getAllInterests();

        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("Sports");
        assertThat(result.get(1).getName()).isEqualTo("Music");
        verify(interestRepository).findAllByOrderByNameAsc();
    }

    @Test
    void testGetAllInterests_EmptyList() {
        when(interestRepository.findAllByOrderByNameAsc()).thenReturn(List.of());

        List<Interest> result = interestService.getAllInterests();

        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
        verify(interestRepository).findAllByOrderByNameAsc();
    }


    @Test
    void testGetInterestById_Success() {
        when(interestRepository.findById(1L)).thenReturn(Optional.of(testInterest));

        Interest result = interestService.getInterestById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Sports");
        verify(interestRepository).findById(1L);
    }

    @Test
    void testGetInterestById_NotFound_ThrowsException() {
        when(interestRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> interestService.getInterestById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Interest not found with ID: 999");

        verify(interestRepository).findById(999L);
    }


    @Test
    void testGetInterestByName_Success() {
        when(interestRepository.findByName("Sports")).thenReturn(Optional.of(testInterest));

        Interest result = interestService.getInterestByName("Sports");

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Sports");
        assertThat(result.getDescription()).isEqualTo("Sports and physical activities");
        verify(interestRepository).findByName("Sports");
    }

    @Test
    void testGetInterestByName_NotFound_ThrowsException() {
        when(interestRepository.findByName("NonExistent")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> interestService.getInterestByName("NonExistent"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Interest not found with name: NonExistent");

        verify(interestRepository).findByName("NonExistent");
    }


    @Test
    void testAddInterestToUser_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(interestRepository.findById(1L)).thenReturn(Optional.of(testInterest));
        when(userInterestRepository.existsByUserIdAndInterestId(1L, 1L)).thenReturn(false);
        when(userInterestRepository.save(any(UserInterest.class))).thenReturn(testUserInterest);

        interestService.addInterestToUser(1L, 1L);

        verify(userRepository).findById(1L);
        verify(interestRepository).findById(1L);
        verify(userInterestRepository).existsByUserIdAndInterestId(1L, 1L);
        verify(userInterestRepository).save(any(UserInterest.class));
    }

    @Test
    void testAddInterestToUser_UserNotFound_ThrowsException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> interestService.addInterestToUser(999L, 1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found with ID: 999");

        verify(userRepository).findById(999L);
        verify(interestRepository, never()).findById(anyLong());
        verify(userInterestRepository, never()).save(any());
    }

    @Test
    void testAddInterestToUser_InterestNotFound_ThrowsException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(interestRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> interestService.addInterestToUser(1L, 999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Interest not found with ID: 999");

        verify(userRepository).findById(1L);
        verify(interestRepository).findById(999L);
        verify(userInterestRepository, never()).save(any());
    }

    @Test
    void testAddInterestToUser_AlreadyExists_ThrowsException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(interestRepository.findById(1L)).thenReturn(Optional.of(testInterest));
        when(userInterestRepository.existsByUserIdAndInterestId(1L, 1L)).thenReturn(true);

        assertThatThrownBy(() -> interestService.addInterestToUser(1L, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User already has this interest");

        verify(userRepository).findById(1L);
        verify(interestRepository).findById(1L);
        verify(userInterestRepository).existsByUserIdAndInterestId(1L, 1L);
        verify(userInterestRepository, never()).save(any());
    }


    @Test
    void testRemoveInterestFromUser_Success() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(interestRepository.existsById(1L)).thenReturn(true);
        when(userInterestRepository.existsByUserIdAndInterestId(1L, 1L)).thenReturn(true);

        interestService.removeInterestFromUser(1L, 1L);

        verify(userRepository).existsById(1L);
        verify(interestRepository).existsById(1L);
        verify(userInterestRepository).existsByUserIdAndInterestId(1L, 1L);
        verify(userInterestRepository).deleteByUserIdAndInterestId(1L, 1L);
    }

    @Test
    void testRemoveInterestFromUser_UserNotFound_ThrowsException() {
        when(userRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> interestService.removeInterestFromUser(999L, 1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found with ID: 999");

        verify(userRepository).existsById(999L);
        verify(interestRepository, never()).existsById(anyLong());
        verify(userInterestRepository, never()).deleteByUserIdAndInterestId(anyLong(), anyLong());
    }

    @Test
    void testRemoveInterestFromUser_InterestNotFound_ThrowsException() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(interestRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> interestService.removeInterestFromUser(1L, 999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Interest not found with ID: 999");

        verify(userRepository).existsById(1L);
        verify(interestRepository).existsById(999L);
        verify(userInterestRepository, never()).deleteByUserIdAndInterestId(anyLong(), anyLong());
    }

    @Test
    void testRemoveInterestFromUser_NotAssigned_DoesNothing() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(interestRepository.existsById(1L)).thenReturn(true);
        when(userInterestRepository.existsByUserIdAndInterestId(1L, 1L)).thenReturn(false);

        interestService.removeInterestFromUser(1L, 1L);

        verify(userRepository).existsById(1L);
        verify(interestRepository).existsById(1L);
        verify(userInterestRepository).existsByUserIdAndInterestId(1L, 1L);
        verify(userInterestRepository, never()).deleteByUserIdAndInterestId(anyLong(), anyLong());
    }


    @Test
    void testRemoveAllInterestsFromUser_Success() {
        UserInterest userInterest2 = new UserInterest();
        userInterest2.setId(2L);
        userInterest2.setUser(testUser);
        userInterest2.setInterest(testInterest2);

        when(userRepository.existsById(1L)).thenReturn(true);
        when(userInterestRepository.findByUserId(1L)).thenReturn(List.of(testUserInterest, userInterest2));

        interestService.removeAllInterestsFromUser(1L);

        verify(userRepository).existsById(1L);
        verify(userInterestRepository).findByUserId(1L);
        verify(userInterestRepository).deleteAll(anyList());
    }

    @Test
    void testRemoveAllInterestsFromUser_UserNotFound_ThrowsException() {
        when(userRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> interestService.removeAllInterestsFromUser(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found with ID: 999");

        verify(userRepository).existsById(999L);
        verify(userInterestRepository, never()).findByUserId(anyLong());
        verify(userInterestRepository, never()).deleteAll(anyList());
    }

    @Test
    void testRemoveAllInterestsFromUser_NoInterests() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(userInterestRepository.findByUserId(1L)).thenReturn(List.of());

        interestService.removeAllInterestsFromUser(1L);

        verify(userRepository).existsById(1L);
        verify(userInterestRepository).findByUserId(1L);
        verify(userInterestRepository).deleteAll(List.of());
    }


    @Test
    void testGetUserInterests_Success() {
        UserInterest userInterest2 = new UserInterest();
        userInterest2.setId(2L);
        userInterest2.setUser(testUser);
        userInterest2.setInterest(testInterest2);

        when(userRepository.existsById(1L)).thenReturn(true);
        when(userInterestRepository.findByUserId(1L)).thenReturn(List.of(testUserInterest, userInterest2));

        List<Interest> result = interestService.getUserInterests(1L);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("Sports");
        assertThat(result.get(1).getName()).isEqualTo("Music");
        verify(userRepository).existsById(1L);
        verify(userInterestRepository).findByUserId(1L);
    }

    @Test
    void testGetUserInterests_UserNotFound_ThrowsException() {
        when(userRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> interestService.getUserInterests(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found with ID: 999");

        verify(userRepository).existsById(999L);
        verify(userInterestRepository, never()).findByUserId(anyLong());
    }

    @Test
    void testGetUserInterests_NoInterests() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(userInterestRepository.findByUserId(1L)).thenReturn(List.of());

        List<Interest> result = interestService.getUserInterests(1L);

        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
        verify(userRepository).existsById(1L);
        verify(userInterestRepository).findByUserId(1L);
    }


    @Test
    void testCreateInterest_Success() {
        Interest newInterest = new Interest();
        newInterest.setName("Reading");
        newInterest.setDescription("Books and reading");

        Interest savedInterest = new Interest();
        savedInterest.setId(3L);
        savedInterest.setName("Reading");
        savedInterest.setDescription("Books and reading");

        when(interestRepository.existsByName("Reading")).thenReturn(false);
        when(interestRepository.save(newInterest)).thenReturn(savedInterest);

        Interest result = interestService.createInterest(newInterest);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(3L);
        assertThat(result.getName()).isEqualTo("Reading");
        assertThat(result.getDescription()).isEqualTo("Books and reading");
        verify(interestRepository).existsByName("Reading");
        verify(interestRepository).save(newInterest);
    }

    @Test
    void testCreateInterest_AlreadyExists_ThrowsException() {
        Interest newInterest = new Interest();
        newInterest.setName("Sports");

        when(interestRepository.existsByName("Sports")).thenReturn(true);

        assertThatThrownBy(() -> interestService.createInterest(newInterest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Interest already exists with name: Sports");

        verify(interestRepository).existsByName("Sports");
        verify(interestRepository, never()).save(any());
    }


    @Test
    void testUpdateInterest_Success() {
        Interest updatedData = new Interest();
        updatedData.setName("Sports & Fitness");
        updatedData.setDescription("Updated description");

        when(interestRepository.findById(1L)).thenReturn(Optional.of(testInterest));
        when(interestRepository.save(testInterest)).thenReturn(testInterest);

        Interest result = interestService.updateInterest(1L, updatedData);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Sports & Fitness");
        assertThat(result.getDescription()).isEqualTo("Updated description");
        verify(interestRepository).findById(1L);
        verify(interestRepository).save(testInterest);
    }

    @Test
    void testUpdateInterest_NotFound_ThrowsException() {
        Interest updatedData = new Interest();
        updatedData.setName("Updated Name");

        when(interestRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> interestService.updateInterest(999L, updatedData))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Interest not found with ID: 999");

        verify(interestRepository).findById(999L);
        verify(interestRepository, never()).save(any());
    }

    @Test
    void testUpdateInterest_WithNullDescription() {
        Interest updatedData = new Interest();
        updatedData.setName("Sports Updated");
        updatedData.setDescription(null);

        when(interestRepository.findById(1L)).thenReturn(Optional.of(testInterest));
        when(interestRepository.save(testInterest)).thenReturn(testInterest);

        Interest result = interestService.updateInterest(1L, updatedData);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Sports Updated");
        assertThat(result.getDescription()).isNull();
        verify(interestRepository).findById(1L);
        verify(interestRepository).save(testInterest);
    }


    @Test
    void testDeleteInterest_Success() {
        when(interestRepository.findById(1L)).thenReturn(Optional.of(testInterest));

        interestService.deleteInterest(1L);

        verify(interestRepository).findById(1L);
        verify(interestRepository).delete(testInterest);
    }

    @Test
    void testDeleteInterest_NotFound_ThrowsException() {
        when(interestRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> interestService.deleteInterest(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Interest not found with ID: 999");

        verify(interestRepository).findById(999L);
        verify(interestRepository, never()).delete(any());
    }


    @Test
    void testAddInterestToUser_VerifiesCorrectAssociation() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(interestRepository.findById(1L)).thenReturn(Optional.of(testInterest));
        when(userInterestRepository.existsByUserIdAndInterestId(1L, 1L)).thenReturn(false);

        interestService.addInterestToUser(1L, 1L);

        verify(userInterestRepository).save(argThat(userInterest ->
                userInterest.getUser().getId().equals(1L) &&
                        userInterest.getInterest().getId().equals(1L)
        ));
    }

    @Test
    void testGetAllInterests_OrderedByName() {
        Interest interestA = new Interest();
        interestA.setId(1L);
        interestA.setName("Art");

        Interest interestZ = new Interest();
        interestZ.setId(2L);
        interestZ.setName("Zen");

        when(interestRepository.findAllByOrderByNameAsc()).thenReturn(List.of(interestA, interestZ));

        List<Interest> result = interestService.getAllInterests();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("Art");
        assertThat(result.get(1).getName()).isEqualTo("Zen");
    }

    @Test
    void testCreateInterest_PreservesAllFields() {
        Interest newInterest = new Interest();
        newInterest.setName("Gaming");
        newInterest.setDescription("Video games and gaming");

        Interest savedInterest = new Interest();
        savedInterest.setId(5L);
        savedInterest.setName("Gaming");
        savedInterest.setDescription("Video games and gaming");

        when(interestRepository.existsByName("Gaming")).thenReturn(false);
        when(interestRepository.save(newInterest)).thenReturn(savedInterest);

        Interest result = interestService.createInterest(newInterest);

        assertThat(result.getId()).isEqualTo(5L);
        assertThat(result.getName()).isEqualTo("Gaming");
        assertThat(result.getDescription()).isEqualTo("Video games and gaming");
    }
}

