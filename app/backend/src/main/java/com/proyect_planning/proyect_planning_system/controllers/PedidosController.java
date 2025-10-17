package com.proyect_planning.proyect_planning_system.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.CrossOrigin;
import com.proyect_planning.proyect_planning_system.services.bonita.BonitaBusinessService;
import com.proyect_planning.proyect_planning_system.services.cloud.CloudService;
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
    public ResponseEntity<Map<String, Object>> getAllPedidos() throws Exception {
        Map<String, Object> response = new HashMap<>();

        // Lógica para obtener todos los pedidos
        List<PedidoCloudDTO> pedidos = null;

        try{
            pedidos = cloudService.getAllPedidos();
            response.put("status", "success");
            response.put("message", "Lista de pedidos obtenida exitosamente");
            response.put("pedidos", pedidos);
        }
        catch(Exception e){
            logger.error("Error al obtener los pedidos de cloud: {}", e.getMessage());
            response.put("status", "error");
            response.put("message", "Error al obtener los pedidos de cloud: " + e.getMessage());
            response.put("pedidos", List.of());
            throw e;
        }
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{pedidoId}/compromisos")
    @CrossOrigin(origins = "*")
    public ResponseEntity<Map<String, Object>> crearCompromiso(
        @PathVariable Long pedidoId, 
        @RequestBody NewCompromisoDto compromisoDto,
        @RequestBody Long proyectId
        ) {
        
        logger.debug("Creando compromiso para pedido ID: {}", pedidoId);
        logger.debug("Datos del compromiso: ONG ID: {}, Descripción: {}", 
                    compromisoDto.getOngColaboranteId(), compromisoDto.getDescripcion());
        
        try {
            // Preparar respuesta exitosa inicial
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Compromiso procesado exitosamente");
            response.put("pedidoId", pedidoId);
            response.put("ongColaboranteId", compromisoDto.getOngColaboranteId());
            
            try {
                // Esto requiere una relación entre pedidos del cloud y proyectos locales
                String bonitaCaseId = obtenerBonitaCaseIdDelPedido(proyectId);
                
                if (bonitaCaseId != null) {
                    // Llamar al método corregido con bonitaCaseId
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

    // Método helper para obtener el bonitaCaseId
    private String obtenerBonitaCaseIdDelPedido(Long proyectId) {
        Proyect proyect = proyectService.getProyectById(proyectId);
        if (proyect != null) {
            return proyect.getBonitaCaseId();
        }
        return null;
    }
}