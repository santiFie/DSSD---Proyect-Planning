package com.proyect_planning.proyect_planning_system.repositories;

import com.proyect_planning.proyect_planning_system.entities.Need;
import com.proyect_planning.proyect_planning_system.entities.Stage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StageRepository extends JpaRepository<Stage, Long> {
    public Stage findByNeeds(Need need);
}
