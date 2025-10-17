package com.proyect_planning.proyect_planning_system.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NewCompromisoDto {

    private String pedidoId;

    private String ongColaboranteId;
    
    private String descripcion;
    
    private String fechaCompromiso;

    private String estado;
    
    private Integer version;
    
}
