package me.iru.datingapp.repository;

import me.iru.datingapp.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Page<User> findByGenderAndAgeBetween(User.Gender gender, Integer minAge, Integer maxAge, Pageable pageable);

    Page<User> findByCity(String city, Pageable pageable);


    @Query("SELECT u FROM User u WHERE " +
            "(:gender IS NULL OR u.gender = :gender) AND " +
            "(:minAge IS NULL OR u.age >= :minAge) AND " +
            "(:maxAge IS NULL OR u.age <= :maxAge) AND " +
            "(:city IS NULL OR u.city = :city)")
    Page<User> findBySearchCriteria(
            @Param("gender") User.Gender gender,
            @Param("minAge") Integer minAge,
            @Param("maxAge") Integer maxAge,
            @Param("city") String city,
            Pageable pageable
    );

    @Query("SELECT u FROM User u WHERE u.id != :userId")
    Page<User> findAllExcludingUser(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT u FROM User u WHERE " +
            "u.id != :excludeUserId AND " +
            "(:gender IS NULL OR u.gender = :gender) AND " +
            "(:minAge IS NULL OR u.age >= :minAge) AND " +
            "(:maxAge IS NULL OR u.age <= :maxAge)")
    Page<User> findMatchingUsers(
            @Param("excludeUserId") Long excludeUserId,
            @Param("gender") User.Gender gender,
            @Param("minAge") Integer minAge,
            @Param("maxAge") Integer maxAge,
            Pageable pageable
    );
}

