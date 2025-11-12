package com.proyect_planning.proyect_planning_system.controllers;

import com.proyect_planning.proyect_planning_system.dto.*;
import com.proyect_planning.proyect_planning_system.entities.User;
import com.proyect_planning.proyect_planning_system.services.ObservacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/observaciones")
@CrossOrigin(origins = "*")
public class ObservacionController {

    @Autowired
    private ObservacionService observacionService;

    /**
     * Crear una nueva observación (solo DIRECTIVO o ADMIN)
     */
    @PostMapping
    @PreAuthorize("hasRole('DIRECTIVO') or hasRole('ADMIN')")
    public ResponseEntity<ObservacionResponse> createObservacion(
            @RequestBody CreateObservacionRequest request,
            @AuthenticationPrincipal User directivo) {
        try {
            ObservacionResponse response = observacionService.createObservacion(request, directivo.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtener todas las observaciones de un proyecto
     */
    @GetMapping("/proyecto/{proyectoId}")
    public ResponseEntity<List<ObservacionResponse>> getObservacionesByProyecto(@PathVariable Long proyectoId) {
        try {
            List<ObservacionResponse> observaciones = observacionService.getObservacionesByProyecto(proyectoId);
            return ResponseEntity.ok(observaciones);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtener todas las observaciones de una ONG (para que vea sus propias observaciones)
     */
    @GetMapping("/ong/{ongId}")
    public ResponseEntity<List<ObservacionResponse>> getObservacionesByOng(@PathVariable Long ongId) {
        try {
            List<ObservacionResponse> observaciones = observacionService.getObservacionesByOng(ongId);
            return ResponseEntity.ok(observaciones);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtener una observación por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ObservacionResponse> getObservacionById(@PathVariable Long id) {
        try {
            ObservacionResponse observacion = observacionService.getObservacionById(id);
            return ResponseEntity.ok(observacion);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * Crear una corrección para una observación (usuarios de la ONG del proyecto)
     */
    @PostMapping("/correcciones")
    public ResponseEntity<CorreccionResponse> createCorreccion(
            @RequestBody CreateCorreccionRequest request,
            @AuthenticationPrincipal User usuario) {
        try {
            CorreccionResponse response = observacionService.createCorreccion(request, usuario.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtener todas las correcciones de una observación
     */
    @GetMapping("/{observacionId}/correcciones")
    public ResponseEntity<List<CorreccionResponse>> getCorreccionesByObservacion(@PathVariable Long observacionId) {
        try {
            List<CorreccionResponse> correcciones = observacionService.getCorreccionesByObservacion(observacionId);
            return ResponseEntity.ok(correcciones);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Resolver una observación (aprobar corrección - solo DIRECTIVO o ADMIN)
     */
    @PutMapping("/{id}/resolver")
    @PreAuthorize("hasRole('DIRECTIVO') or hasRole('ADMIN')")
    public ResponseEntity<ObservacionResponse> resolverObservacion(@PathVariable Long id) {
        try {
            ObservacionResponse response = observacionService.resolverObservacion(id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Rechazar una corrección (solo DIRECTIVO o ADMIN)
     */
    @PutMapping("/{id}/rechazar")
    @PreAuthorize("hasRole('DIRECTIVO') or hasRole('ADMIN')")
    public ResponseEntity<ObservacionResponse> rechazarCorreccion(
            @PathVariable Long id,
            @RequestBody Map<String, String> payload) {
        try {
            String motivoRechazo = payload.getOrDefault("motivo", "No se especificó motivo");
            ObservacionResponse response = observacionService.rechazarCorreccion(id, motivoRechazo);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Verificar observaciones vencidas (puede ser llamado por un cron job o manualmente)
     */
    @PostMapping("/verificar-vencidas")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DIRECTIVO')")
    public ResponseEntity<Void> verificarObservacionesVencidas() {
        try {
            observacionService.verificarObservacionesVencidas();
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
