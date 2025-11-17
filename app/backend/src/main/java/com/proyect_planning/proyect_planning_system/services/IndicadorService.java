package com.proyect_planning.proyect_planning_system.services;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.proyect_planning.proyect_planning_system.dto.IndicadorEtapaPedidoDTO;
import com.proyect_planning.proyect_planning_system.dto.IndicadorProyectoFechaFinalizacionDTO;
import com.proyect_planning.proyect_planning_system.services.bonita.BonitaApiService;
import com.proyect_planning.proyect_planning_system.services.bonita.dto.ArchivedCaseDTO;
import com.proyect_planning.proyect_planning_system.services.bonita.exception.BonitaException;
import com.proyect_planning.proyect_planning_system.services.cloud.CloudService;
import com.proyect_planning.proyect_planning_system.services.cloud.dto.CompromisoCloudDTO;
import com.proyect_planning.proyect_planning_system.services.cloud.exceptions.CloudException;

@Service
public class IndicadorService {
    private final Logger logger = LoggerFactory.getLogger(IndicadorService.class);

    private final ProyectService proyectSvc;
    private final BonitaApiService bonitaApiSvc;
    private final CloudService cloudSvc;

    public IndicadorService(@Autowired ProyectService proyectService, @Autowired BonitaApiService bonitaApiService,
            @Autowired CloudService cloudService) {
        this.proyectSvc = proyectService;
        this.bonitaApiSvc = bonitaApiService;
        this.cloudSvc = cloudService;
    }

    /**
     * Obtiene los pedidos, con su fecha de finalización del proyecto y del caso en
     * Bonita
     * 
     * @return Lista de IndicadorPedidoFechaFinalizacionDTO
     */
    public List<IndicadorProyectoFechaFinalizacionDTO> getProjectEndDate() {
        return proyectSvc.getAllProjects().stream().map(proj -> {
            IndicadorProyectoFechaFinalizacionDTO indicador = new IndicadorProyectoFechaFinalizacionDTO();
            indicador.setIdProject(proj.getId());
            indicador.setEndDateProject(proj.getEndDate() != null ? proj.getEndDate() : null);
            List<ArchivedCaseDTO> archivedCases = null;
            try {
                archivedCases = bonitaApiSvc.getArchivedCase(proj.getBonitaCaseId());
            } catch (BonitaException e) {
                logger.error("Error obteniendo caso archivado de Bonita para el proyecto ID {}: {}", proj.getId(),
                        e.getMessage());
            }
            if (archivedCases != null && !archivedCases.isEmpty()) {
                indicador.setEndDateCase(archivedCases.stream().sorted(ArchivedCaseDTO.endDateComparator.reversed())
                        .findFirst().get().getEndDate());
            } else {
                indicador.setEndDateCase(null);
            }
            return indicador;
        }).toList();
    }

    /**
     * Obtiene las etapas con necesidades no cubiertas y ONG colaborante asociada
     * 
     * @return Lista de IndicadorEtapaPedidoDTO
     */
    public List<IndicadorEtapaPedidoDTO> getStagesAndNeeds() {
        return proyectSvc.getAllProjects().stream()
                .flatMap(proj -> proj.getStages().stream()
                        .filter(stage -> !stage.getCovered())
                        .map(stage -> {
                            IndicadorEtapaPedidoDTO dto = new IndicadorEtapaPedidoDTO();
                            dto.setIdProject(proj.getId());
                            dto.setIdStage(stage.getId());
                            dto.setCategoryStage(stage.getCategory());
                            CompromisoCloudDTO compromiso = null;
                            try {
                                compromiso = cloudSvc.getCompromisosByEtapaId(stage.getId())
                                        .stream()
                                        .filter(c -> c.getEstado().equals("ACEPTADO"))
                                        .findFirst()
                                        .orElse(null);
                            } catch (CloudException e) {
                                logger.error("Error al obtener compromisos por etapa", e);
                            }
                            dto.setOngColaboranteId(compromiso != null ? compromiso.getOngColaboranteId() : null);
                            return dto;
                        }))
                .toList();
    }
}
