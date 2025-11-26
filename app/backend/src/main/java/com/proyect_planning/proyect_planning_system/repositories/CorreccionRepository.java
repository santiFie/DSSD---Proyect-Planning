package com.proyect_planning.proyect_planning_system.repositories;

import com.proyect_planning.proyect_planning_system.entities.Correccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CorreccionRepository extends JpaRepository<Correccion, Long> {
    
    List<Correccion> findByObservacionId(Long observacionId);
    
    List<Correccion> findByUsuarioId(Long usuarioId);
}
