package com.proyect_planning.proyect_planning_system.repositories;

import com.proyect_planning.proyect_planning_system.entities.Observacion;
import com.proyect_planning.proyect_planning_system.entities.EstadoObservacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ObservacionRepository extends JpaRepository<Observacion, Long> {
    
    List<Observacion> findByProyectoId(Long proyectoId);
    
    List<Observacion> findByOngId(Long ongId);
    
    List<Observacion> findByEstado(EstadoObservacion estado);
    
    List<Observacion> findByProyectoIdAndEstado(Long proyectoId, EstadoObservacion estado);
}
