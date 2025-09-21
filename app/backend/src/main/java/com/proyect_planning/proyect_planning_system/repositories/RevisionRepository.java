package com.proyect_planning.proyect_planning_system.repositories;

import com.proyect_planning.proyect_planning_system.entities.Revision;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RevisionRepository extends JpaRepository<Revision,Long> {
}
