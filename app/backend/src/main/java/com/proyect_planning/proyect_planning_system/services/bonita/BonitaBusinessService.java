package com.proyect_planning.proyect_planning_system.services.bonita;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.proyect_planning.proyect_planning_system.entities.Correccion;
import com.proyect_planning.proyect_planning_system.entities.Observacion;
import com.proyect_planning.proyect_planning_system.entities.Proyect;
import com.proyect_planning.proyect_planning_system.entities.Stage;
import com.proyect_planning.proyect_planning_system.services.bonita.exception.BonitaException;

@Service
public class BonitaBusinessService {

    private static final Logger logger = LoggerFactory.getLogger(BonitaBusinessService.class);
    private final BonitaApiService bonitaApiSvc;

    public BonitaBusinessService(@Autowired BonitaApiService bonitaApiService) {
        this.bonitaApiSvc = bonitaApiService;
    }

    /**
     * Registra un nuevo proyecto en Bonita, iniciando el proceso "Gestion Proyecto"
     * 
     * @param project El proyecto a registrar
     * @return El ID del caso (caseId) creado en Bonita
     * @throws BonitaException Ante un error en la comunicación con Bonita
     */
    public String registrarProyecto(Proyect project) throws BonitaException {
        // Obtener el ID del proceso "Gestion Proyecto"
        String processId = bonitaApiSvc.getProcessId("Gestion Proyecto");
        // Preparar variables para el proceso
        Map<String, Object> processVariables = new HashMap<>();

        Map<String, Object> proyectoInput = new HashMap<>();
        proyectoInput.put("id", project.getId());
        proyectoInput.put("nombre", project.getName());
        proyectoInput.put("fecha_inicio", project.getStartDate());
        proyectoInput.put("tipo_proyecto", "");
        proyectoInput.put("descripcion", project.getDescription());

        List<Map<String, Object>> etapasInput = new ArrayList<>();
        if (project.getStages() != null) {
            for (int i = 0; i < project.getStages().size(); i++) {
                Stage stage = project.getStages().get(i);
                Map<String, Object> etapa = new HashMap<>();
                etapa.put("nro_orden", stage.getId());
                etapa.put("nombre", stage.getName());
                etapa.put("fecha_inicio", stage.getStartDate());
                etapa.put("fecha_fin", stage.getEndDate() != null ? stage.getEndDate() : null);
                etapa.put("requiere_pedido", !stage.getCovered());
                etapa.put("desc_pedido", stage.getNeeds() != null ? stage.getNeeds().getDescription() : "");
                etapa.put("estado", "PENDIENTE");
                etapa.put("proyecto_id", project.getId());
                etapa.put("ong_id", project.getOngOriginante());
                etapasInput.add(etapa);
            }
        }
        processVariables.put("proyectoInput", proyectoInput);
        processVariables.put("etapasInput", etapasInput);

        logger.debug("Variables para Bonita {} ", processVariables);

        // Iniciar el proceso en Bonita
        Map<String, String> processInstance = bonitaApiSvc.startProcessInstance(processId, processVariables);
        String caseId = processInstance.get("caseId");
        // Buscar tarea humana lista
        List<Map<String, String>> humanTasks = bonitaApiSvc.getTasksByCaseId(caseId);

        if (humanTasks != null && !humanTasks.isEmpty()) {
            // Ejecutar la primera tarea humana
            logger.debug("Tarea humana encontrada: {}", humanTasks.get(0));
            bonitaApiSvc.executeTask(humanTasks.get(0).get("id"), null);
        } else {
            logger.error("No se encontraron tareas humanas para el caso ID: {}", caseId);
        }
        return caseId;
    }

    /**
     * Comprometer ayuda para una etapa del proyecto en Bonita
     * 
     * @param bonitaCaseId     ID del caso en Bonita (asociado al proyecto)
     * @param pedidoId         ID del pedido asociado (id del pedido en cloud)
     * @param ongColaboranteId ID de la ONG colaborante (identificador de la ONG,
     *                         solo descriptivo)
     * @param descripcion      Descripción del compromiso (descripcion de la ayuda
     *                         propuesta)
     * @throws BonitaException Ante un error en la comunicación con Bonita
     */
    public void comprometerAyuda(String bonitaCaseId, String pedidoId, String ongColaboranteId, String descripcion)
            throws BonitaException {
        List<Map<String, String>> tareas = this.bonitaApiSvc.getTasksByCaseId(bonitaCaseId);
        if (tareas != null && !tareas.isEmpty()) {
            // Ejecutar la tarea humana, que debería ser "Comprometer Ayuda para la Etapa"
            if (Boolean.FALSE.equals(tareas.get(0).get("name").contains("Comprometer Ayuda para la Etapa"))) {
                logger.warn("La tarea encontrada no es 'Comprometer Ayuda para la Etapa': {}", tareas.get(0));
                throw new BonitaException("La tarea encontrada no es 'Comprometer Ayuda para la Etapa'. Nombre tarea: " + tareas.get(0).get("name"));
            }
            Map<String, Object> compromiso = new HashMap<>();
            compromiso.put("pedidoId", pedidoId);
            compromiso.put("ongColaboranteId", ongColaboranteId);
            compromiso.put("descripcion", descripcion);
            Map<String, Object> taskData = new HashMap<>();
            taskData.put("compromiso", compromiso);
            bonitaApiSvc.executeTask(tareas.get(0).get("id"), taskData);
        } else {
            logger.error("No se encontraron tareas humanas para el caso ID: {}", bonitaCaseId);
        }
    }

