package com.proyect_planning.proyect_planning_system.controllers;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
import com.proyect_planning.proyect_planning_system.services.bonita.BonitaApiService;
import com.proyect_planning.proyect_planning_system.services.bonita.BonitaAuthService;

import jakarta.transaction.Transactional;

import com.proyect_planning.proyect_planning_system.dtos.NewProjectDto;
import com.proyect_planning.proyect_planning_system.dtos.NewStageDto;
import com.proyect_planning.proyect_planning_system.dtos.ProyectDto;
import com.proyect_planning.proyect_planning_system.entities.Proyect;
import com.proyect_planning.proyect_planning_system.entities.Stage;
import com.proyect_planning.proyect_planning_system.repositories.ProyectRepository;
import com.proyect_planning.proyect_planning_system.entities.Stage;
import com.proyect_planning.proyect_planning_system.services.ProyectService;

@RestController
@RequestMapping("/api/projects")
@CrossOrigin(origins = "http://localhost:4200")
public class ProyectController {

    private final Logger logger = LoggerFactory.getLogger(ProyectController.class);

    @Autowired
    private ProyectService proyectService;

    @Autowired
    private BonitaApiService bonitaSvc;

    @PostMapping
    public ResponseEntity<Map<String, Object>> createProject(@RequestBody NewProjectDto newProjectDto) {

        System.out.println("Creando proyecto: " + newProjectDto.getName());
        System.out.println("Stages: " + newProjectDto.getStages().stream().map(s -> s.getName()).collect(Collectors.toList()));

        try {
            // Crear el proyecto en la base de datos primero
            Proyect project = proyectService.createProject(newProjectDto);

            System.out.println("Creando proyecto en entidad: " + project.getName());
            System.out.println("Stages en entidad: " + project.getStages().stream().map(s -> s.getName()).collect(Collectors.toList()));

            // Preparar respuesta exitosa (sin Bonita por ahora)
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Proyecto creado exitosamente");
            response.put("project", ProyectDto.fromEntity(project));
            
            // Intentar integración con Bonita (opcional)
            try {
                
                // Obtener el ID del proceso "Gestion Proyecto"
                String processId = bonitaSvc.getProcessId("Gestion Proyecto");
                
                if (processId != null) {
                    // Preparar variables para el proceso
                    Map<String, Object> processVariables = new HashMap<>();

                    Map<String, Object> proyectoInput = new HashMap<>();
                    proyectoInput.put("id", project.getId());
                    proyectoInput.put("nombre", project.getName());
                    proyectoInput.put("descripcion", project.getDescription());

                    List<Map<String, Object>> etapasInput = new ArrayList<>();
                    if (project.getStages() != null) {
                        for (int i = 0; i < project.getStages().size(); i++) {
                            Stage stage = project.getStages().get(i);
                            Map<String, Object> etapa = new HashMap<>();
                            etapa.put("nro_orden", i + 1);
                            etapa.put("requiere_pedido", stage.getCovered());
                            etapa.put("desc_pedido", stage.getNeeds() != null ? stage.getNeeds() : "");
                            etapa.put("estado","PENDIENTE");
                            etapa.put("proyecto_id", project.getId());
                            etapasInput.add(etapa);
                        }
                    }
                    processVariables.put("proyectoInput", proyectoInput);
                    processVariables.put("etapasInput", etapasInput);

                    // Iniciar el proceso en Bonita
                    Map<String, String> processInstance = bonitaSvc.startProcessInstance(processId, processVariables);
                    
                    //Buscar tarea humana lista
                    List<Map<String,String>> humanTasks = bonitaSvc.getTasksByCaseId(processInstance.get("caseId"));

                    if(humanTasks != null && !humanTasks.isEmpty()) {
                        //Ejecutar la primera tarea humana
                        logger.debug("Tarea humana encontrada: {}", humanTasks.get(0));
                        bonitaSvc.assignUserTask(humanTasks.get(0).get("id"), "4");
                        bonitaSvc.executeTask(humanTasks.get(0).get("id"));
                    }else{
                        logger.error("No se encontraron tareas humanas para el caso ID: {}", processInstance.get("caseId"));
                    }

                    // Actualizar respuesta con información de Bonita
                    response.put("message", "Proyecto y proceso Bonita creados exitosamente");
                    response.put("bonita_process_id", processInstance.get("caseId"));
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
