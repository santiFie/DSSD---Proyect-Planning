package com.proyect_planning.proyect_planning_system.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StageResponseDto {
    private Long id;
    private String name;
    private String needs;
    private Boolean covered;
    private String startDate;
    private String endDate;
    // No incluimos las revisiones para simplificar la respuesta
    // Si necesitas revisiones, puedes crear un endpoint específico
}