    /**
     * Permite registrar el analisis de un compromiso en Bonita
     * 
     * @param bonitaCaseId     ID del caso en Bonita (asociado al proyecto)
     * @param aceptaCompromiso Boolean que indica si se acepta o no el compromiso
     * @param compromisoId     ID del compromiso (id compromiso en cloud). Opcional,
     *                         si aceptaCompromiso es false puede ser null
     * @throws BonitaException Ante un error en la comunicación con Bonita
     */
    public void analizarCompromiso(String bonitaCaseId, Boolean aceptaCompromiso, Long compromisoId)
            throws BonitaException {
        List<Map<String, String>> tareas = this.bonitaApiSvc.getTasksByCaseId(bonitaCaseId);
        if (tareas != null && !tareas.isEmpty()) {
            // Ejecutar la tarea humana, que debería ser "Analizar compromiso"
            if (Boolean.FALSE.equals(tareas.get(0).get("name").contains("Analizar compromiso"))) {
                logger.warn("La tarea encontrada no es 'Analizar compromiso': {}", tareas.get(0));
                throw new BonitaException("La tarea encontrada no es 'Analizar compromiso'. Nombre tarea: " + tareas.get(0).get("name"));
            }
            Map<String, Object> analisisCompromiso = new HashMap<>();
            analisisCompromiso.put("acepta_compromiso", aceptaCompromiso);
            analisisCompromiso.put("compromisoId", (compromisoId != null) ? compromisoId.toString() : null);
            Map<String, Object> taskData = new HashMap<>();
            taskData.put("analisis_compromiso", analisisCompromiso);
            bonitaApiSvc.executeTask(tareas.get(0).get("id"), taskData);

            logger.error("Compromiso analizado en Bonita. CaseId: {}, Acepta: {}, CompromisoId: {}",
                    bonitaCaseId, aceptaCompromiso, compromisoId);
        } else {
            logger.error("No se encontraron tareas humanas para el caso ID: {}", bonitaCaseId);
        }
    }


        /**
     * Registra una nueva observación en Bonita, iniciando el proceso "Revision_Consejo"
     * y ejecutando automáticamente la tarea "Análisis y creación de revisiones"
     * 
     * @param observacion La observación a registrar
     * @return El ID del caso (caseId) creado en Bonita
     * @throws BonitaException Ante un error en la comunicación con Bonita
     */
    public String registrarObservacionEnBonita(Observacion observacion) throws BonitaException {
        // Obtener el ID del proceso "Revision_Consejo"
        String processId = bonitaApiSvc.getProcessId("Revision_Consejo");

        // Preparar variables para el proceso
        Map<String, Object> processVariables = new HashMap<>();
        
        Map<String, Object> observacionInput = new HashMap<>();
        observacionInput.put("id", observacion.getId());
        observacionInput.put("descripcion", observacion.getDescripcion());
        observacionInput.put("proyectoId", observacion.getProyecto().getId());
        observacionInput.put("ongId", observacion.getOng().getId());
        observacionInput.put("estado", observacion.getEstado().toString());
        observacionInput.put("fechaCreacion", observacion.getFechaCreacion().toString());
        observacionInput.put("fechaLimite", observacion.getFechaLimite().toString());
        
        List<Map<String, Object>> observacionesInput = new ArrayList<>();
        observacionesInput.add(observacionInput);
        processVariables.put("observacionesInput", observacionesInput);

        logger.info("Variables para Bonita (Observación): {}", processVariables);

        // Iniciar el proceso en Bonita
        Map<String, String> processInstance = bonitaApiSvc.startProcessInstance(processId, processVariables);
        String caseId = processInstance.get("caseId");

        // Buscar la tarea "Análisis y creación de revisiones"
        List<Map<String, String>> humanTasks = bonitaApiSvc.getTasksByCaseId(caseId);

        if (humanTasks != null && !humanTasks.isEmpty()) {
            logger.info("Tarea humana encontrada para observación: {}", humanTasks.get(0));
            
            // Preparar el objeto de observación para la tarea con el formato requerido por Bonita
            Map<String, Object> observacionParaTarea = new HashMap<>();
            observacionParaTarea.put("persistenceId_string", caseId);
            observacionParaTarea.put("estado", observacion.getEstado().toString());
            
            // Crear el array de observaciones
            List<Map<String, Object>> observacionesArray = new ArrayList<>();
            observacionesArray.add(observacionParaTarea);
            
            // Crear el objeto taskData con el formato correcto
            Map<String, Object> taskData = new HashMap<>();
            taskData.put("observacionesInput", observacionesArray);
            
            bonitaApiSvc.executeTask(humanTasks.get(0).get("id"), taskData);
            logger.info("Tarea ejecutada para observación con caseId: {}", caseId);
        } else {
            logger.warn("No se encontraron tareas humanas para la observación, caso ID: {}", caseId);
        }

        return caseId;
    }

