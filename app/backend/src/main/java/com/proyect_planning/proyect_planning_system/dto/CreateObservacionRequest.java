package com.proyect_planning.proyect_planning_system.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateObservacionRequest {
    private String descripcion;
    private Long proyectoId;
    private Long ongId;
}
