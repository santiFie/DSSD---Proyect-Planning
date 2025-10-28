package com.proyect_planning.proyect_planning_system.repositories;

import com.proyect_planning.proyect_planning_system.entities.Ong;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OngRepository extends JpaRepository<Ong, Long> {
    boolean existsByName(String name);
    Optional<Ong> findByName(String name);
}
