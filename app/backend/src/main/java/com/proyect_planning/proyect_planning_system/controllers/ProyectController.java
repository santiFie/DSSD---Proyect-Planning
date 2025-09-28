package com.proyect_planning.proyect_planning_system.controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
import com.proyect_planning.proyect_planning_system.services.bonita.BonitaApiService;
import com.proyect_planning.proyect_planning_system.services.bonita.BonitaAuthService;

import jakarta.transaction.Transactional;

import com.proyect_planning.proyect_planning_system.dtos.NewProjectDto;
import com.proyect_planning.proyect_planning_system.dtos.NewStageDto;
import com.proyect_planning.proyect_planning_system.dtos.ProyectDto;
import com.proyect_planning.proyect_planning_system.entities.Proyect;
import com.proyect_planning.proyect_planning_system.repositories.ProyectRepository;
import com.proyect_planning.proyect_planning_system.services.ProyectService;

@RestController
@RequestMapping("/api/projects")
@CrossOrigin(origins = "http://localhost:4200")
public class ProyectController {

    @Autowired
    private ProyectService proyectService;

    @Autowired
    private ProyectRepository proyectRepository;

    @Autowired
    private BonitaAuthService authService;

    @Autowired
    private BonitaApiService apiService;

    @PostMapping
    public ResponseEntity<Map<String, Object>> createProject(@RequestBody NewProjectDto newProjectDto) {
        try {
            // Crear el proyecto en la base de datos primero
            Proyect project = proyectService.createProject(newProjectDto);
            
            // Preparar respuesta exitosa (sin Bonita por ahora)
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Proyecto creado exitosamente");
            response.put("project", ProyectDto.fromEntity(project));
            
            // Intentar integración con Bonita (opcional)
            try {
                // Autenticar con Bonita
                authService.login();
                
                // Obtener el ID del proceso "Gestion Proyecto"
                String processId = apiService.getProcessId("Gestion Proyecto");
                
                if (processId != null) {
                    // Preparar variables para el proceso
                    Map<String, Object> processVariables = new HashMap<>();
                    processVariables.put("project_id", project.getId());
                    processVariables.put("project_name", project.getName());
                    processVariables.put("project_description", project.getDescription());
                    processVariables.put("start_date", project.getStartDate().toString());
                    processVariables.put("end_date", project.getEndDate().toString());
                    
                    // Para stages_list, crear una lista simple de strings
                    List<String> stageNames = new ArrayList<>();
                    if (project.getStages() != null) {
                        project.getStages().forEach(stage -> stageNames.add(stage.getName()));
                    }
                    processVariables.put("stages_list", stageNames);
                    
                    // Iniciar el proceso en Bonita
                    Map<String, Object> processInstance = apiService.startProcessInstance(processId, processVariables);
                    
                    // Actualizar respuesta con información de Bonita
                    response.put("message", "Proyecto y proceso Bonita creados exitosamente");
                    response.put("bonita_process_id", processInstance.get("id"));
                    response.put("bonita_process_name", "Gestion Proyecto");
                    response.put("bonita_enabled", true);
                } else {
                    response.put("message", "Proyecto creado (Bonita: proceso no encontrado)");
                    response.put("bonita_enabled", false);
                }
            } catch (Exception bonitaError) {
                System.out.println("Bonita no disponible: " + bonitaError.getMessage());
                response.put("message", "Proyecto creado exitosamente (Bonita no disponible)");
                response.put("bonita_enabled", false);
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("Error creando proyecto: " + e.getMessage());
            
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
