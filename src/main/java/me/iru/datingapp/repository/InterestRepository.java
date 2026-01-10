package me.iru.datingapp.repository;

import me.iru.datingapp.entity.Interest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InterestRepository extends JpaRepository<Interest, Long> {

    Optional<Interest> findByName(String name);

    List<Interest> findAllByOrderByNameAsc();

    boolean existsByName(String name);
}

