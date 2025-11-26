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

    @Autowired
    private EmailService emailService;

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
        }

        // Enviar email a los usuarios de la organización del proyecto
        try {
            enviarEmailNuevaObservacion(observacion);
        } catch (Exception e) {
            logger.error("Error al enviar email de notificación: {}", e.getMessage(), e);
        }

        return toObservacionResponse(observacion);
    }


    public List<ObservacionResponse> getObservacionesByProyecto(Long proyectoId) {
        List<Observacion> observaciones = observacionRepository.findByProyectoId(proyectoId);
        return observaciones.stream()
                .map(this::toObservacionResponse)
                .collect(Collectors.toList());
    }


    public List<ObservacionResponse> getObservacionesByOng(Long ongId) {
        List<Observacion> observaciones = observacionRepository.findByOngId(ongId);
        return observaciones.stream()
                .map(this::toObservacionResponse)
                .collect(Collectors.toList());
    }


    public ObservacionResponse getObservacionById(Long id) {
        Observacion observacion = observacionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Observación no encontrada"));
        return toObservacionResponse(observacion);
    }


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
                .detalle("CORRECCIÓN RECHAZADA - Motivo: " + motivoRechazo)
                .usuario(sistema)
                .build();
        
        correccionRepository.save(comentarioRechazo);

        // Establecer estado RECHAZADA
        observacion.setEstado(EstadoObservacion.RECHAZADA);
        observacion = observacionRepository.save(observacion);

        // Notificar a Bonita sobre el rechazo (estado RECHAZADA)
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

    /**
     * Envía un email de notificación a los usuarios de la organización del proyecto
     */
    private void enviarEmailNuevaObservacion(Observacion observacion) {
        // Obtener la organización originante del proyecto
        Long ongOriginanteId = observacion.getProyecto().getOngOriginante();
        
        // Buscar todos los usuarios que pertenecen a esa organización
        List<User> usuarios = userRepository.findByOngId(ongOriginanteId);
        
        if (usuarios.isEmpty()) {
            logger.warn("No se encontraron usuarios para la organización ID: {}", ongOriginanteId);
            return;
        }
        
        // Extraer los emails de los usuarios
        List<String> emails = usuarios.stream()
                .map(User::getEmail)
                .filter(email -> email != null && !email.isEmpty())
                .collect(Collectors.toList());
        
        if (emails.isEmpty()) {
            logger.warn("Ningún usuario tiene email configurado para la organización ID: {}", ongOriginanteId);
            return;
        }
        
        // Obtener el nombre de la ONG
        Ong ong = ongRepository.findById(ongOriginanteId)
                .orElse(null);
        String ongNombre = ong != null ? ong.getName() : "Desconocida";
        
        // Generar el asunto y cuerpo del email
        String asunto = "Nueva Observación en Proyecto: " + observacion.getProyecto().getName();
        String cuerpo = emailService.generarCuerpoEmailObservacion(
                observacion.getProyecto().getName(),
                observacion.getDescripcion(),
                ongNombre,
                observacion.getId()
        );
        
        // Enviar el email
        emailService.sendEmail(emails, asunto, cuerpo);
        logger.info("Email enviado a {} usuarios de la organización {}", emails.size(), ongNombre);
    }

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
