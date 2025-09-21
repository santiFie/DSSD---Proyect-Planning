package com.proyect_planning.proyect_planning_system.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NewStageDto {

    private String name;

    private String needs;

    private String startDate;

    private Long proyectId;
}
