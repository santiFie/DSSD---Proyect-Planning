package com.proyect_planning.proyect_planning_system.repositories;

import com.proyect_planning.proyect_planning_system.entities.Proyect;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProyectRepository extends JpaRepository<Proyect, Long> {
    boolean existsByName(String name);
    Proyect findByName(String name);
    List<Proyect> findAll();
}
