package com.proyect_planning.proyect_planning_system.dtos;

import com.proyect_planning.proyect_planning_system.entities.Need;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StageDto {
    
    private Long id;
    
    private String name;
    
    private Need needs;
    
    private Boolean covered = false;
    
    private String startDate;
    
    private String endDate;
    
    // Constructor que convierte de entidad a DTO
    public static StageDto fromEntity(com.proyect_planning.proyect_planning_system.entities.Stage stage) {
        StageDto dto = new StageDto();
        dto.setId(stage.getId());
        dto.setName(stage.getName());
        dto.setNeeds(stage.getNeeds());
        dto.setCovered(stage.getCovered());
        dto.setStartDate(stage.getStartDate());
        dto.setEndDate(stage.getEndDate());
        return dto;
    }
}