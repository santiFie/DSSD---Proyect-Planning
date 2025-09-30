package com.proyect_planning.proyect_planning_system.dtos;

import com.proyect_planning.proyect_planning_system.entities.Need;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NewStageDto {

    private String name;

    private Need needs;

    private String startDate;

    private Long proyectId;
}
