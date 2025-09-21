package com.proyect_planning.proyect_planning_system.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NewRevisionDto {

    private List<String> comments;

    private Long stageId;
}
