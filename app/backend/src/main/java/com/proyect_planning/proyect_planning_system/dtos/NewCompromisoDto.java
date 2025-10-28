package com.proyect_planning.proyect_planning_system.dtos;

import com.proyect_planning.proyect_planning_system.services.cloud.dto.PedidoCloudDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NewCompromisoDto {

    private String ongColaboranteId;
    
    private String descripcion;
    
    private String fechaCompromiso;

    private String estado;
    
    private Integer version;

    private PedidoCloudDTO pedido;
    
}
