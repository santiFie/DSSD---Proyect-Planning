package com.proyect_planning.proyect_planning_system.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CorreccionResponse {
    private Long id;
    private Long observacionId;
    private String detalle;
    private Long usuarioId;
    private String usuarioNombre;
    private LocalDateTime fechaCreacion;
}
