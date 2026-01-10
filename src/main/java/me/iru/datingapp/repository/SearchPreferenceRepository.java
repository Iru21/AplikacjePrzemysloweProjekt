package me.iru.datingapp.repository;

import me.iru.datingapp.entity.SearchPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SearchPreferenceRepository extends JpaRepository<SearchPreference, Long> {

    Optional<SearchPreference> findByUserId(Long userId);

    boolean existsByUserId(Long userId);

    void deleteByUserId(Long userId);
}

