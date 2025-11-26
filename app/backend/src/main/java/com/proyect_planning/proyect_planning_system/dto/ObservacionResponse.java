package com.proyect_planning.proyect_planning_system.dto;

import com.proyect_planning.proyect_planning_system.entities.EstadoObservacion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ObservacionResponse {
    private Long id;
    private String descripcion;
    private EstadoObservacion estado;
    private Long proyectoId;
    private String proyectoNombre;
    private Long ongId;
    private String ongNombre;
    private String bonitaCaseId;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaLimite;
    private LocalDateTime fechaResolucion;
    private List<CorreccionResponse> correcciones;
}
