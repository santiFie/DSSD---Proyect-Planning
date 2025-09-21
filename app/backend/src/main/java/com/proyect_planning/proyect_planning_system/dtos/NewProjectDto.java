package com.proyect_planning.proyect_planning_system.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NewProjectDto {

    private String name;

    private String description;
    
    private String startDate;
    
    private String endDate;
    
}
