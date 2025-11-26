package com.proyect_planning.proyect_planning_system.repositories;

import com.proyect_planning.proyect_planning_system.entities.Proyect;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProyectRepository extends JpaRepository<Proyect, Long> {
    boolean existsByName(String name);
    Proyect findByName(String name);
    List<Proyect> findAll();
    List<Proyect> findByOngOriginante(Long ongOriginante);

    @Query("""
    SELECT p
    FROM Proyect p
    WHERE p.ongOriginante = :ongId
      AND NOT EXISTS (
          SELECT s
          FROM Stage s
          WHERE s.proyect = p
            AND s.covered = false
      )
    """)
    List<Proyect> findCoveredProjectsByOng(Long ongId);
}
