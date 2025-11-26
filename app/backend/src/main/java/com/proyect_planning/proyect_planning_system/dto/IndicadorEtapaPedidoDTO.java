package com.proyect_planning.proyect_planning_system.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IndicadorEtapaPedidoDTO {
    private Long idProject;
    private Long idStage;
    private String categoryStage;
    private Long ongColaboranteId;
}
