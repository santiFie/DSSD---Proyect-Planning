package com.proyect_planning.proyect_planning_system.services.bonita;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyect_planning.proyect_planning_system.config.BonitaConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BonitaApiService {
    
    private final BonitaConfig bonitaConfig;
    private final BonitaAuthService authService;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    @Autowired
    public BonitaApiService(BonitaConfig bonitaConfig, BonitaAuthService authService) {
        this.bonitaConfig = bonitaConfig;
        this.authService = authService;
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * Obtiene la lista de procesos disponibles en Bonita
     */
    public List<Map<String, Object>> getProcesses() {
        try {
            HttpHeaders headers = authService.createAuthenticatedHeaders();
            HttpEntity<String> requestEntity = new HttpEntity<>(headers);
            
            // Agregar parámetros para obtener la lista completa de procesos
            String url = bonitaConfig.getApiUrl() + "/bpm/process?c=100&p=0";
            
            ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                requestEntity,
                String.class
            );
            
            // Convertir respuesta JSON a lista
            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            return objectMapper.convertValue(jsonNode, List.class);
            
        } catch (Exception e) {
            System.err.println("Error obteniendo procesos de Bonita: " + e.getMessage());
            throw new RuntimeException("Error obteniendo procesos de Bonita", e);
        }
    }

    /**
     * Obtiene el id de un proceso por su nombre
     */
    public String getProcessId(String processName) {
        List<Map<String, Object>> processes = getProcesses();
        for (Map<String, Object> process : processes) {
            if (processName.equals(process.get("name"))) {
                return process.get("id").toString();
            }
        }
        throw new RuntimeException("Proceso no encontrado: " + processName);
    }
    
    /**
     * Inicia una nueva instancia de proceso en Bonita
     */
    public Map<String, Object> startProcessInstance(String processId, Map<String, Object> variables) {
        try {
            HttpHeaders headers = authService.createAuthenticatedHeaders();
            
            // Preparar el payload para iniciar el proceso
            Map<String, Object> payload = new HashMap<>();
            if (variables != null && !variables.isEmpty()) {
                payload.put("variables", variables);
            }
            
            String jsonPayload = objectMapper.writeValueAsString(payload);
            HttpEntity<String> requestEntity = new HttpEntity<>(jsonPayload, headers);
            
            String url = bonitaConfig.getApiUrl() + "/bpm/process/" + processId + "/instantiation";
            
            ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                requestEntity,
                String.class
            );
            
            // Convertir respuesta a Map
            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            Map<String, Object> result = objectMapper.convertValue(jsonNode, Map.class);
            
            System.out.println("Proceso iniciado exitosamente. Instance ID: " + result.get("id"));
            return result;
            
        } catch (Exception e) {
            System.err.println("Error iniciando proceso en Bonita: " + e.getMessage());
            throw new RuntimeException("Error iniciando proceso en Bonita", e);
        }
    }
    
    /**
     * Obtiene las instancias de proceso en ejecución
     */
    public List<Map<String, Object>> getProcessInstances() {
        try {
            HttpHeaders headers = authService.createAuthenticatedHeaders();
            HttpEntity<String> requestEntity = new HttpEntity<>(headers);
            
            String url = bonitaConfig.getApiUrl() + "/bpm/case?c=100&p=0";
            
            ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                requestEntity,
                String.class
            );
            
            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            return objectMapper.convertValue(jsonNode, List.class);
            
        } catch (Exception e) {
            System.err.println("Error obteniendo instancias de proceso: " + e.getMessage());
            throw new RuntimeException("Error obteniendo instancias de proceso", e);
        }
    }
    
    /**
     * Obtiene las tareas pendientes para un usuario
     */
    public List<Map<String, Object>> getPendingTasks(String userId) {
        try {
            HttpHeaders headers = authService.createAuthenticatedHeaders();
            HttpEntity<String> requestEntity = new HttpEntity<>(headers);
            
            String url = bonitaConfig.getApiUrl() + "/bpm/humanTask?assigned_id=" + userId + "&c=100&p=0";
            
            ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                requestEntity,
                String.class
            );
            
            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            return objectMapper.convertValue(jsonNode, List.class);
            
        } catch (Exception e) {
            System.err.println("Error obteniendo tareas pendientes: " + e.getMessage());
            throw new RuntimeException("Error obteniendo tareas pendientes", e);
        }
    }
    
    /**
     * Ejecuta una tarea humana
     */
    public void executeTask(String taskId, Map<String, Object> taskData) {
        try {
            HttpHeaders headers = authService.createAuthenticatedHeaders();
            
            String jsonPayload = objectMapper.writeValueAsString(taskData);
            HttpEntity<String> requestEntity = new HttpEntity<>(jsonPayload, headers);
            
            String url = bonitaConfig.getApiUrl() + "/bpm/userTask/" + taskId + "/execution";
            
            restTemplate.exchange(
                url,
                HttpMethod.POST,
                requestEntity,
                String.class
            );
            
            System.out.println("Tarea ejecutada exitosamente. Task ID: " + taskId);
            
        } catch (Exception e) {
            System.err.println("Error ejecutando tarea: " + e.getMessage());
            throw new RuntimeException("Error ejecutando tarea", e);
        }
    }
}