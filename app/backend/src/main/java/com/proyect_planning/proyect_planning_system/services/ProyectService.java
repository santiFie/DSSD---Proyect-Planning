package com.proyect_planning.proyect_planning_system.services;

import java.time.LocalDate;
import java.util.List;

import com.proyect_planning.proyect_planning_system.dtos.NewStageDto;
import com.proyect_planning.proyect_planning_system.dtos.StageDto;
import com.proyect_planning.proyect_planning_system.entities.Stage;
import com.proyect_planning.proyect_planning_system.repositories.StageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.proyect_planning.proyect_planning_system.dtos.NewProjectDto;
import com.proyect_planning.proyect_planning_system.entities.Proyect;
import com.proyect_planning.proyect_planning_system.repositories.ProyectRepository;


@Service
public class ProyectService {

    @Autowired
    private ProyectRepository proyectRepository;
    @Autowired
    private StageRepository stageRepository;

    public ProyectService(ProyectRepository proyectRepository, StageRepository stageRepository) {
        this.proyectRepository = proyectRepository;
        this.stageRepository = stageRepository;
    }

    public Proyect createProject(NewProjectDto newProjectDto) {
        if (proyectRepository.existsByName(newProjectDto.getName())) {
            throw new IllegalArgumentException("Ya existe un proyecto con ese nombre.");
        }

        // validate end date is after start date
        if (newProjectDto.getEndDate() != null) {
            try {
                LocalDate end = LocalDate.parse(newProjectDto.getEndDate());
                LocalDate start = newProjectDto.getStartDate() != null ? LocalDate.parse(newProjectDto.getStartDate()) : null;
                if (start != null && end.isBefore(start)) {
                    throw new IllegalArgumentException("La fecha de fin debe ser posterior a la de inicio.");
                }
            } catch (java.time.format.DateTimeParseException e) {
                throw new IllegalArgumentException("Formato de fecha invalido, espera aaaa-MM-dd", e);
            }
        }

        for (StageDto stageDto : newProjectDto.getStages()) {
            // Validate end date is after start date for each stage and start date is equal or after project start date
            if (stageDto.getEndDate() != null) {
                try {
                    LocalDate stageEnd = LocalDate.parse(stageDto.getEndDate());
                    LocalDate stageStart = stageDto.getStartDate() != null ? LocalDate.parse(stageDto.getStartDate()) : null;
                    LocalDate projectStart = newProjectDto.getStartDate() != null ? LocalDate.parse(newProjectDto.getStartDate()) : null;
                    if (stageStart != null && stageEnd.isBefore(stageStart)) {
                        throw new IllegalArgumentException("La fecha de fin debe ser posterior a la de inicio en la etapa.");
                    }
                    if (projectStart != null && stageStart != null && stageStart.isBefore(projectStart)) {
                        throw new IllegalArgumentException("La fecha de inicio de la etapa debe ser igual o posterior a la de inicio del proyecto.");
                    }
                } catch (java.time.format.DateTimeParseException e) {
                    throw new IllegalArgumentException("Formato de fecha invalido, espera aaaa-MM-dd", e);
                }
            }
        }

        Proyect proyect = Proyect.builder()
                .name(newProjectDto.getName())
                .description(newProjectDto.getDescription())
                .startDate(newProjectDto.getStartDate())
                .endDate(newProjectDto.getEndDate())
                .neighborhood(newProjectDto.getNeighborhood())
                .build();

        // Guardar el proyecto primero
        proyect = proyectRepository.save(proyect);

        // Crear y agregar las etapas si existen
        if (newProjectDto.getStages() != null && !newProjectDto.getStages().isEmpty()) {
            for (StageDto stageDto : newProjectDto.getStages()) {
                // Validate end date is after start date for each stage and start date is equal or after project start date
                if (stageDto.getEndDate() != null) {
                    try {
                        LocalDate stageEnd = LocalDate.parse(stageDto.getEndDate());
                        LocalDate stageStart = stageDto.getStartDate() != null ? LocalDate.parse(stageDto.getStartDate()) : null;
                        LocalDate projectStart = proyect.getStartDate() != null ? LocalDate.parse(proyect.getStartDate()) : null;
                        if (stageStart != null && stageEnd.isBefore(stageStart)) {
                            throw new IllegalArgumentException("La fecha de fin debe ser posterior a la de inicio en la etapa.");
                        }
                        if (projectStart != null && stageStart != null && stageStart.isBefore(projectStart)) {
                            throw new IllegalArgumentException("La fecha de inicio de la etapa debe ser igual o posterior a la de inicio del proyecto.");
                        }
                    } catch (java.time.format.DateTimeParseException e) {
                        throw new IllegalArgumentException("Formato de fecha invalido, espera aaaa-MM-dd", e);
                    }
                }

                Stage stage = Stage.builder()
                        .name(stageDto.getName())
                        .needs(stageDto.getNeeds())
                        .covered(stageDto.getCovered() != null ? stageDto.getCovered() : false)
                        .startDate(stageDto.getStartDate())
                        .endDate(stageDto.getEndDate())
                        .proyect(proyect)
                        .build();
                
                proyect.addStage(stage);
            }
            
            // Guardar el proyecto con las etapas
            proyect = proyectRepository.save(proyect);
        }

        return proyect;
    }

    public Proyect getProjectByName(String name) {
        return proyectRepository.findByName(name);
    }

    public Proyect getProyectById(Long id) {
        return proyectRepository.findById(id).orElse(null);
    }

    public List<Proyect> getAllProjects() {
        return proyectRepository.findAll();
    }

    public Proyect addStageToProject(Long projectId, NewStageDto newStageDto) {
        Proyect proyect = proyectRepository.findById(projectId).orElseThrow(() -> new IllegalArgumentException("Project not found"));

        Stage s = Stage.builder()
                .name(newStageDto.getName())
                .needs(newStageDto.getNeeds())
                .startDate(newStageDto.getStartDate())
                .proyect(proyect)
                .build();
        stageRepository.save(s);

        proyect.addStage(s);
        return proyectRepository.save(proyect);
    }

    public Proyect updateProyect(Proyect proyect) {
        return proyectRepository.save(proyect);
    }

}