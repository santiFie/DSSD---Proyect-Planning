package com.proyect_planning.proyect_planning_system.controllers;

import java.util.ArrayList;
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
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyect_planning.proyect_planning_system.services.bonita.BonitaBusinessService;
import com.proyect_planning.proyect_planning_system.services.cloud.CloudService;
import com.proyect_planning.proyect_planning_system.services.cloud.dto.CompromisoCloudDTO;
import com.proyect_planning.proyect_planning_system.services.cloud.dto.PedidoCloudDTO;
import com.proyect_planning.proyect_planning_system.dtos.NewCompromisoDto;
import com.proyect_planning.proyect_planning_system.entities.Proyect;
import com.proyect_planning.proyect_planning_system.services.ProyectService;

@RestController
@RequestMapping("/api/pedidos")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class PedidosController {

    private final Logger logger = LoggerFactory.getLogger(PedidosController.class);

    @Autowired
    private CloudService cloudService;
    
    @Autowired
    private BonitaBusinessService bonitaBusinessSvc;

    @Autowired
    private ProyectService proyectService;

    @GetMapping("/all")
    public ResponseEntity<?> getAllPedidos() {
        try {
            // Primero intentar obtener pedidos desde Bonita
            List<Map<String, Object>> pedidosFromBonita = bonitaBusinessSvc.getAllPedidosFromBonita();
            
            if (pedidosFromBonita != null && !pedidosFromBonita.isEmpty()) {
                // Aplanar la lista de pedidos parseando el JSON
                List<Map<String, Object>> allPedidos = new ArrayList<>();
                ObjectMapper mapper = new ObjectMapper();
                
                for (Map<String, Object> pedidoInfo : pedidosFromBonita) {
                    String pedidosJson = (String) pedidoInfo.get("pedidos");
                    
                    if (pedidosJson != null && !pedidosJson.isEmpty()) {
                        List<Map<String, Object>> pedidosList = mapper.readValue(
                            pedidosJson, 
                            new TypeReference<List<Map<String, Object>>>() {}
                        );
                        allPedidos.addAll(pedidosList);
                    }
                }                
                return ResponseEntity.ok().body(allPedidos);
            }
            
            // Si no hay pedidos en Bonita, los obtiene desde Cloud
            logger.warn("No se encontraron pedidos en Bonita, obteniendo desde Cloud...");
            List<PedidoCloudDTO> pedidos = cloudService.getAllPedidos();
            return ResponseEntity.ok().body(pedidos);
            
        } catch (Exception e) {
            logger.error("Error al obtener los pedidos: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Error al obtener los pedidos: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getPedidoById(@PathVariable Long id) {
        try {
            PedidoCloudDTO pedido = cloudService.getPedidoById(id);
            return ResponseEntity.ok().body(pedido);
        } catch (Exception e) {
            logger.error("Error al obtener el pedido {}: {}", id, e.getMessage());
            return ResponseEntity.status(500).body("Error al obtener el pedido: " + e.getMessage());
        }
    }

    @GetMapping("/proyecto/{proyectoId}")
    public ResponseEntity<?> getPedidosByProyecto(@PathVariable Long proyectoId) {
        try {
            List<PedidoCloudDTO> allPedidos = cloudService.getAllPedidos();
            // Filtrar pedidos por proyectoId
            List<PedidoCloudDTO> pedidosProyecto = allPedidos.stream()
                .filter(pedido -> pedido.getProyectoId() != null && pedido.getProyectoId().equals(proyectoId))
                .toList();
            return ResponseEntity.ok().body(pedidosProyecto);
        } catch (Exception e) {
            logger.error("Error al obtener los pedidos del proyecto {}: {}", proyectoId, e.getMessage());
            return ResponseEntity.status(500).body("Error al obtener los pedidos del proyecto: " + e.getMessage());
        }
    }

    private List<CompromisoCloudDTO> getCompromisosByPedidoIdFromCloud(Long pedidoId) {
        try {
            List<CompromisoCloudDTO> compromisos = cloudService.getCompromisosByPedidoId(pedidoId);
            return compromisos;
        } catch (Exception e) {
            logger.error("Error al obtener los compromisos del pedido {}: {}", pedidoId, e.getMessage());
            return new ArrayList<>();
        }
    }

    @GetMapping("/{pedidoId}/compromisos")
    public ResponseEntity<?> getCompromisosByPedidoId(@PathVariable Long pedidoId){
        try {
            // Primero intentar obtener compromisos desde Bonita
            List<Map<String, Object>> compromisosFromBonita = bonitaBusinessSvc.getAllCompromisos();
            
            if (compromisosFromBonita != null && !compromisosFromBonita.isEmpty()) {
                // Aplanar la lista de compromisos parseando el JSON
                List<Map<String, Object>> allCompromisos = new ArrayList<>();
                ObjectMapper mapper = new ObjectMapper();
                
                for (Map<String, Object> compromisoInfo : compromisosFromBonita) {
                    String compromisosJson = (String) compromisoInfo.get("compromisos");
                    
                    if (compromisosJson != null && !compromisosJson.isEmpty()) {
                        List<Map<String, Object>> compromisosList = mapper.readValue(
                            compromisosJson, 
                            new TypeReference<List<Map<String, Object>>>() {}
                        );
                        
                        // Filtrar compromisos por pedidoId
                        for (Map<String, Object> compromiso : compromisosList) {
                            // Primero intentar con pedidoId directo
                            Object pedidoIdObj = compromiso.get("pedidoId");
                            
                            if (pedidoIdObj != null && pedidoIdObj.toString().equals(pedidoId.toString())) {
                                allCompromisos.add(compromiso);
                                continue;
                            }
                            
                            // Si no existe, intentar con objeto pedido anidado
                            Object pedidoObj = compromiso.get("pedido");
                            if (pedidoObj instanceof Map) {
                                Object idObj = ((Map<?, ?>) pedidoObj).get("id");
                                if (idObj != null && idObj.toString().equals(pedidoId.toString())) {
                                    allCompromisos.add(compromiso);
                                }
                            }
                        }
                    }
                }                
                
                return ResponseEntity.ok().body(allCompromisos);
            }
            
            // Si no hay pedidos en Bonita, los obtiene desde Cloud
            logger.warn("No se encontraron pedidos en Bonita, obteniendo desde Cloud...");
            List<CompromisoCloudDTO> compromisoCloudDTOs = this.getCompromisosByPedidoIdFromCloud(pedidoId);
            return ResponseEntity.ok().body(compromisoCloudDTOs);            
        } catch (Exception e) {
            logger.error("Error al obtener los pedidos: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Error al obtener los pedidos: " + e.getMessage());
        }
    }

    @PutMapping("/{pedidoId}/compromisos/{compromisoId}/aceptar/{proyectoId}")
    public ResponseEntity<?> aceptarCompromiso(
            @PathVariable Long pedidoId, 
            @PathVariable Long compromisoId,
            @PathVariable Long proyectoId) {
        try {
            // Preparar respuesta exitosa inicial
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Compromiso aceptado exitosamente");

            try {
                // Obtener el bonitaCaseId del proyecto
                String bonitaCaseId = obtenerBonitaCaseIdDelPedido(proyectoId);
                
                if (bonitaCaseId != null) {
                    // Llamar al proceso de Bonita para analizar el compromiso (aceptado)
                    // Bonita se encargará de actualizar el estado en el Cloud
                    bonitaBusinessSvc.analizarCompromiso(
                        bonitaCaseId,
                        true, // aceptado = true
                        compromisoId
                    );
                    
                    response.put("message", "Compromiso aceptado y proceso Bonita avanzado exitosamente");
                    response.put("bonita_case_id", bonitaCaseId);
                    response.put("bonita_enabled", true);
                } else {
                    response.put("message", "Compromiso aceptado (no se encontró caso Bonita asociado)");
                    response.put("bonita_enabled", false);
                }
            } catch (Exception bonitaError) {
                logger.error("Error en Bonita al analizar compromiso: {}", bonitaError.getMessage(), bonitaError);
                
                // Si Bonita falla, seguir con el proceso pero indicarlo
                response.put("message", "Compromiso aceptado (Bonita no disponible)");
                response.put("bonita_enabled", false);
                response.put("bonita_error", bonitaError.getMessage());
            }
            
            return ResponseEntity.ok().body(response);
        } catch (Exception e) {
            logger.error("Error al aceptar el compromiso {}: {}", compromisoId, e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Error al aceptar el compromiso: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @PutMapping("/{pedidoId}/compromisos/{compromisoId}/rechazar/{proyectoId}")
    public ResponseEntity<?> rechazarCompromiso(
            @PathVariable Long pedidoId, 
            @PathVariable Long compromisoId,
            @PathVariable Long proyectoId) {
        try {
            // Preparar respuesta exitosa inicial
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Compromiso rechazado exitosamente");

            try {
                // Obtener el bonitaCaseId del proyecto
                String bonitaCaseId = obtenerBonitaCaseIdDelPedido(proyectoId);
                
                if (bonitaCaseId != null) {
                    // Llamar al proceso de Bonita para analizar el compromiso (rechazado)
                    // Bonita después se encargs de actualizar el estado en el Cloud
                    bonitaBusinessSvc.analizarCompromiso(
                        bonitaCaseId,
                        false, // aceptado = false
                        compromisoId
                    );
                    
                    response.put("message", "Compromiso rechazado y proceso Bonita avanzado exitosamente");
                    response.put("bonita_case_id", bonitaCaseId);
                    response.put("bonita_enabled", true);
                } else {
                    response.put("message", "Compromiso rechazado (no se encontró caso Bonita asociado)");
                    response.put("bonita_enabled", false);
                }
            } catch (Exception bonitaError) {
                logger.error("Error en Bonita al analizar compromiso: {}", bonitaError.getMessage(), bonitaError);
                
                // Si Bonita falla, seguir con el proceso pero indicarlo
                response.put("message", "Compromiso rechazado (Bonita no disponible)");
                response.put("bonita_enabled", false);
                response.put("bonita_error", bonitaError.getMessage());
            }
            
            return ResponseEntity.ok().body(response);
        } catch (Exception e) {
            logger.error("Error al rechazar el compromiso {}: {}", compromisoId, e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Error al rechazar el compromiso: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @PostMapping("/{pedidoId}/compromisos")
    public ResponseEntity<Map<String, Object>> crearCompromiso(
        @PathVariable Long pedidoId, 
        @RequestBody NewCompromisoDto compromisoDto
        ) {
        
        logger.debug("Creando compromiso para pedido ID: {}", pedidoId);
        logger.debug("Datos del compromiso: ONG ID: {}, Descripción: {}, ProyectId: {}", 
        compromisoDto.getOngColaboranteId(), 
        compromisoDto.getDescripcion(),
        compromisoDto.getPedido().getProyectoId());
    
        
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Compromiso procesado exitosamente");
            response.put("pedidoId", pedidoId);
            response.put("ongColaboranteId", compromisoDto.getOngColaboranteId());
            
            try {
                String bonitaCaseId = obtenerBonitaCaseIdDelPedido(compromisoDto.getPedido().getProyectoId());
                
                if (bonitaCaseId != null) {
                    bonitaBusinessSvc.comprometerAyuda(
                        bonitaCaseId,  // ID del caso en Bonita
                        pedidoId.toString(), 
                        compromisoDto.getOngColaboranteId(), 
                        compromisoDto.getDescripcion()
                    );
                    
                    // Actualizar respuesta con información de Bonita
                    response.put("message", "Compromiso creado y tarea Bonita avanzada exitosamente");
                    response.put("bonita_task", "Comprometer Ayuda para la Etapa");
                    response.put("bonita_case_id", bonitaCaseId);
                    response.put("bonita_enabled", true);
                } else {
                    response.put("message", "Compromiso procesado (no se encontró caso Bonita asociado)");
                    response.put("bonita_enabled", false);
                }
            } catch (Exception bonitaError) {
                logger.error("Error en Bonita al comprometer ayuda: {}", bonitaError.getMessage(), bonitaError);
                
                // Si Bonita falla, seguir con el proceso pero indicarlo
                response.put("message", "Compromiso registrado (Bonita no disponible)");
                response.put("bonita_enabled", false);
                response.put("bonita_error", bonitaError.getMessage());
            }
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException iae) {
            logger.warn("Datos de entrada inválidos: {}", iae.getMessage());
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Datos inválidos: " + iae.getMessage());
            
            return ResponseEntity.badRequest().body(errorResponse);
            
        } catch (Exception e) {
            logger.error("Error creando compromiso: {}", e.getMessage(), e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Error al procesar compromiso: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    private String obtenerBonitaCaseIdDelPedido(Long proyectId) {
        Proyect proyect = proyectService.getProyectById(proyectId);
        if (proyect != null) {
            return proyect.getBonitaCaseId();
        }
        return null;
    }
}