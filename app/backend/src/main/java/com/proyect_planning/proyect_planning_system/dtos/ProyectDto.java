package com.proyect_planning.proyect_planning_system.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProyectDto {
    
    private Long id;
    private String name;
    private String description;
    private String startDate;
    private String endDate;
    private String neighborhood;
    private List<StageDto> stages;
    
    
    // Constructor que convierte de entidad a DTO
    public static ProyectDto fromEntity(com.proyect_planning.proyect_planning_system.entities.Proyect proyect) {
        List<StageDto> stageDtos = proyect.getStages() != null ? 
            proyect.getStages().stream()
                    .map(StageDto::fromEntity)
                    .collect(Collectors.toList()) : 
            null;
            
        return ProyectDto.builder()
                .id(proyect.getId())
                .name(proyect.getName())
                .description(proyect.getDescription())
                .startDate(proyect.getStartDate())
                .endDate(proyect.getEndDate())
                .neighborhood(proyect.getNeighborhood())
                .stages(stageDtos)
                .build();
    }
}