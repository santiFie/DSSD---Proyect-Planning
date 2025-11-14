package com.proyect_planning.proyect_planning_system.services;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.proyect_planning.proyect_planning_system.dto.IndicadorPedidoFechaFinalizacionDTO;
import com.proyect_planning.proyect_planning_system.services.bonita.BonitaApiService;
import com.proyect_planning.proyect_planning_system.services.bonita.dto.ArchivedCaseDTO;
import com.proyect_planning.proyect_planning_system.services.bonita.exception.BonitaException;

@Service
public class IndicadorService {
    private final Logger logger = LoggerFactory.getLogger(IndicadorService.class);

    private final ProyectService proyectSvc;
    private final BonitaApiService bonitaApiSvc;

    public IndicadorService(@Autowired ProyectService proyectService, @Autowired BonitaApiService bonitaApiService) {
        this.proyectSvc = proyectService;
        this.bonitaApiSvc = bonitaApiService;
    }

    /**
     * Obtiene los pedidos, con su fecha de finalización del proyecto y del caso en Bonita
     * 
     * @return Lista de IndicadorPedidoFechaFinalizacionDTO
     */
    public List<IndicadorPedidoFechaFinalizacionDTO> getProjectEndDate() {
        return proyectSvc.getAllProjects().stream().map(proj -> {
            IndicadorPedidoFechaFinalizacionDTO indicador = new IndicadorPedidoFechaFinalizacionDTO();
            indicador.setIdProject(proj.getId());
            indicador.setEndDateProject(proj.getEndDate() != null ? proj.getEndDate() : null);
            List<ArchivedCaseDTO> archivedCases = null;
            try {
                archivedCases = bonitaApiSvc.getArchivedCase(proj.getBonitaCaseId());
            } catch (BonitaException e) {
                logger.error("Error obteniendo caso archivado de Bonita para el proyecto ID {}: {}", proj.getId(), e.getMessage());
            }
            if (archivedCases != null && !archivedCases.isEmpty()) {
                indicador.setEndDateCase(archivedCases.stream().sorted(ArchivedCaseDTO.endDateComparator.reversed()).findFirst().get().getEndDate());
            } else {
                indicador.setEndDateCase(null);
            }
            return indicador;
        }).toList();
    }
}