    /**
     * Notifica a Bonita que se ha agregado una corrección, ejecutando la tarea
     * "Resolver Observaciones" por parte del usuario de la ONG
     * 
     * @param observacion La observación que recibe la corrección
     * @param correccion La corrección agregada
     * @throws BonitaException Ante un error en la comunicación con Bonita
     */
    public void notificarCorreccionEnBonita(Observacion observacion, Correccion correccion) throws BonitaException {
        if (observacion.getBonitaCaseId() == null) {
            logger.warn("La observación {} no tiene un caso en Bonita asociado", observacion.getId());
            return;
        }

        List<Map<String, String>> tareas = bonitaApiSvc.getTasksByCaseId(observacion.getBonitaCaseId());
        if (tareas != null && !tareas.isEmpty()) {
            // Buscar la tarea "Resolver Observaciones"
            Map<String, String> tarea = tareas.stream()
                    .filter(t -> t.get("name").contains("Resolver Observaciones"))
                    .findFirst()
                    .orElse(null);

            if (tarea != null) {
                // Preparar el objeto de corrección según el contrato de Bonita
                Map<String, Object> correccionInput = new HashMap<>();
                correccionInput.put("id", correccion.getId());
                correccionInput.put("fechaCreacion", correccion.getFechaCreacion().toString());
                correccionInput.put("detalle", correccion.getDetalle());
                correccionInput.put("usuarioId", correccion.getUsuario().getId());
                correccionInput.put("observacionId", observacion.getId());
                
                // Crear el taskData con el formato esperado por Bonita
                Map<String, Object> taskData = new HashMap<>();
                taskData.put("correccion", correccionInput);
                
                bonitaApiSvc.executeTask(tarea.get("id"), taskData);
                logger.info("Corrección notificada en Bonita para la observación {}. CorreccionId: {}", 
                    observacion.getId(), correccion.getId());
            } else {
                logger.warn("No se encontró la tarea 'Resolver Observaciones' para el caso {}", 
                    observacion.getBonitaCaseId());
            }
        }
    }

    /**
     * Actualiza el estado de la observación en Bonita (para aprobar/rechazar corrección)
     * Este método se llama cuando un directivo/admin marca la observación como RESUELTA o la rechaza
     */
    public void actualizarEstadoObservacionEnBonita(Observacion observacion) throws BonitaException {
        if (observacion.getBonitaCaseId() == null) {
            logger.warn("La observación {} no tiene un caso en Bonita asociado", observacion.getId());
            return;
        }

        List<Map<String, String>> tareas = bonitaApiSvc.getTasksByCaseId(observacion.getBonitaCaseId());
        if (tareas != null && !tareas.isEmpty()) {
            // Buscar la tarea "Verificar Correcciones"
            Map<String, String> tarea = tareas.stream()
                    .filter(t -> t.get("name").contains("Verificar Correcciones"))
                    .findFirst()
                    .orElse(null);

            if (tarea != null) {
                // Preparar el objeto observacion con el nuevo estado
                Map<String, Object> observacionInput = new HashMap<>();
                observacionInput.put("id", observacion.getId());
                observacionInput.put("estado", observacion.getEstado().toString());
                
                // Crear el taskData
                Map<String, Object> taskData = new HashMap<>();
                taskData.put("observacion", observacionInput);
                
                bonitaApiSvc.executeTask(tarea.get("id"), taskData);
                logger.info("Estado de observación actualizado en Bonita: {} - Estado: {}", 
                    observacion.getId(), observacion.getEstado());
            } else {
                logger.warn("No se encontró la tarea 'Verificar Correcciones' para el caso {}", 
                    observacion.getBonitaCaseId());
            }
        }
    }

    /**
     * @deprecated Use actualizarEstadoObservacionEnBonita instead
     */
    @Deprecated
    public void resolverObservacionEnBonita(Observacion observacion) throws BonitaException {
        actualizarEstadoObservacionEnBonita(observacion);
    }

    public void notificarVencimientoEnBonita(Observacion observacion) throws BonitaException {
        if (observacion.getBonitaCaseId() == null) {
            return;
        }

        // Aquí podrías implementar una notificación especial en Bonita
        logger.warn("Observación {} ha vencido el plazo de 5 días", observacion.getId());
    }
}
