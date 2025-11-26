package com.proyect_planning.proyect_planning_system.controllers;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import com.proyect_planning.proyect_planning_system.services.bonita.BonitaBusinessService;

import jakarta.transaction.Transactional;

import com.proyect_planning.proyect_planning_system.dtos.NewProjectDto;
import com.proyect_planning.proyect_planning_system.dtos.NewStageDto;
import com.proyect_planning.proyect_planning_system.dtos.StageDto;
import com.proyect_planning.proyect_planning_system.dtos.ProyectDto;
import com.proyect_planning.proyect_planning_system.entities.Proyect;
import com.proyect_planning.proyect_planning_system.entities.User;
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

        logger.error("Creando proyecto: {}", newProjectDto.getName());
        logger.error("Stages: {}", newProjectDto.getStages().stream().toList());

        try {
            // Obtener el usuario autenticado
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User authenticatedUser = (User) authentication.getPrincipal();
            Long ongId = authenticatedUser.getOng().getId();

            // Crear el proyecto en la base de datos primero
            Proyect project = proyectService.createProject(newProjectDto, ongId);

            //logger.error("Creando proyecto en entidad: {}", project.getName());
            //logger.error("Stages en entidad: {}", project.getStages().stream().toList());

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

    @GetMapping("/ong/{ongId}")
    @Transactional
    public ResponseEntity<List<ProyectDto>> getProjectsByOng(@PathVariable Long ongId) {
        List<Proyect> projects = proyectService.getProjectsByOng(ongId);
        List<ProyectDto> projectDtos = projects.stream()
                .map(ProyectDto::fromEntity)
                .toList();
        return ResponseEntity.ok(projectDtos);
    }

    @GetMapping("/my-projects/covered")
    @Transactional
    public ResponseEntity<List<ProyectDto>> getCoveredProjectsByOng() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User authenticatedUser = (User) authentication.getPrincipal();
        Long ongId = authenticatedUser.getOng().getId();

        List<Proyect> projects = proyectService.getCoveredProjectsByOng(ongId);
        List<ProyectDto> projectDtos = projects.stream()
                .map(ProyectDto::fromEntity)
                .toList();
        logger.debug("Proyectos cubiertos: {}", projectDtos.toString());
        return ResponseEntity.ok(projectDtos);
    }

    @GetMapping("/my-projects")
    @Transactional
    public ResponseEntity<List<ProyectDto>> getMyProjects() {
        // Obtener el usuario autenticado
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User authenticatedUser = (User) authentication.getPrincipal();
        Long ongId = authenticatedUser.getOng().getId();

        List<Proyect> projects = proyectService.getProjectsByOng(ongId);
        List<ProyectDto> projectDtos = projects.stream()
                .map(ProyectDto::fromEntity)
                .toList();
        logger.debug("Proyectos: {}", projectDtos.toString());
        return ResponseEntity.ok(projectDtos);
    }

    @PutMapping("/{id}/addStage")
    public ResponseEntity<Proyect> addStageToProject(@PathVariable Long id, @RequestBody NewStageDto newStageDto) {
        Proyect updatedProject = proyectService.addStageToProject(id, newStageDto);
        return ResponseEntity.ok(updatedProject);   
    }

    @GetMapping("/{id}/stages")
    public ResponseEntity<List<StageDto>> getProjectStages(@PathVariable Long id) {
        Proyect project = proyectService.getProyectById(id);
        if (project == null) {
            return ResponseEntity.notFound().build();
        }
        List<StageDto> stageDtos = project.getStages().stream()
                .map(StageDto::fromEntity)
                .sorted(StageDto.idComparator)
                .toList();
        logger.debug("Stages del proyecto {}: {}", id, stageDtos.toString());
        logger.debug(stageDtos.toString());
        return ResponseEntity.ok(stageDtos);
    }

    @PutMapping("/my-projects/{id}/execute-stage/{stageId}")
    public ResponseEntity<?> executeStage(@PathVariable Long id, @PathVariable Long stageId) {
        Proyect proyect = proyectService.getProyectById(id);
        if (proyect == null) {
            return ResponseEntity.notFound().build();
        }

        // Generate endDate as today
        LocalDate today = LocalDate.now();
        String endDate = today.toString();

        try {
            int cantidad = 0;
            // Primero ejecutar la etapa localmente
            cantidad = proyectService.executeStage(proyect, stageId, endDate);

            // Preparar respuesta exitosa inicial
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Compromiso aceptado exitosamente");
            try {
                // Luego notificar a Bonita
                String bonitaCaseId = proyect.getBonitaCaseId();

                if (bonitaCaseId != null) {
                    // Llamar al proceso de Bonita para ejecutar la etapa
                    // Bonita se encargará de actualizar el estado en el Cloud
                    bonitaBusinessSvc.ejecutarEtapa(bonitaCaseId, cantidad);

                    response.put("message", "Etapa ejecutada correctamente y proceso Bonita avanzado exitosamente");
                    response.put("bonita_case_id", bonitaCaseId);
                    response.put("bonita_enabled", true);
                } else {
                    response.put("message", "Etapa ejecutada (no se encontró caso Bonita asociado)");
                    response.put("bonita_enabled", false);
                }
            } catch (Exception bonitaError) {
                logger.error("Error en Bonita al ejecutar etapa: {}", bonitaError.getMessage(), bonitaError);

                // Si Bonita falla, seguir con el proceso pero indicarlo
                response.put("message", "Etapa ejecutada (Bonita no disponible)");
                response.put("bonita_enabled", false);
                response.put("bonita_error", bonitaError.getMessage());
            }

            return ResponseEntity.ok().body(response);
        } catch (IllegalArgumentException iae) {
            logger.warn("Invalid input data: {}", iae.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Error executing stage ", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("my-projects/{id}/close-project")
    public ResponseEntity<?> closeProject(@PathVariable Long id, @RequestParam String closeDescription) {
        Proyect proyect = proyectService.getProyectById(id);
        if (proyect == null) {
            return ResponseEntity.notFound().build();
        }

        try {
            // Primero cerrar el proyecto localmente
            proyectService.closeProject(proyect, closeDescription);

            // Preparar respuesta exitosa inicial
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Proyecto cerrado exitosamente");

            try {
                // Luego notificar a Bonita
                String bonitaCaseId = proyect.getBonitaCaseId();

                if (bonitaCaseId != null) {
                    // Llamar al proceso de Bonita para cerrar el proyecto
                    bonitaBusinessSvc.finalizarProyecto(bonitaCaseId);

                    response.put("message", "Proyecto cerrado correctamente y proceso Bonita avanzado exitosamente");
                    response.put("bonita_case_id", bonitaCaseId);
                    response.put("bonita_enabled", true);
                } else {
                    response.put("message", "Proyecto cerrado (no se encontró caso Bonita asociado)");
                    response.put("bonita_enabled", false);
                }
            } catch (Exception bonitaError) {
                logger.error("Error en Bonita al cerrar proyecto: {}", bonitaError.getMessage(), bonitaError);

                // Si Bonita falla, seguir con el proceso pero indicarlo
                response.put("message", "Proyecto cerrado (Bonita no disponible)");
                response.put("bonita_enabled", false);
                response.put("bonita_error", bonitaError.getMessage());
            }

            return ResponseEntity.ok().body(response);
        } catch (IllegalArgumentException iae) {
            logger.warn("Invalid input data: {}", iae.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Error closing project ", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
