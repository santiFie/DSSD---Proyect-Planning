package com.proyect_planning.proyect_planning_system.controllers;

import com.proyect_planning.proyect_planning_system.services.bonita.BonitaApiService;
import com.proyect_planning.proyect_planning_system.services.bonita.BonitaAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bonita")
@CrossOrigin(origins = "http://localhost:4200")
public class BonitaController {
    
    private final BonitaAuthService authService;
    private final BonitaApiService apiService;
    
    @Autowired
    public BonitaController(BonitaAuthService authService, BonitaApiService apiService) {
        this.authService = authService;
        this.apiService = apiService;
    }
    
    /**
     * Endpoint para realizar login en Bonita
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login() {
        try {
            String token = authService.login();
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Login exitoso en Bonita",
                "token", token
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "status", "error",
                "message", "Error en login: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Endpoint para verificar el estado de autenticación
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getAuthStatus() {
        return ResponseEntity.ok(Map.of(
            "authenticated", authService.isAuthenticated(),
            "token", authService.getSessionToken() != null ? "***" : null
        ));
    }
    
    /**
     * Endpoint para obtener los procesos disponibles
     */
    @GetMapping("/processes")
    public ResponseEntity<?> getProcesses() {
        try {
            List<Map<String, Object>> processes = apiService.getProcesses();
            return ResponseEntity.ok(processes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "status", "error",
                "message", "Error obteniendo procesos: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Endpoint para iniciar una instancia de proceso
     */
    @PostMapping("/processes/{processId}/start")
    public ResponseEntity<?> startProcess(
            @PathVariable String processId,
            @RequestBody(required = false) Map<String, Object> variables) {
        try {
            Map<String, String> instance = apiService.startProcessInstance(processId, variables);
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Proceso iniciado exitosamente",
                "instance", instance
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "status", "error",
                "message", "Error iniciando proceso: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Endpoint para obtener las instancias de proceso
     */
    @GetMapping("/instances")
    public ResponseEntity<?> getProcessInstances() {
        try {
            List<Map<String, Object>> instances = apiService.getProcessInstances();
            return ResponseEntity.ok(instances);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "status", "error",
                "message", "Error obteniendo instancias: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Endpoint para obtener tareas pendientes
     */
    @GetMapping("/tasks")
    public ResponseEntity<?> getPendingTasks(@RequestParam(defaultValue = "1") String userId) {
        try {
            List<Map<String, Object>> tasks = apiService.getPendingTasksByUserId(userId);
            return ResponseEntity.ok(tasks);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "status", "error",
                "message", "Error obteniendo tareas: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Endpoint para ejecutar una tarea
     */
    @PostMapping("/tasks/{taskId}/execute")
    public ResponseEntity<?> executeTask(@PathVariable String taskId) {
        try {
            apiService.executeTask(taskId);
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Tarea ejecutada exitosamente"
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "status", "error",
                "message", "Error ejecutando tarea: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Endpoint para realizar logout
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout() {
        try {
            authService.logout();
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Logout exitoso de Bonita"
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Logout completado"
            ));
        }
    }
}