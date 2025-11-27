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
        logger.debug("Iniciando...");
        String processId = bonitaApiSvc.getProcessId("Gestion Proyecto");
        // Preparar variables para el proceso
        Map<String, Object> processVariables = new HashMap<>();

        Map<String, Object> proyectoInput = new HashMap<>();
        proyectoInput.put("id", project.getId());
        proyectoInput.put("nombre", project.getName());
        proyectoInput.put("fecha_inicio", project.getStartDate());
        proyectoInput.put("tipo_proyecto", "");
        proyectoInput.put("descripcion", project.getDescription());

        logger.debug("Proyecto registrado: {}", proyectoInput.toString());

        List<Map<String, Object>> etapasInput = new ArrayList<>();
        if (project.getStages() != null) {
            logger.debug("Stages registrado: {}", project.getStages().size());
            for (int i = 0; i < project.getStages().size(); i++) {
                logger.debug("Stages registrado dentro del for: {}", project.getStages().get(i).getId());
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
                logger.debug("Tareas encontradas: {}", tareas);
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
                logger.debug("Tareas encontradas: {}", tareas);
                logger.warn("La tarea encontrada no es 'Analizar compromiso': {}", tareas.get(0));
                throw new BonitaException("La tarea encontrada no es 'Analizar compromiso'. Nombre tarea: " + tareas.get(0).get("name"));
            }
            Map<String, Object> analisisCompromiso = new HashMap<>();
            analisisCompromiso.put("acepta_compromiso", aceptaCompromiso);
            analisisCompromiso.put("compromisoId", (compromisoId != null) ? compromisoId.toString() : null);
            Map<String, Object> taskData = new HashMap<>();
            taskData.put("analisis_compromiso", analisisCompromiso);
            bonitaApiSvc.executeTask(tareas.get(0).get("id"), taskData);

            logger.debug("Compromiso analizado en Bonita. CaseId: {}, Acepta: {}, CompromisoId: {}",
                    bonitaCaseId, aceptaCompromiso, compromisoId);
        } else {
            logger.error("No se encontraron tareas humanas para el caso ID: {}", bonitaCaseId);
        }
    }

    /**
     * Ejecuta la etapa del proyecto en Bonita, registrando la cantidad de etapas
     * ejecutadas
     *
     * @param bonitaCaseId ID del caso en Bonita (asociado al proyecto)
     * @param cantidad     Cantidad de etapas ejecutadas
     * @throws BonitaException Ante un error en la comunicación con Bonita
     */
    public void ejecutarEtapa(String bonitaCaseId, int cantidad) throws BonitaException {
        // Obtener el ID del proceso "Ejecutar Tarea Comprometida"
        List<Map<String, String>> tareas = this.bonitaApiSvc.getTasksByCaseId(bonitaCaseId);
        if (tareas != null && !tareas.isEmpty()) {
            // Ejecutar la tarea humana, que debería ser "Ejecutar Tarea Comprometida"
            if (Boolean.FALSE.equals(tareas.get(0).get("name").contains("Ejecutar Tarea Comprometida"))) {
                logger.debug("Tareas encontradas: {}", tareas);
                logger.warn("La tarea encontrada no es 'Ejecutar Tarea Comprometida': {}", tareas.get(0));
                throw new BonitaException("La tarea encontrada no es 'Ejecutar Tarea Comprometida'. Nombre tarea: " + tareas.get(0).get("name"));
            }
            // Ingresar cantidad
            Map<String, Object> cantEtapasEjecutadas = new HashMap<>();
            cantEtapasEjecutadas.put("cantidad", cantidad);
            Map<String, Object> taskData = new HashMap<>();
            taskData.put("cantEtapasEjecutadas", cantEtapasEjecutadas);
            // Ejecutar la tarea
            bonitaApiSvc.executeTask(tareas.get(0).get("id"), taskData);
        } else {
            logger.error("No se encontraron tareas humanas para el caso ID: {}", bonitaCaseId);
        }
    }

    /**
     * Finaliza el proyecto en Bonita, ejecutando la tarea "Terminar Proyecto"
     *
     * @param bonitaCaseId ID del caso en Bonita (asociado al proyecto)
     * @throws BonitaException Ante un error en la comunicación con Bonita
     */
    public void finalizarProyecto(String bonitaCaseId) throws BonitaException {
        // Obtener el ID del proceso "Terminar proyecto"
        List<Map<String, String>> tareas = this.bonitaApiSvc.getTasksByCaseId(bonitaCaseId);
        if (tareas != null && !tareas.isEmpty()) {
            // Ejecutar la tarea humana, que debería ser "Terminar proyecto"
            if (Boolean.FALSE.equals(tareas.get(0).get("name").contains("Terminar proyecto"))) {
                logger.debug("Tareas encontradas: {}", tareas);
                logger.warn("La tarea encontrada no es 'Terminar proyecto': {}", tareas.get(0));
                throw new BonitaException("La tarea encontrada no es 'Terminar proyecto'. Nombre tarea: " + tareas.get(0).get("name"));
            }
            // Ejecutar la tarea
            bonitaApiSvc.executeTask(tareas.get(0).get("id"), null);
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

        logger.debug("Variables para Bonita (Observación): {}", processVariables);

        // Iniciar el proceso en Bonita
        Map<String, String> processInstance = bonitaApiSvc.startProcessInstance(processId, processVariables);
        String caseId = processInstance.get("caseId");

        // Buscar la tarea "Análisis y creación de revisiones"
        List<Map<String, String>> humanTasks = bonitaApiSvc.getTasksByCaseId(caseId);

        if (humanTasks != null && !humanTasks.isEmpty()) {
            logger.debug("Tarea humana encontrada para observación: {}", humanTasks.get(0));

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
            logger.debug("Tarea ejecutada para observación con caseId: {}", caseId);
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

    /**
     * Obtiene los pedidos desde Bonita para un caso específico
     * Busca la variable "jsonPedidos" en los subprocesos del caso
     *
     * @param caseId ID del caso en Bonita (proceso padre)
     * @return String JSON con los pedidos o null si no se encuentra
     * @throws BonitaException Ante un error en la comunicación con Bonita
     */
    public String getPedidosFromBonita(String caseId) throws BonitaException {
        try {
            logger.info("Buscando jsonPedidos para caso padre: {}", caseId);

            // Obtener los subprocesos del caso padre
            List<Map<String, Object>> subprocesses = bonitaApiSvc.getSubProcessesByCaseId(caseId);

            logger.debug("Subprocesos encontrados: {}", subprocesses.size());

            // Buscar en cada subproceso la variable jsonPedidos
            for (Map<String, Object> subprocess : subprocesses) {
                String subprocessCaseId = subprocess.get("id").toString();
                logger.debug(" Buscando en subproceso: {}", subprocessCaseId);

                // Obtener variables del subproceso
                List<Map<String, Object>> variables = bonitaApiSvc.getVariablesByCaseId(subprocessCaseId);

                for (Map<String, Object> variable : variables) {
                    String varName = (String) variable.get("name");

                    if ("jsonPedidos".equals(varName)) {
                        logger.debug("jsonPedidos se encontro y tiene estos valores: {}  ", 
                            variable.get("value"));
                
                        Object value = variable.get("value");
                        logger.debug(" jsonPedidos encontrado en subproceso {}: {}",
                            subprocessCaseId, value != null ? "valor presente" : "null");
                        return value != null ? value.toString() : null;
                    }
                }
            }

            logger.warn("No se encontro la variable 'jsonPedidos' en ningun subproceso del caso {}", caseId);
            return null;

        } catch (Exception e) {
            logger.error("Error obteniendo pedidos desde Bonita. CaseId: {}", caseId, e);
            throw new BonitaException("Error obteniendo pedidos desde Bonita. CaseId: " + caseId, e);
        }
    }

    /**
     * Obtiene todos los pedidos desde todos los casos activos en Bonita
     * Usa las tareas humanas para encontrar los subprocesos con jsonPedidos
     *
     * @return Lista de mapas con información de pedidos y sus casos asociados
     * @throws BonitaException Ante un error en la comunicación con Bonita
     */
    public List<Map<String, Object>> getAllPedidosFromBonita() throws BonitaException {
        try {
            List<Map<String, Object>> result = new ArrayList<>();

            // Obtener todas las instancias de proceso padre
            List<Map<String, Object>> allCases = bonitaApiSvc.getProcessInstances();

            logger.debug("Buscando pedidos en Bonita");
            logger.debug("Total de casos padre encontrados: {}", allCases.size());

            // Para cada caso padre, buscar sus tareas humanas
            for (Map<String, Object> caseInstance : allCases) {
                String parentCaseId = caseInstance.get("id").toString();
                String processName = caseInstance.get("processDefinitionId") != null ?
                    caseInstance.get("processDefinitionId").toString() : "unknown";

                logger.info("Procesando caso padre: caseId={}, proceso={}", parentCaseId, processName);

                try {
                    // Obtener las tareas humanas del caso padre
                    List<Map<String, String>> humanTasks = bonitaApiSvc.getTasksByCaseId(parentCaseId);
                    logger.info("Tareas humanas encontradas: {}", humanTasks.size());

                    // Para cada tarea, el parentCaseId indica el subproceso
                    for (Map<String, String> task : humanTasks) {
                        String taskParentCaseId = task.get("parentCaseId");

                        if (taskParentCaseId != null && !taskParentCaseId.equals(parentCaseId)) {
                            // Este parentCaseId es el ID del subproceso
                            logger.info("Subproceso encontrado via tarea: {}", taskParentCaseId);

                            // Obtener variables del subproceso
                            List<Map<String, Object>> variables = bonitaApiSvc.getVariablesByCaseId(taskParentCaseId);
                            logger.info("       Variables en subproceso: {}", variables.size());

                            // Buscar jsonPedidos
                            for (Map<String, Object> variable : variables) {
                                String varName = (String) variable.get("name");

                                if ("jsonPedidos".equals(varName)) {
                                    Object value = variable.get("value");

                                    logger.info(" jsonPedidos encontrado! Subproceso={}, valor presente={}",
                                        taskParentCaseId, value != null);

                                    Map<String, Object> pedidoInfo = new HashMap<>();
                                    pedidoInfo.put("parentCaseId", parentCaseId);
                                    pedidoInfo.put("subprocessCaseId", taskParentCaseId);
                                    pedidoInfo.put("pedidos", value);
                                    pedidoInfo.put("processName", processName);
                                    result.add(pedidoInfo);
                                    break;
                                }
                            }
                        }
                    }

                } catch (Exception e) {
                    logger.warn("Error procesando caso padre {}: {}", parentCaseId, e.getMessage());
                }
            }

            logger.debug(" Resultado: {} subprocesos con pedidos encontrados ═══", result.size());
            return result;

        } catch (Exception e) {
            logger.error("Error obteniendo todos los pedidos desde Bonita", e);
            throw new BonitaException("Error obteniendo todos los pedidos desde Bonita", e);
        }
    }

    public List<Map<String, Object>> getAllCompromisos() throws BonitaException {
        try {

            List<Map<String, Object>> result = new ArrayList<>();
            
            // Obtener todas las instancias de proceso padre
            List<Map<String, Object>> allCases = bonitaApiSvc.getProcessInstances();

            // Para cada caso padre, buscar sus tareas humanas
            for (Map<String, Object> caseInstance : allCases) {
                String parentCaseId = caseInstance.get("id").toString();
                String processName = caseInstance.get("processDefinitionId") != null ? 
                    caseInstance.get("processDefinitionId").toString() : "unknown";
                                
                try {
                    // Obtener las tareas humanas del caso padre
                    List<Map<String, String>> humanTasks = bonitaApiSvc.getTasksByCaseId(parentCaseId);
                    
                    // Para cada tarea, el parentCaseId indica el subproceso
                    for (Map<String, String> task : humanTasks) {
                        String taskParentCaseId = task.get("parentCaseId");
                        
                        if (taskParentCaseId != null && !taskParentCaseId.equals(parentCaseId)) {
                            // Este parentCaseId es el ID del subproceso
                            logger.info("Subproceso encontrado via tarea: {}", taskParentCaseId);
                            
                            // Obtener variables del subproceso
                            List<Map<String, Object>> variables = bonitaApiSvc.getVariablesByCaseId(taskParentCaseId);
                            
                            // Buscar jsonPedidos
                            for (Map<String, Object> variable : variables) {
                                String varName = (String) variable.get("name");
                                
                                if ("jsonCompromisos".equals(varName)) {
                                    Object value = variable.get("value");
                                    
                                    logger.debug("Se encontro jsonCompromisos - valor presente={}", value != null);
                                    
                                    Map<String, Object> compromisoInfo = new HashMap<>();
                                    compromisoInfo.put("parentCaseId", parentCaseId);
                                    compromisoInfo.put("subprocessCaseId", taskParentCaseId);
                                    compromisoInfo.put("compromisos", value);
                                    compromisoInfo.put("processName", processName);
                                    result.add(compromisoInfo);
                                    break;
                                }
                            }
                        }
                    }
                    
                } catch (Exception e) {
                    logger.warn("Error procesando caso padre {}: {}", parentCaseId, e.getMessage());
                }
            }
            
            logger.debug(" Resultado: {} subprocesos con pedidos encontrados ═══", result.size());
            return result;
            
        } catch (Exception e) {
            logger.error("Error obteniendo todos los compromisos desde Bonita", e);
            throw new BonitaException("Error obteniendo todos los compromisos desde Bonita", e);
        }
    }

}
