package com.proyect_planning.proyect_planning_system.services.bonita;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyect_planning.proyect_planning_system.config.BonitaConfig;
import com.proyect_planning.proyect_planning_system.entities.Stage;

import com.proyect_planning.proyect_planning_system.services.bonita.exception.BonitaException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class BonitaApiService {
    private final Logger logger = LoggerFactory.getLogger(BonitaApiService.class);
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
            return objectMapper.convertValue(jsonNode, new TypeReference<List<Map<String, Object>>>() {});
            
        } catch (Exception e) {
            logger.error("Error obteniendo procesos de Bonita", e);
            throw new RuntimeException("Error obteniendo procesos de Bonita", e);
        }
    }

    /**
     * Obtiene el id de un proceso por su nombre
     * @throws BonitaException
     */
    public String getProcessId(String processName) throws BonitaException {
        try {
            HttpHeaders headers = authService.createAuthenticatedHeaders();
            HttpEntity<String> requestEntity = new HttpEntity<>(headers);

            // Agregar parámetros para obtener la lista completa de procesos
            String url = bonitaConfig.getApiUrl() + "/bpm/process?p=0&c=100&f=name=" + processName;

            ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                requestEntity,
                String.class
            );

            // Convertir respuesta JSON a lista
            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            List<Map<String, Object>> processes = objectMapper.convertValue(jsonNode, new TypeReference<List<Map<String, Object>>>() {});
            if (processes.isEmpty()) {
                throw new BonitaException("No se encontró el proceso con nombre: " + processName);
            }else {
                return processes.get(0).get("id").toString();
            }
        } catch (Exception e) {
            logger.error("Error buscando procesos por nombre. processName: {}", processName, e);
            throw new BonitaException("Error buscando procesos por nombre. processName: " + processName, e);
        }
    }
    
    /**
     * Inicia una nueva instancia de proceso en Bonita
     * @throws BonitaException
     */
    public Map<String, String> startProcessInstance(String processId, Map<String, Object> variables) throws BonitaException {
        try {
            HttpHeaders headers = authService.createAuthenticatedHeaders();
            String jsonPayload = objectMapper.writeValueAsString(variables);
            logger.debug("Iniciando proceso. processId: {}, variables: {}", processId, jsonPayload);
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
            Map<String, String> result = objectMapper.convertValue(jsonNode, new TypeReference<Map<String, String>>() {});
            
            logger.info("Proceso iniciado exitosamente. Case ID {}", result.get("caseId"));

            return result;
            
        } catch (Exception e) {
            logger.error("Error iniciando proceso en Bonita. processId: {}, variables: {}", processId, variables, e);
            throw new BonitaException("Error iniciando proceso en Bonita. processId: " + processId, e);
        }
    }
    
    /**
     * Obtiene las instancias de proceso en ejecución
     * @throws BonitaException
     */
    public List<Map<String, Object>> getProcessInstances() throws BonitaException {
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
            return objectMapper.convertValue(jsonNode, new TypeReference<List<Map<String, Object>>>() {});
            
        } catch (Exception e) {
            logger.error("Error obteniendo instancias de proceso", e);
            throw new BonitaException("Error obteniendo instancias de proceso", e);
        }
    }
    
    /**
     * Obtiene las tareas pendientes para un usuario
     * @throws BonitaException
     */
    public List<Map<String, Object>> getPendingTasksByUserId(String userId) throws BonitaException {
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
            return objectMapper.convertValue(jsonNode, new TypeReference<List<Map<String, Object>>>() {});
            
        } catch (Exception e) {
            logger.error("Error obteniendo tareas pendientes. userId: {}", userId, e);
            throw new BonitaException("Error obteniendo tareas pendientes. userId: " + userId, e);
        }
    }
    
    /**
     * Ejecuta una tarea humana
     *
     * @throws BonitaException
     */
    public void executeTask(String taskId) throws BonitaException {
        try {
            HttpHeaders headers = authService.createAuthenticatedHeaders();
            HttpEntity<String> requestEntity = new HttpEntity<>(headers);
            
            String url = bonitaConfig.getApiUrl() + "/bpm/userTask/" + taskId + "/execution";
            
            restTemplate.exchange(
                url,
                HttpMethod.POST,
                requestEntity,
                String.class
            );
            
            logger.info("Tarea ejecutada exitosamente. Task ID: {}", taskId);

        } catch (Exception e) {
            logger.error("Error ejecutando tarea. taskId: {}", taskId, e);
            throw new BonitaException("Error ejecutando tarea. taskId: " + taskId, e);
        }
    }

    /**
     * Asigna una tarea a un usuario específico
     * @throws BonitaException
     */
    public String assignUserTask(String taskId, String userId) throws BonitaException {
        try {
            HttpHeaders headers = authService.createAuthenticatedHeaders();
            Map<String, Object> taskData = new HashMap<>();
            taskData.put("assigned_id", userId);
            String jsonPayload = objectMapper.writeValueAsString(taskData);

            HttpEntity<String> requestEntity = new HttpEntity<>(jsonPayload, headers);

            String url = bonitaConfig.getApiUrl() + "/bpm/humanTask/" + taskId;

            ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.PUT,
                requestEntity,
                String.class
            );

            return response.getBody();

        } catch (Exception e) {
            logger.error("Error asignando tarea a usuario. taskId: {}, userId: {}", taskId, userId, e);
            throw new BonitaException("Error asignando tarea a usuario. taskId: " + taskId + ", userId: " + userId, e);
        }
    }

    /**
     * Obtiene las tareas en estado listo, para un caso específico
     *
     * @throws BonitaException
     */
    public List<Map<String, String>> getTasksByCaseId(String caseId) throws BonitaException {
        try {
            HttpHeaders headers = authService.createAuthenticatedHeaders();
            HttpEntity<String> requestEntity = new HttpEntity<>(headers);

            String url = bonitaConfig.getApiUrl() + "/bpm/humanTask?f=caseId=" + caseId + "&f=state=ready";

            ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                requestEntity,
                String.class
            );

            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            return objectMapper.convertValue(jsonNode, new TypeReference<List<Map<String, String>>>() {});

        } catch (Exception e) {
            logger.error("Error obteniendo tareas en estado listo. caseId: {}", caseId, e);
            throw new BonitaException("Error obteniendo tareas en estado listo. caseId: " + caseId, e);
        }
    }

}