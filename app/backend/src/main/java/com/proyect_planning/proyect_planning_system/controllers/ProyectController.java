package com.proyect_planning.proyect_planning_system.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.CrossOrigin;
import com.proyect_planning.proyect_planning_system.services.bonita.BonitaBusinessService;

import jakarta.transaction.Transactional;

import com.proyect_planning.proyect_planning_system.dtos.NewProjectDto;
import com.proyect_planning.proyect_planning_system.dtos.NewStageDto;
import com.proyect_planning.proyect_planning_system.dtos.ProyectDto;
import com.proyect_planning.proyect_planning_system.entities.Proyect;
import com.proyect_planning.proyect_planning_system.services.ProyectService;

@RestController
@RequestMapping("/api/projects")
@CrossOrigin(origins = "http://localhost:4200")
public class ProyectController {

    private final Logger logger = LoggerFactory.getLogger(ProyectController.class);

    @Autowired
    private ProyectService proyectService;

    @Autowired
    private BonitaBusinessService bonitaBusinessSvc;

    @PostMapping
    public ResponseEntity<Map<String, Object>> createProject(@RequestBody NewProjectDto newProjectDto) {

        logger.debug("Creando proyecto: {}", newProjectDto.getName());
        logger.debug("Stages: {}", newProjectDto.getStages().stream().toList());

        try {
            // Crear el proyecto en la base de datos primero
            Proyect project = proyectService.createProject(newProjectDto);

            logger.debug("Creando proyecto en entidad: {}", project.getName());
            logger.debug("Stages en entidad: {}", project.getStages().stream().toList());

            // Preparar respuesta exitosa (sin Bonita por ahora)
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Proyecto creado exitosamente");
            response.put("project", ProyectDto.fromEntity(project));

            // Intentar integración con Bonita (opcional)
            try {
                String caseId = bonitaBusinessSvc.registrarProyecto(project);
                if (caseId != null) {
                    // Guardar el ID del caso de Bonita en el proyecto
                    project.setBonitaCaseId(caseId);
                    proyectService.updateProyect(project);

                    // Actualizar respuesta con información de Bonita
                    response.put("message", "Proyecto y proceso Bonita creados exitosamente");
                    response.put("bonita_process_id", caseId);
                    response.put("bonita_process_name", "Gestion Proyecto");
                    response.put("bonita_enabled", true);
                } else {
                    response.put("message", "Proyecto creado (Bonita: proceso no encontrado)");
                    response.put("bonita_enabled", false);
                }
            } catch (Exception bonitaError) {
                logger.error("Bonita error", bonitaError);
                response.put("message", "Proyecto creado exitosamente (Bonita no disponible)");
                response.put("bonita_enabled", false);
            }

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException iae) {
            logger.warn("Invalid input data: {}", iae.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Datos invalidos: " + iae.getMessage());

            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            logger.error("Error creando proyecto ", e);

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Error creando proyecto: " + e.getMessage());

            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @GetMapping("/{name}")
    public ResponseEntity<ProyectDto> getProjectByName(@PathVariable String name) {
        Proyect project = proyectService.getProjectByName(name);
        return ResponseEntity.ok(ProyectDto.fromEntity(project));
    }

    @GetMapping("/id/{id}")
    @Transactional
    public ResponseEntity<ProyectDto> getProjectById(@PathVariable Long id) {
        Proyect project = proyectService.getProyectById(id);
        return ResponseEntity.ok(ProyectDto.fromEntity(project));
    }

    @GetMapping("/all")
    @Transactional
    public ResponseEntity<List<ProyectDto>> getAllProjects() {
        List<Proyect> projects = proyectService.getAllProjects();
        List<ProyectDto> projectDtos = projects.stream()
                .map(ProyectDto::fromEntity)
                .toList();
        return ResponseEntity.ok(projectDtos);
    }

    @PutMapping("/{id}/addStage")
    public ResponseEntity<Proyect> addStageToProject(@PathVariable Long id, @RequestBody NewStageDto newStageDto) {
        Proyect updatedProject = proyectService.addStageToProject(id, newStageDto);
        return ResponseEntity.ok(updatedProject);
    }
}
