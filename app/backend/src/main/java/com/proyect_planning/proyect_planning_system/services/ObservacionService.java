package com.proyect_planning.proyect_planning_system.services;

import com.proyect_planning.proyect_planning_system.dto.*;
import com.proyect_planning.proyect_planning_system.entities.*;
import com.proyect_planning.proyect_planning_system.repositories.*;
import com.proyect_planning.proyect_planning_system.services.bonita.BonitaBusinessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ObservacionService {

    private static final Logger logger = LoggerFactory.getLogger(ObservacionService.class);

    @Autowired
    private ObservacionRepository observacionRepository;

    @Autowired
    private CorreccionRepository correccionRepository;

    @Autowired
    private ProyectRepository proyectRepository;

    @Autowired
    private OngRepository ongRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BonitaBusinessService bonitaBusinessService;

    /**
     * Crea una nueva observación y la registra en Bonita
     */
    @Transactional
    public ObservacionResponse createObservacion(CreateObservacionRequest request, Long directivoId) {
        // Validar que el proyecto existe
        Proyect proyecto = proyectRepository.findById(request.getProyectoId())
                .orElseThrow(() -> new RuntimeException("Proyecto no encontrado"));

        // Validar que la ONG existe
        Ong ong = ongRepository.findById(request.getOngId())
                .orElseThrow(() -> new RuntimeException("ONG no encontrada"));

        // Crear la observación
        Observacion observacion = Observacion.builder()
                .descripcion(request.getDescripcion())
                .estado(EstadoObservacion.PENDIENTE)
                .proyecto(proyecto)
                .ong(ong)
                .build();

        observacion = observacionRepository.save(observacion);

        // Registrar en Bonita
        try {
            String bonitaCaseId = bonitaBusinessService.registrarObservacionEnBonita(observacion);
            observacion.setBonitaCaseId(bonitaCaseId);
            observacion = observacionRepository.save(observacion);
        } catch (Exception e) {
            logger.error("Error al registrar observación en Bonita: {}", e.getMessage(), e);
            // Continuamos aunque falle Bonita
        }

        return toObservacionResponse(observacion);
    }

    /**
     * Obtiene todas las observaciones de un proyecto
     */
    public List<ObservacionResponse> getObservacionesByProyecto(Long proyectoId) {
        List<Observacion> observaciones = observacionRepository.findByProyectoId(proyectoId);
        return observaciones.stream()
                .map(this::toObservacionResponse)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene todas las observaciones de una ONG
     */
    public List<ObservacionResponse> getObservacionesByOng(Long ongId) {
        List<Observacion> observaciones = observacionRepository.findByOngId(ongId);
        return observaciones.stream()
                .map(this::toObservacionResponse)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene una observación por ID
     */
    public ObservacionResponse getObservacionById(Long id) {
        Observacion observacion = observacionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Observación no encontrada"));
        return toObservacionResponse(observacion);
    }

    /**
     * Crea una corrección para una observación
     */
    @Transactional
    public CorreccionResponse createCorreccion(CreateCorreccionRequest request, Long usuarioId) {
        Observacion observacion = observacionRepository.findById(request.getObservacionId())
                .orElseThrow(() -> new RuntimeException("Observación no encontrada"));

        User usuario = userRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Correccion correccion = Correccion.builder()
                .observacion(observacion)
                .detalle(request.getDetalle())
                .usuario(usuario)
                .build();

        correccion = correccionRepository.save(correccion);

        // Actualizar el estado de la observación a EN_REVISION
        if (observacion.getEstado() == EstadoObservacion.PENDIENTE) {
            observacion.setEstado(EstadoObservacion.EN_REVISION);
            observacionRepository.save(observacion);
        }

        // Notificar a Bonita sobre la corrección
        try {
            bonitaBusinessService.notificarCorreccionEnBonita(observacion, correccion);
        } catch (Exception e) {
            logger.error("Error al notificar corrección en Bonita: {}", e.getMessage(), e);
        }

        return toCorreccionResponse(correccion);
    }

    /**
     * Obtiene todas las correcciones de una observación
     */
    public List<CorreccionResponse> getCorreccionesByObservacion(Long observacionId) {
        List<Correccion> correcciones = correccionRepository.findByObservacionId(observacionId);
        return correcciones.stream()
                .map(this::toCorreccionResponse)
                .collect(Collectors.toList());
    }

    /**
     * Resuelve una observación (aprueba la corrección)
     */
    @Transactional
    public ObservacionResponse resolverObservacion(Long id) {
        Observacion observacion = observacionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Observación no encontrada"));

        observacion.setEstado(EstadoObservacion.RESUELTA);
        observacion.setFechaResolucion(LocalDateTime.now());
        observacion = observacionRepository.save(observacion);

        // Notificar a Bonita sobre la aprobación (estado RESUELTA)
        try {
            bonitaBusinessService.actualizarEstadoObservacionEnBonita(observacion);
        } catch (Exception e) {
            logger.error("Error al actualizar estado en Bonita: {}", e.getMessage(), e);
        }

        return toObservacionResponse(observacion);
    }

    /**
     * Rechaza una corrección y establece la observación en estado RECHAZADA con un comentario
     */
    @Transactional
    public ObservacionResponse rechazarCorreccion(Long id, String motivoRechazo) {
        Observacion observacion = observacionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Observación no encontrada"));

        // Agregar un comentario/corrección del directivo indicando el motivo del rechazo
        User sistema = userRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("Usuario sistema no encontrado"));
        
        Correccion comentarioRechazo = Correccion.builder()
                .observacion(observacion)
                .detalle("❌ CORRECCIÓN RECHAZADA - Motivo: " + motivoRechazo)
                .usuario(sistema)
                .build();
        
        correccionRepository.save(comentarioRechazo);

        // Establecer estado RECHAZADA (no PENDIENTE)
        observacion.setEstado(EstadoObservacion.RECHAZADA);
        observacion = observacionRepository.save(observacion);

        // Notificar a Bonita sobre el rechazo (estado RECHAZADA != RESUELTA)
        try {
            bonitaBusinessService.actualizarEstadoObservacionEnBonita(observacion);
        } catch (Exception e) {
            logger.error("Error al actualizar estado en Bonita: {}", e.getMessage(), e);
        }

        return toObservacionResponse(observacion);
    }

    /**
     * Verifica y marca como vencidas las observaciones que superaron el plazo
     */
    @Transactional
    public void verificarObservacionesVencidas() {
        List<Observacion> observacionesPendientes = observacionRepository.findByEstado(EstadoObservacion.PENDIENTE);
        observacionesPendientes.addAll(observacionRepository.findByEstado(EstadoObservacion.EN_REVISION));

        LocalDateTime ahora = LocalDateTime.now();
        for (Observacion obs : observacionesPendientes) {
            if (obs.getFechaLimite() != null && obs.getFechaLimite().isBefore(ahora)) {
                obs.setEstado(EstadoObservacion.VENCIDA);
                observacionRepository.save(obs);

                // Notificar a Bonita sobre el vencimiento
                try {
                    this.bonitaBusinessService.notificarVencimientoEnBonita(obs);
                } catch (Exception e) {
                    logger.error("Error al notificar vencimiento en Bonita: {}", e.getMessage(), e);
                }
            }
        }
    }

    // ========== Métodos de conversión ==========

    private ObservacionResponse toObservacionResponse(Observacion observacion) {
        List<CorreccionResponse> correcciones = correccionRepository
                .findByObservacionId(observacion.getId())
                .stream()
                .map(this::toCorreccionResponse)
                .collect(Collectors.toList());

        return ObservacionResponse.builder()
                .id(observacion.getId())
                .descripcion(observacion.getDescripcion())
                .estado(observacion.getEstado())
                .proyectoId(observacion.getProyecto().getId())
                .proyectoNombre(observacion.getProyecto().getName())
                .ongId(observacion.getOng().getId())
                .ongNombre(observacion.getOng().getName())
                .bonitaCaseId(observacion.getBonitaCaseId())
                .fechaCreacion(observacion.getFechaCreacion())
                .fechaLimite(observacion.getFechaLimite())
                .fechaResolucion(observacion.getFechaResolucion())
                .correcciones(correcciones)
                .build();
    }

    private CorreccionResponse toCorreccionResponse(Correccion correccion) {
        return CorreccionResponse.builder()
                .id(correccion.getId())
                .observacionId(correccion.getObservacion().getId())
                .detalle(correccion.getDetalle())
                .usuarioId(correccion.getUsuario().getId())
                .usuarioNombre(correccion.getUsuario().getUsername())
                .fechaCreacion(correccion.getFechaCreacion())
                .build();
    }

}
